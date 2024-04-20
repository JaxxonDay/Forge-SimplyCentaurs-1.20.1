package com.jaxxonday.simplycentaurs.entity.client;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.jaxxonday.simplycentaurs.util.ModMethods;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;

public class CentaurHeldItemRenderLayer extends RenderLayer<CentaurEntity, CentaurModel<CentaurEntity>> {
    private final ItemInHandRenderer itemInHandRenderer;
    public CentaurHeldItemRenderLayer(RenderLayerParent<CentaurEntity, CentaurModel<CentaurEntity>> pRenderer, ItemInHandRenderer pItemInHandRenderer) {
        super(pRenderer);
        this.itemInHandRenderer = pItemInHandRenderer;
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CentaurEntity pCentaurEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if(pCentaurEntity.hasItemInHand()) {
            addItemRender(pCentaurEntity, pPoseStack, pBuffer, pPackedLight);
        }
    }

    private void addItemRender(CentaurEntity centaurEntity, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        // Start of pose
        poseStack.pushPose();

        ItemStack itemstack = centaurEntity.getHeldItem();
        boolean isHandheldRender = ModMethods.isHandheldRender(itemstack);
        boolean bowItem = itemstack.getItem() instanceof BowItem;

        ////ITEM////
//        if(isHandheldRender && !bowItem) {
//            poseStack.translate(0.0, 0.0, -0.1);
//        } else if(bowItem) {
//            poseStack.translate(0.0, 0.03, -0.1);
//        }
//
//        poseStack.translate(0.0, 0.07, 0.0);


        poseStack = this.getParentModel().applyPoseStackTransformations(poseStack, true);

        // Final translation for rHandWeapon positioning
        //poseStack = this.getParentModel().applyRHandWeaponTransformation(poseStack);


        float xRadians = (float) Math.toRadians(180);
        float zRadians = (float) Math.toRadians(180);
        Quaternionf baseRotation = new Quaternionf();
        if(isHandheldRender && !bowItem) {
            // Rotates the item upwards slightly
            baseRotation.rotateX((float) Math.toRadians(-10));
            baseRotation.rotateZ((float) Math.toRadians(-10));
        }
        //baseRotation.rotateX(xRadians);
        baseRotation.rotateZ(zRadians);
        poseStack.mulPose(baseRotation);

        // Applied after transformations, coordinates are different

        if(isHandheldRender && !bowItem) {
            poseStack.translate(0.0, 0.1, -0.1);
        } else if(centaurEntity.getIsAiming() && bowItem) {
            poseStack.translate(0.0, 0.2, 0.1);
        } else if(bowItem) {
            poseStack.translate(0.0, 0.1, 0.0);
        }




        // Render the item
        //ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        //itemRenderer.renderStatic(Items.DIAMOND_SWORD.getDefaultInstance(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, pPackedLight, OverlayTexture.NO_OVERLAY, poseStack, pBuffer, centaurEntity.level(), centaurEntity.getId());
        //itemRenderer.render(Items.DIAMOND_SWORD.getDefaultInstance(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, pPackedLight, OverlayTexture.NO_OVERLAY, poseStack, pBuffer, centaurEntity.level(), centaurEntity.getId());

        this.itemInHandRenderer.renderItem(centaurEntity, itemstack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, pBuffer, pPackedLight);

        // End of pose
        poseStack.popPose();
    }
}
