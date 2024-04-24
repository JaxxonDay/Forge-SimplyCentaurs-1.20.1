package com.jaxxonday.simplycentaurs.entity.ai;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.jaxxonday.simplycentaurs.util.ModMethods;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CentaurAvoidAngryCauseGoal extends Goal {

    private final CentaurEntity centaurEntity;
    private final double speedModifier;
    private ServerPlayer angryCause;

    private double posX;
    private double posY;
    private double posZ;

    public CentaurAvoidAngryCauseGoal(CentaurEntity pCentaurEntity, double pSpeedModifier) {
        this.centaurEntity = pCentaurEntity;
        this.speedModifier = pSpeedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if(this.centaurEntity.level().isClientSide()) {
            return false;
        }
        ServerPlayer testAngryCause = this.centaurEntity.getAngryCause();
        if(testAngryCause == null || this.centaurEntity.canGetAngry()) {
            return false;
        }
        this.angryCause = testAngryCause;
        return setAvoidanceTarget();
    }

    @Override
    public void start() {
        this.centaurEntity.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
    }

    @Override
    public void stop() {
        super.stop();
        this.centaurEntity.getNavigation().stop();
    }

    private boolean setAvoidanceTarget() {
        LivingEntity entityToAvoid = this.angryCause;
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
}
