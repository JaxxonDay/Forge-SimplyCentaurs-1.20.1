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

public class CentaurSaddleRenderLayer extends RenderLayer<CentaurEntity, CentaurModel<CentaurEntity>> {
    private static final ResourceLocation CENTAUR_SADDLE_TEXTURE = new ResourceLocation(CentaurMod.MOD_ID, "textures/entity/centaur_texture_saddle.png");

    public CentaurSaddleRenderLayer(RenderLayerParent<CentaurEntity, CentaurModel<CentaurEntity>> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CentaurEntity pCentaurEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        VertexConsumer vertexConsumer = null;
        if(!pCentaurEntity.isSaddled()) {
            return;
        }

        vertexConsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(CENTAUR_SADDLE_TEXTURE));

        getParentModel().renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pCentaurEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
