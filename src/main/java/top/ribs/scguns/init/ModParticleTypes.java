package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.particles.BulletHoleData;
import top.ribs.scguns.particles.SparkData;
import top.ribs.scguns.particles.TrailData;

/**
 * Author: MrCrayfish
 */
public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, Reference.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<BulletHoleData>> BULLET_HOLE = REGISTER.register("bullet_hole", () -> new ParticleType<>(false) {
        @Override
        public MapCodec<BulletHoleData> codec() {
            return BulletHoleData.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, BulletHoleData> streamCodec() {
            return BulletHoleData.STREAM_CODEC;
        }
    });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD = REGISTER.register("blood", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, ParticleType<TrailData>> TRAIL = REGISTER.register("trail", () -> new ParticleType<>(false) {
        @Override
        public MapCodec<TrailData> codec() {
            return TrailData.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TrailData> streamCodec() {
            return TrailData.STREAM_CODEC;
        }
    });
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> COPPER_CASING_PARTICLE = REGISTER.register("copper_casing", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> IRON_CASING_PARTICLE = REGISTER.register("iron_casing", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DIAMOND_STEEL_CASING_PARTICLE = REGISTER.register("diamond_steel_casing", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHULK_CASING_PARTICLE = REGISTER.register("shulk_casing", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType>BRASS_CASING_PARTICLE = REGISTER.register("brass_casing", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHELL_PARTICLE = REGISTER.register("shell", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BEARPACK_PARTICLE = REGISTER.register("bearpack_shell", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ROCKET_TRAIL = REGISTER.register("rocket_trail", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SONIC_BLAST = REGISTER.register("sonic_blast", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GREEN_FLAME = REGISTER.register("green_flame", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLASMA_RING = REGISTER.register("plasma_ring", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SULFUR_SMOKE = REGISTER.register("sulfur_smoke", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SULFUR_DUST = REGISTER.register("sulfur_dust", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ACID_BUBBLE = REGISTER.register("acid_bubble", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLASMA_EXPLOSION = REGISTER.register("plasma_explosion", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAMROD_IMPACT = REGISTER.register("ramrod_impact", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BEOWULF_IMPACT = REGISTER.register("beowulf_impact", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ROCKET_EXPLOSION = REGISTER.register("rocket_explosion", () -> new SimpleParticleType(true));


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TURRET_MUZZLE_FLASH = REGISTER.register("turret_muzzle_flash", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LASER = REGISTER.register("laser", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMALL_LASER = REGISTER.register("small_laser", () -> new SimpleParticleType(true));

    // Shot effects: all full-bright so they read as light sources (also under shader packs)
    public static final DeferredHolder<ParticleType<?>, ParticleType<SparkData>> SPARK = REGISTER.register("spark", () -> new ParticleType<>(true) {
        @Override
        public MapCodec<SparkData> codec() {
            return SparkData.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, SparkData> streamCodec() {
            return SparkData.STREAM_CODEC;
        }
    });
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BULLET_IMPACT_FLASH = REGISTER.register("bullet_impact_flash", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GUN_FLAME = REGISTER.register("gun_flame", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GUN_SMALL_FLAME = REGISTER.register("gun_small_flame", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GUN_SOUL_FLAME = REGISTER.register("gun_soul_flame", () -> new SimpleParticleType(true));
}





