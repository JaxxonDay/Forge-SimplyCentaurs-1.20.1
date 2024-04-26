package com.jaxxonday.simplycentaurs.entity.custom;

import com.jaxxonday.simplycentaurs.entity.ai.*;
import com.jaxxonday.simplycentaurs.entity.custom.handler.CentaurAdditionalAttributeHandler;
import com.jaxxonday.simplycentaurs.entity.custom.handler.CentaurInteractionHandler;
import com.jaxxonday.simplycentaurs.entity.custom.handler.CentaurMoodHandler;
import com.jaxxonday.simplycentaurs.entity.custom.handler.CentaurSoundHandler;
import com.jaxxonday.simplycentaurs.util.ModMethods;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CentaurEntity extends ModAbstractSmartCreature implements Saddleable {
    public enum Armor {
        NONE, LEATHER, IRON, GOLDEN, DIAMOND, NETHERITE;

        public static Armor byOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                return NONE;  // Default
            }
            return values()[ordinal];
        }
    }

    public enum InventorySlot {
        HAND, ARMOR
    }

    public static final List<Class<? extends LivingEntity>> HOSTILE_TOWARDS = List.of(
            Zombie.class,
            Skeleton.class,
            Phantom.class,
            PatrollingMonster.class,
            Silverfish.class,
            Spider.class,
            Blaze.class,
            Hoglin.class
    );

    private static final EntityDataAccessor<Integer> DATA_ARMOR = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_SADDLED = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_EXTERNAL_JUMP = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_AIMING = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_RETREATING = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<String> DATA_ATTACK_TARGET_UUID = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.STRING);

    public static final UUID NO_TARGET_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");


    public Map<Item, Integer> likedItems = new HashMap<>();

    private UUID friendUUID = NO_TARGET_UUID;

    private final CentaurInteractionHandler interactionHandler;
    private final CentaurAdditionalAttributeHandler attributeHandler;
    private final CentaurMoodHandler moodHandler;
    private final CentaurSoundHandler soundHandler;




    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;

    public final AnimationState jumpAnimationState = new AnimationState();
    public int jumpAnimationTimeout = 0;

    private boolean activeJumpAnimation = false;


    public double heightBoost = 0.0d;
    private float positionBoost = 0.7f;
    private int wildness = 500;
    private int maxWildness = 500;
    public long lastTimeRidden = -7700;

    public int currentTimeRidden = 0;
    public final int RIDDEN_ANGRY_INTERVAL = 7700;

    public boolean persoM = false;
    public boolean persoT = false;
    public boolean persoS = false;


    public CentaurEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setMaxUpStep(1.2f);
        this.attributeHandler = new CentaurAdditionalAttributeHandler(this);
        this.moodHandler = new CentaurMoodHandler(this);
        this.interactionHandler = new CentaurInteractionHandler(this, this.moodHandler);
        this.soundHandler = new CentaurSoundHandler(this);
        initializeLikedItems();
        initializeTemptGoals();
    }


    private void initializeLikedItems() {
        likedItems.put(Items.APPLE, 10);
        likedItems.put(Items.GOLDEN_APPLE, 30);
        likedItems.put(Items.ENCHANTED_GOLDEN_APPLE, 350);
        likedItems.put(Items.DIAMOND, 300);
        likedItems.put(Items.EMERALD, 250);
        likedItems.put(Items.AMETHYST_SHARD, 300);
        likedItems.put(Items.DIAMOND_SWORD, 150);
    }

    public boolean isLikedItem(Item item) {
        return likedItems.containsKey(item);
    }


    private void initializeTemptGoals() {
        this.goalSelector.addGoal(3, new TemptGoal(this, 0.9d, createTemptIngredient(), false));
    }

    private Ingredient createTemptIngredient() {
        // Collects all items from the likedItems map into an Ingredient
        return Ingredient.of(this.likedItems.keySet().stream()
                .map(ItemStack::new)
                .toArray(ItemStack[]::new));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ARMOR, 0);
        this.entityData.define(DATA_IS_SADDLED, false);
        this.entityData.define(DATA_EXTERNAL_JUMP, false);
        this.entityData.define(DATA_IS_AIMING, false);
        this.entityData.define(DATA_IS_ATTACKING, false);
        this.entityData.define(DATA_IS_RETREATING, false);
        this.entityData.define(DATA_ATTACK_TARGET_UUID, CentaurEntity.NO_TARGET_UUID.toString());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.1D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(2, new CentaurRetreatFromHurtByGoal(this, 1.5d));
        this.goalSelector.addGoal(3, new CentaurAvoidAngryCauseGoal(this, 1.5d));
        this.goalSelector.addGoal(3, new CentaurRetreatGoal(this, 1.5d));
        this.goalSelector.addGoal(4, new CentaurAttackGoal(this, 1.5d, true, 6, 6, 700));
        //this.goalSelector.addGoal(1, new CentaurHurtByTargetGoal(this));
        for (Class<? extends LivingEntity> targetClass : HOSTILE_TOWARDS) {
            this.targetSelector.addGoal(5, new CentaurNearestAttackableTargetGoal(this, targetClass, true));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ModAbstractSmartCreature.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30d)
                .add(Attributes.FOLLOW_RANGE, 50d)
                .add(Attributes.MOVEMENT_SPEED, 0.25d)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0d)
                .add(Attributes.ATTACK_DAMAGE, 2.0d)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0d);
    }


    // MAIN METHODS //


    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.persoM = this.random.nextBoolean();
        this.persoT = this.random.nextBoolean();
        if(!this.persoM && !this.persoT) {
            this.persoS = this.random.nextBoolean();
        }
    }

    @Override
    public void tick() {
        super.tick();

        setHurtByTarget();

        if(this.level().isClientSide()) {
            setupAnimationStates();
        } else {
            if(isSprinting() && !isBeingRidden()) {
                setSprinting(false);
            } else if(getIsExternalJump() && !isBeingRidden()) {
                setExternalJump(false);
            }

            if(this.isBeingRidden()) {
                ++this.currentTimeRidden;
                this.lastTimeRidden = this.level().getGameTime();
            } else if(this.currentTimeRidden > 0) {
                this.currentTimeRidden = 0;
            }

            doRiddenLongTermReaction();
        }
    }


    private void doRiddenLongTermReaction() {
        float modifier = 2.0f;
        if(this.isSprinting()) {
            modifier = 0.75f;
        }
        if(this.currentTimeRidden > 720 * modifier) {
            if(this.getRandom().nextBoolean()) {
                this.moodHandler.setNervous(null);
            }

        }

        if(this.currentTimeRidden > 960 * modifier) {
            if(this.persoT && this.getRandom().nextBoolean()) {
                this.moodHandler.setAngry(null);
            }
        }

        if(this.currentTimeRidden > 1920 * modifier) {
            if(this.persoT && this.getRandom().nextInt(10) == 0) {
                //TODO: bucking
                this.activeJumpAnimation = true;
                this.ejectPassengers();
            }
            if(!this.persoM && this.isSprinting() && this.getRandom().nextInt(50) == 0) {
                this.setSprinting(false);
            } else if(this.persoM && this.getRandom().nextInt(32) == 0) {
                this.moodHandler.setInLove(null);
            }

//                if(this.persoS && this.getRandom().nextInt(25) == 0) {
//                    this.setExternalJump(true);
//                }
        }

        if(this.currentTimeRidden > 3840 * modifier) {
            if(this.random.nextInt(20) == 0) {
                if(this.isSprinting()) {
                    this.setSprinting(false);
                }
            } else if(this.random.nextInt(70) == 0) {
                this.activeJumpAnimation = true;
                this.ejectPassengers();
                if(this.persoM) {
                    this.moodHandler.setInLove(null);
                }
            }

            MobEffectInstance slowEffect = new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                    120,
                    2,
                    false,
                    false,
                    false);

            this.addEffect(slowEffect, this);
        }
    }


    private void setupAnimationStates() {
        updateIdleAnimation();
        updateAttackAnimation();
        updateJumpAnimation();
    }


    private void updateIdleAnimation() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }


    private void updateAttackAnimation() {
        // For attacking animation
        if(this.getIsAttacking() && this.attackAnimationTimeout <= 0) {
            this.attackAnimationTimeout = 80; // Length in ticks of animation + time spacing
            this.attackAnimationState.start(this.tickCount);
        } else {
            --this.attackAnimationTimeout;
        }

        if(!this.getIsAttacking()) {
            this.attackAnimationState.stop();
        }
    }

    protected void updateJumpAnimation() {
        if(this.activeJumpAnimation && this.jumpAnimationTimeout <= 0 && !this.jumpAnimationState.isStarted()) {
            this.jumpAnimationTimeout = 14;
            this.jumpAnimationState.start(this.tickCount);
        } else if (this.jumpAnimationTimeout > 0 && this.jumpAnimationState.isStarted()){
            --this.jumpAnimationTimeout;
        } else {
            this.jumpAnimationState.stop();
            this.activeJumpAnimation = false;
        }
    }


    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if(this.getPose() == Pose.STANDING && !this.jumpAnimationState.isStarted()) {
            f = Math.min(pPartialTick * 6f, 1f);
        } else {
            f = 0f;
        }
        this.walkAnimation.update(f, 0.2f);
    }


    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {

        ItemStack itemStack = pPlayer.getItemInHand(pHand);

        if(this.interactionHandler.handleSaddlePlacement(pPlayer, pHand, itemStack)) {
            this.getNavigation().stop();
            return InteractionResult.SUCCESS;
        }

        if(this.interactionHandler.handleArmorPlacement(pPlayer, pHand, itemStack)) {
            this.getNavigation().stop();
            return InteractionResult.SUCCESS;
        }

        if(this.interactionHandler.handleItemPlacement(pPlayer, pHand, itemStack)) {
            this.getNavigation().stop();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }


    public void adjustWildness(Player player, int amount) {
        if(this.getFriendUUID().toString().equals(player.getUUID().toString())) {
            System.out.println("Won't adjust wildness, since we're friends!");
            if(amount > 0) {
                this.setAngry(player);
            } else if(amount < 0) {
                this.setInLove(player);
            }
            return;
        }
        this.wildness += amount;

        if(this.wildness <= 0) {
            this.wildness = 0;
            if(this.getFriendUUID() == CentaurEntity.NO_TARGET_UUID) {
                setFriendUUID(player.getUUID());
                this.setInLove(player);
            }
        }
        System.out.println("WILDNESS is now " + this.wildness);
    }


    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        GroundPathNavigation groundPathNavigation = new GroundPathNavigation(this, pLevel);

        groundPathNavigation.setCanOpenDoors(true);
        groundPathNavigation.setCanWalkOverFences(true);
        return groundPathNavigation;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.8f;
    }


    private void doSwim() {
        boolean isFloating = this.isInWater() &&
                this.getFluidHeight(FluidTags.WATER) > this.getFluidJumpThreshold() - 0.5d ||
                this.isInLava() ||
                this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType) &&
                        height > this.getFluidJumpThreshold());

        if(!isFloating) {
            return;
        }

        if(this.getRandom().nextFloat() < 0.1f) {
            Vec3 currentVelocity = this.getDeltaMovement();
            this.setDeltaMovement(currentVelocity.x, currentVelocity.y + 0.1d, currentVelocity.z);
        }
    }


    public float getBaseMovementSpeed() {
        return (float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED));
    }


    public void doPlayerRide(Player pPlayer) {
        if (!this.level().isClientSide) {
            pPlayer.setYRot(this.getYRot());
            pPlayer.setXRot(this.getXRot());
            pPlayer.startRiding(this);
        }
    }


    @Override
    protected void updateControlFlags() {
        boolean flag = !(this.getControllingPassenger() instanceof Player);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
    }

    @Override
    protected void tickRidden(Player pPlayer, Vec3 pTravelVector) {
        super.tickRidden(pPlayer, pTravelVector);
        if(pPlayer.zza != 0.0d || pPlayer.xxa != 0.0d || this.isSprinting()) {
            ModMethods.setRotLerp(this, pPlayer.getYRot(), (pPlayer.getXRot() * 0.5F) - 10f, 0.2f); //-10f rotates head upward
            this.yRotO = this.yBodyRot = this.getYRot();
            this.yHeadRot = pPlayer.getYHeadRot();
        }

        if (pTravelVector.z <= 0.0D) {
            this.soundHandler.gallopSoundCounter = 0;
        }


        // Swim check
        doSwim();
    }

    @Override
    protected float getRiddenSpeed(Player pPlayer) {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.8f;
    }

    @Override
    protected Vec3 getRiddenInput(Player pPlayer, Vec3 pTravelVector) {
        boolean gapFound = false;
        // Defining forward input
        double forwardInput = 1.05d;
        boolean gapOne = ModMethods.getGapBelow(this, 1.0f, 4);
        boolean gapTwo = ModMethods.getGapBelow(this, 2.0f, 4);
        boolean smallGapOne = ModMethods.getGapBelow(this, 1.0f, 2);
        boolean smallGapFour = ModMethods.getGapBelow(this, 4.0f, 2);
        boolean smallGapFive = ModMethods.getGapBelow(this, 5.0f, 2);

        //if((gapOne || gapTwo) && !getIsExternalJump()) {
        if((gapOne) && !getIsExternalJump() && !this.isSprinting()) {
            forwardInput = 0.0d;
            gapFound = true;
        } else if(smallGapOne && !smallGapFour && !smallGapFive && this.isSprinting()) {
            if(!this.getIsExternalJump()) {
                this.setExternalJump(true);
            }
        } else if(gapOne) {
            forwardInput = 0.0d;
            gapFound = true;
        }
        // Sprinting, we automatically jump up blocks
        if(this.isSprinting() && !this.getIsExternalJump() && ModMethods.getBlocksBlocking(this, 1)) {
            this.setExternalJump(true);
        }

        if(!this.level().isClientSide()) {
            if(pPlayer.isSprinting()) {
                pPlayer.setSprinting(false);
                this.setSprinting(true);
                if(this.persoM && this.getRandom().nextInt(18) == 0) {
                    this.moodHandler.setInLove(pPlayer);
                }
            } else if(pPlayer.zza < 0.0f && this.isSprinting()) {
                this.setSprinting(false);
            }
        }


        // Defining side movement
        double sideInput = pPlayer.xxa * 0.2d;

        // Set to normal inputs if not sprinting
        if(!this.isSprinting()) {
            // Only apply movement if gap not found
            if(!gapFound) {
                forwardInput = pPlayer.zza;
            } else {
                // There's a gap found so we lock movement to being only
                // in reverse
                forwardInput = Math.max(pPlayer.zza, 0.0d);
            }
            sideInput = pPlayer.xxa * 0.5d;;
        }

        // Jumping
        if(this.getIsExternalJump()) {
            this.setExternalJump(false);
            if(this.onGround()) {
                this.soundHandler.playJumpSound();
                this.activeJumpAnimation = true;

                doJumpVelocity(0.6d);
            }
        }
        // Backwards movement
        if(forwardInput < 0.0d) {
            forwardInput *= 0.5d;
        }
        return new Vec3(sideInput, 0.0d, forwardInput);
    }

    @Override
    public boolean canSprint() {
        return true;
    }


    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if(entity instanceof Player) {
            if(this.isSprinting() || ((Player) entity).zza != 0.0d || ((Player) entity).xxa != 0.0d) {
                return ((Player) entity);
            }
        }
        return super.getControllingPassenger();
    }

    private void doJumpVelocity(double upVelocity) {
        Vec3 currentVelocity = this.getDeltaMovement();
        this.setDeltaMovement(new Vec3(currentVelocity.x * 3.0d, upVelocity, currentVelocity.z * 3.0d));
    }

    @Override
    protected void positionRider(Entity pPassenger, MoveFunction pCallback) {
        super.positionRider(pPassenger, pCallback);
        float standAnimO = this.positionBoost; //+ this.ridePositionBoost;
        if(this.activeJumpAnimation && this.jumpAnimationTimeout >= 3) {
            this.positionBoost = Mth.lerp(0.75f, this.positionBoost, 1.2f);
        } else if(this.positionBoost > 0.7f) {
            this.positionBoost = Mth.lerp(0.5f, this.positionBoost, 0.7f);
        }
        float f = Mth.sin(this.yBodyRot * ((float)Math.PI / 180F));
        float f1 = Mth.cos(this.yBodyRot * ((float)Math.PI / 180F));
        float f2 = 0.7F * standAnimO;
        float f3 = 0.15F * standAnimO;
        pCallback.accept(pPassenger, this.getX() + (double)(f2 * f), this.getY() + this.getPassengersRidingOffset() + pPassenger.getMyRidingOffset() + (double)f3, this.getZ() - (double)(f2 * f1));
        if (pPassenger instanceof LivingEntity) {
            ((LivingEntity)pPassenger).yBodyRot = this.yBodyRot;
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        double height = super.getPassengersRidingOffset() * 0.565D;
        //height += this.heightBoost;
        return height + this.heightBoost;
    }

    public boolean isBeingRidden() {
        return !this.getPassengers().isEmpty();
    }


    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if (pFallDistance > 1.0F) {
            this.soundHandler.playLandSound();
            //this.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
        }
        return super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }

    // Saddle Data
    @Override
    public void containerChanged(Container pContainer) {

    }

    @Override
    public boolean isSaddleable() {
        return true; //TODO: friendship mechanic
    }

    @Override
    public void equipSaddle(@Nullable SoundSource pSource) {
        this.entityData.set(DATA_IS_SADDLED, true);
        this.playSound(getSaddleSoundEvent(), 0.5f, 1f);
    }

    public void unequipSaddle() {
        this.entityData.set(DATA_IS_SADDLED, false);
    }

    @Override
    public boolean isSaddled() {
        return this.entityData.get(DATA_IS_SADDLED);
    }


    public boolean isArmored() {
        return this.getEquippedArmor() != Armor.NONE;
    }


    public void equipItem(Player pPlayer, InteractionHand pHand, ItemStack itemStack, @Nullable SoundEvent soundEvent) {
        ItemStack existingItem = this.inventory.getItem(InventorySlot.HAND.ordinal());

        if(soundEvent != null) {
            this.playSound(soundEvent, 0.5F, 1.0F);
        }

        if(existingItem != ItemStack.EMPTY) {
            this.dropItem(existingItem.copy());
        }

        if(itemStack.getCount() > 1) {
            this.inventory.setItem(InventorySlot.HAND.ordinal(), new ItemStack(itemStack.getItem(), 1));
        } else {
            this.inventory.setItem(InventorySlot.HAND.ordinal(), itemStack.copy());
        }

        this.attributeHandler.updateAttackAttribute();


        this.usePlayerItem(pPlayer, pHand, itemStack, true);
    }

    public void placeItemInInventory(Player pPlayer, InteractionHand pHand, ItemStack itemStack, @Nullable SoundEvent soundEvent) {

        boolean isFoodItem = ModMethods.isFoodItem(itemStack);
        float saturationValue = isFoodItem ? ModMethods.getSaturationValue(itemStack) * 10f : 0f;

        if(isFoodItem && saturationValue >= 2.0f) {
            ItemStack singleItemStack = new ItemStack(itemStack.getItem());
            if(this.inventory.canAddItem(singleItemStack)) {
                this.playSound(SoundEvents.ITEM_PICKUP, 0.5f, 1.0f);
                this.inventory.addItem(singleItemStack);
                this.usePlayerItem(pPlayer, pHand, itemStack, true);
            }
        } else {
            //TEMP
            if(ModMethods.isFoodItem(itemStack)) {
                float saturationVal = ModMethods.getSaturationValue(itemStack) * 10f;
                System.out.println("Saturation value too low for this item. Saturation is: " + saturationVal);
            }
            ////
            equipItem(pPlayer, pHand, itemStack, soundEvent);
        }
    }


    public void unequipItem(@Nullable SoundEvent soundEvent) {
        if(soundEvent != null) {
            this.playSound(soundEvent, 0.5F, 0.8F);
        }

        ItemStack existingItem = this.inventory.getItem(InventorySlot.HAND.ordinal());
        if(!existingItem.isEmpty()) {
            this.inventory.setItem(InventorySlot.HAND.ordinal(), ItemStack.EMPTY);
        }

        this.attributeHandler.updateAttackAttribute();
    }

    public boolean hasItemInHand() {
        ItemStack existingItem = this.inventory.getItem(InventorySlot.HAND.ordinal());
        return existingItem != ItemStack.EMPTY;
    }


    public ItemStack getHeldItem() {
        ItemStack existingItem = this.inventory.getItem(InventorySlot.HAND.ordinal());
        return existingItem;
    }

    public boolean isCanGallop() {
        return true;
    }


    @Override
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        if(!this.soundHandler.handledStepSound(pPos, pBlock)) {
            super.playStepSound(pPos, pBlock);
        }
    }


    private void setHurtByTarget() {
        if(this.level().isClientSide()) {
            return;
        }

        if(this.getLastHurtByMob() == null) {
            return;
        }

        LivingEntity lastHurtByMob = this.getLastHurtByMob();
        if(this.getFriendUUID().toString().equals(lastHurtByMob.getUUID().toString())) {
            return;
        }

        setAggroTowards(this.getLastHurtByMob());
    }

    public void setAggroTowards(LivingEntity livingEntity) {
        this.setTarget(livingEntity);
        this.setAttackTargetUUID(livingEntity.getUUID());
    }

    public int getLikedItemValue(Item item) {
        if(!isLikedItem(item)) {
            System.out.println("Item " + item + " wasn't liked");
            return 0;
        }
        System.out.println("Item was liked, value was: " + likedItems.get(item));
        return likedItems.get(item);
    }


    public UUID getFriendUUID() {
        return this.friendUUID;
    }

    public void setFriendUUID(UUID pUuid) {
        if(this.level().isClientSide()) {
            return;
        }
        this.friendUUID = pUuid;
    }

    public UUID getAttackTargetUUID() {
        if(this.level().isClientSide()) {
            return NO_TARGET_UUID;
        }
        return UUID.fromString(this.entityData.get(DATA_ATTACK_TARGET_UUID));
    }

    public void setAttackTargetUUID(UUID pUuid) {
        if(this.level().isClientSide()) {
            return;
        }
        this.entityData.set(DATA_ATTACK_TARGET_UUID, pUuid.toString());
    }


    public Armor getEquippedArmor() {
        return Armor.byOrdinal(this.entityData.get(DATA_ARMOR));
    }

    public boolean hasArmor() {
        return Armor.byOrdinal(this.entityData.get(DATA_ARMOR)) != Armor.NONE;
    }

    public void setEquippedArmor(Armor pEquippedArmor) {
        this.entityData.set(DATA_ARMOR, pEquippedArmor.ordinal());
    }


    public boolean getIsExternalJump() {
        return this.entityData.get(DATA_EXTERNAL_JUMP);
    }

    public void setExternalJump(boolean pExternalJump) {
        this.entityData.set(DATA_EXTERNAL_JUMP, pExternalJump);
    }


    public boolean getIsAiming() {
        return this.entityData.get(DATA_IS_AIMING);
    }

    public void setIsAiming(boolean pIsAiming) {
        this.entityData.set(DATA_IS_AIMING, pIsAiming);
    }

    public boolean getIsAttacking() {
        return this.entityData.get(DATA_IS_ATTACKING);
    }

    public void setIsAttacking(boolean pIsAttacking) {
        this.entityData.set(DATA_IS_ATTACKING, pIsAttacking);
    }


    public boolean getIsRetreating() {
        return this.entityData.get(DATA_IS_RETREATING);
    }

    public void setIsRetreating(boolean pIsRetreating) {
        this.entityData.set(DATA_IS_RETREATING, pIsRetreating);
    }
}
