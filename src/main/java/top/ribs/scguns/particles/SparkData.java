package top.ribs.scguns.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import top.ribs.scguns.init.ModParticleTypes;

/**
 * A hot, full-bright spark tinted with an RGB colour (muzzle and bullet impact sparks).
 */
public record SparkData(int color) implements ParticleOptions {
    public static final MapCodec<SparkData> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("color").forGetter(SparkData::color)
    ).apply(builder, SparkData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SparkData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SparkData::color,
            SparkData::new
    );

    @Override
    public ParticleType<?> getType() {
        return ModParticleTypes.SPARK.get();
    }
}
