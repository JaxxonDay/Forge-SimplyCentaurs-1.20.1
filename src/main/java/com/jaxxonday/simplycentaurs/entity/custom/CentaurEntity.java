package com.jaxxonday.simplycentaurs.entity.custom;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CentaurEntity extends ModAbstractSmartCreature implements Saddleable {
    public CentaurEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
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
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.FOLLOW_RANGE, 50D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.2D);
    }










    // Saddle Data
    @Override
    public void containerChanged(Container pContainer) {

    }

    @Override
    public boolean isSaddleable() {
        return false;
    }

    @Override
    public void equipSaddle(@Nullable SoundSource pSource) {

    }

    @Override
    public boolean isSaddled() {
        return false;
    }
}
