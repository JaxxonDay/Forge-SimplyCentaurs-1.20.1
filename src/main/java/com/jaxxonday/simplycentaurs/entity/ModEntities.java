package com.jaxxonday.simplycentaurs.entity;

import com.jaxxonday.simplycentaurs.CentaurMod;
import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CentaurMod.MOD_ID);

    public static final RegistryObject<EntityType<CentaurEntity>> CENTAUR =
            ENTITY_TYPES.register("centaur", () -> EntityType.Builder.of(CentaurEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 2.2f).build("centaur"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
