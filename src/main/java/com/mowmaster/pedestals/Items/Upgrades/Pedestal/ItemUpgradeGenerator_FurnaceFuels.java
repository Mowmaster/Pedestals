package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.PedestalUtils.References;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeGenerator_FurnaceFuels extends ItemUpgradeBase {
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

    private int getBurnTimeLeft(BasePedestalBlockEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getOrCreateTag(), "_numdelay");
    }

    private void setBurnTime(BasePedestalBlockEntity pedestal, int burnTime) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), burnTime, "_numdelay");
    }

    private int incrementBurnTime(BasePedestalBlockEntity pedestal, boolean simulate) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int current = getBurnTimeLeft(pedestal);
        if (current > 0) {
            int ticks = 1 + getSpeedTicksReduced(pedestal.getCoinOnPedestal());
            int maxTicksReduced = Math.min(current, ticks);
            if (!simulate) {
                MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), (current - maxTicksReduced), "_numdelay");
            }
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
    public void updateAction(Level level, BasePedestalBlockEntity pedestal) {
        ItemStack stackInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        int simulatedBurnTime = incrementBurnTime(pedestal, true);

        if (simulatedBurnTime > 0) {
            int calcRF = simulatedBurnTime * baseEnergyProduction();
            if (pedestal.addEnergy(calcRF, true)>=calcRF) {
                incrementBurnTime(pedestal, false);
                pedestal.addEnergy(calcRF, false);
                if (pedestal.canSpawnParticles()) {
                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+0.75D,pedestalPos.getZ(),255,0,0));
                }
            }
        } else if(!stackInPedestal.isEmpty()) {
            ItemStack toBurn = stackInPedestal.copy();
            toBurn.setCount(1);
            int burnTime = ForgeHooks.getBurnTime(toBurn, RecipeType.SMELTING);

            if (burnTime > 0 && !pedestal.removeItemStack(toBurn,true).isEmpty()) {
                if (toBurn.hasCraftingRemainingItem())
                {
                    if(pedestal.itemPassesFilter(toBurn.getCraftingRemainingItem()))
                    {
                        setBurnTime(pedestal, burnTime);
                        pedestal.removeItemStack(toBurn, false);
                        pedestal.addItem(toBurn.getCraftingRemainingItem(),false);
                    }
                    else
                    {
                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+0.75D,pedestalPos.getZ(),128,128,128));
                    }
                }
                else
                {
                    setBurnTime(pedestal, burnTime);
                    pedestal.removeItemStack(toBurn, false);
                }
            }
        }
    }
}
