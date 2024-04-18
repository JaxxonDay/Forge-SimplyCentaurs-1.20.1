package com.jaxxonday.simplycentaurs.datagen;

import com.jaxxonday.simplycentaurs.CentaurMod;
import com.jaxxonday.simplycentaurs.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CentaurMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.LEATHER_CENTAUR_ARMOR);
        simpleItem(ModItems.IRON_CENTAUR_ARMOR);
        simpleItem(ModItems.GOLDEN_CENTAUR_ARMOR);
        simpleItem(ModItems.DIAMOND_CENTAUR_ARMOR);
    }


    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(CentaurMod.MODID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder handheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/handheld")).texture("layer0",
                new ResourceLocation(CentaurMod.MODID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder handheldRodItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/handheld_rod")).texture("layer0",
                new ResourceLocation(CentaurMod.MODID,"item/" + item.getId().getPath()));
    }



}
