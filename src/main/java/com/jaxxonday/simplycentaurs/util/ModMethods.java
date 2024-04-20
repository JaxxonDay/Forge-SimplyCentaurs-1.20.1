package com.jaxxonday.simplycentaurs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
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
}
