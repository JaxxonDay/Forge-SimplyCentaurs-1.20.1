package com.jaxxonday.simplycentaurs.entity.ai;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

import java.util.UUID;

public class CentaurHurtByTargetGoal extends HurtByTargetGoal {

    private final CentaurEntity centaurEntity;

    public CentaurHurtByTargetGoal(PathfinderMob pMob, Class<?>... pToIgnoreDamage) {
        super(pMob, pToIgnoreDamage);
        this.centaurEntity = ((CentaurEntity) pMob);
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.mob.getLastHurtByMob();
        if(livingEntity == null) {
            return false;
        }

        UUID friendUUID = this.centaurEntity.getFriendUUID();
        if(livingEntity.getUUID().equals(friendUUID)) {
            System.out.println("Found friend, not attacking");
            return false;
        }

        boolean result = super.canUse();

        if(result) {
            this.centaurEntity.setAttackTargetUUID(livingEntity.getUUID());
        }

        return result;
    }
}
