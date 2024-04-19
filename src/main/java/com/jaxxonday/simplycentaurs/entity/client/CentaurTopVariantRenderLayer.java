package com.jaxxonday.simplycentaurs.entity.client;

import com.jaxxonday.simplycentaurs.CentaurMod;
import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CentaurTopVariantRenderLayer extends RenderLayer<CentaurEntity, CentaurModel<CentaurEntity>> {
    private static final ResourceLocation FEMALE_VARIANT_0_TEXTURE = new ResourceLocation(CentaurMod.MOD_ID, "textures/entity/variant/centaur_texture_fem_top0.png");
    private static final ResourceLocation MALE_VARIANT_0_TEXTURE = new ResourceLocation(CentaurMod.MOD_ID, "textures/entity/variant/centaur_texture_male_top0.png");

    public CentaurTopVariantRenderLayer(RenderLayerParent<CentaurEntity, CentaurModel<CentaurEntity>> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CentaurEntity pCentaurEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        VertexConsumer vertexConsumer = null;

        if(pCentaurEntity.getGender() == 1) {
            vertexConsumer = pBuffer.getBuffer(RenderType.entityTranslucent(FEMALE_VARIANT_0_TEXTURE));
        } else if(pCentaurEntity.getGender() == 2) {
            vertexConsumer = pBuffer.getBuffer(RenderType.entityTranslucent(MALE_VARIANT_0_TEXTURE));
        }

        if(vertexConsumer == null) {
            return;
        }

        float alpha = 1.0f;
        //if(pCentaurEntity.getFirstPassenger() instanceof Player) {
        //    alpha = 0.3f;
        //}

        getParentModel().renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pCentaurEntity, 0.0F), 1.0F, 1.0F, 1.0F, alpha);
    }
}
