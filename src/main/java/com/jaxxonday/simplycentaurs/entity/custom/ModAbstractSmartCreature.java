package com.jaxxonday.simplycentaurs.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ModAbstractSmartCreature extends PathfinderMob implements ContainerListener {
    private static final EntityDataAccessor<Integer> DATA_GENDER = SynchedEntityData.defineId(ModAbstractSmartCreature.class, EntityDataSerializers.INT);
    private int variant = 0;

    protected SimpleContainer inventory;

    private net.minecraftforge.common.util.LazyOptional<?> itemHandler = null;
    protected boolean loadedSaveData = false;
    protected boolean hasBeenAddedBefore = false;

    protected ModAbstractSmartCreature(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.createInventory();
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


    protected int getInventorySize() {
        return 2;
    }


    protected void createInventory() {
        SimpleContainer simplecontainer = this.inventory;
        this.inventory = new SimpleContainer(this.getInventorySize());
        if (simplecontainer != null) {
            simplecontainer.removeListener(this);
            int i = Math.min(simplecontainer.getContainerSize(), this.inventory.getContainerSize());

            for(int j = 0; j < i; ++j) {
                ItemStack itemstack = simplecontainer.getItem(j);
                if (!itemstack.isEmpty()) {
                    this.inventory.setItem(j, itemstack.copy());
                }
            }
        }

        this.inventory.addListener(this);
        //this.updateContainerEquipment();
        this.itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this.inventory));
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

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }


    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (itemHandler != null) {
            net.minecraftforge.common.util.LazyOptional<?> oldHandler = itemHandler;
            itemHandler = null;
            oldHandler.invalidate();
        }
    }

    protected void usePlayerItem(Player pPlayer, InteractionHand pHand, ItemStack pStack, boolean consumeInCreative) {
        if (!pPlayer.getAbilities().instabuild || consumeInCreative) {
            pStack.shrink(1);
        }
    }

    protected void dropItem(ItemStack itemStack) {
        this.spawnAtLocation(itemStack, 0.0f);
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
