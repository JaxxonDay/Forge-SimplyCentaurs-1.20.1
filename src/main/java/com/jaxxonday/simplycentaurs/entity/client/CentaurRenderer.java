package com.jaxxonday.simplycentaurs.entity.client;

import com.jaxxonday.simplycentaurs.CentaurMod;
import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CentaurRenderer extends MobRenderer<CentaurEntity, CentaurModel<CentaurEntity>> {
    public CentaurRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new CentaurModel<>(pContext.bakeLayer(ModModelLayers.CENTAUR_LAYER)), 0.8f);
    }

    @Override
    public ResourceLocation getTextureLocation(CentaurEntity pEntity) {
        return new ResourceLocation(CentaurMod.MOD_ID, "textures/entity/centaur_texture_base.png");
    }

    @Override
    public void render(CentaurEntity pEntity,
                       float pEntityYaw,
                       float pPartialTicks,
                       PoseStack pMatrixStack,
                       MultiBufferSource pBuffer,
                       int pPackedLight) {


        if(pEntity.isBaby()) {
            pMatrixStack.scale(0.5f, 0.5f, 0.5f);
        }


        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
