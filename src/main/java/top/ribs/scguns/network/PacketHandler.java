package top.ribs.scguns.network;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import net.minecraft.network.protocol.PacketFlow;
import top.ribs.scguns.Reference;
import top.ribs.scguns.network.message.*;

public class PacketHandler
{
    private static FrameworkNetwork playChannel;

    public static void init()
    {
        playChannel = FrameworkAPI.createNetworkBuilder(Reference.id("play"), 1)
                .registerPlayMessage("c2s_offhand_melee", C2SMessageOffhandMelee.class, C2SMessageOffhandMelee.STREAM_CODEC, C2SMessageOffhandMelee::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("c2s_aim", C2SMessageAim.class, C2SMessageAim.STREAM_CODEC, C2SMessageAim::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("c2s_melee_attack", C2SMessageMeleeAttack.class, C2SMessageMeleeAttack.STREAM_CODEC, C2SMessageMeleeAttack::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("s2c_muzzle_flash", S2CMessageMuzzleFlash.class, S2CMessageMuzzleFlash.STREAM_CODEC, S2CMessageMuzzleFlash::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("c2s_reload", C2SMessageReload.class, C2SMessageReload.STREAM_CODEC, C2SMessageReload::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("c2s_gun_loaded", C2SMessageGunLoaded.class, C2SMessageGunLoaded.STREAM_CODEC, C2SMessageGunLoaded::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("c2s_eject_casing", C2SMessageEjectCasing.class, C2SMessageEjectCasing.STREAM_CODEC, C2SMessageEjectCasing::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("s2c_update_ammo", S2CMessageUpdateAmmo.class, S2CMessageUpdateAmmo.STREAM_CODEC, S2CMessageUpdateAmmo::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("c2s_shoot", C2SMessageShoot.class, C2SMessageShoot.STREAM_CODEC, C2SMessageShoot::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("c2s_charge_sync", C2SMessageChargeSync.class, C2SMessageChargeSync.STREAM_CODEC, C2SMessageChargeSync::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("c2s_pre_fire_sound", C2SMessagePreFireSound.class, C2SMessagePreFireSound.STREAM_CODEC, C2SMessagePreFireSound::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("c2s_unload", C2SMessageUnload.class, C2SMessageUnload.STREAM_CODEC, C2SMessageUnload::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("c2s_swap_ammo", C2SMessageSwapAmmo.class, C2SMessageSwapAmmo.STREAM_CODEC, C2SMessageSwapAmmo::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("s2c_stun_grenade", S2CMessageStunGrenade.class, S2CMessageStunGrenade.STREAM_CODEC, S2CMessageStunGrenade::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_bullet_trail", S2CMessageBulletTrail.class, S2CMessageBulletTrail.STREAM_CODEC, S2CMessageBulletTrail::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("c2s_attachments", C2SMessageAttachments.class, C2SMessageAttachments.STREAM_CODEC, C2SMessageAttachments::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("s2c_update_guns", S2CMessageUpdateGuns.class, S2CMessageUpdateGuns.STREAM_CODEC, S2CMessageUpdateGuns::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_blood", S2CMessageBlood.class, S2CMessageBlood.STREAM_CODEC, S2CMessageBlood::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_reload", S2CMessageReload.class, S2CMessageReload.STREAM_CODEC, S2CMessageReload::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_beam_update", S2CMessageBeamUpdate.class, S2CMessageBeamUpdate.STREAM_CODEC, S2CMessageBeamUpdate::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_beam_penetration", S2CMessageBeamPenetration.class, S2CMessageBeamPenetration.STREAM_CODEC, S2CMessageBeamPenetration::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_stop_beam", S2CMessageStopBeam.class, S2CMessageStopBeam.STREAM_CODEC, S2CMessageStopBeam::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_beam_impact", S2CMessageBeamImpact.class, S2CMessageBeamImpact.STREAM_CODEC, S2CMessageBeamImpact::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("c2s_shooting", C2SMessageShooting.class, C2SMessageShooting.STREAM_CODEC, C2SMessageShooting::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("c2s_stop_beam", C2SMessageStopBeam.class, C2SMessageStopBeam.STREAM_CODEC, C2SMessageStopBeam::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("s2c_gun_sound", S2CMessageGunSound.class, S2CMessageGunSound.STREAM_CODEC, S2CMessageGunSound::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_distant_gun_sound", S2CMessageDistantGunSound.class, S2CMessageDistantGunSound.STREAM_CODEC, S2CMessageDistantGunSound::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_dual_wield_shot_count", S2CMessageDualWieldShotCount.class, S2CMessageDualWieldShotCount.STREAM_CODEC, S2CMessageDualWieldShotCount::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_melee_attack", S2CMessageMeleeAttack.class, S2CMessageMeleeAttack.STREAM_CODEC, S2CMessageMeleeAttack::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_hot_barrel_sync", S2CMessageHotBarrelSync.class, S2CMessageHotBarrelSync.STREAM_CODEC, S2CMessageHotBarrelSync::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_stop_reload", S2CMessageStopReload.class, S2CMessageStopReload.STREAM_CODEC, S2CMessageStopReload::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_projectile_hit_block", S2CMessageProjectileHitBlock.class, S2CMessageProjectileHitBlock.STREAM_CODEC, S2CMessageProjectileHitBlock::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_projectile_hit_entity", S2CMessageProjectileHitEntity.class, S2CMessageProjectileHitEntity.STREAM_CODEC, S2CMessageProjectileHitEntity::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("c2s_left_over_ammo", C2SMessageLeftOverAmmo.class, C2SMessageLeftOverAmmo.STREAM_CODEC, C2SMessageLeftOverAmmo::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage("s2c_remove_projectile", S2CMessageRemoveProjectile.class, S2CMessageRemoveProjectile.STREAM_CODEC, S2CMessageRemoveProjectile::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("s2c_show_totem_animation", S2CShowTotemAnimationMessage.class, S2CShowTotemAnimationMessage.STREAM_CODEC, S2CShowTotemAnimationMessage::handle, PacketFlow.CLIENTBOUND)
                .build();

    }

    public static FrameworkNetwork getPlayChannel()
    {
        return playChannel;
    }


}
