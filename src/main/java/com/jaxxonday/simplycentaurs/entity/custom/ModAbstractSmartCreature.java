package com.jaxxonday.simplycentaurs.entity.custom;

import com.jaxxonday.simplycentaurs.util.ModMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class ModAbstractSmartCreature extends PathfinderMob implements ContainerListener {
    private static final EntityDataAccessor<Integer> DATA_GENDER = SynchedEntityData.defineId(ModAbstractSmartCreature.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_ANGRY = SynchedEntityData.defineId(ModAbstractSmartCreature.class, EntityDataSerializers.BOOLEAN);

    private int variant = 0;

    protected SimpleContainer inventory;

    private net.minecraftforge.common.util.LazyOptional<?> itemHandler = null;
    protected boolean loadedSaveData = false;
    protected boolean hasBeenAddedBefore = false;

    private final int LOVE_TIME = 200;
    private final int HAPPY_TIME = 40;
    private final int ANGRY_TIME = 200;
    private final int NERVOUS_TIME = 200;

    private int inLove;
    private int inHappy;
    private int inAngry;
    private int inNervous;

    @Nullable
    private UUID loveCause;
    @Nullable
    private UUID happyCause;
    @Nullable
    private UUID angryCause;
    @Nullable
    private UUID nervousCause;


    protected ModAbstractSmartCreature(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.createInventory();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_GENDER, 0);
        this.entityData.define(DATA_IS_ANGRY, false);
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

    public SimpleContainer getInventory() {
        return this.inventory;
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
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isAlive()) {
            if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
                this.heal(1.0F);
            }
        }

        eatIfPossible();
        doLoveParticlesAndTimer();
        doHappyParticlesAndTimer();
        doAngryParticlesAndTimer();
        doNervousParticlesAndTimer();
    }


    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            this.inLove = 0;
            //TODO: other consequences perhaps
        }
        return super.hurt(pSource, pAmount);
    }


    @Override
    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
        return pLevel.getBlockState(pPos.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : pLevel.getPathfindingCostFromLightLevels(pPos);
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


    protected void eatIfPossible() {
        // Only does this step on occasion
        if(this.random.nextInt(200) != 0) {
            return;
        }

        //System.out.println("Attempted to Eat!!!");

        int index = ModMethods.getFoodInInventoryIndex(this.inventory);
        if(index > -1 && this.getHealth() < this.getMaxHealth()) {
            ItemStack itemStack = this.inventory.getItem(index);
            //System.out.println("Found food!");
            float saturationValue = ModMethods.getSaturationValue(itemStack) * 10f;
            float healAmount = 0.0f;
            if(saturationValue >= 2.0f) {
                healAmount = saturationValue / 2.0f;
                this.heal(healAmount);
                this.inventory.removeItem(index, 1);
                if(this.random.nextBoolean()) {
                    this.playSound(itemStack.getEatingSound(), 0.5f, 1.0f);
                } else {
                    this.playSound(SoundEvents.PLAYER_BURP, 0.5f, 1.0f);
                }

            }
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

    public boolean canFallInLove() {
        return this.inLove <= 0;
    }

    public boolean canGetHappy() {
        return this.inHappy <= 0;
    }

    public boolean canGetAngry() {
        return this.inAngry <= 0;
    }

    public boolean canGetNervous() {
        return this.inNervous <= 0;
    }


    public void setInLove(@Nullable Player pPlayer) {
        this.inLove = LOVE_TIME;
        if (pPlayer != null) {
            this.loveCause = pPlayer.getUUID();
        }

        this.level().broadcastEntityEvent(this, (byte)18);
    }

    public void setHappy(@Nullable Player pPlayer) {
        this.inHappy = HAPPY_TIME;
        if (pPlayer != null) {
            this.happyCause = pPlayer.getUUID();
        }

        this.level().broadcastEntityEvent(this, (byte)14);
    }

    public void setAngry(@Nullable Player pPlayer) {
        this.inAngry = ANGRY_TIME;
        if (pPlayer != null) {
            this.angryCause = pPlayer.getUUID();
            this.entityData.set(DATA_IS_ANGRY, true);
        }

        this.level().broadcastEntityEvent(this, (byte)13);
    }

    public void setNervous(@Nullable Player pPlayer) {
        this.inNervous = NERVOUS_TIME;
        if (pPlayer != null) {
            this.nervousCause = pPlayer.getUUID();
        }

        this.level().broadcastEntityEvent(this, (byte)42);
    }


    @Nullable
    public ServerPlayer getLoveCause() {
        if (this.loveCause == null) {
            return null;
        } else {
            Player player = this.level().getPlayerByUUID(this.loveCause);
            return player instanceof ServerPlayer ? (ServerPlayer)player : null;
        }
    }

    @Nullable
    public ServerPlayer getHappyCause() {
        if (this.happyCause == null) {
            return null;
        } else {
            Player player = this.level().getPlayerByUUID(this.happyCause);
            return player instanceof ServerPlayer ? (ServerPlayer)player : null;
        }
    }

    @Nullable
    public ServerPlayer getAngryCause() {
        if (this.angryCause == null) {
            return null;
        } else {
            Player player = this.level().getPlayerByUUID(this.angryCause);
            return player instanceof ServerPlayer ? (ServerPlayer)player : null;
        }
    }

    @Nullable
    public ServerPlayer getNervousCause() {
        if (this.nervousCause == null) {
            return null;
        } else {
            Player player = this.level().getPlayerByUUID(this.nervousCause);
            return player instanceof ServerPlayer ? (ServerPlayer)player : null;
        }
    }

    public boolean isInLove() {
        return this.inLove > 0;
    }

    public boolean isHappy() {
        return this.inHappy > 0;
    }

    public boolean isAngry() {
        return this.entityData.get(DATA_IS_ANGRY);
        //return this.inAngry > 0;
    }

    public boolean isNervous() {
        return this.inNervous > 0;
    }

    public void resetLove() {
        this.inLove = 0;
    }

    public void resetHappy() {
        this.inHappy = 0;
    }

    public void resetAngry() {
        if(!this.level().isClientSide()) {
            this.entityData.set(DATA_IS_ANGRY, false);
            this.inAngry = 0;
        }
    }

    public void resetNervous() {
        this.inNervous = 0;
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 18) {
            this.addParticlesAroundSelf(ParticleTypes.HEART);
        } else if (pId == 13) {
            this.addParticlesAroundSelf(ParticleTypes.ANGRY_VILLAGER);
        } else if (pId == 14) {
            this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
        } else if (pId == 42) {
            this.addParticlesAroundSelf(ParticleTypes.SPLASH);
        } else {
            super.handleEntityEvent(pId);
        }
    }


    protected void doLoveParticlesAndTimer() {
        if (this.inLove > 0) {
            --this.inLove;
            if (this.inLove % 10 == 0) {
                doSingleParticleEffect(ParticleTypes.HEART, new Vec3(0.0d, 0.5d, 0.0d));
            }
        }
    }

    protected void doHappyParticlesAndTimer() {
        if (this.inHappy > 0) {
            --this.inHappy;
            if (this.inHappy % 10 == 0) {
                doSingleParticleEffect(ParticleTypes.HAPPY_VILLAGER, new Vec3(0.0d, 0.5d, 0.0d));
            }
        }
    }

    protected void doAngryParticlesAndTimer() {
        if (this.inAngry > 0) {
            --this.inAngry;
            if (this.inAngry % 10 == 0) {
                doSingleParticleEffect(ParticleTypes.ANGRY_VILLAGER, new Vec3(0.0d, 0.5d, 0.0d));
            }
        } else {
            this.resetAngry();
        }
    }

    protected void doNervousParticlesAndTimer() {
        if (this.inNervous > 0) {
            --this.inNervous;
            if (this.inNervous % 10 == 0) {
                doSingleParticleEffect(ParticleTypes.SPLASH, new Vec3(0.0d, 0.5d, 0.0d));
            }
        }
    }

    protected void addParticlesAroundSelf(ParticleOptions pParticleOption) {
        for(int i = 0; i < 7; ++i) {
            doSingleParticleEffect(pParticleOption, new Vec3(0.0d, 1.0d, 0.0d));
        }
    }

    protected void doSingleParticleEffect(ParticleOptions pParticleOption, Vec3 randomOffset) {
        double d0 = this.random.nextGaussian() * 0.02D;
        double d1 = this.random.nextGaussian() * 0.02D;
        double d2 = this.random.nextGaussian() * 0.02D;
        this.level().addParticle(pParticleOption, this.getRandomX(1.0D) + randomOffset.x, this.getRandomY() + randomOffset.y, this.getRandomZ(1.0D) + randomOffset.z, d0, d1, d2);
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
