package com.jaxxonday.simplycentaurs.entity.custom;

import com.jaxxonday.simplycentaurs.entity.ai.*;
import com.jaxxonday.simplycentaurs.item.ModItems;
import com.jaxxonday.simplycentaurs.util.ModMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
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

    private static final UUID ARMOR_UUID = UUID.randomUUID();

    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.randomUUID();

    public Map<Item, Integer> likedItems = new HashMap<>();



    private UUID friendUUID = NO_TARGET_UUID;
    private UUID avoidEntityUUID = NO_TARGET_UUID;




    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;

    public double heightBoost = 0.0d;

    protected int gallopSoundCounter;
    protected boolean canGallop = true;

    private int avoidTime = 0;

    private int wildness = 500;



    public CentaurEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setMaxUpStep(1.0f);
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

    public boolean isLikedItem(Item item) {
        return likedItems.containsKey(item);
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

    public UUID getAvoidEntityUUID() {
        return this.avoidEntityUUID;
    }

    public void setAvoidEntityUUID(UUID pUuid) {
        this.avoidEntityUUID = pUuid;
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




    public int getAvoidTime() {
        return this.avoidTime;
    }

    public void setAvoidTime(int pAvoidTime) {
        this.avoidTime = pAvoidTime;
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
        }
    }


    private void setupAnimationStates() {
        updateIdleAnimation();
        updateAttackAnimation();
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


    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if(this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6f, 1f);
        } else {
            f = 0f;
        }

        this.walkAnimation.update(f, 0.2f);
    }


    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {

        ItemStack itemStack = pPlayer.getItemInHand(pHand);

        if(handleSaddlePlacement(pPlayer, pHand, itemStack)) {
            this.getNavigation().stop();
            return InteractionResult.SUCCESS;
        }

        if(handleArmorPlacement(pPlayer, pHand, itemStack)) {
            this.getNavigation().stop();
            return InteractionResult.SUCCESS;
        }

        if(handleItemPlacement(pPlayer, pHand, itemStack)) {
            this.getNavigation().stop();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }



    public void updateAttackAttribute() {
        ItemStack heldItem = this.inventory.getItem(InventorySlot.HAND.ordinal()); // Check the designated slot for the item
        if(!ModMethods.canCauseDamage(this.inventory)) {
            removeAttackAttribute();
            return;
        }
        double additionalDamage = getAdditionalDamage(heldItem);
        if (additionalDamage != 0.0d) {
            AttributeModifier modifier = new AttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID, "Weapon modifier", additionalDamage, AttributeModifier.Operation.ADDITION);
            if (this.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_DAMAGE_MODIFIER_UUID) == null) {
                this.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(modifier);
                System.out.println("Added Attack Damage modifier of amount " + additionalDamage);
            }
        } else {
            removeAttackAttribute();
        }
    }

    private void removeAttackAttribute() {
        if (this.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_DAMAGE_MODIFIER_UUID) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
            System.out.println("Removed Attack Damage modifier");
        }
    }

    private double getAdditionalDamage(ItemStack itemStack) {
        if (itemStack != null && !itemStack.isEmpty()) { // Check if itemStack is not null and not empty
            Collection<AttributeModifier> modifiers = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
            if (modifiers != null && !modifiers.isEmpty()) { // Ensure modifiers are not null or empty
                return modifiers.stream()
                        .filter(modifier -> modifier.getOperation() == AttributeModifier.Operation.ADDITION)
                        .mapToDouble(AttributeModifier::getAmount)
                        .sum();  // Sum all the addition modifiers
            }
        }
        return 0;  // Return 0 if no valid modifiers are found or itemStack is null/empty
    }


    private boolean handleItemPlacement(Player pPlayer, InteractionHand pHand, ItemStack itemStack) {
        if(!(pPlayer.getUUID().toString().equals(this.getFriendUUID().toString())) && !itemStack.isEmpty()) {
            if(!itemStack.isEdible() && !isLikedItem(itemStack.getItem())) {
                return true;
            }
        }


        if(itemStack.isEmpty() && pPlayer.isCrouching() && hasItemInHand()) {
            dropItem(getHeldItem().copy());
            unequipItem(null);

            return true;
        } else if(!itemStack.isEmpty() && !hasItemInHand()) {
            equipItem(pPlayer, pHand, itemStack, null);
            return true;
        } else if(!itemStack.isEmpty() && hasItemInHand()) {
            if(!ModMethods.isFoodItem(itemStack)) {
                dropItem(getHeldItem().copy());
                unequipItem(null);
                equipItem(pPlayer, pHand, itemStack, null);
            } else {
                System.out.println("Tried placing item in inventory");
                placeItemInInventory(pPlayer, pHand, itemStack, null);
            }

            return true;
        }

        return false;
    }


    private boolean handleSaddlePlacement(Player pPlayer, InteractionHand pHand, ItemStack itemStack) {
        if(!(pPlayer.getUUID().toString().equals(this.getFriendUUID().toString()))) {
            this.getLookControl().setLookAt(pPlayer);
            //ServerPlayer angryCause = this.getAngryCause();
            this.setAngry(pPlayer);

            return true;
        }

        if(itemStack.isEmpty() && pPlayer.isCrouching() && isSaddled()) {
            unequipSaddle();
            dropItem(new ItemStack(Items.SADDLE));
            return true;
        } else if(itemStack.isEmpty() && !pPlayer.isCrouching() && isSaddled()) {
            doPlayerRide(pPlayer);
            return true;
        } else if(itemStack.is(Items.SADDLE) && !isSaddled()) {
            equipSaddle(SoundSource.BLOCKS);
            usePlayerItem(pPlayer, pHand, itemStack, true);
            return true;
        }
        return false;
    }

    private boolean handleArmorPlacement(Player pPlayer, InteractionHand pHand, ItemStack itemStack) {
        if(!pPlayer.getUUID().toString().equals(this.getFriendUUID().toString())) {
            return true;
        }

        if(itemStack.isEmpty() && pPlayer.isCrouching() && isArmored()) {
            Armor equippedArmor = getEquippedArmor();
            if(equippedArmor == Armor.LEATHER) {
                this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.5f, 0.7f);
                dropItem(new ItemStack(ModItems.LEATHER_CENTAUR_ARMOR.get()));
            } else if(equippedArmor == Armor.IRON) {
                this.playSound(SoundEvents.ARMOR_EQUIP_IRON, 0.5f, 0.7f);
                dropItem(new ItemStack(ModItems.IRON_CENTAUR_ARMOR.get()));
            } else if(equippedArmor == Armor.GOLDEN) {
                this.playSound(SoundEvents.ARMOR_EQUIP_GOLD, 0.5f, 0.7f);
                dropItem(new ItemStack(ModItems.GOLDEN_CENTAUR_ARMOR.get()));
            } else if(equippedArmor == Armor.DIAMOND) {
                this.playSound(SoundEvents.ARMOR_EQUIP_DIAMOND, 0.5f, 0.7f);
                dropItem(new ItemStack(ModItems.DIAMOND_CENTAUR_ARMOR.get()));
            }

            setEquippedArmor(Armor.NONE);
            return true;
        }

        if(!pPlayer.isCrouching() && !isArmored()) {
            boolean equipped = false;
            if(itemStack.is(ModItems.LEATHER_CENTAUR_ARMOR.get())) {
                setEquippedArmor(Armor.LEATHER);
                this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.5f, 1f);
                equipped = true;
            } else if(itemStack.is(ModItems.IRON_CENTAUR_ARMOR.get())) {
                setEquippedArmor(Armor.IRON);
                this.playSound(SoundEvents.ARMOR_EQUIP_IRON, 0.5f, 1f);
                equipped = true;
            } else if(itemStack.is(ModItems.GOLDEN_CENTAUR_ARMOR.get())) {
                setEquippedArmor(Armor.GOLDEN);
                this.playSound(SoundEvents.ARMOR_EQUIP_GOLD, 0.5f, 1f);
                equipped = true;
            } else if(itemStack.is(ModItems.DIAMOND_CENTAUR_ARMOR.get())) {
                setEquippedArmor(Armor.DIAMOND);
                this.playSound(SoundEvents.ARMOR_EQUIP_DIAMOND, 0.5f, 1f);
                equipped = true;
            }
            if(equipped) {
                usePlayerItem(pPlayer, pHand, itemStack, true);
                return true;
            }
        }

        return false;
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
                this.getFluidHeight(FluidTags.WATER) > this.getFluidJumpThreshold() + 0.2d ||
                this.isInLava() ||
                this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType) &&
                        height > this.getFluidJumpThreshold());

        if(!isFloating) {
            return;
        }

        if(this.getRandom().nextFloat() < 0.3f) {
            Vec3 currentVelocity = this.getDeltaMovement();
            this.setDeltaMovement(currentVelocity.x, currentVelocity.y + 0.1d, currentVelocity.z);
        }
    }


    public float getBaseMovementSpeed() {
        return (float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED));
    }


    protected void doPlayerRide(Player pPlayer) {
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
            this.setRotLerp(pPlayer.getYRot(), (pPlayer.getXRot() * 0.5F) - 10f, 0.2f); //-10f rotates head upward
            this.yRotO = this.yBodyRot = this.getYRot();
            this.yHeadRot = pPlayer.getYHeadRot();
        }

        if (pTravelVector.z <= 0.0D) {
            this.gallopSoundCounter = 0;
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
        boolean gapOne = ModMethods.getGapBelow(this, 1.0f);
        boolean gapTwo = ModMethods.getGapBelow(this, 2.0f);

        //if((gapOne || gapTwo) && !getIsExternalJump()) {
        if((gapOne) && !getIsExternalJump()) {
            forwardInput = 0.0d;
            gapFound = true;
        }

        if(pPlayer.isSprinting()) {
            pPlayer.setSprinting(false);
            this.setSprinting(true);
        } else if(pPlayer.zza < 0.0f && this.isSprinting()) {
            this.setSprinting(false);
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
            sideInput = pPlayer.xxa * 0.7d;;
        }

        // Jumping
        if(this.getIsExternalJump()) {
            this.setExternalJump(false);
            if(this.onGround()) {
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
        float standAnimO = 0.7F; //+ this.ridePositionBoost;
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

        updateAttackAttribute();


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

        updateAttackAttribute();
    }

    public boolean hasItemInHand() {
        ItemStack existingItem = this.inventory.getItem(InventorySlot.HAND.ordinal());
        return existingItem != ItemStack.EMPTY;
    }


    public ItemStack getHeldItem() {
        ItemStack existingItem = this.inventory.getItem(InventorySlot.HAND.ordinal());
        return existingItem;
    }


    @Override
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        if(random.nextInt(10) == 0) {
            super.playStepSound(pPos, pBlock);
            return;
        }

        if (!pBlock.liquid()) {
            BlockState blockstate = this.level().getBlockState(pPos.above());
            SoundType soundtype = pBlock.getSoundType(level(), pPos, this);
            if (blockstate.is(Blocks.SNOW)) {
                soundtype = blockstate.getSoundType(level(), pPos, this);
            }

            if (this.isVehicle() && this.canGallop) {
                ++this.gallopSoundCounter;
                if(this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0 && this.random.nextBoolean()) {
                    this.playGallopSound(soundtype);
                }
                int type = this.random.nextInt(2);
                if(type == 0) {
                    this.playSound(SoundEvents.HORSE_STEP, soundtype.getVolume() * 0.05F, soundtype.getPitch());
                } else if(type == 1) {
                    this.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.05F, soundtype.getPitch());
                }
            } else if (this.isWoodSoundType(soundtype)) {
                this.gallopSoundCounter = 0;
                this.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.05F, soundtype.getPitch());
            } else {
                this.gallopSoundCounter = 0;
                this.playSound(SoundEvents.HORSE_STEP, soundtype.getVolume() * 0.05F, soundtype.getPitch());
            }

        }
    }


    protected void playGallopSound(SoundType pSoundType) {
        this.playSound(SoundEvents.HORSE_GALLOP, pSoundType.getVolume() * 0.05F, pSoundType.getPitch() + 0.2f);
    }


    private boolean isWoodSoundType(SoundType pSoundType) {
        return pSoundType == SoundType.WOOD || pSoundType == SoundType.NETHER_WOOD || pSoundType == SoundType.STEM || pSoundType == SoundType.CHERRY_WOOD || pSoundType == SoundType.BAMBOO_WOOD;
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

    private void setAggroTowards(LivingEntity livingEntity) {
        this.setTarget(livingEntity);
        this.setAttackTargetUUID(livingEntity.getUUID());
    }




    private void setRotLerp(float pYRot, float pXRot, float delta) {
        float yRot = this.getYRot();
        float xRot = this.getXRot();

        yRot = Mth.rotLerp(delta, yRot, pYRot);
        xRot = Mth.rotLerp(delta, xRot, pXRot);
        this.setYRot(yRot % 360.0F);
        this.setXRot(xRot % 360.0F);
    }
}
