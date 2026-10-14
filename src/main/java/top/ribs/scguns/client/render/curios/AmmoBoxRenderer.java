package top.ribs.scguns.client.render.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import net.minecraft.world.item.ItemDisplayContext;


public class AmmoBoxRenderer implements ICurioRenderer {

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
        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            ICurioRenderer.translateIfSneaking(matrixStack, entity);
            humanoidModel.body.translateAndRotate(matrixStack);
        }

        // Ammo boxes are 3D 12x10x12 packages: one on each side at belt height, set behind the arm
        // (the body is 2px deep each way, +z is the back) so the arm swings in front of it rather than
        // through it, with the box face pointing outwards.
        boolean left = slotContext.index() == 0;
        float scale = 0.28F;
        double halfBox = 0.75D * scale / 2.0D;
        double side = 0.30D;
        double back = 0.125D + halfBox;
        matrixStack.translate(left ? -side : side, 0.68D, back);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        matrixStack.mulPose(Axis.YP.rotationDegrees(left ? -90.0F : 90.0F));
        matrixStack.scale(scale, scale, scale);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(stack, entity.level(), entity, 0);

        itemRenderer.render(stack, ItemDisplayContext.NONE, false, matrixStack, bufferSource, light, OverlayTexture.NO_OVERLAY, bakedModel);

        matrixStack.popPose();
    }
}
