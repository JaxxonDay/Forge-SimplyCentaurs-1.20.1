package com.jaxxonday.simplycentaurs.entity.custom;

import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class ModAbstractSmartCreature extends PathfinderMob implements ContainerListener {
    protected ModAbstractSmartCreature(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
}
