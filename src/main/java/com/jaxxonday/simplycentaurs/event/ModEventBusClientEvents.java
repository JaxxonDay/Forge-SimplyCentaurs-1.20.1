package com.jaxxonday.simplycentaurs.event;


import com.jaxxonday.simplycentaurs.CentaurMod;
import com.jaxxonday.simplycentaurs.entity.client.CentaurModel;
import com.jaxxonday.simplycentaurs.entity.client.ModModelLayers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CentaurMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.CENTAUR_LAYER, CentaurModel::createBodyLayer);
    }
}
