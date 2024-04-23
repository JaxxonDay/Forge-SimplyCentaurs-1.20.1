package com.jaxxonday.simplycentaurs.entity.ai;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.jaxxonday.simplycentaurs.util.ModMethods;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.item.ItemStack;

public class CentaurNearestAttackableTargetGoal extends NearestAttackableTargetGoal {
    private final CentaurEntity centaurEntity;
    public CentaurNearestAttackableTargetGoal(Mob pMob, Class pTargetType, boolean pMustSee) {
        super(pMob, pTargetType, pMustSee);
        this.centaurEntity = ((CentaurEntity) pMob);
    }

    @Override
    public boolean canUse() {
        ItemStack itemHeld = this.centaurEntity.getHeldItem();
        if(!ModMethods.canCauseDamage(this.centaurEntity.getInventory()) && !ModMethods.isBowWeapon(itemHeld)) {
            return false;
        }

        if(this.centaurEntity.getAttackTargetUUID() != CentaurEntity.NO_TARGET_UUID) {
            return false;
        }

        return super.canUse();
    }
}
