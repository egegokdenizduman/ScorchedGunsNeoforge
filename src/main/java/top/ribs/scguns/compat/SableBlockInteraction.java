package top.ribs.scguns.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import top.ribs.scguns.ScorchedGuns;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class SableBlockInteraction {
    private static boolean unavailable;

    @Nullable
    private static Class<?> boundingBoxClass;
    @Nullable
    private static Class<?> boundingBoxInterfaceClass;
    @Nullable
    private static Field sableHelperField;
    @Nullable
    private static Method getAllIntersectingMethod;
    @Nullable
    private static Method getContainingMethod;
    @Nullable
    private static Constructor<?> boundingBoxConstructor;
    @Nullable
    private static Constructor<?> boundingBoxBlockConstructor;
    @Nullable
    private static Constructor<?> boundingBoxEmptyConstructor;
    @Nullable
    private static Method boundingBoxTransformInverseMethod;
    @Nullable
    private static Method boundingBoxMinXMethod;
    @Nullable
    private static Method boundingBoxMinYMethod;
    @Nullable
    private static Method boundingBoxMinZMethod;
    @Nullable
    private static Method boundingBoxMaxXMethod;
    @Nullable
    private static Method boundingBoxMaxYMethod;
    @Nullable
    private static Method boundingBoxMaxZMethod;
    @Nullable
    private static Method subLevelLogicalPoseMethod;
    @Nullable
    private static Method subLevelGetLevelMethod;
    @Nullable
    private static Method transformPositionMethod;
    @Nullable
    private static Method transformPositionInverseMethod;
    @Nullable
    private static Class<?> clipContextExtensionClass;
    @Nullable
    private static Method setDoNotProjectMethod;
    @Nullable
    private static Field clipBlockField;
    @Nullable
    private static Field clipFluidField;
    @Nullable
    private static Field clipCollisionContextField;
    @Nullable
    private static Method getContainingVec3iMethod;
    private static boolean impulseUnavailable;
    @Nullable
    private static Class<?> serverSubLevelClass;
    @Nullable
    private static Method rigidBodyOfMethod;
    @Nullable
    private static Method applyImpulseAtPointMethod;

    private SableBlockInteraction() {
    }

    public interface EmbeddedBlockHit {
        @Nullable
        Level scguns$getInteractionLevel();

        @Nullable
        BlockPos scguns$getInteractionBlockPos();
    }

    public static class SableBlockHitResult extends BlockHitResult implements EmbeddedBlockHit {
        private final Level interactionLevel;
        private final BlockPos interactionBlockPos;

        public SableBlockHitResult(Vec3 globalLocation, Direction localDirection, BlockPos localBlockPos, boolean insideBlock, Level interactionLevel) {
            super(globalLocation, localDirection, localBlockPos, insideBlock);
            this.interactionLevel = interactionLevel;
            this.interactionBlockPos = localBlockPos;
        }

        @Override
        public Level scguns$getInteractionLevel() {
            return this.interactionLevel;
        }

        @Override
        public BlockPos scguns$getInteractionBlockPos() {
            return this.interactionBlockPos;
        }
    }

    @Nullable
    public static BlockHitResult clipSubLevels(Level world, ClipContext context) {
        return clipSubLevels(world, context, state -> false);
    }

    @Nullable
    public static BlockHitResult clipSubLevels(Level world, ClipContext context, Predicate<BlockState> ignorePredicate) {
        if (!ScorchedGuns.sableLoaded || unavailable) {
            return null;
        }

        try {
            ensureReflection();
            Object helper = sableHelperField.get(null);
            Object boundingBox = boundingBoxConstructor.newInstance(context.getFrom(), context.getTo());
            Iterable<?> subLevels = (Iterable<?>) getAllIntersectingMethod.invoke(helper, world, boundingBox);
            BlockHitResult nearest = null;
            double nearestDistance = Double.MAX_VALUE;

            for (Object subLevel : subLevels) {
                Object pose = subLevelLogicalPoseMethod.invoke(subLevel);
                Vec3 localFrom = transformPosition(pose, context.getFrom(), true);
                Vec3 localTo = transformPosition(pose, context.getTo(), true);
                Object containing = getContainingMethod.invoke(helper, world, localFrom);
                if (containing != subLevel) {
                    continue;
                }

                Level interactionLevel = (Level) subLevelGetLevelMethod.invoke(subLevel);
                ClipContext localContext = copyContext(context, localFrom, localTo);
                setDoNotProject(localContext, true);
                BlockHitResult localResult = interactionLevel.clip(localContext);
                if (localResult == null || localResult.getType() == HitResult.Type.MISS) {
                    continue;
                }

                BlockPos localPos = localResult.getBlockPos();
                BlockState localState = interactionLevel.getBlockState(localPos);
                if (ignorePredicate.test(localState)) {
                    continue;
                }

                Vec3 globalLocation = transformPosition(pose, localResult.getLocation(), false);
                double distance = context.getFrom().distanceToSqr(globalLocation);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = new SableBlockHitResult(globalLocation, localResult.getDirection(), localPos, localResult.isInside(), interactionLevel);
                }
            }

            return nearest;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            unavailable = true;
            return null;
        }
    }

    public static Level levelFor(Level fallback, BlockHitResult hitResult) {
        if (hitResult instanceof EmbeddedBlockHit embedded && embedded.scguns$getInteractionLevel() != null) {
            return embedded.scguns$getInteractionLevel();
        }
        return fallback;
    }

    public static BlockPos blockPosFor(BlockHitResult hitResult) {
        if (hitResult instanceof EmbeddedBlockHit embedded && embedded.scguns$getInteractionBlockPos() != null) {
            return embedded.scguns$getInteractionBlockPos();
        }
        return hitResult.getBlockPos();
    }

    public static BlockState getBlockState(Level fallback, BlockHitResult hitResult) {
        Level level = levelFor(fallback, hitResult);
        return level.getBlockState(blockPosFor(hitResult));
    }

    public static boolean destroyBlock(Level fallback, BlockHitResult hitResult, boolean drop) {
        return levelFor(fallback, hitResult).destroyBlock(blockPosFor(hitResult), drop);
    }

    public static boolean removeBlock(Level fallback, BlockHitResult hitResult, boolean moving) {
        return levelFor(fallback, hitResult).removeBlock(blockPosFor(hitResult), moving);
    }

    public static boolean destroyBlock(Level fallback, BlockPos fallbackPos, boolean drop, @Nullable Level interactionLevel, @Nullable BlockPos interactionPos) {
        Level level = interactionLevel != null ? interactionLevel : fallback;
        BlockPos pos = interactionPos != null ? interactionPos : fallbackPos;
        return level.destroyBlock(pos, drop);
    }

    public static boolean removeBlock(Level fallback, BlockPos fallbackPos, boolean moving, @Nullable Level interactionLevel, @Nullable BlockPos interactionPos) {
        Level level = interactionLevel != null ? interactionLevel : fallback;
        BlockPos pos = interactionPos != null ? interactionPos : fallbackPos;
        return level.removeBlock(pos, moving);
    }

    public static float collectExplosionBlocks(Level world, Explosion explosion, ExplosionDamageCalculator calculator, Set<BlockPos> affectedBlocks, BlockPos globalPos, BlockState globalState, float strength) {
        if (!ScorchedGuns.sableLoaded || unavailable || !globalState.isAir()) {
            return strength;
        }

        try {
            ensureReflection();
            Object helper = sableHelperField.get(null);
            Object globalBox = boundingBoxBlockConstructor.newInstance(globalPos);
            Iterable<?> subLevels = (Iterable<?>) getAllIntersectingMethod.invoke(helper, world, globalBox);
            float currentStrength = strength;

            for (Object subLevel : subLevels) {
                Object pose = subLevelLogicalPoseMethod.invoke(subLevel);
                Object localBox = boundingBoxEmptyConstructor.newInstance();
                boundingBoxTransformInverseMethod.invoke(globalBox, pose, localBox);

                int minX = floorBoxValue(boundingBoxMinXMethod, localBox);
                int minY = floorBoxValue(boundingBoxMinYMethod, localBox);
                int minZ = floorBoxValue(boundingBoxMinZMethod, localBox);
                int maxX = floorBoxValue(boundingBoxMaxXMethod, localBox);
                int maxY = floorBoxValue(boundingBoxMaxYMethod, localBox);
                int maxZ = floorBoxValue(boundingBoxMaxZMethod, localBox);

                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            BlockPos localPos = new BlockPos(x, y, z);
                            BlockState localState = world.getBlockState(localPos);
                            FluidState localFluid = world.getFluidState(localPos);
                            Optional<Float> resistance = calculator.getBlockExplosionResistance(explosion, world, localPos, localState, localFluid);
                            if (resistance.isPresent()) {
                                currentStrength -= (resistance.get() + 0.3F) * 0.3F;
                            }

                            if (currentStrength > 0.0F && calculator.shouldBlockExplode(explosion, world, localPos, localState, currentStrength)) {
                                affectedBlocks.add(localPos);
                            }
                        }
                    }
                }
            }

            return currentStrength;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            unavailable = true;
            return strength;
        }
    }

    /**
     * True when the block hit belongs to a Sable sub-level (a Create Aeronautics ship, a physics contraption...).
     * Always false without Sable.
     */
    public static boolean isSubLevelHit(Level world, BlockHitResult hitResult) {
        if (hitResult instanceof SableBlockHitResult) {
            return true;
        }
        return getContainingSubLevel(world, blockPosFor(hitResult)) != null;
    }

    /**
     * Pushes the Sable sub-level that owns the hit block, like Sable does for arrows and explosions.
     * {@code globalPoint} and {@code globalImpulse} are in world space; they are converted into the
     * sub-level's local frame before being handed to its rigid body. Server side only; no-op without Sable.
     */
    public static void applyImpulse(Level world, BlockHitResult hitResult, Vec3 globalPoint, Vec3 globalImpulse) {
        if (!ScorchedGuns.sableLoaded || impulseUnavailable || world.isClientSide() || globalImpulse.lengthSqr() < 1.0E-8) {
            return;
        }

        try {
            Object subLevel = getContainingSubLevel(world, blockPosFor(hitResult));
            if (subLevel == null) {
                return;
            }
            ensureImpulseReflection();
            if (!serverSubLevelClass.isInstance(subLevel)) {
                return;
            }

            Object pose = subLevelLogicalPoseMethod.invoke(subLevel);
            Vec3 localPoint = transformPosition(pose, globalPoint, true);
            Method normalInverse = pose.getClass().getMethod("transformNormalInverse", Vec3.class);
            Object localImpulseObject = normalInverse.invoke(pose, globalImpulse);
            if (!(localImpulseObject instanceof Vec3 localImpulse)) {
                return;
            }

            Object handle = rigidBodyOfMethod.invoke(null, subLevel);
            if (handle != null) {
                applyImpulseAtPointMethod.invoke(handle, localPoint, localImpulse);
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            impulseUnavailable = true;
        }
    }

    @Nullable
    private static Object getContainingSubLevel(Level world, BlockPos pos) {
        if (!ScorchedGuns.sableLoaded || unavailable) {
            return null;
        }

        try {
            ensureReflection();
            if (getContainingVec3iMethod == null) {
                getContainingVec3iMethod = sableHelperField.getType().getMethod("getContaining", Level.class, Vec3i.class);
            }
            return getContainingVec3iMethod.invoke(sableHelperField.get(null), world, pos);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static void ensureImpulseReflection() throws ReflectiveOperationException {
        if (applyImpulseAtPointMethod != null) {
            return;
        }

        ensureReflection();
        serverSubLevelClass = Class.forName("dev.ryanhcode.sable.sublevel.ServerSubLevel");
        Class<?> rigidBodyHandleClass = Class.forName("dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle");
        rigidBodyOfMethod = rigidBodyHandleClass.getMethod("of", serverSubLevelClass);
        applyImpulseAtPointMethod = rigidBodyHandleClass.getMethod("applyImpulseAtPoint", Vec3.class, Vec3.class);
    }

    private static void ensureReflection() throws ReflectiveOperationException {
        if (sableHelperField != null) {
            return;
        }

        Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
        Class<?> activeCompanionClass = Class.forName("dev.ryanhcode.sable.ActiveSableCompanion");
        Class<?> subLevelClass = Class.forName("dev.ryanhcode.sable.sublevel.SubLevel");
        boundingBoxClass = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3d");
        boundingBoxInterfaceClass = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3dc");
        Class<?> poseInterfaceClass = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc");
        clipContextExtensionClass = Class.forName("dev.ryanhcode.sable.mixinterface.clip_overwrite.ClipContextExtension");

        sableHelperField = sableClass.getField("HELPER");
        getAllIntersectingMethod = activeCompanionClass.getMethod("getAllIntersecting", Level.class, boundingBoxInterfaceClass);
        getContainingMethod = activeCompanionClass.getMethod("getContaining", Level.class, Position.class);
        boundingBoxConstructor = boundingBoxClass.getConstructor(Vec3.class, Vec3.class);
        boundingBoxBlockConstructor = boundingBoxClass.getConstructor(BlockPos.class);
        boundingBoxEmptyConstructor = boundingBoxClass.getConstructor();
        boundingBoxTransformInverseMethod = boundingBoxClass.getMethod("transformInverse", poseInterfaceClass, boundingBoxClass);
        boundingBoxMinXMethod = boundingBoxClass.getMethod("minX");
        boundingBoxMinYMethod = boundingBoxClass.getMethod("minY");
        boundingBoxMinZMethod = boundingBoxClass.getMethod("minZ");
        boundingBoxMaxXMethod = boundingBoxClass.getMethod("maxX");
        boundingBoxMaxYMethod = boundingBoxClass.getMethod("maxY");
        boundingBoxMaxZMethod = boundingBoxClass.getMethod("maxZ");
        subLevelLogicalPoseMethod = subLevelClass.getMethod("logicalPose");
        subLevelGetLevelMethod = subLevelClass.getMethod("getLevel");
        setDoNotProjectMethod = clipContextExtensionClass.getMethod("sable$setDoNotProject", boolean.class);
        clipBlockField = accessibleClipField("block");
        clipFluidField = accessibleClipField("fluid");
        clipCollisionContextField = accessibleClipField("collisionContext");
    }

    private static Field accessibleClipField(String name) throws NoSuchFieldException {
        Field field = ClipContext.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static int floorBoxValue(Method method, Object box) throws ReflectiveOperationException {
        return (int) Math.floor(((Number) method.invoke(box)).doubleValue());
    }

    private static ClipContext copyContext(ClipContext original, Vec3 from, Vec3 to) {
        try {
            ClipContext.Block block = (ClipContext.Block) clipBlockField.get(original);
            ClipContext.Fluid fluid = (ClipContext.Fluid) clipFluidField.get(original);
            CollisionContext collisionContext = (CollisionContext) clipCollisionContextField.get(original);
            return new ClipContext(from, to, block, fluid, collisionContext);
        } catch (IllegalAccessException | RuntimeException ignored) {
            return new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
        }
    }

    private static void setDoNotProject(ClipContext context, boolean value) {
        try {
            if (clipContextExtensionClass.isInstance(context)) {
                setDoNotProjectMethod.invoke(context, value);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // The local clip still works for normal levels; this only prevents Sable from re-projecting.
        }
    }

    private static Vec3 transformPosition(Object pose, Vec3 position, boolean inverse) throws ReflectiveOperationException {
        Method method = inverse ? transformPositionInverseMethod : transformPositionMethod;
        if (method == null || !method.getDeclaringClass().isAssignableFrom(pose.getClass())) {
            method = pose.getClass().getMethod(inverse ? "transformPositionInverse" : "transformPosition", Vec3.class);
            if (inverse) {
                transformPositionInverseMethod = method;
            } else {
                transformPositionMethod = method;
            }
        }
        Object result = method.invoke(pose, position);
        return result instanceof Vec3 vec ? vec : position;
    }
}
