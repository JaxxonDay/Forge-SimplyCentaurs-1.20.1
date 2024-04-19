package com.jaxxonday.simplycentaurs.entity.custom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
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

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public CentaurEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    public Armor getEquippedArmor() {
        return Armor.byOrdinal(this.entityData.get(DATA_ARMOR));
    }

    public void setEquippedArmor(Armor pEquippedArmor) {
        this.entityData.set(DATA_ARMOR, pEquippedArmor.ordinal());
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ARMOR, 0);
        this.entityData.define(DATA_IS_SADDLED, false);
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

    protected void doPlayerRide(Player pPlayer) {
        if (!this.level().isClientSide) {
            pPlayer.setYRot(this.getYRot());
            pPlayer.setXRot(this.getXRot());
            pPlayer.startRiding(this);
        }
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
        return height;
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
}
