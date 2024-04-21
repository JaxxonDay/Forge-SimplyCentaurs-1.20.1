package com.jaxxonday.simplycentaurs.entity.ai;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.C;

public class CentaurAttackGoal extends MeleeAttackGoal {

    private final CentaurEntity centaurEntity;

    private int attackDelay = 20; // Delay from beginning of animation to point in animation where attack begins
    private int ticksUntilNextAttack = 40;

    private int aggroTimeLeft = 0;
    private boolean shouldCountTillNextAttack = false;

    private final int attackSpeed;
    private final int attackTimeBetween;

    private final int aggroTime;

    public CentaurAttackGoal(PathfinderMob pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen, int pAttackSpeed, int pAttackTimeBetween, int pAggroCooldown) {
        super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
        this.centaurEntity = (CentaurEntity) pMob;
        this.attackSpeed = pAttackSpeed;
        this.attackTimeBetween = pAttackTimeBetween;
        this.attackDelay = pAttackSpeed;
        this.ticksUntilNextAttack = pAttackTimeBetween;
        this.aggroTime = pAggroCooldown;
        this.aggroTimeLeft = pAggroCooldown;
    }


    @Override
    protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
        if(isEnemyWithinAttackDistance(pEnemy, pDistToEnemySqr)) {
            // If enemy is within distance, set up attack
            this.shouldCountTillNextAttack = true;

            // If it's the time to start animation, start animation
            if(isTimeToStartAttackAnimation()) {
                this.centaurEntity.setIsAttacking(true);
            }
            // If it's the exact time to attack do attack
            if(isTimeToAttack() && this.centaurEntity.getIsAttacking()) {
                this.mob.getLookControl().setLookAt(pEnemy.getX(), pEnemy.getY(), pEnemy.getZ());
                this.performAttack(pEnemy, pDistToEnemySqr);
            }


        } else {
            // If enemy is outside attack distance
            if(centaurEntity.position().distanceTo(pEnemy.position()) < 2) {
                Vec3 currentVelocity = this.centaurEntity.getDeltaMovement();
                Vec3 lookAngle = this.centaurEntity.getLookAngle().multiply(0.1d, 0.1d, 0.1d);
                Vec3 boost = new Vec3(currentVelocity.x + lookAngle.x, currentVelocity.y + lookAngle.y, currentVelocity.z + lookAngle.z);
                this.centaurEntity.setDeltaMovement(boost);
            } else {
                this.centaurEntity.setIsAttacking(false);
                this.resetAttackCooldown();
                this.shouldCountTillNextAttack = false;
                this.centaurEntity.attackAnimationTimeout = 0;
            }
        }
    }


    private boolean isEnemyWithinAttackDistance(LivingEntity pEnemy, double pDistToEnemySqr) {
        return pDistToEnemySqr <= this.getAttackReachSqr(pEnemy);
    }


    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(attackDelay * 2);
    }

    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected boolean isTimeToStartAttackAnimation() {
        return this.ticksUntilNextAttack <= attackDelay;
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    protected void performAttack(LivingEntity pEnemy, double pDist) {
        this.resetAttackCooldown();
        this.mob.swing(InteractionHand.MAIN_HAND);
        this.mob.doHurtTarget(pEnemy);
    }


    @Override
    public void start() {
        super.start();
        this.attackDelay = this.attackSpeed;
        this.ticksUntilNextAttack = this.attackTimeBetween;
    }

    @Override
    public void tick() {
        super.tick();
        // If we're counting towards next attack, decrement down to a minimum of 0
        if(shouldCountTillNextAttack) {
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        }

        this.aggroTimeLeft = Math.max(this.aggroTimeLeft - 1, 0);

        if(this.aggroTimeLeft <= 0 && this.centaurEntity.getTarget() != null) {
            System.out.println("Attempted to disable target");
            if(!CentaurEntity.HOSTILE_TOWARDS.contains(this.centaurEntity.getTarget().getClass())) {
                this.centaurEntity.setForgivenEntityUUID(this.centaurEntity.getTarget().getUUID());
            }

        }
    }

    @Override
    public boolean canUse() {
        // No target means can't use
        if(this.centaurEntity.getTarget() == null) {
            return false;
        }

        //Forgiven entity not attacked again
        if(this.centaurEntity.getTarget().getUUID() == this.centaurEntity.getForgivenEntityUUID()) {
            return false;
        }

        if(!this.centaurEntity.level().isClientSide()) {
            if(this.centaurEntity.getAvoidTime() > 0) {
                if(this.centaurEntity.getIsAttacking()) {
                    this.centaurEntity.setIsAttacking(false);
                }
                return false;
            }
        }


        boolean result = super.canUse();

        if(result) {
            this.aggroTimeLeft = this.aggroTime;
        }

        return result;
    }

    @Override
    public boolean canContinueToUse() {
        if(this.centaurEntity.getTarget() == null) {
            return false;
        }

        if(this.centaurEntity.getTarget().getUUID() == this.centaurEntity.getForgivenEntityUUID()) {
            return false;
        }

        return super.canContinueToUse();
    }

    @Override
    public void stop() {
        this.centaurEntity.setIsAttacking(false);
        super.stop();
    }
}
