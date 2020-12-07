package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

//Filters by number of enchants on an item
public class ItemUpgradeFilterEnchantedCount extends ItemUpgradeBaseFilter
{
    public ItemUpgradeFilterEnchantedCount(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {return true;}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return !EnchantmentRegistry.COINUPGRADE.equals(enchantment.type) && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {

    }

    public int getSize(ItemStack stack)
    {
        int value = 1;
        switch (getCapacityModifier(stack))
        {
            case 0:
                value = 1;//
                break;
            case 1:
                value=2;//
                break;
            case 2:
                value = 3;//
                break;
            case 3:
                value = 4;//
                break;
            case 4:
                value = 5;//
                break;
            case 5:
                value=6;//
                break;
            default: value=1;
        }

        return  value;
    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        boolean returner = false;
        TileEntity tile = world.getTileEntity(posPedestal);
        if(tile instanceof PedestalTileEntity)
        {
            PedestalTileEntity ped = (PedestalTileEntity)tile;
            int count = getSize(ped.getCoinOnPedestal());
            if(itemStackIn.isEnchanted() || itemStackIn.getItem().equals(Items.ENCHANTED_BOOK))
            {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStackIn);
                if(map.size() == count)
                {
                    returner = true;
                }
            }
        }


        return returner;
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {

    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".chat_name");
        TranslationTextComponent name2 = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.appendString(name2.getString());
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent enchantsize = new TranslationTextComponent(getTranslationKey() + ".chat_size");
        enchantsize.appendString(""+ getSize(stack) +"");
        enchantsize.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(enchantsize, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //super.addInformation(stack, worldIn, tooltip, flagIn);
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".tooltip_size");
        xpstored.appendString(""+ getSize(stack) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(xpstored);
    }

    public static final Item ENCHANTEDCOUNT1 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount"));
    /*public static final Item ENCHANTEDCOUNT2 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount2"));
    public static final Item ENCHANTEDCOUNT3 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount3"));
    public static final Item ENCHANTEDCOUNT4 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount4"));
    public static final Item ENCHANTEDCOUNT5 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount5"));
    public static final Item ENCHANTEDCOUNT6 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount6"));
    public static final Item ENCHANTEDCOUNT7 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount7"));
    public static final Item ENCHANTEDCOUNT8 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount8"));
    public static final Item ENCHANTEDCOUNT9 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount9"));
    public static final Item ENCHANTEDCOUNT10 = new ItemUpgradeFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedcount10"));
    */
    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENCHANTEDCOUNT1);
        /*event.getRegistry().register(ENCHANTEDCOUNT2);
        event.getRegistry().register(ENCHANTEDCOUNT3);
        event.getRegistry().register(ENCHANTEDCOUNT4);
        event.getRegistry().register(ENCHANTEDCOUNT5);
        event.getRegistry().register(ENCHANTEDCOUNT6);
        event.getRegistry().register(ENCHANTEDCOUNT7);
        event.getRegistry().register(ENCHANTEDCOUNT8);
        event.getRegistry().register(ENCHANTEDCOUNT9);
        event.getRegistry().register(ENCHANTEDCOUNT10);*/
    }
}
