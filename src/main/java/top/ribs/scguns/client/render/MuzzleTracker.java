package top.ribs.scguns.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Remembers where each entity's gun muzzle was actually drawn, so effects that live in the world (beams) can start
 * at the rendered barrel instead of at a guessed offset.
 * <p>
 * Third-person / other players' guns are drawn during the level pass, before the beams, so their muzzle is stored as
 * a world position for the same frame. The first-person gun is drawn after the level with its own fixed field of view,
 * so it is stored in view space and turned into a world position with the current camera when it is used: the point
 * is scaled so it lands on the same screen pixel under the world's field of view, and does not lag a frame behind
 * when the camera turns.
 */
public final class MuzzleTracker {
    private static final Map<Integer, Muzzle> MUZZLES = new HashMap<>();

    /** First person keeps {@code eye} (plus the hand projection's y scale); everything else keeps {@code world}. */
    private record Muzzle(@Nullable Vector3f eye, @Nullable Vec3 world, float handProjY, long frame) {
        boolean firstPerson() {
            return eye != null;
        }
    }

    private MuzzleTracker() {
    }

    /**
     * Records the muzzle of the gun being drawn: the origin of {@code pose} is the muzzle.
     *
     * @param firstPerson true when drawn as the first-person held item (hand pass)
     */
    public static void record(Entity entity, Matrix4f pose, boolean firstPerson) {
        // Iris draws a shadow pass from the sun's point of view; those poses do not map back to the world
        if (isIrisShadowPass()) {
            return;
        }
        Vector3f eye = RenderSpace.eyeFrameOrigin(pose);
        if (eye == null) {
            return;
        }
        long frame = MuzzleFlashRenderer.currentFrame();
        Muzzle muzzle = firstPerson
                ? new Muzzle(eye, null, RenderSystem.getProjectionMatrix().m11(), frame)
                : new Muzzle(null, RenderSpace.eyeToWorld(eye), 0F, frame);
        MUZZLES.put(entity.getId(), muzzle);
        if (MUZZLES.size() > 64) {
            MUZZLES.entrySet().removeIf(e -> e.getValue().frame() < frame - 1);
        }
    }

    private static boolean irisChecked;
    @Nullable
    private static Object irisApi;
    @Nullable
    private static Method irisShadowPass;

    private static boolean isIrisShadowPass() {
        if (!irisChecked) {
            irisChecked = true;
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisApi = api.getMethod("getInstance").invoke(null);
                irisShadowPass = api.getMethod("isRenderingShadowPass");
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Iris is not installed
            }
        }
        if (irisApi == null || irisShadowPass == null) {
            return false;
        }
        try {
            return (boolean) irisShadowPass.invoke(irisApi);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * World position of the entity's rendered muzzle, or null if its gun was not drawn in the last frame
     * (or was drawn in the other camera mode).
     *
     * @param firstPerson     whether the caller wants the first-person (hand) muzzle
     * @param worldProjection the level's projection matrix for this frame
     */
    @Nullable
    public static Vec3 get(Entity entity, boolean firstPerson, Matrix4f worldProjection) {
        Muzzle muzzle = MUZZLES.get(entity.getId());
        if (muzzle == null || muzzle.firstPerson() != firstPerson || muzzle.frame() < MuzzleFlashRenderer.currentFrame() - 1) {
            return null;
        }
        if (!firstPerson) {
            return muzzle.world();
        }
        // same screen position under the world's field of view: x and y scale by the ratio of the projections, depth stays
        float k = muzzle.handProjY() / worldProjection.m11();
        if (!Float.isFinite(k) || k <= 0F) {
            return null;
        }
        Vector3f eye = muzzle.eye();
        return RenderSpace.eyeToWorld(new Vector3f(eye.x * k, eye.y * k, eye.z));
    }
}
