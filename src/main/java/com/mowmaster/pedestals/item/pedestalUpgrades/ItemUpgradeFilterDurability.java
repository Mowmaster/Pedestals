package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFilterDurability extends ItemUpgradeBaseFilter
{
    public ItemUpgradeFilterDurability(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {return true;}

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(stack.getItem() instanceof ItemUpgradeBase && enchantment.getRegistryName().getNamespace().equals(Reference.MODID))
        {
            return !EnchantmentRegistry.COINUPGRADE.equals(enchantment.type) && super.canApplyAtEnchantingTable(stack, enchantment);
        }
        return false;
    }

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return (stack.getCount()==1)?(super.isBookEnchantable(stack, book)):(false);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }


    public void updateAction(World world, PedestalTileEntity pedestal)
    {

    }

    public int getPercentDamaged(ItemStack itemIn)
    {
        if(itemIn.isDamageable())
        {
            int maxDamage = itemIn.getMaxDamage();
            int damage = itemIn.getDamage();
            int durabilityCurrent = maxDamage-damage;
            int percentDurability = Math.floorDiv((durabilityCurrent*100),maxDamage);
            return percentDurability;
        }
        return 100;
    }

    public int getCapacityTarget(int capacity)
    {
        //Durability based on capacity...
        /*
        0=full
        1=90    2=80    3=70    4=60    5=50    6=40    7=30    8=20    9=10
        10=95   11=85   12=75   13=65   14=55   15=45   16=35   17=25   18=15   19=5
        20=99/98/97/etc
         */
        int returner = 100;
        switch(capacity)
        {
            case 0:returner=100;
                break;
            case 1:returner=90;
                break;
            case 2:returner=80;
                break;
            case 3:returner=70;
                break;
            case 4:returner=60;
                break;
            case 5:returner=50;
                break;
            case 6:returner=40;
                break;
            case 7:returner=30;
                break;
            case 8:returner=20;
                break;
            case 9:returner=10;
                break;
            case 10:returner=95;
                break;
            case 11:returner=85;
                break;
            case 12:returner=75;
                break;
            case 13:returner=65;
                break;
            case 14:returner=55;
                break;
            case 15:returner=45;
                break;
            case 16:returner=35;
                break;
            case 17:returner=25;
                break;
            case 18:returner=15;
                break;
            case 19:returner=5;
                break;
            default: return returner= 100 - ((capacity-19));

        }
        return returner;
    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        boolean returner = true;
        BlockPos posInventory = getBlockPosOfBlockBelow(world, posPedestal, 1);
        int capacity = 0;

        TileEntity tile = world.getTileEntity(posPedestal);
        if(tile instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = (PedestalTileEntity)tile;
            ItemStack coin = pedestal.getCoinOnPedestal();
            capacity = (getCapacityModifierOver(coin)*4);
        }

        int percentDurabilityCurrent = getPercentDamaged(itemStackIn);
        int percentToBeat = getCapacityTarget(capacity);

        return (itemStackIn.isDamageable())?((percentDurabilityCurrent>=percentToBeat)?(true):(false)):(false);
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {

    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.NIL_UUID);

        int overEnchanted = (getCapacityModifierOver(stack));
        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".chat_rate");
        TranslationTextComponent rate2 = new TranslationTextComponent(getDescriptionId() + ".chat_rate2");
        rate.append("" + getCapacityTarget(overEnchanted) + "");
        rate.append(rate2);
        rate.withStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int overEnchanted = (getCapacityModifierOver(stack));
        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".tooltip_rate");
        TranslationTextComponent rate2 = new TranslationTextComponent(getDescriptionId() + ".tooltip_rate2");
        rate.append("" + getCapacityTarget(overEnchanted) + "");
        rate.append(rate2);
        rate.withStyle(TextFormatting.GRAY);
        tooltip.add(rate);
    }

    public static final Item DURABILITY = new ItemUpgradeFilterDurability(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterdurability"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(DURABILITY);
    }



}
