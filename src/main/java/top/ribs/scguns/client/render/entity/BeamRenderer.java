package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Energy beam for beam weapons: a white-hot core, a pulsing coloured sheath, two energy strands spiralling around it
 * and a flickering arc, plus sparks where it lands. Everything is drawn with the vanilla lightning render type
 * (additive, full-bright), which shader packs treat as emissive.
 */
@OnlyIn(Dist.CLIENT)
public class BeamRenderer {
    private static final int SIDES = 8;
    private static final RandomSource SPARK_RANDOM = RandomSource.create();

    public BeamRenderer() {
    }

    public static void renderBeam(PoseStack poseStack, MultiBufferSource buffer, float partialTicks, Vec3 start, Vec3 end, Vec3 lastStart, Vec3 lastEnd, float[] color, float fadeProgress) {
        Vec3 from = lerpVec3(partialTicks, lastStart, start);
        Vec3 to = lerpVec3(partialTicks, lastEnd, end);
        Vec3 beam = to.subtract(from);
        float length = (float) beam.length();
        if (length < 0.01F) {
            return;
        }
        Vec3 dir = beam.normalize();
        ClientLevel level = Minecraft.getInstance().level;
        float time = level != null ? (level.getGameTime() % 24000L) + partialTicks : partialTicks;
        float fade = Mth.clamp(1.0F - fadeProgress, 0.0F, 1.0F);
        if (fadeProgress <= 0.0F) {
            fade = 1.0F;
        }
        float[] bright = brighten(color);

        poseStack.pushPose();
        poseStack.translate(from.x, from.y, from.z);
        alignYAxis(poseStack, dir);
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float pulse = 1.0F + 0.15F * Mth.sin(time * 0.9F);
        // white-hot core and coloured sheaths
        prism(consumer, matrix, 0.035F * pulse, length, 1.0F, 1.0F, 1.0F, 0.9F * fade);
        prism(consumer, matrix, 0.07F * pulse, length, bright[0], bright[1], bright[2], 0.55F * fade);
        prism(consumer, matrix, 0.14F * pulse, length, bright[0], bright[1], bright[2], 0.22F * fade);
        prism(consumer, matrix, 0.26F * pulse, length, bright[0], bright[1], bright[2], 0.08F * fade);

        // two strands spiralling around the beam
        for (int strand = 0; strand < 2; strand++) {
            float phase = strand * Mth.PI;
            Vector3f prev = null;
            for (float y = 0; y <= length; y += 0.2F) {
                float angle = y * 2.4F - time * 0.6F + phase;
                Vector3f point = new Vector3f(Mth.cos(angle) * 0.18F, y, Mth.sin(angle) * 0.18F);
                if (prev != null) {
                    segment(consumer, matrix, prev, point, 0.018F, bright[0], bright[1], bright[2], 0.85F * fade);
                }
                prev = point;
            }
        }

        // flickering arc that re-rolls every tick
        RandomSource arcRandom = RandomSource.create((long) time * 31L);
        Vector3f prev = new Vector3f(0, 0, 0);
        for (float y = 0.5F; y <= length; y += 0.5F) {
            Vector3f point = new Vector3f((arcRandom.nextFloat() - 0.5F) * 0.3F, y, (arcRandom.nextFloat() - 0.5F) * 0.3F);
            segment(consumer, matrix, prev, point, 0.012F, 1.0F, 1.0F, 1.0F, 0.6F * fade);
            prev = point;
        }
        poseStack.popPose();

        // sparks where the beam lands
        if (level != null && SPARK_RANDOM.nextFloat() < 0.6F * fade) {
            level.addParticle(ParticleTypes.END_ROD, to.x, to.y, to.z,
                    (SPARK_RANDOM.nextDouble() - 0.5) * 0.12, SPARK_RANDOM.nextDouble() * 0.08, (SPARK_RANDOM.nextDouble() - 0.5) * 0.12);
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, to.x, to.y, to.z,
                    (SPARK_RANDOM.nextDouble() - 0.5) * 0.4, SPARK_RANDOM.nextDouble() * 0.3, (SPARK_RANDOM.nextDouble() - 0.5) * 0.4);
        }
    }

    public static Vec3 lerpVec3(float partialTicks, Vec3 lastPos, Vec3 currentPos) {
        return new Vec3(Mth.lerp(partialTicks, lastPos.x, currentPos.x),
                Mth.lerp(partialTicks, lastPos.y, currentPos.y),
                Mth.lerp(partialTicks, lastPos.z, currentPos.z));
    }

    /** Scales a colour so its strongest channel is 1: dark beam colours (e.g. Flayed God's) still glow vividly. */
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

    /** Open-ended prism along +Y from 0 to height. */
    private static void prism(VertexConsumer consumer, Matrix4f matrix, float radius, float height, float r, float g, float b, float a) {
        for (int i = 0; i < SIDES; i++) {
            float a0 = Mth.TWO_PI * i / SIDES;
            float a1 = Mth.TWO_PI * (i + 1) / SIDES;
            float x0 = Mth.cos(a0) * radius, z0 = Mth.sin(a0) * radius;
            float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;
            quad(consumer, matrix, x0, 0, z0, x1, 0, z1, x1, height, z1, x0, height, z0, r, g, b, a);
        }
    }

    /** Thin four-sided strip between two points. */
    private static void segment(VertexConsumer consumer, Matrix4f matrix, Vector3f p0, Vector3f p1, float width,
                                float r, float g, float b, float a) {
        quad(consumer, matrix, p0.x - width, p0.y, p0.z, p0.x + width, p0.y, p0.z, p1.x + width, p1.y, p1.z, p1.x - width, p1.y, p1.z, r, g, b, a);
        quad(consumer, matrix, p0.x, p0.y, p0.z - width, p0.x, p0.y, p0.z + width, p1.x, p1.y, p1.z + width, p1.x, p1.y, p1.z - width, r, g, b, a);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float r, float g, float b, float a) {
        // both windings so the strip is visible from any side
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
        consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
    }
}
