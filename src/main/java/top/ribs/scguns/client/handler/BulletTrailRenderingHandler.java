package top.ribs.scguns.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Matrix4f;
import top.ribs.scguns.client.BulletTrail;
import top.ribs.scguns.client.GunRenderType;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.entity.projectile.ShulkshotProjectileEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class BulletTrailRenderingHandler
{
    private static BulletTrailRenderingHandler instance;

    public static BulletTrailRenderingHandler get()
    {
        if(instance == null)
        {
            instance = new BulletTrailRenderingHandler();
        }
        return instance;
    }

    private final Map<Integer, BulletTrail> bullets = new HashMap<>();

    private BulletTrailRenderingHandler() {}

    /**
     * Adds a bullet trail to render into the world
     *
     * @param trail the bullet trail get
     */
    public void add(BulletTrail trail) {
        Level world = Minecraft.getInstance().level;
        if (world != null) {
            // Check if the entity is not a ShulkshotProjectileEntity before adding the trail
            if (!(world.getEntity(trail.getEntityId()) instanceof ShulkshotProjectileEntity)) {
                this.bullets.put(trail.getEntityId(), trail);
            }
        }
    }
    /**
     * Removes the bullet for the given entity id.
     *
     * @param entityId the entity id of the bullet
     */
    public void remove(int entityId)
    {
        this.bullets.remove(entityId);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event)
    {
        Level world = Minecraft.getInstance().level;
        if(world != null)
        {
            this.bullets.values().forEach(BulletTrail::tick);
            this.bullets.values().removeIf(BulletTrail::isDead);
        }
        else if(!this.bullets.isEmpty())
        {
            this.bullets.clear();
        }
    }

    public void render(PoseStack stack, float partialSticks)
    {
        for(BulletTrail bulletTrail : this.bullets.values())
        {
            this.renderBulletTrail(bulletTrail, stack, partialSticks);
        }
    }

    @SubscribeEvent
    public void onRespawn(ClientPlayerNetworkEvent.Clone event)
    {
        this.bullets.clear();
    }

    @SubscribeEvent
    public void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        this.bullets.clear();
    }

    /**
     * Voxel tracer (in the spirit of Iron's Artifice): a thin bright core plus a chain of small glowing cubes that
     * shrink and fade toward the tail, white-hot at the bullet and blending into the gun's trail colour.
     * Drawn with the additive, full-bright lightning render type so it glows (also under shader packs).
     * The trail runs along -Y in the current pose, starting at the bullet.
     */
    private static void renderVoxelTracer(VertexConsumer consumer, Matrix4f matrix, float length, float width, float red, float green, float blue)
    {
        float core = Math.max(0.006F, width * 0.35F);
        cube(consumer, matrix, -core, -length, -core, core, 0, core, red, green, blue, 0.55F);

        float spacing = 0.3F;
        int count = Math.max(1, (int) (length / spacing));
        for(int i = 0; i < count; i++)
        {
            float t = (float) i / (float) count;          // 0 at the bullet, 1 at the tail
            float size = Math.max(0.012F, width * 1.6F * (1.0F - t * 0.8F));
            float heat = 1.0F - t;                          // white-hot head blending into the trail colour
            float r = red + (1.0F - red) * heat * 0.8F;
            float g = green + (1.0F - green) * heat * 0.8F;
            float b = blue + (1.0F - blue) * heat * 0.8F;
            float alpha = 0.9F * (1.0F - t) + 0.1F;
            float y = -i * spacing;
            cube(consumer, matrix, -size, y - size, -size, size, y + size, size, r, g, b, alpha);
        }
    }

    private static void cube(VertexConsumer c, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b, float a)
    {
        // six faces, outward winding
        face(c, m, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, r, g, b, a);
        face(c, m, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, r, g, b, a);
        face(c, m, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, r, g, b, a);
        face(c, m, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, r, g, b, a);
        face(c, m, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, r, g, b, a);
        face(c, m, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, r, g, b, a);
    }

    private static void face(VertexConsumer c, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz, float r, float g, float b, float a)
    {
        c.addVertex(m, ax, ay, az).setColor(r, g, b, a);
        c.addVertex(m, bx, by, bz).setColor(r, g, b, a);
        c.addVertex(m, cx, cy, cz).setColor(r, g, b, a);
        c.addVertex(m, dx, dy, dz).setColor(r, g, b, a);
        c.addVertex(m, dx, dy, dz).setColor(r, g, b, a);
        c.addVertex(m, cx, cy, cz).setColor(r, g, b, a);
        c.addVertex(m, bx, by, bz).setColor(r, g, b, a);
        c.addVertex(m, ax, ay, az).setColor(r, g, b, a);
    }

    private void renderBulletTrail(BulletTrail trail, PoseStack poseStack, float deltaTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getCameraEntity();
        if(entity == null || trail.isDead())
            return;

        Entity shooter = trail.getShooter();
        boolean ownFirstPerson = mc.options.getCameraType().isFirstPerson() && shooter instanceof Player player && player.isLocalPlayer();

        poseStack.pushPose();

        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 position = trail.getPosition();
        Vec3 motion = trail.getMotion();
        double bulletX = position.x + motion.x * deltaTicks;
        double bulletY = position.y + motion.y * deltaTicks;
        double bulletZ = position.z + motion.z * deltaTicks;
        poseStack.translate(bulletX - view.x(), bulletY - view.y(), bulletZ - view.z());

        poseStack.mulPose(Axis.YP.rotationDegrees(trail.getYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-trail.getPitch() + 90));

        Vec3 motionVec = new Vec3(motion.x, motion.y, motion.z);
        float trailLength = (float) (motionVec.length() * trail.getTrailLengthMultiplier());
        float red = (float) (trail.getTrailColor() >> 16 & 255) / 255.0F;
        float green = (float) (trail.getTrailColor() >> 8 & 255) / 255.0F;
        float blue = (float) (trail.getTrailColor() & 255) / 255.0F;
        float alpha = 0.3F;
        float trailWidth = (float) (0.035F * trail.getTrailThickness());

        // Prevents the trail length from being longer than the distance to shooter
        if(shooter != null)
        {
            double toShooter = shooter.getEyePosition(deltaTicks).distanceTo(new Vec3(bulletX,bulletY, bulletZ));
            // the tracer's tail starts at the gun, not inside the shooter's head: further out in your own first-person
            // view (the gun model is drawn close to the camera), at roughly arm's length for everyone else
            trailLength = (float) Math.min(trailLength, ownFirstPerson ? toShooter - 1.0 : toShooter - 0.6);
        }
        if(ownFirstPerson && trailLength <= 0.1F)
        {
            poseStack.popPose();
            return;
        }

        Matrix4f matrix4f = poseStack.last().pose();
        MultiBufferSource.BufferSource renderTypeBuffer = mc.renderBuffers().bufferSource();

        if(trail.isTrailVisible() && trailLength > 0.1F)
        {
            renderVoxelTracer(renderTypeBuffer.getBuffer(RenderType.lightning()), matrix4f, trailLength, trailWidth, red, green, blue);
            renderTypeBuffer.endBatch(RenderType.lightning());
        }

        if(!trail.getItem().isEmpty())
        {
            poseStack.mulPose(Axis.YP.rotationDegrees((trail.getAge() + deltaTicks) * (float) 50));
            poseStack.scale(0.275F, 0.275F, 0.275F);

            int combinedLight = LevelRenderer.getLightColor(entity.level(), BlockPos.containing(entity.position()));
            ItemStack stack = trail.getItem();
            RenderUtil.renderModel(stack, ItemDisplayContext.NONE, poseStack, renderTypeBuffer, combinedLight, OverlayTexture.NO_OVERLAY, null, null);
        }

        poseStack.popPose();
    }
}
