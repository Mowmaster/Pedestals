package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

public class ItemUpgradeExpRelay extends ItemUpgradeBaseExp
{

    public ItemUpgradeExpRelay(Item.Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isClientSide)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getBlockPos();

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.hasNeighborSignal(pedestalPos))
            {
                int getMaxXpValue = getExpCountByLevel(getExpBuffer(coinInPedestal));
                if(!hasMaxXpSet(coinInPedestal) || readMaxXpFromNBT(coinInPedestal) != getMaxXpValue) {setMaxXP(coinInPedestal, getMaxXpValue);}
                upgradeActionSendExp(pedestal);
            }
        }
    }

    @Override
    public int getExpBuffer(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = (capacityOver*5)+30;

        //20k being the max before we get close to int overflow
        return  (overEnchanted>=maxLVLStored)?(maxLVLStored):(overEnchanted);
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.NIL_UUID);

        TranslationTextComponent xpstored = new TranslationTextComponent(getDescriptionId() + ".chat_xp");
        xpstored.append(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.withStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.NIL_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".chat_rate");
        rate.append(getExpTransferRateString(stack));
        rate.withStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".tooltip_rate");
        rate.append(getExpTransferRateString(stack));
        rate.withStyle(TextFormatting.GRAY);
        tooltip.add(rate);
    }

    public static final Item XPRELAY = new ItemUpgradeExpRelay(new Item.Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xprelay"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPRELAY);
    }


}
