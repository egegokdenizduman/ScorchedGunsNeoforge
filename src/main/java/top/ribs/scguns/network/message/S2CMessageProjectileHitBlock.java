package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import top.ribs.scguns.client.network.ClientPlayHandler;

/**
 * Author: MrCrayfish
 */
public class S2CMessageProjectileHitBlock
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageProjectileHitBlock> STREAM_CODEC = StreamCodec.of(
            S2CMessageProjectileHitBlock::encode,
            S2CMessageProjectileHitBlock::decode
    );

    private double x;
    private double y;
    private double z;
    private BlockPos pos;
    private Direction face;
    /** Shot effect style (MuzzleFlashStyle ordinal) used to colour the impact sparks. */
    private int style;

    public S2CMessageProjectileHitBlock() {}

    public S2CMessageProjectileHitBlock(double x, double y, double z, BlockPos pos, Direction face)
    {
        this(x, y, z, pos, face, 0);
    }

    public S2CMessageProjectileHitBlock(double x, double y, double z, BlockPos pos, Direction face, int style)
    {
        this.style = style;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pos = pos;
        this.face = face;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageProjectileHitBlock message)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeBlockPos(message.pos);
        buffer.writeEnum(message.face);
        buffer.writeVarInt(message.style);
    }
    private static S2CMessageProjectileHitBlock decode(RegistryFriendlyByteBuf buffer)
    {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        BlockPos pos = buffer.readBlockPos();
        Direction face = buffer.readEnum(Direction.class);
        int style = buffer.readVarInt();
        return new S2CMessageProjectileHitBlock(x, y, z, pos, face, style);
    }
    public static void handle(S2CMessageProjectileHitBlock message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleProjectileHitBlock(message));
        context.setHandled(true);
    }

    public int getStyle()
    {
        return this.style;
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public Direction getFace()
    {
        return this.face;
    }
}