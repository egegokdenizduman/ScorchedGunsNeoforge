package top.ribs.scguns.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * Converts points from whatever pose a renderer is currently drawing with into the world.
 * <p>
 * The level pass splits the view between the model-view matrix (camera rotation) and the pose stack (camera-relative
 * translation); the first-person hand pass keeps everything in the pose stack. In both cases their product maps a
 * point into the camera's eye frame, so we combine them first and only then rotate back by the camera's orientation.
 */
public final class RenderSpace {
    private RenderSpace() {
    }

    /** The pose's origin in the camera's eye frame (x right, y up, -z forward), or null if the pose is degenerate. */
    @Nullable
    public static Vector3f eyeFrameOrigin(Matrix4f pose) {
        Matrix4f eyeFromPose = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(pose);
        Vector3f p = new Vector3f(eyeFromPose.m30(), eyeFromPose.m31(), eyeFromPose.m32());
        return p.isFinite() ? p : null;
    }

    /** The pose's -Z axis (the way a barrel points) in the camera's eye frame, normalised, or null if degenerate. */
    @Nullable
    public static Vector3f eyeFrameForward(Matrix4f pose) {
        Matrix4f eyeFromPose = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(pose);
        Vector3f d = new Vector3f(-eyeFromPose.m20(), -eyeFromPose.m21(), -eyeFromPose.m22());
        float len = d.length();
        return len > 1.0E-3F && Float.isFinite(len) ? d.div(len) : null;
    }

    /** A direction in the camera's eye frame expressed in world axes. */
    public static Vector3f eyeToWorldDirection(Vector3f eyeDir) {
        Quaternionf orientation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        return orientation.transform(eyeDir, new Vector3f());
    }

    /** A point in the camera's eye frame expressed as a world position, using the camera as it is right now. */
    public static Vec3 eyeToWorld(Vector3f eyePoint) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f offset = eyeToWorldDirection(eyePoint);
        return camera.getPosition().add(offset.x, offset.y, offset.z);
    }
}
