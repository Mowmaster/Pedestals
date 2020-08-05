package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
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

public class ItemUpgradeEnergyRelay extends ItemUpgradeBaseEnergy
{
    public ItemUpgradeEnergyRelay(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);

            if(!world.isBlockPowered(pedestalPos))
            {
                if (tick%speed == 0) {
                    upgradeActionSendEnergy(world,coinInPedestal,pedestalPos);
                    upgradeAction(world,pedestalPos,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
        if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}
    }

    public static final Item RFRELAY = new ItemUpgradeEnergyRelay(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rfrelay"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFRELAY);
    }


}
