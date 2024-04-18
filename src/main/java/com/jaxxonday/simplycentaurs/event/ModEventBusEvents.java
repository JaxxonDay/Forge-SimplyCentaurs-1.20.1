package com.jaxxonday.simplycentaurs.event;


import com.jaxxonday.simplycentaurs.CentaurMod;
import com.jaxxonday.simplycentaurs.entity.ModEntities;
import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CentaurMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CENTAUR.get(), CentaurEntity.createAttributes().build());
    }
}
