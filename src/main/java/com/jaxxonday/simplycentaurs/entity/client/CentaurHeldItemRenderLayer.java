package com.jaxxonday.simplycentaurs.entity.client;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
        addItemRender(pCentaurEntity, pPoseStack, pBuffer, pPackedLight);
    }

    private void addItemRender(CentaurEntity centaurEntity, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        // Start of pose
        poseStack.pushPose();

        boolean isWeapon = true;

        // Rotation of item to be aligned with body
        //float yRadians = (float) Math.toRadians(-centaurEntity.yBodyRot);
//        float yRadians = (float) Math.toRadians(180);
//        Quaternionf baseRotation = new Quaternionf();
//        baseRotation.rotateY(yRadians);
//        poseStack.mulPose(baseRotation);


        // Get model parts
        ModelPart root = this.getParentModel().centaur;
        ModelPart body = this.getParentModel().body;
        ModelPart frontBody = this.getParentModel().frontBody;
        ModelPart waist = this.getParentModel().waist;
        ModelPart chest = this.getParentModel().chest;
        ModelPart rArm = this.getParentModel().rArm;
        ModelPart rHandWeapon = this.getParentModel().rHandWeapon;

        if(!isWeapon) {
            ////ITEM////
            /////////////////////////////////////////////////////////////
            // Item Positioning (don't touch because it's working!) /////
            poseStack.translate(0.0, 1.0, 0.0);
            poseStack.translate(0.0, 1.1, 0.0);

            poseStack = this.getParentModel().applyPoseStackTransformations(poseStack, false);

            poseStack.translate(0.0, -1.1, 0.0);
            /////////////////////////////////////////////////////////////
        } else {
            ////WEAPON////
            /////////////////////////////////////////////////////////////
            // Weapon Item Positioning (don't touch because it's working!) /////
            //poseStack.translate(0.0, 0.72, 0.0);
            //poseStack.translate(0.0, 1.38, 0.0);

            poseStack = this.getParentModel().applyPoseStackTransformations(poseStack, false);


            //poseStack.translate(0.0, -1.38, 0.0);

            // Final translation for rHandWeapon positioning
            poseStack = this.getParentModel().applyRHandWeaponTransformation(poseStack);
            //rHandWeapon.translateAndRotate(poseStack);
            ////////////////////////////////////////////////////////////////////
        }

        float xRadians = (float) Math.toRadians(180);
        Quaternionf baseRotation = new Quaternionf();
        baseRotation.rotateX(xRadians);
        poseStack.mulPose(baseRotation);




        // Render the item
        //ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        //itemRenderer.renderStatic(Items.DIAMOND_SWORD.getDefaultInstance(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, pPackedLight, OverlayTexture.NO_OVERLAY, poseStack, pBuffer, centaurEntity.level(), centaurEntity.getId());
        //itemRenderer.render(Items.DIAMOND_SWORD.getDefaultInstance(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, pPackedLight, OverlayTexture.NO_OVERLAY, poseStack, pBuffer, centaurEntity.level(), centaurEntity.getId());
        ItemStack itemstack = Items.DIAMOND_SWORD.getDefaultInstance().copy();
        this.itemInHandRenderer.renderItem(centaurEntity, itemstack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, pBuffer, pPackedLight);

        // End of pose
        poseStack.popPose();
    }
}
