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

import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

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
    public boolean canModifyRemoteStorage(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean needsWorkCard(ItemStack upgradeItemStack) {
        return hasRemoteStorage(upgradeItemStack);
    }

    @Override
    public int getWorkCardType() { return 2; }

    @Override
    public int getUpgradeWorkRange(ItemStack upgradeItemStack) {
        if (hasRemoteStorage(upgradeItemStack)) {
            return super.getUpgradeWorkRange(upgradeItemStack);
        } else {
            return 0;
        }
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(MODID, coinInPedestal);
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        List<BlockPos> allPositions = hasRemoteStorage(coin) ? getValidWorkCardPositions(pedestal, coin, getWorkCardType(), MODID) : List.of(getPosOfBlockBelow(level, pedestalPos, 1));
        if (allPositions.isEmpty() || allPositions.size() > 8) return;

        Direction pedestalFacing = getPedestalFacing(level, pedestalPos);
        for (BlockPos position : allPositions) {
            if (level.getBlockEntity(position) instanceof BasePedestalBlockEntity) continue;

            List<Boolean> exportResults = List.of(
                exportItemAction(level, position, pedestal, pedestalFacing, coin),
                exportFluidAction(level, position, pedestal, pedestalFacing, coin),
                exportEnergyAction(level, position, pedestal, pedestalFacing, coin),
                exportXPAction(level, position, pedestal, pedestalFacing, coin),
                exportDustAction(level, position, pedestal, pedestalFacing, coin)
            );

            if (exportResults.contains(true)) {
                break;
            }
        }
    }

    private boolean exportItemAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferItems(upgradeItemStack)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseItemTransferSpeed.get() + getItemCapacityIncrease(upgradeItemStack);

            LazyOptional<IItemHandler> cap;
            if (hasEntityContainer(upgradeItemStack)) {
                cap = findItemHandlerAtPosEntity(level, position, pedestalFacing, true);
            } else {
                cap = MowLibItemUtils.findItemHandlerAtPos(level, position, pedestalFacing, true);
            }
            return cap.map(handler -> {
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
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    private boolean exportFluidAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferFluids(upgradeItemStack)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseFluidTransferSpeed.get() + getFluidCapacityIncrease(upgradeItemStack);

            LazyOptional<IFluidHandler> cap;
            if (hasEntityContainer(upgradeItemStack)) {
                cap = findFluidHandlerAtPosEntity(level, position, pedestalFacing,true);
            } else {
                cap = MowLibFluidUtils.findFluidHandlerAtPos(level, position, pedestalFacing,true);
            }
            return cap.map(handler -> {
                FluidStack toTransfer = pedestal.getStoredFluid().copy();
                if (toTransfer.getAmount() > maxToTransfer) {
                    toTransfer.setAmount(maxToTransfer);
                }
                int amountTransferred = handler.fill(toTransfer.copy(), IFluidHandler.FluidAction.EXECUTE);
                if (amountTransferred > 0) {
                    FluidStack toRemove = toTransfer.copy();
                    toRemove.setAmount(amountTransferred);
                    pedestal.removeFluid(toRemove, IFluidHandler.FluidAction.EXECUTE);
                    return true;
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    private boolean exportEnergyAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferEnergy(upgradeItemStack)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseEnergyTransferSpeed.get() + getEnergyCapacityIncrease(upgradeItemStack);

            LazyOptional<IEnergyStorage> cap;
            if (hasEntityContainer(upgradeItemStack)) {
                cap = findEnergyHandlerAtPosEntity(level, position, pedestalFacing, true);
            } else {
                cap = MowLibEnergyUtils.findEnergyHandlerAtPos(level, position, pedestalFacing, true);
            }
            return cap.map(handler -> {
                if (handler.canReceive()) {
                    int toTransfer = Math.min(maxToTransfer, pedestal.getStoredEnergy());
                    int amountTransferred = handler.receiveEnergy(toTransfer, false);
                    if (amountTransferred > 0) {
                        pedestal.removeEnergy(amountTransferred, false);
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }
    private boolean exportXPAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferXP(upgradeItemStack)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseExpTransferSpeed.get() + MowLibXpUtils.getExpCountByLevel(getXPCapacityIncrease(upgradeItemStack));

            LazyOptional<IExperienceStorage> cap = MowLibXpUtils.findExperienceHandlerAtPos(level, position, pedestalFacing, true);
            return cap.map(handler -> {
                if (handler.canReceive()) {
                    int toTransfer = Math.min(maxToTransfer, pedestal.getStoredExperience());
                    int amountTransferred = handler.receiveExperience(toTransfer, false);
                    if (amountTransferred > 0) {
                        pedestal.removeExperience(amountTransferred,false);
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }
    private boolean exportDustAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferDust(upgradeItemStack)) {
            int maxToTransfer = PedestalConfig.COMMON.upgrade_export_baseDustTransferSpeed.get() + getDustCapacityIncrease(upgradeItemStack);

            LazyOptional<IDustHandler> cap = MowLibDustUtils.findDustHandlerAtPos(level, position, pedestalFacing);
            return cap.map(handler -> {
                DustMagic toTransfer = pedestal.getStoredDust().copy();
                if (toTransfer.getDustAmount() > maxToTransfer) {
                    toTransfer.setDustAmount(maxToTransfer);
                }
                int amountTransferred = handler.fill(toTransfer.copy(), IDustHandler.DustAction.EXECUTE);
                if (amountTransferred > 0) {
                    DustMagic toRemove = toTransfer.copy();
                    toRemove.setDustAmount(amountTransferred);
                    pedestal.removeDust(toRemove, IDustHandler.DustAction.EXECUTE);
                    return true;
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }
}