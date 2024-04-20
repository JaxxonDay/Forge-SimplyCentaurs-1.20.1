package com.jaxxonday.simplycentaurs.event;

import com.jaxxonday.simplycentaurs.CentaurMod;
import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = CentaurMod.MOD_ID, value = Dist.CLIENT)
public class ModEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if(player == null) {
                return;
            }

            if(player.isPassenger() && mc.options.keyJump.isDown()) {
                Entity mount = player.getVehicle();
                if(mount instanceof CentaurEntity) {
                    ((CentaurEntity) mount).setExternalJump(true);
                    System.out.println("Jump Key Pressed");
                }
            }
        }
    }
}
