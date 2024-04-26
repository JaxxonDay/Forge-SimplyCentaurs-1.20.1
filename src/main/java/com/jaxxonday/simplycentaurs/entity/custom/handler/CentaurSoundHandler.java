package com.jaxxonday.simplycentaurs.entity.custom.handler;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class CentaurSoundHandler {
    private final CentaurEntity centaurEntity;

    protected int gallopSoundCounter;

    public CentaurSoundHandler(CentaurEntity centaurEntity) {
        this.centaurEntity = centaurEntity;
    }


    public boolean handledStepSound(BlockPos pPos, BlockState pBlock) {
        CentaurEntity entity = this.centaurEntity;
        if(entity.getRandom().nextInt(10) == 0) {
            return false;
        }

        if (!pBlock.liquid()) {
            BlockState blockstate = entity.level().getBlockState(pPos.above());
            SoundType soundtype = pBlock.getSoundType(entity.level(), pPos, entity);
            if (blockstate.is(Blocks.SNOW)) {
                soundtype = blockstate.getSoundType(entity.level(), pPos, entity);
            }

            if (entity.isVehicle() && entity.isCanGallop()) {
                ++this.gallopSoundCounter;
                if(this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0 && entity.getRandom().nextBoolean()) {
                    playGallopSound(soundtype);
                }
                int type = entity.getRandom().nextInt(2);
                if(type == 0) {
                    entity.playSound(SoundEvents.HORSE_STEP, soundtype.getVolume() * 0.05F, soundtype.getPitch());
                } else if(type == 1) {
                    entity.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.05F, soundtype.getPitch());
                }
            } else if (isWoodSoundType(soundtype)) {
                this.gallopSoundCounter = 0;
                entity.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.05F, soundtype.getPitch());
            } else {
                this.gallopSoundCounter = 0;
                entity.playSound(SoundEvents.HORSE_STEP, soundtype.getVolume() * 0.05F, soundtype.getPitch());
            }

        }
        return true;
    }

    protected void playGallopSound(SoundType pSoundType) {
        CentaurEntity entity = this.centaurEntity;
        entity.playSound(SoundEvents.HORSE_GALLOP, pSoundType.getVolume() * 0.05F, pSoundType.getPitch() + 0.2f);
    }


    private boolean isWoodSoundType(SoundType pSoundType) {
        return pSoundType == SoundType.WOOD || pSoundType == SoundType.NETHER_WOOD || pSoundType == SoundType.STEM || pSoundType == SoundType.CHERRY_WOOD || pSoundType == SoundType.BAMBOO_WOOD;
    }


}
