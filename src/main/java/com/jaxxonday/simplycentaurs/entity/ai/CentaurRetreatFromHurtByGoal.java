package com.jaxxonday.simplycentaurs.entity.ai;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.jaxxonday.simplycentaurs.util.ModMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CentaurRetreatFromHurtByGoal extends Goal {

    private final CentaurEntity centaurEntity;

    private final double speedModifier;
    private double posX;
    private double posY;
    private double posZ;

    private int effectCounter = 0;

    public CentaurRetreatFromHurtByGoal(CentaurEntity centaurEntity, double pSpeedModifier) {
        this.centaurEntity = centaurEntity;
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
        this.centaurEntity.getNavigation().stop();
        this.centaurEntity.setIsRetreating(false);
    }

    @Override
    public boolean canUse() {
        // If we don't have armor and health is less than 20 hearts, we retreat
        if(this.centaurEntity.getHealth() < this.centaurEntity.getMaxHealth() && !this.centaurEntity.hasArmor()) {
            this.centaurEntity.setIsRetreating(true);
            return setAvoidanceTarget();
        }
        // If we do have armor and health is less than 10 hearts, we retreat
        else if(this.centaurEntity.getHealth() < this.centaurEntity.getMaxHealth() / 2.0f) {
            this.centaurEntity.setIsRetreating(true);
            return setAvoidanceTarget();
        }

        this.centaurEntity.setIsRetreating(false);
        return false;
    }

    private boolean setAvoidanceTarget() {
        LivingEntity entityToAvoid = findAttackedByEntityNearby(this.centaurEntity.level(), ModMethods.getEntityBlockPos(this.centaurEntity), 12d);
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

    public static LivingEntity findAttackedByEntityNearby(Level world, BlockPos center, double radius) {
        // Create a bounding box around the center with the specified radius
        AABB area = new AABB(center).inflate(radius);

        // Get all entities within the bounding box
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, area, e -> {
            // Check if the entity has a 'lastHurtByMob' and if that mob is in the HOSTILE_TOWARDS list
            return e.getLastHurtByMob() != null;
        });

        // Return the first matched entity, if any
        return nearbyEntities.isEmpty() ? null : nearbyEntities.get(0);
    }


    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
