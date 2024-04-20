package com.jaxxonday.simplycentaurs.entity.custom;

import com.jaxxonday.simplycentaurs.util.ModMethods;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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

    private static final EntityDataAccessor<Integer> DATA_ARMOR = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_SADDLED = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_EXTERNAL_JUMP = SynchedEntityData.defineId(CentaurEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public double heightBoost = 0.0d;

    public CentaurEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setMaxUpStep(1.0f);
    }


    public Armor getEquippedArmor() {
        return Armor.byOrdinal(this.entityData.get(DATA_ARMOR));
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

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ARMOR, 0);
        this.entityData.define(DATA_IS_SADDLED, false);
        this.entityData.define(DATA_EXTERNAL_JUMP, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.3D, Ingredient.of(Items.DIAMOND), false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.1D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ModAbstractSmartCreature.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20d)
                .add(Attributes.FOLLOW_RANGE, 50d)
                .add(Attributes.MOVEMENT_SPEED, 0.25d)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0d)
                .add(Attributes.ATTACK_DAMAGE, 1.0d)
                .add(Attributes.ATTACK_KNOCKBACK, 0.2d);
    }


    // MAIN METHODS //

    @Override
    public void tick() {
        super.tick();

        //handleTickLoadingSavedData();

        if(this.level().isClientSide()) {
            runAnimationStates();
        }
    }


    private void runAnimationStates() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
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
        //return super.mobInteract(pPlayer, pHand);

        ItemStack itemStack = pPlayer.getItemInHand(pHand);

        if(itemStack.isEmpty()) {
            if(pPlayer.isCrouching()) {
                if(isSaddled()) {
                    unequipSaddle();
                    return InteractionResult.SUCCESS;
                }
            } else if(isSaddled()) {
                doPlayerRide(pPlayer);
                return InteractionResult.SUCCESS;
            }

            if(getEquippedArmor() == Armor.NONE) {
                setEquippedArmor(Armor.IRON);
            } else {
                setEquippedArmor(Armor.NONE);
            }
            return InteractionResult.SUCCESS;
        }

        if(itemStack.is(Items.SADDLE)) {
            if(!isSaddled()) {
                equipSaddle(SoundSource.AMBIENT);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;

    }

//    @Override
//    public void travel(Vec3 pTravelVector) {
//        if (this.isBeingRidden() && this.getFirstPassenger() instanceof Player player) {
//
//            float forward = player.zza;  // Forward key input from the player
//            float strafe = player.xxa;   // Strafe key input from the player
//
//            if(forward != 0.0f || strafe != 0.0f) {
//                this.setRotLerp(player.getYRot(), this.getXRot(), 0.3f);
//                //this.setYRot(player.getYRot());
//                //this.yBodyRot = player.getYRot();
//                //this.yHeadRot = player.getYHeadRot();
//                this.yHeadRot = Mth.rotLerp(0.3f, this.yHeadRot, player.getYHeadRot());
//            }
//
//            //this.setXRot(player.getXRot() * 0.5f); // You may adjust or remove this line based on your requirements
//            // Set the centaur's speed and movement direction based on the player
//
//            setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
//            super.travel(new Vec3(strafe, pTravelVector.y, forward));  // Apply the player's movement input
//        } else {
//            super.travel(pTravelVector);  // Normal movement
//        }
//    }


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


        // Swim check
        doSwim();
    }

    @Override
    protected float getRiddenSpeed(Player pPlayer) {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.8f;
    }

    @Override
    protected Vec3 getRiddenInput(Player pPlayer, Vec3 pTravelVector) {
        boolean gapOne = ModMethods.getGapBelow(this, 1.0f);
        boolean gapTwo = ModMethods.getGapBelow(this, 2.0f);

        if((gapOne || gapTwo) && !getIsExternalJump()) {
            return new Vec3(0.0d, 0.0d, 0.0d);
        }

        if(pPlayer.isSprinting()) {
            pPlayer.setSprinting(false);
            this.setSprinting(true);
        } else if(pPlayer.zza < 0.0f && this.isSprinting()) {
            this.setSprinting(false);
        }

        // Defining forward and side movement
        double forwardInput = 1.05d;
        double sideInput = pPlayer.xxa * 0.2d;

        // Set to normal inputs if not sprinting
        if(!this.isSprinting()) {
            forwardInput = pPlayer.zza;
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


    private void setRotLerp(float pYRot, float pXRot, float delta) {
        float yRot = this.getYRot();
        float xRot = this.getXRot();

        yRot = Mth.rotLerp(delta, yRot, pYRot);
        xRot = Mth.rotLerp(delta, xRot, pXRot);
        this.setYRot(yRot % 360.0F);
        this.setXRot(xRot % 360.0F);
    }
}
