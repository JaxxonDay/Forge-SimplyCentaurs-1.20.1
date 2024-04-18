package com.jaxxonday.simplycentaurs.item;

import com.jaxxonday.simplycentaurs.CentaurMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CentaurMod.MOD_ID);


//    public static final RegistryObject<Item> SAPPHIRE = ITEMS.register("sapphire",
//            () -> new Item(new Item.Properties()));


    public static final RegistryObject<Item> LEATHER_CENTAUR_ARMOR = ITEMS.register("leather_centaur_armor",
            () -> new Item(new Item.Properties().stacksTo(1).durability(256)));

    public static final RegistryObject<Item> IRON_CENTAUR_ARMOR = ITEMS.register("iron_centaur_armor",
            () -> new Item(new Item.Properties().stacksTo(1).durability(512)));

    public static final RegistryObject<Item> GOLDEN_CENTAUR_ARMOR = ITEMS.register("golden_centaur_armor",
            () -> new Item(new Item.Properties().stacksTo(1).durability(128)));

    public static final RegistryObject<Item> DIAMOND_CENTAUR_ARMOR = ITEMS.register("diamond_centaur_armor",
            () -> new Item(new Item.Properties().stacksTo(1).durability(1024)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
