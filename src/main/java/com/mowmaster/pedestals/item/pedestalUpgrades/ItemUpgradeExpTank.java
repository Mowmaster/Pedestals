package com.mowmaster.pedestals.item.pedestalUpgrades;


import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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

public class ItemUpgradeExpTank extends ItemUpgradeBaseExp
{

    public ItemUpgradeExpTank(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public int getExpBuffer(ItemStack stack)
    {
        int value = 100;
        switch (getCapacityModifier(stack))
        {
            case 0:
                value = 100;//
                break;
            case 1:
                value=250;//
                break;
            case 2:
                value = 500;//
                break;
            case 3:
                value = 1000;//
                break;
            case 4:
                value = 10000;//
                break;
            case 5:
                value=100000;//
                break;
            default: value=100;
        }

        return  value;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        int speed = getOperationSpeed(coinInPedestal);
        if(!world.isBlockPowered(pedestalPos))
        {
            if (tick%speed == 0) {
                upgradeAction(coinInPedestal);
                upgradeActionSendExp( world, coinInPedestal, pedestalPos);
            }
        }
    }

    public void upgradeAction(ItemStack coinInPedestal)
    {
        int maxXPLevel = getExpBuffer(coinInPedestal);
        if(!hasMaxXpSet(coinInPedestal) || readMaxXpFromNBT(coinInPedestal) != maxXPLevel) {setMaxXP(coinInPedestal, maxXPLevel);}
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.func_240702_b_(getExpTransferRateString(stack));
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        rate.func_240699_a_(TextFormatting.GRAY);
        speed.func_240699_a_(TextFormatting.RED);

        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item XPTANK = new ItemUpgradeExpTank(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xptank"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPTANK);
    }


}
