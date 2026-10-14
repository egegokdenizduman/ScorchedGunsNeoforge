package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import top.ribs.scguns.Reference;

/**
 * World-space beams for beam weapons. Every beam is drawn in camera-relative world coordinates, so the shooter, players
 * in third person and other players watching all see the same beam.
 * <p>
 * Each beam gun gets its own {@link Style}. Glowing parts use the vanilla lightning render type (additive, full-bright,
 * treated as emissive by shader packs); dark parts (the Flayed God's wither shroud) use the vanilla
 * entity_translucent_emissive type so they stay dark but unlit, and the travelling wither skulls reuse the vanilla
 * wither skull model and textures.
 */
@OnlyIn(Dist.CLIENT)
public class BeamRenderer {
    /** Visual style per beam gun. */
    public enum Style {
        /** Flayed God: wither shroud, nether-star core, double helix, travelling wither skulls. */
        WITHER,
        /** CR4K mining laser: brass screw thread around a hot core, spinning cog where it bites. */
        MINING,
        /** Laser musket / Waltz conversion: crisp thin laser with pulses racing along it. */
        LASER,
        /** Minksy / Shard Culler: crystal shards strung along the beam. */
        CRYSTAL,
        /** Fallback for anything else. */
        ENERGY
    }

    private static final ResourceLocation WITHER_SHROUD = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/effect/wither_beam.png");
    private static final ResourceLocation WHITE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/effect/beam_white.png");
    private static final ResourceLocation WITHER_SKULL = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither.png");
    private static final ResourceLocation WITHER_SKULL_GLOW = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_invulnerable.png");
    private static final RandomSource RANDOM = RandomSource.create();
    private static SkullModel skullModel;

    // nether star palette (vanilla nether_star.png)
    private static final float[] STAR_WHITE = {0.85F, 0.89F, 0.89F};
    private static final float[] STAR_YELLOW = {0.99F, 1.0F, 0.66F};
    private static final float[] STAR_TEAL = {0.53F, 0.64F, 0.64F};
    private static final float[] WITHER_DARK = {0.09F, 0.07F, 0.08F};

    private BeamRenderer() {
    }

    /**
     * Renders one beam segment. The pose stack must already be translated so world coordinates are camera relative.
     *
     * @param muzzle         draw the muzzle charge at {@code from}
     * @param impact         draw the impact burst at {@code to}
     * @param spawnParticles spawn this tick's particles (call with true at most once per tick per beam)
     * @param impactFace     the block face the beam ends on, or null when it ends on an entity or in the air
     */
    public static void renderBeam(PoseStack poseStack, MultiBufferSource buffer, float partialTicks, Vec3 from, Vec3 to,
                                  float[] color, float fadeProgress, Style style, boolean muzzle, boolean impact, boolean spawnParticles,
                                  @Nullable Direction impactFace) {
        Vec3 beam = to.subtract(from);
        float length = (float) beam.length();
        if (length < 0.01F) {
            return;
        }
        Vec3 dir = beam.normalize();
        ClientLevel level = Minecraft.getInstance().level;
        float time = level != null ? (level.getGameTime() % 24000L) + partialTicks : partialTicks;
        float fade = fadeProgress <= 0.0F ? 1.0F : Mth.clamp(1.0F - fadeProgress, 0.0F, 1.0F);
        float[] bright = brighten(color);

        switch (style) {
            case WITHER -> renderWither(poseStack, buffer, from, to, dir, length, time, fade, muzzle, impact, impactFace);
            case MINING -> renderMining(poseStack, buffer, from, dir, length, time, fade, bright, muzzle, impact);
            case LASER -> renderLaser(poseStack, buffer, from, dir, length, time, fade, bright, muzzle, impact);
            case CRYSTAL -> renderCrystal(poseStack, buffer, from, dir, length, time, fade, bright, muzzle, impact);
            default -> renderEnergy(poseStack, buffer, from, dir, length, time, fade, bright);
        }

        if (spawnParticles && level != null && fade > 0.5F) {
            spawnParticles(level, style, from, to, dir, length, bright, impact, impactFace);
        }
    }

    // ------------------------------------------------------------------ Flayed God

    private static void renderWither(PoseStack ps, MultiBufferSource buffer, Vec3 from, Vec3 to, Vec3 dir, float length, float time,
                                     float fade, boolean muzzle, boolean impact, @Nullable Direction impactFace) {
        float pulse = 1.0F + 0.12F * Mth.sin(time * 0.7F);

        // 1) dark wither shroud: two counter-rotating square sleeves with the smoke texture flowing toward the target
        ps.pushPose();
        ps.translate(from.x, from.y, from.z);
        alignYAxis(ps, dir);
        ps.pushPose();
        ps.mulPose(Axis.YP.rotation(time * 0.05F));
        VertexConsumer shroud = buffer.getBuffer(RenderType.entityTranslucentEmissive(WITHER_SHROUD));
        texturedPrism(shroud, ps.last(), 0.22F * pulse, length, -time * 0.25F, 1.0F, 1.0F, 1.0F, 0.85F * fade);
        ps.popPose();
        ps.pushPose();
        ps.mulPose(Axis.YP.rotation(-time * 0.035F + Mth.PI / 4.0F));
        texturedPrism(shroud, ps.last(), 0.34F * pulse, length, -time * 0.12F, 1.0F, 1.0F, 1.0F, 0.35F * fade);
        ps.popPose();

        // dark half of the double helix
        VertexConsumer dark = buffer.getBuffer(RenderType.entityTranslucentEmissive(WHITE));
        for (float y = 0.3F; y < length; y += 0.4F) {
            float angle = y * 1.4F - time * 0.3F + Mth.PI;
            float size = 0.055F * edgeTaper(y, length);
            texturedCube(dark, ps, Mth.cos(angle) * 0.42F, y, Mth.sin(angle) * 0.42F, size, angle,
                    WITHER_DARK[0], WITHER_DARK[1], WITHER_DARK[2], 0.95F * fade);
        }
        ps.popPose();

        // 2) wither skulls riding the beam (vanilla model + textures, like the Wither's own skulls)
        renderSkulls(ps, buffer, from, dir, length, time, fade);

        // 3) glowing nether-star parts (additive)
        ps.pushPose();
        ps.translate(from.x, from.y, from.z);
        alignYAxis(ps, dir);
        VertexConsumer glow = buffer.getBuffer(RenderType.lightning());
        Matrix4f m = ps.last().pose();
        squarePrism(glow, m, 0.045F * pulse, 0, length, 1.0F, 1.0F, 1.0F, 0.95F * fade);
        squarePrism(glow, m, 0.085F * pulse, 0, length, STAR_YELLOW[0], STAR_YELLOW[1], STAR_YELLOW[2], 0.4F * fade);
        squarePrism(glow, m, 0.14F * pulse, 0, length, STAR_TEAL[0], STAR_TEAL[1], STAR_TEAL[2], 0.18F * fade);
        for (float y = 0.3F; y < length; y += 0.4F) {
            float angle = y * 1.4F - time * 0.3F;
            float size = 0.045F * edgeTaper(y, length);
            cube(glow, ps, Mth.cos(angle) * 0.42F, y, Mth.sin(angle) * 0.42F, size, angle,
                    STAR_WHITE[0], STAR_WHITE[1], STAR_WHITE[2], 0.9F * fade);
        }
        if (muzzle) {
            float charge = 0.11F + 0.025F * Mth.sin(time * 1.3F);
            netherStar(glow, ps, 0, 0, 0, charge, time * 0.25F, fade);
        }
        ps.popPose();

        if (impact) {
            renderWitherImpact(ps, buffer, to, dir, impactFace, time, fade, pulse);
        }
    }

    /**
     * The black ring, yellow ring and nether star where the Flayed God's beam lands. On a block they lie flat on the face
     * that was hit, lifted just off it, so they never sink into the block; on an entity or in the air they sit across the
     * beam.
     */
    private static void renderWitherImpact(PoseStack ps, MultiBufferSource buffer, Vec3 to, Vec3 dir, @Nullable Direction face,
                                           float time, float fade, float pulse) {
        Vec3 axis = face != null ? Vec3.atLowerCornerOf(face.getNormal()) : dir;
        float ringY = face != null ? 0.1F : -0.05F;
        float starY = face != null ? 0.3F * pulse : 0.0F;

        ps.pushPose();
        ps.translate(to.x, to.y, to.z);
        alignYAxis(ps, axis);
        VertexConsumer dark = buffer.getBuffer(RenderType.entityTranslucentEmissive(WHITE));
        for (int i = 0; i < 8; i++) {
            float angle = time * 0.15F + i * Mth.TWO_PI / 8.0F;
            texturedCube(dark, ps, Mth.cos(angle) * 0.62F * pulse, ringY, Mth.sin(angle) * 0.62F * pulse, 0.09F, angle,
                    WITHER_DARK[0], WITHER_DARK[1], WITHER_DARK[2], 0.9F * fade);
        }
        VertexConsumer glow = buffer.getBuffer(RenderType.lightning());
        netherStar(glow, ps, 0, starY, 0, 0.28F * pulse, -time * 0.18F, fade);
        for (int i = 0; i < 6; i++) {
            float angle = -time * 0.22F + i * Mth.TWO_PI / 6.0F;
            cube(glow, ps, Mth.cos(angle) * 0.4F, ringY, Mth.sin(angle) * 0.4F, 0.04F, angle,
                    STAR_YELLOW[0], STAR_YELLOW[1], STAR_YELLOW[2], 0.9F * fade);
        }
        ps.popPose();
    }

    private static void renderSkulls(PoseStack ps, MultiBufferSource buffer, Vec3 from, Vec3 dir, float length, float time, float fade) {
        if (length < 2.0F) {
            return;
        }
        if (skullModel == null) {
            skullModel = new SkullModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.WITHER_SKULL));
        }
        float spacing = 4.5F;
        float offset = (time * 0.55F) % spacing;
        // same rotation convention as projectiles (ProjectileUtil.rotateTowardsMovement)
        float yRot = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) + 90.0F;
        float xRot = (float) (Mth.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * Mth.RAD_TO_DEG);
        float scale = 0.85F;

        for (int pass = 0; pass < 2; pass++) {
            VertexConsumer consumer = pass == 0
                    ? buffer.getBuffer(RenderType.entityTranslucentEmissive(WITHER_SKULL))
                    : buffer.getBuffer(RenderType.eyes(WITHER_SKULL_GLOW));
            for (float y = offset + 0.6F; y < length - 0.4F; y += spacing) {
                float alpha = fade * Mth.clamp((y - 0.6F) / 1.5F, 0.0F, 1.0F) * Mth.clamp((length - 0.4F - y) / 1.0F, 0.0F, 1.0F);
                if (alpha <= 0.02F) {
                    continue;
                }
                ps.pushPose();
                ps.translate(from.x + dir.x * y, from.y + dir.y * y, from.z + dir.z * y);
                ps.scale(-scale, -scale, scale);
                ps.translate(0.0F, -0.25F, 0.0F);
                skullModel.setupAnim(0.0F, yRot, xRot);
                int color = pass == 0
                        ? FastColor.ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F)
                        : FastColor.ARGB32.colorFromFloat(1.0F, 0.55F * alpha, 0.55F * alpha, 0.6F * alpha);
                skullModel.renderToBuffer(ps, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
                ps.popPose();
            }
        }
    }

    /** A 3D six-pointed star like the nether star: a white-hot centre with pale yellow points, tumbling. */
    private static void netherStar(VertexConsumer c, PoseStack ps, float x, float y, float z, float size, float spin, float fade) {
        ps.pushPose();
        ps.translate(x, y, z);
        ps.mulPose(Axis.YP.rotation(spin));
        ps.mulPose(Axis.XP.rotation(spin * 0.6F));
        box(c, ps, size * 0.42F, size * 0.42F, size * 0.42F, 1.0F, 1.0F, 1.0F, 0.95F * fade);
        float t = size * 0.16F;
        box(c, ps, size, t, t, STAR_YELLOW[0], STAR_YELLOW[1], STAR_YELLOW[2], 0.85F * fade);
        box(c, ps, t, size, t, STAR_WHITE[0], STAR_WHITE[1], STAR_WHITE[2], 0.85F * fade);
        box(c, ps, t, t, size, STAR_YELLOW[0], STAR_YELLOW[1], STAR_YELLOW[2], 0.85F * fade);
        box(c, ps, size * 0.7F, size * 0.7F, size * 0.7F, STAR_TEAL[0], STAR_TEAL[1], STAR_TEAL[2], 0.15F * fade);
        ps.popPose();
    }

    // ------------------------------------------------------------------ other beam guns

    private static void renderMining(PoseStack ps, MultiBufferSource buffer, Vec3 from, Vec3 dir, float length, float time,
                                     float fade, float[] col, boolean muzzle, boolean impact) {
        ps.pushPose();
        ps.translate(from.x, from.y, from.z);
        alignYAxis(ps, dir);
        VertexConsumer c = buffer.getBuffer(RenderType.lightning());
        Matrix4f m = ps.last().pose();
        squarePrism(c, m, 0.035F, 0, length, 1.0F, 1.0F, 0.9F, 0.9F * fade);
        squarePrism(c, m, 0.07F, 0, length, col[0], col[1], col[2], 0.5F * fade);
        squarePrism(c, m, 0.12F, 0, length, col[0], col[1], col[2], 0.15F * fade);
        // screw thread: two strands of brass voxels turning fast, like a drill bit
        for (int strand = 0; strand < 2; strand++) {
            for (float y = 0.15F; y < length; y += 0.22F) {
                float angle = y * 3.0F - time * 1.1F + strand * Mth.PI;
                float heat = 1.0F - Mth.clamp(y / 6.0F, 0.0F, 0.6F);
                cube(c, ps, Mth.cos(angle) * 0.15F, y, Mth.sin(angle) * 0.15F, 0.03F, angle,
                        mix(col[0], 1.0F, 0.3F), mix(col[1], 0.85F, 0.3F), col[2] * 0.6F, 0.85F * heat * fade);
            }
        }
        if (muzzle) {
            cube(c, ps, 0, 0, 0, 0.06F, time * 0.6F, 1.0F, 1.0F, 0.85F, 0.9F * fade);
        }
        if (impact) {
            // a little Create cog biting into the block
            float spin = time * 0.45F;
            for (int i = 0; i < 8; i++) {
                float angle = spin + i * Mth.TWO_PI / 8.0F;
                cube(c, ps, Mth.cos(angle) * 0.24F, length - 0.04F, Mth.sin(angle) * 0.24F, 0.045F, angle,
                        col[0], col[1], col[2] * 0.7F, 0.9F * fade);
            }
            ps.pushPose();
            ps.translate(0, length - 0.04F, 0);
            ps.mulPose(Axis.YP.rotation(spin));
            box(c, ps, 0.17F, 0.03F, 0.17F, col[0], col[1], col[2], 0.45F * fade);
            box(c, ps, 0.07F, 0.05F, 0.07F, 1.0F, 1.0F, 0.9F, 0.9F * fade);
            ps.popPose();
        }
        ps.popPose();
    }

    private static void renderLaser(PoseStack ps, MultiBufferSource buffer, Vec3 from, Vec3 dir, float length, float time,
                                    float fade, float[] col, boolean muzzle, boolean impact) {
        ps.pushPose();
        ps.translate(from.x, from.y, from.z);
        alignYAxis(ps, dir);
        VertexConsumer c = buffer.getBuffer(RenderType.lightning());
        Matrix4f m = ps.last().pose();
        squarePrism(c, m, 0.02F, 0, length, 1.0F, 1.0F, 1.0F, 0.95F * fade);
        squarePrism(c, m, 0.045F, 0, length, col[0], col[1], col[2], 0.65F * fade);
        squarePrism(c, m, 0.09F, 0, length, col[0], col[1], col[2], 0.16F * fade);
        // pulses racing down the beam
        float spacing = 3.0F;
        for (float y = (time * 2.2F) % spacing; y < length; y += spacing) {
            cube(c, ps, 0, y, 0, 0.055F, 0, mix(col[0], 1.0F, 0.6F), mix(col[1], 1.0F, 0.6F), mix(col[2], 1.0F, 0.6F), 0.9F * fade);
        }
        if (muzzle) {
            cube(c, ps, 0, 0, 0, 0.05F, Mth.PI / 4.0F, col[0], col[1], col[2], 0.8F * fade);
        }
        if (impact) {
            float flick = 0.08F + 0.02F * Mth.sin(time * 2.0F);
            cube(c, ps, 0, length, 0, flick, time * 0.3F, 1.0F, 1.0F, 1.0F, 0.9F * fade);
            cube(c, ps, 0, length, 0, flick * 2.0F, -time * 0.2F, col[0], col[1], col[2], 0.3F * fade);
        }
        ps.popPose();
    }

    private static void renderCrystal(PoseStack ps, MultiBufferSource buffer, Vec3 from, Vec3 dir, float length, float time,
                                      float fade, float[] col, boolean muzzle, boolean impact) {
        ps.pushPose();
        ps.translate(from.x, from.y, from.z);
        alignYAxis(ps, dir);
        VertexConsumer c = buffer.getBuffer(RenderType.lightning());
        Matrix4f m = ps.last().pose();
        squarePrism(c, m, 0.03F, 0, length, 1.0F, 1.0F, 1.0F, 0.9F * fade);
        squarePrism(c, m, 0.07F, 0, length, col[0], col[1], col[2], 0.35F * fade);
        // amethyst-like shards strung along the beam, slowly turning
        int index = 0;
        for (float y = 0.5F; y < length; y += 0.7F, index++) {
            float angle = index * 2.4F + time * 0.06F;
            float tilt = (index % 2 == 0 ? 0.45F : -0.45F);
            float shade = 0.65F + 0.35F * ((index * 7) % 3) / 2.0F;
            ps.pushPose();
            ps.translate(Mth.cos(angle) * 0.13F, y, Mth.sin(angle) * 0.13F);
            ps.mulPose(Axis.YP.rotation(angle));
            ps.mulPose(Axis.ZP.rotation(tilt));
            box(c, ps, 0.035F, 0.13F, 0.035F, mix(col[0], 1.0F, 0.25F) * shade, mix(col[1], 1.0F, 0.25F) * shade,
                    mix(col[2], 1.0F, 0.25F) * shade, 0.7F * fade);
            ps.popPose();
        }
        if (muzzle) {
            cube(c, ps, 0, 0, 0, 0.05F, time * 0.2F, 1.0F, 1.0F, 1.0F, 0.85F * fade);
        }
        if (impact) {
            for (int i = 0; i < 5; i++) {
                float angle = i * Mth.TWO_PI / 5.0F + time * 0.05F;
                float grow = 1.0F + 0.15F * Mth.sin(time * 0.9F + i);
                ps.pushPose();
                ps.translate(0, length, 0);
                ps.mulPose(Axis.YP.rotation(angle));
                ps.mulPose(Axis.ZP.rotation(2.2F));
                ps.translate(0, 0.12F * grow, 0);
                box(c, ps, 0.035F, 0.12F * grow, 0.035F, col[0], col[1], col[2], 0.8F * fade);
                ps.popPose();
            }
        }
        ps.popPose();
    }

    private static void renderEnergy(PoseStack ps, MultiBufferSource buffer, Vec3 from, Vec3 dir, float length, float time,
                                     float fade, float[] col) {
        ps.pushPose();
        ps.translate(from.x, from.y, from.z);
        alignYAxis(ps, dir);
        VertexConsumer c = buffer.getBuffer(RenderType.lightning());
        Matrix4f m = ps.last().pose();
        float pulse = 1.0F + 0.15F * Mth.sin(time * 0.9F);
        squarePrism(c, m, 0.035F * pulse, 0, length, 1.0F, 1.0F, 1.0F, 0.9F * fade);
        squarePrism(c, m, 0.07F * pulse, 0, length, col[0], col[1], col[2], 0.5F * fade);
        squarePrism(c, m, 0.14F * pulse, 0, length, col[0], col[1], col[2], 0.15F * fade);
        for (int strand = 0; strand < 2; strand++) {
            for (float y = 0.2F; y < length; y += 0.3F) {
                float angle = y * 2.4F - time * 0.6F + strand * Mth.PI;
                cube(c, ps, Mth.cos(angle) * 0.18F, y, Mth.sin(angle) * 0.18F, 0.025F, angle, col[0], col[1], col[2], 0.8F * fade);
            }
        }
        ps.popPose();
    }

    // ------------------------------------------------------------------ particles (vanilla only)

    private static void spawnParticles(ClientLevel level, Style style, Vec3 from, Vec3 to, Vec3 dir, float length, float[] col, boolean impact,
                                       @Nullable Direction impactFace) {
        RandomSource r = RANDOM;
        Vec3 along = from.add(dir.scale(r.nextFloat() * length));
        switch (style) {
            case WITHER -> {
                if (r.nextFloat() < 0.6F) {
                    level.addParticle(ParticleTypes.SMOKE, along.x, along.y, along.z, 0, 0.01, 0);
                }
                if (impact) {
                    // start just off the block face that was hit, not inside the block
                    Vec3 at = impactFace != null ? to.add(Vec3.atLowerCornerOf(impactFace.getNormal()).scale(0.1)) : to;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, at.x, at.y, at.z, rnd(r, 0.05), 0.02, rnd(r, 0.05));
                    for (int i = 0; i < 2; i++) {
                        level.addParticle(ParticleTypes.END_ROD, at.x, at.y, at.z, rnd(r, 0.15), r.nextDouble() * 0.1, rnd(r, 0.15));
                    }
                    if (r.nextFloat() < 0.35F) {
                        level.addParticle(ParticleTypes.SOUL, at.x + rnd(r, 0.3), at.y + 0.1, at.z + rnd(r, 0.3), 0, 0.04, 0);
                    }
                }
            }
            case MINING -> {
                if (impact) {
                    BlockPos pos = BlockPos.containing(to.add(dir.scale(0.1)));
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) {
                        for (int i = 0; i < 2; i++) {
                            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), to.x, to.y, to.z, rnd(r, 0.3), 0.15, rnd(r, 0.3));
                        }
                    }
                    level.addParticle(ParticleTypes.ELECTRIC_SPARK, to.x, to.y, to.z, rnd(r, 0.4), r.nextDouble() * 0.3, rnd(r, 0.4));
                    if (r.nextFloat() < 0.3F) {
                        level.addParticle(ParticleTypes.SMOKE, to.x, to.y, to.z, 0, 0.03, 0);
                    }
                }
            }
            case LASER -> {
                if (impact) {
                    DustParticleOptions dust = new DustParticleOptions(new Vector3f(col[0], col[1], col[2]), 0.8F);
                    for (int i = 0; i < 2; i++) {
                        level.addParticle(dust, to.x + rnd(r, 0.15), to.y + rnd(r, 0.15), to.z + rnd(r, 0.15), 0, 0, 0);
                    }
                    if (r.nextFloat() < 0.4F) {
                        level.addParticle(ParticleTypes.SMOKE, to.x, to.y, to.z, 0, 0.02, 0);
                    }
                }
            }
            case CRYSTAL -> {
                DustParticleOptions dust = new DustParticleOptions(new Vector3f(col[0], col[1], col[2]), 1.0F);
                if (r.nextFloat() < 0.5F) {
                    level.addParticle(dust, along.x, along.y, along.z, 0, 0, 0);
                }
                if (impact) {
                    level.addParticle(dust, to.x + rnd(r, 0.2), to.y + rnd(r, 0.2), to.z + rnd(r, 0.2), 0, 0, 0);
                    if (r.nextFloat() < 0.3F) {
                        level.addParticle(ParticleTypes.END_ROD, to.x, to.y, to.z, rnd(r, 0.1), r.nextDouble() * 0.08, rnd(r, 0.1));
                    }
                }
            }
            default -> {
                if (impact) {
                    level.addParticle(ParticleTypes.END_ROD, to.x, to.y, to.z, rnd(r, 0.12), r.nextDouble() * 0.08, rnd(r, 0.12));
                    level.addParticle(ParticleTypes.ELECTRIC_SPARK, to.x, to.y, to.z, rnd(r, 0.4), r.nextDouble() * 0.3, rnd(r, 0.4));
                }
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    public static Vec3 lerpVec3(float partialTicks, Vec3 lastPos, Vec3 currentPos) {
        return new Vec3(Mth.lerp(partialTicks, lastPos.x, currentPos.x),
                Mth.lerp(partialTicks, lastPos.y, currentPos.y),
                Mth.lerp(partialTicks, lastPos.z, currentPos.z));
    }

    private static double rnd(RandomSource r, double spread) {
        return (r.nextDouble() - 0.5) * spread;
    }

    private static float mix(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /** Helix voxels shrink in near the muzzle and the impact so the helix grows out of / into them. */
    private static float edgeTaper(float y, float length) {
        return Mth.clamp(Math.min(y, length - y) / 0.8F, 0.25F, 1.0F);
    }

    /** Scales a colour so its strongest channel is 1, so dark configured colours still glow vividly. */
    private static float[] brighten(float[] color) {
        float max = Math.max(color[0], Math.max(color[1], color[2]));
        if (max <= 0.001F) {
            return new float[]{1.0F, 1.0F, 1.0F};
        }
        return new float[]{color[0] / max, color[1] / max, color[2] / max};
    }

    private static void alignYAxis(PoseStack poseStack, Vec3 dir) {
        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f target = new Vector3f((float) dir.x, (float) dir.y, (float) dir.z);
        float dot = up.dot(target);
        if (dot < -0.9999F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        } else if (dot < 0.9999F) {
            Vector3f axis = up.cross(target, new Vector3f()).normalize();
            poseStack.mulPose(Axis.of(axis).rotation((float) Math.acos(dot)));
        }
    }

    /** Open-ended square prism along +Y (voxel look), additive. */
    private static void squarePrism(VertexConsumer c, Matrix4f m, float r, float y0, float y1, float red, float g, float b, float a) {
        quad(c, m, -r, y0, -r, r, y0, -r, r, y1, -r, -r, y1, -r, red, g, b, a);
        quad(c, m, r, y0, r, -r, y0, r, -r, y1, r, r, y1, r, red, g, b, a);
        quad(c, m, -r, y0, r, -r, y0, -r, -r, y1, -r, -r, y1, r, red, g, b, a);
        quad(c, m, r, y0, -r, r, y0, r, r, y1, r, r, y1, -r, red, g, b, a);
    }

    /** Cube of half-size {@code half} centred at (x, y, z), turned by {@code rotY} around the beam axis. */
    private static void cube(VertexConsumer c, PoseStack ps, float x, float y, float z, float half, float rotY,
                             float r, float g, float b, float a) {
        ps.pushPose();
        ps.translate(x, y, z);
        ps.mulPose(Axis.YP.rotation(rotY));
        box(c, ps, half, half, half, r, g, b, a);
        ps.popPose();
    }

    /** Additive box centred on the current pose origin. */
    private static void box(VertexConsumer c, PoseStack ps, float hx, float hy, float hz, float r, float g, float b, float a) {
        Matrix4f m = ps.last().pose();
        float x0 = -hx, y0 = -hy, z0 = -hz, x1 = hx, y1 = hy, z1 = hz;
        quad(c, m, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, r, g, b, a);
        quad(c, m, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, r, g, b, a);
        quad(c, m, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, r, g, b, a);
        quad(c, m, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, r, g, b, a);
        quad(c, m, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, r, g, b, a);
        quad(c, m, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, r, g, b, a);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float r, float g, float b, float a) {
        // both windings so it is visible from any side (lightning culls back faces)
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
        consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
    }

    /**
     * Square textured sleeve along +Y (entity_translucent_emissive: dark colours stay dark, no lighting).
     * The texture repeats with square pixels and scrolls along the beam by {@code scroll} tiles.
     */
    private static void texturedPrism(VertexConsumer c, PoseStack.Pose pose, float r, float length, float scroll,
                                      float red, float g, float b, float a) {
        float v0 = scroll;
        float v1 = scroll + length / (2.0F * r);
        texQuad(c, pose, -r, 0, -r, r, 0, -r, r, length, -r, -r, length, -r, v0, v1, 0, 0, -1, red, g, b, a);
        texQuad(c, pose, r, 0, -r, r, 0, r, r, length, r, r, length, -r, v0, v1, 1, 0, 0, red, g, b, a);
        texQuad(c, pose, r, 0, r, -r, 0, r, -r, length, r, r, length, r, v0, v1, 0, 0, 1, red, g, b, a);
        texQuad(c, pose, -r, 0, r, -r, 0, -r, -r, length, -r, -r, length, r, v0, v1, -1, 0, 0, red, g, b, a);
    }

    /** Solid-colour cube with the plain white texture (entity_translucent_emissive). */
    private static void texturedCube(VertexConsumer c, PoseStack ps, float x, float y, float z, float half, float rotY,
                                     float r, float g, float b, float a) {
        ps.pushPose();
        ps.translate(x, y, z);
        ps.mulPose(Axis.YP.rotation(rotY));
        PoseStack.Pose p = ps.last();
        float h = half;
        texQuad(c, p, -h, -h, -h, h, -h, -h, h, h, -h, -h, h, -h, 0, 1, 0, 0, -1, r, g, b, a);
        texQuad(c, p, h, -h, h, -h, -h, h, -h, h, h, h, h, h, 0, 1, 0, 0, 1, r, g, b, a);
        texQuad(c, p, -h, -h, h, -h, -h, -h, -h, h, -h, -h, h, h, 0, 1, -1, 0, 0, r, g, b, a);
        texQuad(c, p, h, -h, -h, h, -h, h, h, h, h, h, h, -h, 0, 1, 1, 0, 0, r, g, b, a);
        texQuad(c, p, -h, h, -h, h, h, -h, h, h, h, -h, h, h, 0, 1, 0, 1, 0, r, g, b, a);
        texQuad(c, p, -h, -h, h, h, -h, h, h, -h, -h, -h, -h, -h, 0, 1, 0, -1, 0, r, g, b, a);
        ps.popPose();
    }

    private static void texQuad(VertexConsumer c, PoseStack.Pose pose,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                float x2, float y2, float z2, float x3, float y3, float z3,
                                float v0, float v1, float nx, float ny, float nz,
                                float r, float g, float b, float a) {
        vertex(c, pose, x0, y0, z0, 0, v0, nx, ny, nz, r, g, b, a);
        vertex(c, pose, x1, y1, z1, 1, v0, nx, ny, nz, r, g, b, a);
        vertex(c, pose, x2, y2, z2, 1, v1, nx, ny, nz, r, g, b, a);
        vertex(c, pose, x3, y3, z3, 0, v1, nx, ny, nz, r, g, b, a);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, float r, float g, float b, float a) {
        c.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }
}
