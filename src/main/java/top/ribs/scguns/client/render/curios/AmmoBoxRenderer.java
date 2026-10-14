package top.ribs.scguns.client.render.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.Map;

/**
 * Ammo boxes worn in the curios belt slots show as a leather belt with a pouch on each hip: belt slot 0 is the
 * right hip, slot 1 the left. Each pouch takes its box's tape colour, darker while the box is empty. The strap is
 * drawn by the first filled slot only, so it appears once.
 */
public class AmmoBoxRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/ammo_belt.png");
    private static final float EMPTY_SHADE = 0.55F;

    private static ModelPart strap;
    private static ModelPart rightPouch;
    private static ModelPart leftPouch;
    private static Map<Item, Integer> colors;

    private static void init() {
        if (strap != null) {
            return;
        }
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Body space: 8x12x4, origin at the top centre, +y down, -z front, -x the player's right.
        // Inflated past leggings (0.5) so it sits on top of armour.
        root.addOrReplaceChild("strap", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, 9.5F, -2.0F, 8, 2, 4, new CubeDeformation(0.6F))
                .texOffs(0, 8).addBox(-1.5F, 9.0F, -3.0F, 3, 3, 1, new CubeDeformation(0.1F)), PartPose.ZERO);
        root.addOrReplaceChild("right_pouch", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4.6F, 10.5F, -3.6F, 3, 4, 2), PartPose.ZERO);
        root.addOrReplaceChild("left_pouch", CubeListBuilder.create()
                .texOffs(0, 16).mirror().addBox(1.6F, 10.5F, -3.6F, 3, 4, 2), PartPose.ZERO);
        ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
        strap = baked.getChild("strap");
        rightPouch = baked.getChild("right_pouch");
        leftPouch = baked.getChild("left_pouch");

        // Mid tone of each box's tape (textures/item/package/<box>.png).
        colors = Map.of(
                ModItems.PISTOL_AMMO_BOX.get(), 0xD6744B,
                ModItems.RIFLE_AMMO_BOX.get(), 0xF0B22C,
                ModItems.SHOTGUN_AMMO_BOX.get(), 0xA0BA40,
                ModItems.MAGNUM_AMMO_BOX.get(), 0xD63038,
                ModItems.ENERGY_AMMO_BOX.get(), 0xF06A7C,
                ModItems.ROCKET_AMMO_BOX.get(), 0x3EAA7C,
                ModItems.SPECIAL_AMMO_BOX.get(), 0xA874C4,
                ModItems.EMPTY_CASING_POUCH.get(), 0x68462E,
                ModItems.CREATIVE_AMMO_BOX.get(), 0xB73EAD);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                          SlotContext slotContext,
                                                                          PoseStack matrixStack,
                                                                          RenderLayerParent<T, M> renderLayerParent,
                                                                          MultiBufferSource bufferSource,
                                                                          int light, float limbSwing,
                                                                          float limbSwingAmount,
                                                                          float partialTicks,
                                                                          float ageInTicks,
                                                                          float netHeadYaw,
                                                                          float headPitch) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }
        init();
        LivingEntity entity = slotContext.entity();
        int index = slotContext.index();

        matrixStack.pushPose();
        ICurioRenderer.translateIfSneaking(matrixStack, entity);
        humanoidModel.body.translateAndRotate(matrixStack);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        if (isFirstFilledSlot(entity, slotContext.identifier(), index)) {
            strap.render(matrixStack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
        ModelPart pouch = index == 0 ? rightPouch : leftPouch;
        pouch.render(matrixStack, buffer, light, OverlayTexture.NO_OVERLAY, pouchColor(stack));

        matrixStack.popPose();
    }

    private static boolean isFirstFilledSlot(LivingEntity entity, String slot, int index) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(inventory -> inventory.getStacksHandler(slot))
                .map(handler -> {
                    for (int i = 0; i < index && i < handler.getSlots(); i++) {
                        if (handler.getStacks().getStackInSlot(i).getItem() instanceof AmmoBoxItem) {
                            return false;
                        }
                    }
                    return true;
                })
                .orElse(true);
    }

    private static int pouchColor(ItemStack stack) {
        int rgb = colors.getOrDefault(stack.getItem(), 0xB0B0B0);
        boolean empty = !(stack.getItem() instanceof CreativeAmmoBoxItem) && AmmoBoxItem.getUsedUnits(stack) <= 0;
        float shade = empty ? EMPTY_SHADE : 1.0F;
        return FastColor.ARGB32.color(255,
                (int) (FastColor.ARGB32.red(rgb) * shade),
                (int) (FastColor.ARGB32.green(rgb) * shade),
                (int) (FastColor.ARGB32.blue(rgb) * shade));
    }
}
