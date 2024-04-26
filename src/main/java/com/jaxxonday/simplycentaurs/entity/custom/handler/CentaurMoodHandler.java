package com.jaxxonday.simplycentaurs.entity.custom.handler;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class CentaurMoodHandler {
    private final CentaurEntity centaurEntity;

    public CentaurMoodHandler(CentaurEntity centaurEntity) {
        this.centaurEntity = centaurEntity;
    }

    public void setHappyAboutReceivingItem(Player player, Item item, int multiplier) {
        CentaurEntity entity = this.centaurEntity;
        int value = entity.getLikedItemValue(item);
        if(value > 0) {
            entity.setHappy(player);
            entity.adjustWildness(player, -value * multiplier);
        }
    }


    public void setAngryAboutLosingItem(Player player, Item item, int multiplier) {
        CentaurEntity entity = this.centaurEntity;
        int value = entity.getLikedItemValue(item);
        if(value > 0) {
            entity.setAngry(player);
            entity.adjustWildness(player, value * multiplier);
        }
    }
}
