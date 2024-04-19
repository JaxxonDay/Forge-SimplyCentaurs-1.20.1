package com.jaxxonday.simplycentaurs.entity.client;

import com.jaxxonday.simplycentaurs.CentaurMod;
import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CentaurRenderer extends MobRenderer<CentaurEntity, CentaurModel<CentaurEntity>> {
    public CentaurRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new CentaurModel<>(pContext.bakeLayer(ModModelLayers.CENTAUR_LAYER)), 0.8f);
        this.addLayer(new CentaurBottomVariantRenderLayer(this));
        this.addLayer(new CentaurTopVariantRenderLayer(this));
        this.addLayer(new CentaurArmorRenderLayer(this));
        this.addLayer(new CentaurSaddleRenderLayer(this));
        this.addLayer(new CentaurHeldItemRenderLayer(this, pContext.getItemInHandRenderer()));

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

        this.getModel().setGenderVisible(pEntity.getGender());

        if(pEntity.isBaby()) {
            pMatrixStack.scale(0.5f, 0.5f, 0.5f);
        }

        pMatrixStack.scale(0.89f, 0.89f, 0.89f);


        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

}
