package com.jaxxonday.simplycentaurs.entity.custom.handler;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.jaxxonday.simplycentaurs.item.ModItems;
import com.jaxxonday.simplycentaurs.util.ModMethods;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

public class CentaurInteractionHandler {
    private static final UUID ARMOR_UUID = UUID.randomUUID();
    private final CentaurEntity centaurEntity;
    private final CentaurMoodHandler moodHandler;

    public CentaurInteractionHandler(CentaurEntity centaurEntity, CentaurMoodHandler moodHandler) {
        this.centaurEntity = centaurEntity;
        this.moodHandler = moodHandler;
    }

    public boolean handleItemPlacement(Player pPlayer, InteractionHand pHand, ItemStack itemStack) {
        CentaurEntity entity = this.centaurEntity;

        if(!(pPlayer.getUUID().toString().equals(entity.getFriendUUID().toString())) && !itemStack.isEmpty()) {
            if(!itemStack.isEdible() && !entity.isLikedItem(itemStack.getItem())) {
                return true;
            }
        }

        boolean likedItem = false;
        if(!itemStack.isEmpty()) {
            likedItem = entity.isLikedItem(itemStack.getItem());
        }

        if(handledItemHandRemoval(pPlayer, itemStack)) {
            return true;
        } else if(handledItemEquipWhenEmptyHand(pPlayer, pHand, itemStack)) {
            return true;
        } else if(handledItemSwapEquip(pPlayer, pHand, itemStack)) {
            return true;
        }
        return false;
    }


    private boolean handledItemHandRemoval(Player pPlayer, ItemStack itemStack) {
        CentaurEntity entity = this.centaurEntity;
        CentaurMoodHandler mood = this.moodHandler;
        if(itemStack.isEmpty() && pPlayer.isCrouching() && entity.hasItemInHand()) {
            mood.setAngryAboutLosingItem(pPlayer, entity.getHeldItem().getItem(), entity.getHeldItem().getCount());
            entity.dropItem(entity.getHeldItem().copy());
            entity.unequipItem(null);
            return true;
        }
        return false;
    }


    private boolean handledItemEquipWhenEmptyHand(Player pPlayer, InteractionHand pHand, ItemStack itemStack) {
        CentaurEntity entity = this.centaurEntity;
        CentaurMoodHandler mood = this.moodHandler;
        if(!itemStack.isEmpty() && !entity.hasItemInHand()) {
            mood.setHappyAboutReceivingItem(pPlayer, itemStack.getItem(), 1);
            entity.equipItem(pPlayer, pHand, itemStack, null);
            return true;
        }
        return false;
    }


    private boolean handledItemSwapEquip(Player pPlayer, InteractionHand pHand, ItemStack itemStack) {
        CentaurEntity entity = this.centaurEntity;
        CentaurMoodHandler mood = this.moodHandler;
        if(!itemStack.isEmpty() && entity.hasItemInHand()) {
            if(!ModMethods.isFoodItem(itemStack)) {
                mood.setHappyAboutReceivingItem(pPlayer, itemStack.getItem(), 1);
                entity.dropItem(entity.getHeldItem().copy());
                entity.unequipItem(null);
                entity.equipItem(pPlayer, pHand, itemStack, null);
            } else {
                System.out.println("Tried placing item in inventory");
                mood.setHappyAboutReceivingItem(pPlayer, itemStack.getItem(), 1);
                entity.placeItemInInventory(pPlayer, pHand, itemStack, null);
            }
            return true;
        }
        return false;
    }


    public boolean handleSaddlePlacement(Player pPlayer, InteractionHand pHand, ItemStack itemStack) {
        if(itemStack.getItem() != Items.SADDLE && !itemStack.isEmpty()) {
            return false;
        }

        CentaurEntity entity = this.centaurEntity;

        if(!(pPlayer.getUUID().toString().equals(entity.getFriendUUID().toString())) && !itemStack.isEmpty()) {
            entity.getLookControl().setLookAt(pPlayer);
            ServerPlayer nervousCause = entity.getNervousCause();

            if(nervousCause != null && entity.isNervous()) {
                if(entity.getRandom().nextBoolean()) {
                    entity.setAngry(nervousCause);
                } else {
                    entity.setAggroTowards(nervousCause);
                }
            } else {
                entity.setNervous(pPlayer);
            }
            return true;
        }

        if(itemStack.isEmpty() && pPlayer.isCrouching() && entity.isSaddled()) {
            entity.unequipSaddle();
            entity.dropItem(new ItemStack(Items.SADDLE));
            return true;
        } else if(itemStack.isEmpty() && !pPlayer.isCrouching() && entity.isSaddled()) {
            entity.doPlayerRide(pPlayer);
            return true;
        } else if(itemStack.is(Items.SADDLE) && !entity.isSaddled()) {
            entity.equipSaddle(SoundSource.BLOCKS);
            entity.usePlayerItem(pPlayer, pHand, itemStack, true);
            return true;
        }
        return false;
    }

    public boolean handleArmorPlacement(Player pPlayer, InteractionHand pHand, ItemStack itemStack) {
        if(itemStack.getItem() != ModItems.LEATHER_CENTAUR_ARMOR.get() &&
                itemStack.getItem() != ModItems.IRON_CENTAUR_ARMOR.get() &&
                itemStack.getItem() != ModItems.GOLDEN_CENTAUR_ARMOR.get() &&
                itemStack.getItem() != ModItems.DIAMOND_CENTAUR_ARMOR.get()) {
            return false;
        }

        CentaurEntity entity = this.centaurEntity;

        if(!pPlayer.getUUID().toString().equals(entity.getFriendUUID().toString())) {
            return true;
        }

        if(itemStack.isEmpty() && pPlayer.isCrouching() && entity.isArmored()) {
            CentaurEntity.Armor equippedArmor = entity.getEquippedArmor();
            if(equippedArmor == CentaurEntity.Armor.LEATHER) {
                entity.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.5f, 0.7f);
                entity.dropItem(new ItemStack(ModItems.LEATHER_CENTAUR_ARMOR.get()));
            } else if(equippedArmor == CentaurEntity.Armor.IRON) {
                entity.playSound(SoundEvents.ARMOR_EQUIP_IRON, 0.5f, 0.7f);
                entity.dropItem(new ItemStack(ModItems.IRON_CENTAUR_ARMOR.get()));
            } else if(equippedArmor == CentaurEntity.Armor.GOLDEN) {
                entity.playSound(SoundEvents.ARMOR_EQUIP_GOLD, 0.5f, 0.7f);
                entity.dropItem(new ItemStack(ModItems.GOLDEN_CENTAUR_ARMOR.get()));
            } else if(equippedArmor == CentaurEntity.Armor.DIAMOND) {
                entity.playSound(SoundEvents.ARMOR_EQUIP_DIAMOND, 0.5f, 0.7f);
                entity.dropItem(new ItemStack(ModItems.DIAMOND_CENTAUR_ARMOR.get()));
            }

            entity.setEquippedArmor(CentaurEntity.Armor.NONE);
            return true;
        }

        if(!pPlayer.isCrouching() && !entity.isArmored()) {
            boolean equipped = false;
            if(itemStack.is(ModItems.LEATHER_CENTAUR_ARMOR.get())) {
                entity.setEquippedArmor(CentaurEntity.Armor.LEATHER);
                entity.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.5f, 1f);
                equipped = true;
            } else if(itemStack.is(ModItems.IRON_CENTAUR_ARMOR.get())) {
                entity.setEquippedArmor(CentaurEntity.Armor.IRON);
                entity.playSound(SoundEvents.ARMOR_EQUIP_IRON, 0.5f, 1f);
                equipped = true;
            } else if(itemStack.is(ModItems.GOLDEN_CENTAUR_ARMOR.get())) {
                entity.setEquippedArmor(CentaurEntity.Armor.GOLDEN);
                entity.playSound(SoundEvents.ARMOR_EQUIP_GOLD, 0.5f, 1f);
                equipped = true;
            } else if(itemStack.is(ModItems.DIAMOND_CENTAUR_ARMOR.get())) {
                entity.setEquippedArmor(CentaurEntity.Armor.DIAMOND);
                entity.playSound(SoundEvents.ARMOR_EQUIP_DIAMOND, 0.5f, 1f);
                equipped = true;
            }
            if(equipped) {
                entity.usePlayerItem(pPlayer, pHand, itemStack, true);
                return true;
            }
        }

        return false;
    }
}
