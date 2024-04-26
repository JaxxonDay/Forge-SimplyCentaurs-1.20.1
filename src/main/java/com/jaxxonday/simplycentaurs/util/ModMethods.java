package com.jaxxonday.simplycentaurs.util;

import com.google.common.collect.Multimap;
import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;

import static java.lang.Math.round;

public class ModMethods {
    public static boolean getGapBelow(Entity entity, float distanceScale, int verticalDistance) {
        Vec3 position = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Vec3 lookAngle = entity.getLookAngle().multiply(distanceScale, distanceScale, distanceScale);
        double offsetY = 0.1D;

        int posX = (int)round(lookAngle.x + position.x);
        int posY = (int)round(position.y + offsetY);
        int posZ = (int)round(lookAngle.z + position.z);

        BlockPos collidePos = new BlockPos(posX, posY, posZ);

        return areAllBlocksBelowAir(entity, collidePos, verticalDistance);

//        boolean belowIsAir = entity.level().getBlockState(collidePos.below()).isAir();
//        boolean below2IsAir = entity.level().getBlockState(collidePos.below(2)).isAir();
//        boolean below3IsAir = entity.level().getBlockState(collidePos.below(3)).isAir();
//        boolean below4IsAir = entity.level().getBlockState(collidePos.below(4)).isAir();
//
//        if(belowIsAir && below2IsAir && below3IsAir && below4IsAir) {
//            return true;
//        }
//
//        return false;
    }



    private static boolean areAllBlocksBelowAir(Entity entity, BlockPos collidePos, int verticalDistance) {
        for (int i = 1; i <= verticalDistance; i++) {
            if (!entity.level().getBlockState(collidePos.below(i)).isAir()) {
                return false;  // Found a block that is not air
            }
        }
        return true;  // All checked blocks are air
    }

    public static boolean getJumpableGapBelow(Entity entity) {
        boolean oneBlockGap = getGapBelow(entity, 1.0f, 4);
        boolean fourBlockGap = getGapBelow(entity, 4.0f, 4);

        if(!oneBlockGap || !fourBlockGap) {
            return true;
        }

        return false;
    }

    public static boolean getBlocksBlocking(Entity entity, float distanceCheck) {
        Vec3 position = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Vec3 lookAngle = entity.getLookAngle().multiply(distanceCheck, distanceCheck, distanceCheck);
        double offsetY = 0.1D;

        int posX = (int)round(lookAngle.x + position.x);
        int posY = (int)round(position.y + offsetY);
        int posZ = (int)round(lookAngle.z + position.z);

        BlockPos collidePos = new BlockPos(posX, posY, posZ);
        boolean blockOne = !entity.level().getBlockState(collidePos.above(1)).isAir();
        boolean blockTwo = !entity.level().getBlockState(collidePos.above(2)).isAir();

        if(blockOne && blockTwo) {
            return true;
        }

        return false;
    }


    public static boolean isWeapon(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item instanceof SwordItem ||
                item instanceof AxeItem ||
                item instanceof TridentItem; // Add more as needed
    }

    public static boolean isBowWeapon(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item instanceof BowItem;
    }

    public static boolean isHandheldRender(ItemStack itemStack) {
        if(isWeapon(itemStack)) {
            return true;
        }
        Item item = itemStack.getItem();
        return item instanceof PickaxeItem ||
                item instanceof HoeItem ||
                item instanceof ShovelItem;
    }


    public static int getFoodInInventoryIndex(Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {  // Loop through all inventory slots
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty() && itemStack.getItem().isEdible()) {
                return i;  // Return the index of the slot containing food
            }
        }
        return -1;  // Return -1 if no food is found
    }

    public static float getSaturationValue(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item.isEdible()) {  // Check if the item is food
            FoodProperties foodProperties = item.getFoodProperties();
            if (foodProperties != null) {
                return foodProperties.getSaturationModifier();  // Return the saturation modifier
            }
        }
        return -1;  // Return -1 if the item is not food
    }

    public static boolean isFoodItem(ItemStack itemStack) {
        return itemStack.getItem().isEdible();
    }

    public static int getEmptyInventoryIndex(Container inventory) {
        // Returns slot index if there's nothing there, otherwise returns -1
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if(itemStack.isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static int getEmptyOrExactItemInventoryIndex(ItemStack itemStack, Container inventory) {
        // Returns slot index if either there's nothing there, or if it's the exact same item
        // Otherwise returns -1
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack invItemStack = inventory.getItem(i);
            if(invItemStack.getCount() < invItemStack.getMaxStackSize() && invItemStack.is(itemStack.getItem())) {
                return i;
            }
        }

        return getEmptyInventoryIndex(inventory);
    }


    public static BlockPos getEntityBlockPos(LivingEntity entity) {
        return new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
    }

    public static boolean canCauseDamage(Container inventory) {
        ItemStack itemInMainHand = inventory.getItem(CentaurEntity.InventorySlot.HAND.ordinal());
        if(itemInMainHand.isEmpty()) {
            return false;
        }

        Multimap<Attribute, AttributeModifier> modifiers = itemInMainHand.getAttributeModifiers(EquipmentSlot.MAINHAND);

        return modifiers.containsKey(Attributes.ATTACK_DAMAGE);
    }



    public static void setRotLerp(LivingEntity entity, float pYRot, float pXRot, float delta) {
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();

        yRot = Mth.rotLerp(delta, yRot, pYRot);
        xRot = Mth.rotLerp(delta, xRot, pXRot);
        entity.setYRot(yRot % 360.0F);
        entity.setXRot(xRot % 360.0F);
    }

}
