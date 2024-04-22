package com.jaxxonday.simplycentaurs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;

import static java.lang.Math.round;

public class ModMethods {
    public static boolean getGapBelow(Entity entity, float distanceScale) {
        Vec3 position = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Vec3 lookAngle = entity.getLookAngle().multiply(distanceScale, distanceScale, distanceScale);
        double offsetY = 0.1D;

        int posX = (int)round(lookAngle.x + position.x);
        int posY = (int)round(position.y + offsetY);
        int posZ = (int)round(lookAngle.z + position.z);

        BlockPos collidePos = new BlockPos(posX, posY, posZ);

        boolean belowIsAir = entity.level().getBlockState(collidePos.below()).isAir();
        boolean below2IsAir = entity.level().getBlockState(collidePos.below(2)).isAir();
        boolean below3IsAir = entity.level().getBlockState(collidePos.below(3)).isAir();
        boolean below4IsAir = entity.level().getBlockState(collidePos.below(4)).isAir();

        if(belowIsAir && below2IsAir && below3IsAir && below4IsAir) {
            return true;
        }

        return false;
    }

    public static boolean getJumpableGapBelow(Entity entity) {
        boolean oneBlockGap = getGapBelow(entity, 1.0f);
        boolean fourBlockGap = getGapBelow(entity, 4.0f);

        if(!oneBlockGap || !fourBlockGap) {
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

}
