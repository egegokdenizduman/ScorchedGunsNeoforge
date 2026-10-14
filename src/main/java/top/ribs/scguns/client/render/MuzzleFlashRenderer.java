package top.ribs.scguns.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import top.ribs.scguns.client.GunRenderType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.MuzzleFlashStyle;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.particles.SparkData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Muzzle flash in the style of Iron's Artifice: a short (3 tick) hand-drawn animation instead of one
 * static sprite. Each shot picks a random shape (star or burst), a random 90 degree roll and mirroring,
 * so consecutive shots never look the same. The flash is drawn as one quad facing down the barrel plus two
 * crossed side-on plume quads along the barrel, so it reads from behind (first person) and from the side
 * (other players / third person). Everything is full-bright plus an additive `eyes` pass, which shader packs
 * (Iris) treat as emissive.
 * <p>
 * On the first frame a shot is drawn, the muzzle's world position is recovered from the render pose and
 * sparks / smoke / flame are spawned there as real world particles, so everyone nearby sees them.
 */
public final class MuzzleFlashRenderer {
    public static final int LIFETIME = 3;
    private static final float[] FRAME_SCALE = {0.7F, 1.0F, 1.15F};
    private static final float EMISSIVE_ALPHA = 0.6F;

    private static final Map<Integer, Shot> SHOTS = new HashMap<>();
    private static final Map<Integer, Long> ANIMATED_DRAW_FRAME = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> BASE_TYPES = new HashMap<>();
    private static long frameId;

    private static final class Shot {
        final long startTick;
        final long seed;
        boolean particlesSpawned;

        Shot(long startTick, long seed) {
            this.startTick = startTick;
            this.seed = seed;
        }
    }

    private MuzzleFlashRenderer() {
    }

    /** A gun was fired by this entity (local shot or the server's gunshot packet for someone else). */
    public static void onShot(int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        SHOTS.put(entityId, new Shot(level.getGameTime(), level.random.nextLong()));
    }

    public static boolean isFlashing(int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        Shot shot = SHOTS.get(entityId);
        return level != null && shot != null && level.getGameTime() - shot.startTick < LIFETIME;
    }

    /** Called once per rendered frame. */
    public static void nextFrame() {
        frameId++;
    }

    /** The animated (GeckoLib) renderer drew this entity's first-person flash this frame. */
    public static void markAnimatedDrawn(int entityId) {
        ANIMATED_DRAW_FRAME.put(entityId, frameId);
    }

    public static boolean wasAnimatedDrawnThisFrame(int entityId) {
        Long frame = ANIMATED_DRAW_FRAME.get(entityId);
        return frame != null && frame == frameId;
    }

    /** Client tick: expire shots, and spawn particles from the eye position for shooters that were never rendered. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            SHOTS.clear();
            ANIMATED_DRAW_FRAME.clear();
            return;
        }
        long now = level.getGameTime();
        Iterator<Map.Entry<Integer, Shot>> it = SHOTS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Shot> entry = it.next();
            Shot shot = entry.getValue();
            long age = now - shot.startTick;
            if (!shot.particlesSpawned && age >= 1) {
                shot.particlesSpawned = true;
                Entity entity = level.getEntity(entry.getKey());
                if (entity instanceof LivingEntity living && living.getMainHandItem().getItem() instanceof GunItem gunItem) {
                    Vec3 look = living.getViewVector(1.0F);
                    Vec3 pos = living.getEyePosition(1.0F).add(look.scale(1.1)).add(0, -0.15, 0);
                    Gun gun = gunItem.getModifiedGun(living.getMainHandItem());
                    spawnParticles(level, living, pos, look, gun, 1.0F);
                }
            }
            if (age > LIFETIME + 2 || age < 0) {
                it.remove();
            }
        }
        if (ANIMATED_DRAW_FRAME.size() > 64) {
            ANIMATED_DRAW_FRAME.clear();
        }
    }

    /**
     * Draws the flash with the pose's origin at the muzzle, -Z pointing out of the barrel.
     *
     * @param size       width of the flash quad in pose units
     * @param beamActive beam weapons keep flickering for as long as the beam is held
     * @return true if anything was drawn
     */
    public static boolean render(LivingEntity entity, ItemStack weapon, Gun gun, PoseStack poseStack, MultiBufferSource buffer, float size, boolean beamActive) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return false;
        }
        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);
        long now = level.getGameTime();
        Shot shot = SHOTS.get(entity.getId());
        int frame = -1;
        long seed = 0;
        if (shot != null) {
            float age = Math.max(0F, (now - shot.startTick) + partialTick);
            if (age < LIFETIME) {
                frame = (int) age;
                seed = shot.seed;
            }
        }
        if (frame < 0) {
            if (!beamActive) {
                return false;
            }
            frame = 1 + (int) ((now / 2) % 2);
            seed = (now / 2) * 31L + entity.getId();
        }

        MuzzleFlashStyle style = MuzzleFlashStyle.of(gun);
        RandomSource random = RandomSource.create(seed);
        int variant = random.nextInt(2);
        int roll = random.nextInt(4);
        boolean mirrorU = random.nextBoolean();
        boolean mirrorV = random.nextBoolean();

        float s = size * FRAME_SCALE[frame];
        ResourceLocation texture = style.getTexture();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotation(roll * Mth.HALF_PI));
        PoseStack.Pose pose = poseStack.last();

        // front quad: looks down the barrel
        float fu0 = frame / 3F, fu1 = (frame + 1) / 3F;
        float fv0 = variant / 3F, fv1 = (variant + 1) / 3F;
        if (mirrorU) { float t = fu0; fu0 = fu1; fu1 = t; }
        if (mirrorV) { float t = fv0; fv0 = fv1; fv1 = t; }
        float h = s / 2F;
        float[][] front = {{-h, -h, 0}, {h, -h, 0}, {h, h, 0}, {-h, h, 0}};
        float[][] frontUv = {{fu0, fv1}, {fu1, fv1}, {fu1, fv0}, {fu0, fv0}};

        // two crossed side plumes along the barrel (third row of the sheet, base of the plume at u0)
        float L = s * 1.4F;
        float pu0 = frame / 3F, pu1 = (frame + 1) / 3F;
        float pv0 = 2 / 3F, pv1 = 1F;
        if (mirrorV) { float t = pv0; pv0 = pv1; pv1 = t; }
        float ph = L / 2F;
        float[][] plumeUv = {{pu0, pv1}, {pu1, pv1}, {pu1, pv0}, {pu0, pv0}};
        float[][] vertical = {{0, -ph, 0}, {0, -ph, -L}, {0, ph, -L}, {0, ph, 0}};
        float[][] horizontal = {{-ph, 0, 0}, {-ph, 0, -L}, {ph, 0, -L}, {ph, 0, 0}};
        float[][][] quads = {front, vertical, horizontal};
        float[][][] uvs = {frontUv, plumeUv, plumeUv};

        // Fetching a second buffer from the source ends the first one, so finish each pass before starting the next
        VertexConsumer base = buffer.getBuffer(baseType(texture));
        for (int q = 0; q < quads.length; q++) {
            quad(base, pose, quads[q], uvs[q], 1.0F, false);
        }
        VertexConsumer glow = buffer.getBuffer(RenderType.eyes(texture));
        for (int q = 0; q < quads.length; q++) {
            // the eyes type culls back faces: emit both windings
            quad(glow, pose, quads[q], uvs[q], EMISSIVE_ALPHA, false);
            quad(glow, pose, quads[q], uvs[q], EMISSIVE_ALPHA, true);
        }

        if (shot != null && !shot.particlesSpawned && frame == 0) {
            Vec3[] muzzle = muzzleInWorld(pose.pose());
            if (muzzle != null && muzzle[0].distanceToSqr(entity.getEyePosition(partialTick)) < 16.0) {
                shot.particlesSpawned = true;
                spawnParticles(level, entity, muzzle[0], muzzle[1], gun, Mth.clamp(size, 0.4F, 2.0F));
            }
        }
        poseStack.popPose();
        return true;
    }

    private static RenderType baseType(ResourceLocation texture) {
        return BASE_TYPES.computeIfAbsent(texture, GunRenderType::getMuzzleFlash);
    }

    private static void quad(VertexConsumer c, PoseStack.Pose pose, float[][] v, float[][] uv, float tint, boolean reversed) {
        Matrix4f m = pose.pose();
        for (int k = 0; k < 4; k++) {
            int i = reversed ? 3 - k : k;
            vertex(c, pose, m, v[i], uv[i], tint);
        }
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, Matrix4f m, float[] p, float[] uv, float tint) {
        c.addVertex(m, p[0], p[1], p[2])
                .setColor(tint, tint, tint, 1.0F)
                .setUv(uv[0], uv[1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    /**
     * World position and barrel direction of the current pose origin. Model-view times pose always lands in
     * camera space; undoing the camera rotation gives the world offset from the camera.
     */
    @Nullable
    private static Vec3[] muzzleInWorld(Matrix4f pose) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Matrix4f toWorld = new Matrix4f().rotation(camera.rotation())
                .mul(RenderSystem.getModelViewMatrix())
                .mul(pose);
        Vector3f origin = toWorld.transformPosition(new Vector3f());
        Vector3f forward = toWorld.transformDirection(new Vector3f(0F, 0F, -1F));
        if (!Float.isFinite(origin.x) || forward.lengthSquared() < 1.0E-6F) {
            return null;
        }
        forward.normalize();
        Vec3 cam = camera.getPosition();
        return new Vec3[]{cam.add(origin.x, origin.y, origin.z), new Vec3(forward.x, forward.y, forward.z)};
    }

    private static void spawnParticles(ClientLevel level, LivingEntity shooter, Vec3 pos, Vec3 forward, Gun gun, float size) {
        RandomSource r = level.random;
        MuzzleFlashStyle style = MuzzleFlashStyle.of(gun);
        Vec3 inherit = shooter.getDeltaMovement().multiply(1.0, 0.0, 1.0);

        SparkData spark = new SparkData(style.getSparkColor());
        int sparks = 3 + r.nextInt(3) + (size > 0.9F ? 2 : 0);
        for (int i = 0; i < sparks; i++) {
            double speed = 0.25 + r.nextDouble() * 0.35;
            Vec3 v = forward.scale(speed).add(inherit)
                    .add((r.nextDouble() - 0.5) * 0.22, (r.nextDouble() - 0.5) * 0.22 + 0.04, (r.nextDouble() - 0.5) * 0.22);
            level.addParticle(spark, true, pos.x, pos.y, pos.z, v.x, v.y, v.z);
        }

        if (style.smokes()) {
            int puffs = 2 + (size > 0.9F ? 2 : 0);
            for (int i = 0; i < puffs; i++) {
                Vec3 p = pos.add(forward.scale(0.1 + r.nextDouble() * 0.25));
                Vec3 v = forward.scale(0.02 + r.nextDouble() * 0.03).add(inherit)
                        .add((r.nextDouble() - 0.5) * 0.02, 0.01 + r.nextDouble() * 0.02, (r.nextDouble() - 0.5) * 0.02);
                level.addParticle(ParticleTypes.SMOKE, true, p.x, p.y, p.z, v.x, v.y, v.z);
            }
        }

        switch (style) {
            case FIRE -> burst(level, ModParticleTypes.GUN_FLAME.get(), 3, pos, forward, inherit, 0.08);
            case PLASMA -> burst(level, ModParticleTypes.GUN_SOUL_FLAME.get(), 2, pos, forward, inherit, 0.05);
            case SCULK -> burst(level, ParticleTypes.SCULK_SOUL, 1, pos, forward, inherit, 0.03);
            case ELECTRIC -> burst(level, ParticleTypes.ELECTRIC_SPARK, 4, pos, forward, inherit, 0.15);
            default -> {
            }
        }

        // the gun's own configured flash particles (campfire smoke, small flames...), now from the muzzle
        Gun.Display.Flash flash = gun.getDisplay().getFlash();
        if (flash != null && flash.shouldSpawnParticles()) {
            SimpleParticleType type = emissive(flash.getParticleType());
            double spread = flash.getParticleSpread();
            for (int i = 0; i < flash.getParticleCount(); i++) {
                level.addParticle(type, true,
                        pos.x + (r.nextDouble() - 0.5) * spread, pos.y + (r.nextDouble() - 0.5) * spread, pos.z + (r.nextDouble() - 0.5) * spread,
                        forward.x * 0.03 + (r.nextDouble() - 0.5) * 0.02, 0.01 + forward.y * 0.03, forward.z * 0.03 + (r.nextDouble() - 0.5) * 0.02);
            }
        }
    }

    private static void burst(ClientLevel level, ParticleOptions type, int count, Vec3 pos, Vec3 forward, Vec3 inherit, double speed) {
        RandomSource r = level.random;
        for (int i = 0; i < count; i++) {
            Vec3 v = forward.scale(speed * (0.5 + r.nextDouble())).add(inherit)
                    .add((r.nextDouble() - 0.5) * speed, (r.nextDouble() - 0.5) * speed, (r.nextDouble() - 0.5) * speed);
            level.addParticle(type, true, pos.x, pos.y, pos.z, v.x, v.y, v.z);
        }
    }

    /** Vanilla flames are lit by the world; swap them for the full-bright gun versions. */
    public static SimpleParticleType emissive(SimpleParticleType type) {
        if (type == ParticleTypes.FLAME) return ModParticleTypes.GUN_FLAME.get();
        if (type == ParticleTypes.SMALL_FLAME) return ModParticleTypes.GUN_SMALL_FLAME.get();
        if (type == ParticleTypes.SOUL_FIRE_FLAME) return ModParticleTypes.GUN_SOUL_FLAME.get();
        return type;
    }
}
