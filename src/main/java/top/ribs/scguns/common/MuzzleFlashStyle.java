package top.ribs.scguns.common;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.Reference;

/**
 * Colour family of a gun's shot effects (muzzle flash, muzzle sparks, impact sparks).
 * Each style has its own palette-swapped flash sheet in textures/effect/muzzle_flash/.
 * Picked from the gun's ammo first, then from its legacy flash texture name.
 */
public enum MuzzleFlashStyle {
    NORMAL("normal", 0xFFC060, true),
    FIRE("fire", 0xFF9A10, true),
    PLASMA("plasma", 0x5CC8FF, false),
    SCULK("sculk", 0x29DFEB, false),
    ELECTRIC("electric", 0xA8B8FF, false),
    LASER("laser", 0xFF5060, false),
    AMETHYST("amethyst", 0xD080F0, false);

    private final String name;
    private final int sparkColor;
    private final boolean smokes;
    private final ResourceLocation texture;

    MuzzleFlashStyle(String name, int sparkColor, boolean smokes) {
        this.name = name;
        this.sparkColor = sparkColor;
        this.smokes = smokes;
        this.texture = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/effect/muzzle_flash/" + name + ".png");
    }

    public String getName() {
        return this.name;
    }

    /** RGB colour the grey spark sprites are tinted with. */
    public int getSparkColor() {
        return this.sparkColor;
    }

    /** Chemical propellant: leaves a puff of smoke. Energy weapons don't. */
    public boolean smokes() {
        return this.smokes;
    }

    /** 96x96 sheet: rows = star / burst / side plume, columns = 3 animation frames (32x32 each). */
    public ResourceLocation getTexture() {
        return this.texture;
    }

    public static MuzzleFlashStyle byOrdinal(int ordinal) {
        MuzzleFlashStyle[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NORMAL;
    }

    public static MuzzleFlashStyle of(@Nullable Gun gun) {
        if (gun == null) {
            return NORMAL;
        }
        Gun.Projectile projectile = gun.getProjectile();
        if (projectile != null && projectile.item != null) {
            String ammo = projectile.item.getPath();
            if (ammo.contains("blaze")) return FIRE;
            if (ammo.contains("sculk")) return SCULK;
            if (ammo.contains("shock")) return ELECTRIC;
        }
        Gun.Display.Flash flash = gun.getDisplay().getFlash();
        String tex = flash != null ? flash.getTextureLocation() : "";
        return switch (tex) {
            case "muzzle_flash_3" -> PLASMA;
            case "muzzle_flash_11" -> SCULK;
            case "muzzle_flash_7" -> ELECTRIC;
            case "muzzle_flash_9", "muzzle_flash_12" -> LASER;
            case "muzzle_flash_8" -> AMETHYST;
            case "muzzle_flash_10" -> FIRE;
            default -> projectile != null && projectile.item != null && projectile.item.getPath().startsWith("energy") ? PLASMA : NORMAL;
        };
    }
}
