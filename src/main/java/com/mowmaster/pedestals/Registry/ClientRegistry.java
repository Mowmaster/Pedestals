package com.mowmaster.pedestals.Registry;


import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntityRenderer;
import com.mowmaster.pedestals.Items.Filters.FilterRestricted;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseDustBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseEnergyBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseFluidBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseXpBulkStorageItem;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "pedestals", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistry
{

    @SubscribeEvent
    public static void registerItemColor(RegisterColorHandlersEvent.Item event) {

        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ITEM.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ITEMSTACK.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_DURABILITY.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ENCHANTED.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ENCHANTED_COUNT.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ENCHANTED_EXACT.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ENCHANTED_FUZZY.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_FOOD.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_MOD.get());
        event.register((stack, color) ->
        {if (color == 1) {return FilterRestricted.getColor(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_RESTRICTED.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_TAG.get());

        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_EXACT.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_FUZZY.get());


        event.register((stack, color) ->
        {if (color == 1) {return BaseFluidBulkStorageItem.getItemColor();} else {return -1;}}, DeferredRegisterItems.MECHANICAL_STORAGE_FLUID.get());
        event.register((stack, color) ->
        {if (color == 1) {return BaseEnergyBulkStorageItem.getItemColor();} else {return -1;}}, DeferredRegisterItems.MECHANICAL_STORAGE_ENERGY.get());
        event.register((stack, color) ->
        {if (color == 1) {return BaseXpBulkStorageItem.getItemColor();} else {return -1;}}, DeferredRegisterItems.MECHANICAL_STORAGE_XP.get());
        event.register((stack, color) ->
        {if (color == 1) {return BaseDustBulkStorageItem.getItemColor();} else {return -1;}}, DeferredRegisterItems.MECHANICAL_STORAGE_DUST.get());




        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.PEDESTAL_UPGRADE_IMPORT.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.PEDESTAL_UPGRADE_EXPORT.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.PEDESTAL_UPGRADE_MAGNET.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.PEDESTAL_UPGRADE_PACKAGER.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.PEDESTAL_UPGRADE_UNPACKAGER.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.PEDESTAL_UPGRADE_VOID.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.PEDESTAL_UPGRADE_DROPPER.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.PEDESTAL_UPGRADE_RECYCLER.get());

        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_ITEM.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_ITEMSTACK.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_DURABILITY.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_ENCHANTED.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_ENCHANTED_COUNT.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_ENCHANTED_EXACT.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_ENCHANTED_FUZZY.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_FOOD.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_MOD.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_RESTRICTED.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_TAG.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_TAG_MACHINE.get());

        //ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_EXACT.get());
        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_FUZZY.get());

        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get());

        ItemModelPropertiesDust.dustItemModes(DeferredRegisterItems.TOOL_MANIFEST.get());






        /*
        *
        * TILE ENTITY BLOCKS HERE
        *
         */
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get());

    }

    @SubscribeEvent
    public static void registerBlockColor(RegisterColorHandlersEvent.Block event) {

        event.register((blockstate, blockReader, blockPos, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromStateInt(blockstate);} else {return -1;}}, DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get());
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void textureStitchPreEvent(TextureStitchEvent.Pre event)
    {

        ResourceLocation location = event.getAtlas().location();

        if(location.equals(TextureAtlas.LOCATION_BLOCKS))
        {

            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_1"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_2"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_3"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_4"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_5"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_6"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_7"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_8"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_9"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_10"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_11"));
            event.addSprite(new ResourceLocation(MODID, "util/pedestal_render_12"));
            event.addSprite(new ResourceLocation(MODID, "util/upgrade_render"));

            event.addSprite(new ResourceLocation(MODID, "util/crystal_dust"));
        }

    }

    @SubscribeEvent
    public static void registerLayers(FMLClientSetupEvent event)
    {

    }


    public static void registerBlockEntityRenderers()
    {
        BlockEntityRenderers.register(DeferredBlockEntityTypes.PEDESTAL.get(), BasePedestalBlockEntityRenderer::new);
    }
}
