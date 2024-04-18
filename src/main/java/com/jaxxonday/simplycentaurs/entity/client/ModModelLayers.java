package com.jaxxonday.simplycentaurs.entity.client;

import com.jaxxonday.simplycentaurs.CentaurMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation CENTAUR_LAYER = new ModelLayerLocation(
            new ResourceLocation(CentaurMod.MOD_ID, "centaur_layer"), "main");
}
