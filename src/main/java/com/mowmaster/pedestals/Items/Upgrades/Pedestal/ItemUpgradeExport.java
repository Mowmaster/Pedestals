package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.Capabilities.Experience.IExperienceStorage;
import com.mowmaster.mowlib.MowLibUtils.*;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemUpgradeExport extends ItemUpgradeBase implements IHasModeTypes {
    public ItemUpgradeExport(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyItemCapacity(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyFluidCapacity(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyEnergyCapacity(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyXPCapacity(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyDustCapacity(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyEntityContainers(ItemStack upgradeItemStack) { return true; }

    @Override
    public int getUpgradeWorkRange(ItemStack coinUpgrade) { return 0; }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        BlockPos posInventory = getPosOfBlockBelow(level, pedestalPos, 1);
        if (level.getBlockEntity(posInventory) instanceof BasePedestalBlockEntity) {
            return;
        }
        Direction pedestalFacing = getPedestalFacing(level, pedestalPos);

        if (canTransferItems(coin)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseItemTransferSpeed.get() + getItemCapacityIncrease(coin);

            LazyOptional<IItemHandler> cap;
            if (hasEntityContainer(coin)) {
                cap = findItemHandlerAtPosEntity(level, posInventory, pedestalFacing, true);
            } else {
                cap = MowLibItemUtils.findItemHandlerAtPos(level, posInventory, pedestalFacing, true);
            }
            cap.ifPresent(handler -> {
                // TODO: keep iterating over items in the pedestal if we have remaining transfer capacity
                ItemStack stackInPedestal = pedestal.removeItem(true);
                if (!stackInPedestal.isEmpty()) {
                    ItemStack toTransfer = stackInPedestal.copy();
                    if (toTransfer.getCount() > maxToTransfer) {
                        toTransfer.setCount(maxToTransfer);
                    }
                    ItemStack leftover = ItemHandlerHelper.insertItem(handler, toTransfer.copy(), false);
                    if (leftover.getCount() != toTransfer.getCount()) {
                        toTransfer.setCount(toTransfer.getCount() - leftover.getCount());
                        pedestal.removeItemStack(toTransfer, false);
                    }
                }
            });
        }

        if (canTransferFluids(coin)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseFluidTransferSpeed.get() + getFluidCapacityIncrease(coin);

            LazyOptional<IFluidHandler> cap;
            if (hasEntityContainer(coin)) {
                cap = findFluidHandlerAtPosEntity(level, posInventory, pedestalFacing,true);
            } else {
                cap = MowLibFluidUtils.findFluidHandlerAtPos(level, posInventory, pedestalFacing,true);
            }
            cap.ifPresent(handler -> {
                FluidStack toTransfer = pedestal.getStoredFluid().copy();
                if (toTransfer.getAmount() > maxToTransfer) {
                    toTransfer.setAmount(maxToTransfer);
                }
                int amountTransferred = handler.fill(toTransfer.copy(), IFluidHandler.FluidAction.EXECUTE);
                if (amountTransferred > 0) {
                    FluidStack toRemove = toTransfer.copy();
                    toRemove.setAmount(amountTransferred);
                    pedestal.removeFluid(toRemove, IFluidHandler.FluidAction.EXECUTE);
                }
            });
        }

        if (canTransferEnergy(coin)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseEnergyTransferSpeed.get() + getEnergyCapacityIncrease(pedestal.getCoinOnPedestal());

            LazyOptional<IEnergyStorage> cap;
            if (hasEntityContainer(coin)) {
                cap = findEnergyHandlerAtPosEntity(level, posInventory, pedestalFacing, true);
            } else {
                cap = MowLibEnergyUtils.findEnergyHandlerAtPos(level, posInventory, pedestalFacing, true);
            }
            cap.ifPresent(handler -> {
                if (handler.canReceive()) {
                    int toTransfer = Math.min(maxToTransfer, pedestal.getStoredEnergy());
                    int amountTransferred = handler.receiveEnergy(toTransfer, false);
                    if (amountTransferred > 0) {
                        pedestal.removeEnergy(amountTransferred, false);
                    }
                }
            });
        }

        if(canTransferXP(coin)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseExpTransferSpeed.get() + MowLibXpUtils.getExpCountByLevel(getXPCapacityIncrease(pedestal.getCoinOnPedestal()));

            LazyOptional<IExperienceStorage> cap = MowLibXpUtils.findExperienceHandlerAtPos(level, posInventory, pedestalFacing, true);
            cap.ifPresent(handler -> {
                if (handler.canReceive()) {
                    int toTransfer = Math.min(maxToTransfer, pedestal.getStoredExperience());
                    int amountTransferred = handler.receiveExperience(toTransfer, false);
                    if (amountTransferred > 0) {
                        pedestal.removeExperience(amountTransferred,false);
                    }
                }
            });
        }

        if(canTransferDust(coin)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseDustTransferSpeed.get() + getDustCapacityIncrease(pedestal.getCoinOnPedestal());

            LazyOptional<IDustHandler> cap = MowLibDustUtils.findDustHandlerAtPos(level, posInventory, pedestalFacing);
            cap.ifPresent(handler -> {
                DustMagic toTransfer = pedestal.getStoredDust().copy();
                if (toTransfer.getDustAmount() > maxToTransfer) {
                    toTransfer.setDustAmount(maxToTransfer);
                }
                int amountTransferred = handler.fill(toTransfer.copy(), IDustHandler.DustAction.EXECUTE);
                if (amountTransferred > 0) {
                    DustMagic toRemove = toTransfer.copy();
                    toRemove.setDustAmount(amountTransferred);
                    pedestal.removeDust(toRemove, IDustHandler.DustAction.EXECUTE);
                }
            });
        }
    }
}