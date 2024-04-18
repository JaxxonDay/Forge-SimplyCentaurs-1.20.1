package com.jaxxonday.simplycentaurs.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public abstract class ModAbstractSmartCreature extends PathfinderMob implements ContainerListener {
    private static final EntityDataAccessor<Integer> DATA_GENDER = SynchedEntityData.defineId(ModAbstractSmartCreature.class, EntityDataSerializers.INT);
    private int variant = 0;
    protected boolean loadedSaveData = false;
    protected boolean hasBeenAddedBefore = false;
    protected ModAbstractSmartCreature(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_GENDER, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("HasBeenAddedBefore", this.getPersistentData().getBoolean("HasBeenAddedBefore"));
        pCompound.putInt("Gender", this.getGender());
        pCompound.putInt("Variant", this.getVariant());
    }


    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.getPersistentData().putBoolean("HasBeenAddedBefore", pCompound.getBoolean("HasBeenAddedBefore"));

        this.hasBeenAddedBefore = pCompound.getBoolean("HasBeenAddedBefore");
        if(this.hasBeenAddedBefore) {
            setGender(pCompound.getInt("Gender"));
            setVariant(pCompound.getInt("Variant"));
            //System.out.println("Setting Gender from Saved Gender");
        }

        this.loadedSaveData = true;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.getPersistentData().getBoolean("HasBeenAddedBefore")) {
            this.getPersistentData().putBoolean("HasBeenAddedBefore", true);
            this.setGender(this.random.nextInt(2) + 1);
            this.setVariant(this.random.nextInt(5));
        }
    }

    public void setGender(int pGender) {
        this.entityData.set(DATA_GENDER, pGender);
    }

    public int getGender() {
        return this.entityData.get(DATA_GENDER);
    }


    public void setVariant(int pVariant) {
        this.variant = pVariant;
    }

    public int getVariant() {
        return this.variant;
    }
}
