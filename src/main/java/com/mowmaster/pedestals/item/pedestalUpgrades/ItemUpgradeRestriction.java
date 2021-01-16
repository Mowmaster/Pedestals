package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeRestriction extends ItemUpgradeBase
{
    public ItemUpgradeRestriction(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }

    @Override
    public int canAcceptCount(World world, BlockPos pos,ItemStack inPedestal, ItemStack itemStackIncoming) {
        if(inPedestal.isEmpty())
        {
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof PedestalTileEntity)
            {
                PedestalTileEntity pedestal = (PedestalTileEntity)tile;
                ItemStack coin = pedestal.getCoinOnPedestal();
                int maxIncomming = itemStackIncoming.getMaxStackSize();
                int overEnchanted = (getCapacityModifierOver(coin)*4);
                return (overEnchanted>0)?((overEnchanted>maxIncomming)?(maxIncomming):(overEnchanted)):(1);
            }

            return 1;
        }

        return 0;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {

    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {

    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.DUMMY_UUID);

        int overEnchanted = (getCapacityModifierOver(stack)*4);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString("" + ((overEnchanted>0)?(overEnchanted):(1)) + "");
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int overEnchanted = (getCapacityModifierOver(stack)*4);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + ((overEnchanted>0)?(overEnchanted):(1)) + "");
        rate.mergeStyle(TextFormatting.GRAY);
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);
    }

    public static final Item DEFAULT = new ItemUpgradeRestriction(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/restriction"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(DEFAULT);
    }


}
