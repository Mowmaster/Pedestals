package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.*;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.mowlib.Capabilities.Experience.IExperienceStorage;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class ItemUpgradeImport extends ItemUpgradeBase implements IHasModeTypes {
    public ItemUpgradeImport(Properties p_41383_) {
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
    public boolean canModifyEntityContainers(ItemStack upgradeItemStack)  { return true; }

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
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        List<BlockPos> allPositions = hasRemoteStorage(coin) ? getValidWorkCardPositions(pedestal) : List.of(getPosOfBlockBelow(level, pedestalPos, 1));
        if (allPositions.isEmpty() || allPositions.size() > 8) return;

        Direction pedestalFacing = getPedestalFacing(level, pedestalPos);
        for (BlockPos position : allPositions) {
            if (level.getBlockEntity(position) instanceof BasePedestalBlockEntity) return;

            List<Boolean> importResults = List.of(
                importItemAction(level, position, pedestal, pedestalFacing, coin),
                importFluidAction(level, position, pedestal, pedestalFacing, coin),
                importEnergyAction(level, position, pedestal, pedestalFacing, coin),
                importXPAction(level, position, pedestal, pedestalFacing, coin),
                importDustAction(level, position, pedestal, pedestalFacing, coin)
            );

            if (importResults.contains(true)) {
                break;
            }
        }
    }

    private boolean importItemAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferItems(upgradeItemStack)) {
            int supportedTransferRate = PedestalConfig.COMMON.upgrade_import_baseItemTransferSpeed.get() + getItemCapacityIncrease(upgradeItemStack);

            LazyOptional<IItemHandler> cap;
            if (hasEntityContainer(upgradeItemStack)) {
                cap = findItemHandlerAtPosEntity(level, position, pedestalFacing, true);
            } else {
                cap = MowLibItemUtils.findItemHandlerAtPos(level, position, pedestalFacing, true);
            }
            return cap.map(handler ->
                getFirstSlotWithNonFilteredItems(pedestal, handler).map(slot -> {
                    ItemStack stackInHandler = handler.getStackInSlot(slot);
                    if (!stackInHandler.isEmpty()) {
                        ItemStack toTransfer = stackInHandler.copy();
                        if (toTransfer.getCount() > supportedTransferRate) {
                            toTransfer.setCount(supportedTransferRate);
                        }
                        ItemStack simulateRemainder = pedestal.addItemStack(toTransfer.copy(), true);
                        if (simulateRemainder.getCount() != toTransfer.getCount()) {
                            ItemStack actualTransferred = handler.extractItem(slot, toTransfer.getCount() - simulateRemainder.getCount(), false);
                            if (!actualTransferred.isEmpty()) {
                                pedestal.addItemStack(actualTransferred, false);
                                return true;
                            }
                        }
                    }
                    return false;
                }).orElse(false)
            ).orElse(false);
        } else {
            return false;
        }
    }

    private boolean importFluidAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferFluids(upgradeItemStack)) {
            int supportedTransferRate = PedestalConfig.COMMON.upgrade_import_baseFluidTransferSpeed.get() + getFluidCapacityIncrease(upgradeItemStack);

            LazyOptional<IFluidHandler> cap;
            if (hasEntityContainer(upgradeItemStack)) {
                cap = findFluidHandlerAtPosEntity(level, position, pedestalFacing, true);
            } else {
                cap = MowLibFluidUtils.findFluidHandlerAtPos(level, position, pedestalFacing, true);
            }
            return cap.map(handler -> {
                int maxToTransfer = Math.min(supportedTransferRate, pedestal.spaceForFluid());
                FluidStack actualToTransfer = handler.drain(maxToTransfer, IFluidHandler.FluidAction.SIMULATE);
                if (!actualToTransfer.isEmpty()) {
                    int actualTransferred = pedestal.addFluid(actualToTransfer.copy(), IFluidHandler.FluidAction.EXECUTE);
                    if (actualTransferred > 0) {
                        FluidStack toRemoveStack = actualToTransfer.copy();
                        toRemoveStack.setAmount(actualTransferred);
                        handler.drain(toRemoveStack, IFluidHandler.FluidAction.EXECUTE);
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    private boolean importEnergyAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferEnergy(upgradeItemStack)) {
            int supportedTransferRate = PedestalConfig.COMMON.upgrade_import_baseEnergyTransferSpeed.get() + getEnergyCapacityIncrease(upgradeItemStack);

            LazyOptional<IEnergyStorage> cap;
            if (hasEntityContainer(upgradeItemStack)) {
                cap = findEnergyHandlerAtPosEntity(level, position, pedestalFacing, true);
            } else {
                cap = MowLibEnergyUtils.findEnergyHandlerAtPos(level, position, pedestalFacing, true);
            }
            return cap.map(handler -> {
                int maxToTransfer = Math.min(supportedTransferRate, pedestal.spaceForEnergy());
                int actualToTransfer = handler.extractEnergy(maxToTransfer, true);
                if (actualToTransfer > 0) {
                    int actualTransferred = pedestal.addEnergy(actualToTransfer, false);
                    if (actualTransferred > 0) {
                        handler.extractEnergy(actualTransferred, false);
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    private boolean importXPAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferXP(upgradeItemStack)) {
            int supportedTransferRate = PedestalConfig.COMMON.upgrade_import_baseExpTransferSpeed.get() + MowLibXpUtils.getExpCountByLevel(getXPCapacityIncrease(upgradeItemStack));

            LazyOptional<IExperienceStorage> cap = MowLibXpUtils.findExperienceHandlerAtPos(level, position, pedestalFacing, true);
            return cap.map(handler -> {
                int maxToTransfer = Math.min(supportedTransferRate, pedestal.spaceForExperience());
                int actualToTransfer = handler.extractExperience(maxToTransfer, true);
                if (actualToTransfer > 0) {
                    int actualTransferred = pedestal.addExperience(actualToTransfer, false);
                    if (actualTransferred > 0) {
                        handler.extractExperience(actualTransferred, false);
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    private boolean importDustAction(Level level, BlockPos position, BasePedestalBlockEntity pedestal, Direction pedestalFacing, ItemStack upgradeItemStack) {
        if (canTransferDust(upgradeItemStack)) {
            int supportedTransferRate = PedestalConfig.COMMON.upgrade_import_baseDustTransferSpeed.get() + getDustCapacityIncrease(upgradeItemStack);

            LazyOptional<IDustHandler> cap = MowLibDustUtils.findDustHandlerAtPos(level, position, pedestalFacing);
            return cap.map(handler -> {
                int maxToTransfer = Math.min(supportedTransferRate, pedestal.spaceForDust());
                DustMagic actualToTransfer = handler.drain(maxToTransfer, IDustHandler.DustAction.SIMULATE);
                if (actualToTransfer.getDustAmount() > 0) {
                    int actualTransferred = pedestal.addDust(actualToTransfer.copy(), IDustHandler.DustAction.EXECUTE);
                    if (actualTransferred > 0) {
                        DustMagic toRemove = actualToTransfer.copy();
                        toRemove.setDustAmount(actualTransferred);
                        handler.drain(toRemove, IDustHandler.DustAction.EXECUTE);
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    @Override
    public void onCollideAction(BasePedestalBlockEntity pedestal) {
        Level level = pedestal.getLevel();
        if (level == null) return;
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack upgradeItemStack = pedestal.getCoinOnPedestal();

        for (Entity entityIn : level.getEntitiesOfClass(Entity.class, new AABB(pedestalPos))) {
            if (canTransferXP(upgradeItemStack) && pedestal.canAcceptExperience()) {
                if (entityIn instanceof Player player) {
                    if (!player.isShiftKeyDown()) {
                        int supportedTransferRate = PedestalConfig.COMMON.upgrade_import_baseExpTransferSpeed.get() + MowLibXpUtils.getExpCountByLevel(getXPCapacityIncrease(upgradeItemStack));
                        int maxToTransfer = Math.min(supportedTransferRate, pedestal.spaceForExperience());
                        if (maxToTransfer > 0) {
                            int actualTransferred = MowLibXpUtils.removeXp(player, maxToTransfer);
                            if (actualTransferred > 0) {
                                pedestal.addExperience(actualTransferred,false);
                                if (pedestal.canSpawnParticles()) {
                                    MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), 0, 255 ,0));
                                }
                            }
                        }
                    }
                } else if (entityIn instanceof ExperienceOrb xpOrb) {
                    int value = xpOrb.getValue();
                    if (pedestal.spaceForExperience() > value) {
                        pedestal.addExperience(value, false);
                        xpOrb.remove(Entity.RemovalReason.DISCARDED);
                        if (pedestal.canSpawnParticles()) {
                            MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), 0, 255 ,0));
                        }
                    }
                }
            }

            if (canTransferFluids(upgradeItemStack) && pedestal.spaceForFluid() > 1000) {
                if (entityIn instanceof ItemEntity itemEntity) {
                    if (itemEntity.getItem().getItem() instanceof BucketItem bucket && bucket.getFluid() != Fluids.EMPTY) {
                        FluidStack fluidStack = new FluidStack(bucket.getFluid(), 1000);
                        int filled = pedestal.addFluid(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                        if (filled > 0) {
                            itemEntity.setItem(new ItemStack(Items.BUCKET, 1));
                        }
                    }
                } else if (entityIn instanceof Player player) {
                    if (!player.isShiftKeyDown()) {
                        player.getInventory().items.stream()
                            .filter(itemStack -> itemStack.getItem() instanceof BucketItem bucket && bucket.getFluid() != Fluids.EMPTY && passesFluidFilter(pedestal, new FluidStack(bucket.getFluid(), 1000)))
                            .findFirst()
                            .ifPresent(inventoryItemStack -> {
                                if (inventoryItemStack.getItem() instanceof BucketItem bucket) {
                                    FluidStack fluidInTank = new FluidStack(bucket.getFluid(),1000);
                                    if (pedestal.addFluid(fluidInTank, IFluidHandler.FluidAction.EXECUTE) > 0) {
                                        inventoryItemStack.shrink(1);
                                        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.BUCKET,1));
                                        String fluid = pedestal.getStoredFluid().getDisplayName().getString() + ": " + pedestal.getStoredFluid().getAmount() + "/" + pedestal.getFluidCapacity();
                                        MowLibMessageUtils.messagePopupText(player,ChatFormatting.WHITE, fluid);
                                    }
                                }
                            });
                    }
                }
            }

            if (canTransferItems(upgradeItemStack)) {
                if (entityIn instanceof ItemEntity itemEntity) {
                    ItemStack stackToAdd = itemEntity.getItem();
                    ItemStack remainder = pedestal.addItemStack(stackToAdd.copy(), false);
                    if (remainder.getCount() != stackToAdd.getCount()) {
                        if (remainder.isEmpty()) {
                            itemEntity.remove(Entity.RemovalReason.DISCARDED);
                        } else {
                            itemEntity.getItem().setCount(remainder.getCount());
                        }
                        if (pedestal.canSpawnParticles()) {
                            MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), 180, 180, 180));
                        }
                    }
                } else if (entityIn instanceof Player player) {
                    if (!player.isShiftKeyDown()) {
                        player.getInventory().items.stream()
                            .filter(itemStack -> !itemStack.isEmpty())
                            .filter(itemStack -> passesItemFilter(pedestal, itemStack))
                            .findFirst()
                            .ifPresent(inventoryItemStack -> {
                                ItemStack remainder = pedestal.addItemStack(inventoryItemStack, false);
                                if (remainder.getCount() != inventoryItemStack.getCount()) {
                                    inventoryItemStack.shrink(inventoryItemStack.getCount() - remainder.getCount());
                                    if (pedestal.canSpawnParticles()) {
                                        MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), 180, 180, 0));
                                    }
                                }
                            });
                    }
                }
            }
        }
    }
}
