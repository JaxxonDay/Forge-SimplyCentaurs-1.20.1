package com.jaxxonday.simplycentaurs.entity.ai;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class CentaurRetreatGoal extends Goal {

    private final CentaurEntity centaurEntity;

    private final double speedModifier;
    private double posX;
    private double posY;
    private double posZ;

    private int effectCounter = 0;


    public CentaurRetreatGoal(CentaurEntity pCentaurEntity, double pSpeedModifier) {
        this.centaurEntity = pCentaurEntity;
        this.speedModifier = pSpeedModifier;
    }


    @Override
    public void tick() {
        super.tick();
        if(this.effectCounter < 20) {
            ++this.effectCounter;
        } else {
            this.effectCounter = 0;
            this.centaurEntity.setNervous(null);
        }
    }


    @Override
    public void start() {
        this.centaurEntity.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
    }

    @Override
    public void stop() {
        super.stop();
        this.centaurEntity.setIsRetreating(false);
    }

    @Override
    public boolean canUse() {
//        if(this.centaurEntity.level().isClientSide()) {
//            return false;
//        }

        // If we don't have armor and health is less than 20 hearts, we retreat
        if(this.centaurEntity.getHealth() < 15.0d && !this.centaurEntity.hasArmor()) {
            this.centaurEntity.setIsRetreating(true);
            return setAvoidanceTarget();
        }
        // If we do have armor and health is less than 10 hearts, we retreat
        else if(this.centaurEntity.getHealth() < 10.0d) {
            this.centaurEntity.setIsRetreating(true);
            return setAvoidanceTarget();
        }

        this.centaurEntity.setIsRetreating(false);
        return false;
    }


    private boolean setAvoidanceTarget() {
        LivingEntity entityToAvoid = findHostileEntityNearby(this.centaurEntity.level(), getEntityBlockPos(this.centaurEntity), 7d);
        if(entityToAvoid == null) {
            return false;
        }
        Vec3 vec3 = LandRandomPos.getPosAway(this.centaurEntity, 15, 5, entityToAvoid.position());
        if(vec3 == null) {
            return false;
        }
        this.posX = vec3.x;
        this.posY = vec3.y;
        this.posZ = vec3.z;
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public LivingEntity findHostileEntityNearby(Level world, BlockPos center, double radius) {
        // Create a bounding box around the center with the specified radius
        AABB area = new AABB(center).inflate(radius);

        // Get all entities within the bounding box that match the HOSTILE_TOWARDS list
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> CentaurEntity.HOSTILE_TOWARDS.contains(e.getClass()));

        // Return the first matched entity, if any
        return nearbyEntities.isEmpty() ? null : nearbyEntities.get(0);
    }

    public BlockPos getEntityBlockPos(LivingEntity entity) {
        return new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
    }
}
