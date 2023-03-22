package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.PedestalUtils.References;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeGenerator_FurnaceFuels extends ItemUpgradeBase
{
    public ItemUpgradeGenerator_FurnaceFuels(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }


    public int baseEnergyProduction(){ return PedestalConfig.COMMON.upgrade_generator_baseEnergyCost.get(); }

    @Override
    public int getUpgradeWorkRange(ItemStack coinUpgrade) { return 0; }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        //remove NBT saved on upgrade here
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,coinInPedestal.getOrCreateTag(),"_numdelay");

    }

    private int calcFuelBurnTime(ItemStack inputFuel)
    {
        if(!inputFuel.isEmpty())
        {
            return ForgeHooks.getBurnTime(inputFuel,RecipeType.SMELTING);
        }
        return 0;
    }



    private int getBurnTimeLeft(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getOrCreateTag(), "_numdelay");
    }

    private void setBurnTime(BasePedestalBlockEntity pedestal, ItemStack input)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), calcFuelBurnTime(input), "_numdelay");
    }

    private void resetBurnTime(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), 0, "_numdelay");
    }

    private int incrementBurnTime(BasePedestalBlockEntity pedestal, boolean simulate)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int current = getBurnTimeLeft(pedestal);
        if(current>0)
        {
            int ticks = 1+getSpeedTicksReduced(pedestal.getCoinOnPedestal());
            int maxTicksReduced = (current>=ticks)?(ticks):(current);
            if(!simulate)MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), (current-maxTicksReduced), "_numdelay");
            return maxTicksReduced;
        }
        return 0;
    }

    /*@Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {
        List<String> messages = new ArrayList<>();

        if(baseEnergyCost()>0)
        {
            boolean hasItem = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,pedestal.getCoinOnPedestal().getOrCreateTag(),"hasitem");
            if(pedestal.getStoredEnergy()<baseEnergyCost() && hasItem)
            {
                messages.add(ChatFormatting.RED + "Needs Energy");
            }
        }

        return messages;
    }*/

    @Override
    public void updateAction(Level level, BasePedestalBlockEntity pedestal)
    {
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack stackInPedestal = pedestal.getItemInPedestal();

        if(getBurnTimeLeft(pedestal)>0)
        {
            int calcRF = incrementBurnTime(pedestal,true) * baseEnergyProduction();
            if(pedestal.addEnergy(calcRF,true)>=calcRF)
            {
                incrementBurnTime(pedestal,false);
                pedestal.addEnergy(calcRF,false);
                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+0.75D,pedestalPos.getZ(),255,0,0));
            }
        }
        else if(!stackInPedestal.isEmpty())
        {
            ItemStack copyStack = stackInPedestal.copy();
            copyStack.setCount(1);

            if(calcFuelBurnTime(copyStack)>0)
            {
                if(!pedestal.removeItem(copyStack.getCount(),true).isEmpty())
                {
                    setBurnTime(pedestal,copyStack);
                    pedestal.removeItem(copyStack.getCount(),false);
                    if(copyStack.hasCraftingRemainingItem())pedestal.addItem(copyStack.getCraftingRemainingItem(),false);
                }
            }
        }
    }
}
