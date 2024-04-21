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

public class CentaurArmorRenderLayer extends RenderLayer<CentaurEntity, CentaurModel<CentaurEntity>> {

    private static final ResourceLocation CENTAUR_LEATHER_ARMOR_TEXTURE = new ResourceLocation(CentaurMod.MOD_ID, "textures/entity/armor/centaur_texture_armor_leather.png");
    private static final ResourceLocation CENTAUR_IRON_ARMOR_TEXTURE = new ResourceLocation(CentaurMod.MOD_ID, "textures/entity/armor/centaur_texture_armor_iron.png");
    private static final ResourceLocation CENTAUR_GOLDEN_ARMOR_TEXTURE = new ResourceLocation(CentaurMod.MOD_ID, "textures/entity/armor/centaur_texture_armor_golden.png");
    private static final ResourceLocation CENTAUR_DIAMOND_ARMOR_TEXTURE = new ResourceLocation(CentaurMod.MOD_ID, "textures/entity/armor/centaur_texture_armor_diamond.png");

    public CentaurArmorRenderLayer(RenderLayerParent<CentaurEntity, CentaurModel<CentaurEntity>> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CentaurEntity pCentaurEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        VertexConsumer vertexConsumer = null;

        switch (pCentaurEntity.getEquippedArmor()) {
            case NONE : return;
            case LEATHER :
                vertexConsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(CENTAUR_LEATHER_ARMOR_TEXTURE));
                break;
            case IRON :
                vertexConsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(CENTAUR_IRON_ARMOR_TEXTURE));
                break;
            case GOLDEN :
                vertexConsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(CENTAUR_GOLDEN_ARMOR_TEXTURE));
                break;
            case DIAMOND:
                vertexConsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(CENTAUR_DIAMOND_ARMOR_TEXTURE));
                break;
        }

        if(vertexConsumer == null) {
            return;
        }

        getParentModel().renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pCentaurEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
