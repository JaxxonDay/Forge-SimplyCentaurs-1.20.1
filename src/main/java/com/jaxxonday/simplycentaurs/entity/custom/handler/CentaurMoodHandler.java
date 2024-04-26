package com.jaxxonday.simplycentaurs.entity.custom.handler;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public class CentaurMoodHandler {
    private final CentaurEntity centaurEntity;

    public CentaurMoodHandler(CentaurEntity centaurEntity) {
        this.centaurEntity = centaurEntity;
    }

    public void setHappyAboutReceivingItem(Player player, Item item, int multiplier) {
        CentaurEntity entity = this.centaurEntity;
        int value = entity.getLikedItemValue(item);
        if(value > 0) {
            setHappy(player);
            entity.adjustWildness(player, -value * multiplier);
        }
    }


    public void setAngryAboutLosingItem(Player player, Item item, int multiplier) {
        CentaurEntity entity = this.centaurEntity;
        int value = entity.getLikedItemValue(item);
        if(value > 0) {
            setAngry(player);
            entity.adjustWildness(player, value * multiplier);
        }
    }


    public void setAngry(@Nullable Player player) {
        CentaurEntity entity = this.centaurEntity;
        entity.setAngry(player);
    }


    public void setInLove(@Nullable Player player) {
        CentaurEntity entity = this.centaurEntity;
        entity.setInLove(player);
    }

    public void setNervous(@Nullable Player player) {
        CentaurEntity entity = this.centaurEntity;
        entity.setNervous(player);
    }

    public void setHappy(@Nullable Player player) {
        CentaurEntity entity = this.centaurEntity;
        entity.setHappy(player);
    }
}
