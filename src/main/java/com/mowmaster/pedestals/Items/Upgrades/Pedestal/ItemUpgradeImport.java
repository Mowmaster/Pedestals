package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.*;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.mowlib.Capabilities.Experience.IExperienceStorage;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

import com.mowmaster.pedestals.PedestalUtils.References;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.stream.IntStream;

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
        List<BlockPos> allPositions = hasRemoteStorage(coin) ? getValidWorkCardPositions(pedestal, coin, getWorkCardType(), MODID) : List.of(getPosOfBlockBelow(level, pedestalPos, 1));
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

            if(canTransferDust(pedestal.getCoinOnPedestal()))
            {
            /*if(entityIn instanceof ItemEntity)
            {
                ItemEntity itemEntity = ((ItemEntity) entityIn);
                ItemStack itemStack = itemEntity.getItem();
                if(itemStack.getItem() instanceof IDustStorage dustStorageItem)
                {
                    DustMagic dustInItem = getDustMagicInItem(itemStack);
                    //Can Accept Color of this Dust
                    if(pedestal.canAcceptDust(dustInItem))
                    {
                        int currentDust = dustInItem.getDustAmount();
                        int currentStackCount = itemStack.getCount();
                        int totalDustInStack = Math.multiplyExact(currentDust,currentStackCount);

                        int getSpaceForDust = pedestal.spaceForDust();
                        int transferRate = Math.min(totalDustInStack, (getSpaceForDust >= pedestal.getDustTransferRate())?(pedestal.getDustTransferRate()):(getSpaceForDust));
                        DustMagic dustToTransfer = new DustMagic(dustInItem.getDustColor(),transferRate);

                        //Can Accept Amount (and Color)
                        if(pedestal.addDust(dustToTransfer, IDustHandler.DustAction.SIMULATE) > 0)
                        {
                            //Actual Insert
                            pedestal.addDust(dustToTransfer, IDustHandler.DustAction.EXECUTE);

                            //Now do dust removal stuff

                            //Requires at least 1 item consumed and maybe part of another
                            if(transferRate >= currentDust)
                            {
                                int remainder = transferRate%currentDust;
                                int quotient = Math.floorDiv(transferRate, currentDust);
                                if(remainder == 0)
                                {
                                    //Removed quotient value of items
                                    if(quotient > 1)
                                    {
                                        itemStack.shrink(quotient);
                                    }
                                }
                                else
                                {
                                    //Removed quotient+1 value of items and return 1 modified item
                                    if(quotient > 1)
                                    {
                                        itemStack.shrink(quotient+1);
                                        //Need to then return 1 item to the player with the difference in amount.
                                        ItemStack remainderItem = new ItemStack(itemStack.getItem(),1,itemStack.getTag());
                                        int modifiedDustAmount = transferRate - Math.multiplyExact(quotient, currentDust);
                                        setDustMagicInItem(remainderItem,new DustMagic(dustToTransfer.getDustColor(), modifiedDustAmount));
                                        MowLibItemUtils.spawnItemStack(level,itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),remainderItem);
                                    }
                                    //For itemstacks == 1
                                    else if(quotient == 1)
                                    {

                                    }
                                }
                            }


                            *//*if(dustToTransfer.getDustAmount() >= getDustMagicInItemStack.getDustAmount() && dustStorageItem.consumedOnEmpty(itemStack))
                            {
                                itemEntity.remove(Entity.RemovalReason.DISCARDED);
                            }
                            else
                            {
                                if((getDustMagicInItemStack.getDustAmount() - dustToTransfer.getDustAmount())>0)
                                {
                                    if(itemStack.getCount() > 1)
                                    {
                                        itemStack.shrink(dustToTransfer.getDustAmount());
                                    }
                                    setDustMagicInStack(itemStack,new DustMagic(dustToTransfer.getDustColor(), getDustMagicInItemStack.getDustAmount() - dustToTransfer.getDustAmount()));
                                }
                                else
                                {
                                    if(itemStack.getCount() > 1)
                                    {
                                        itemStack.shrink(dustToTransfer.getDustAmount());
                                    }
                                    setDustMagicInStack(itemStack,DustMagic.EMPTY);
                                }

                                itemEntity.setItem(itemStack);
                            }*//*
                        }
                    }
                }
            }*/
            /*else if (entityIn instanceof Player) {
                Player player = ((Player) entityIn);
                if(!player.isShiftKeyDown())
                {
                    //.filter(itemStack -> passesDustFilter(pedestal,itemStack))
                    ItemStack dustItemStack = IntStream.range(0,(player.getInventory().items.size()))//Int Range
                            .mapToObj((player.getInventory().items)::get)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .filter(itemStack -> itemStack.getItem() instanceof IDustStorage)
                            .filter(itemStack -> !getDustMagicInItemStack(itemStack).isEmpty())
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!dustItemStack.isEmpty())
                    {
                        IDustStorage dustItem = ((IDustStorage)dustItemStack.getItem());
                        DustMagic getDustMagicInItemStack = getDustMagicInItemStack(dustItemStack);
                        if(pedestal.canAcceptDust(getDustMagicInItemStack))
                        {
                            int containerCurrentDust = getDustMagicInItemStack.getDustAmount();
                            int getSpaceForDust = pedestal.spaceForDust();
                            int transferRate = Math.min(totalDustInStack, (getSpaceForDust >= pedestal.getDustTransferRate())?(pedestal.getDustTransferRate()):(getSpaceForDust));

                            DustMagic dustToTransfer = new DustMagic(getDustMagicInItemStack.getDustColor(),transferRate);
                            //transferRate at this point is equal to what we can send.
                            if(pedestal.addDust(dustToTransfer, IDustHandler.DustAction.SIMULATE) > 0)
                            {
                                int slot = player.getInventory().findSlotMatchingItem(dustItemStack);
                                pedestal.addDust(dustToTransfer, IDustHandler.DustAction.EXECUTE);
                                //remove dust from item and maybe remove it from inventory
                                boolean remover = dustItem.consumedOnEmpty(dustItemStack);
                                if((transferRate >= containerCurrentDust) && remover)
                                {
                                    if(!player.isCreative())player.getInventory().getItem(slot).shrink(dustItemStack.getCount());
                                }
                                else
                                {
                                    if(!player.isCreative())
                                    {
                                        int difference = getDustMagicInItemStack.getDustAmount() - dustToTransfer.getDustAmount();
                                        if(difference>0)
                                        {
                                            if(remover && dustItemStack.getCount()>1)player.getInventory().getItem(slot).shrink(dustToTransfer.getDustAmount());
                                            setDustMagicInStack(dustItemStack,new DustMagic(dustToTransfer.getDustColor(), difference));
                                        }
                                        else
                                        {
                                            if(remover)player.getInventory().getItem(slot).shrink(dustItemStack.getCount());
                                            else setDustMagicInStack(dustItemStack,DustMagic.EMPTY);
                                        }
                                    }
                                }

                                String dustString = MowLibColorReference.getColorName(dustToTransfer.getDustColor()) +": " +pedestal.getStoredDust().getDustAmount() +"/"+pedestal.getDustCapacity();
                                MowLibMessageUtils.messagePopupText(player,ChatFormatting.LIGHT_PURPLE,dustString);
                            }
                        }
                    }
                }
            }*/
            }

            if(canTransferFluids(pedestal.getCoinOnPedestal()))
            {
                if(entityIn instanceof ItemEntity)
                {
                    ItemEntity itemEntity = ((ItemEntity) entityIn);
                    ItemStack itemStack = itemEntity.getItem();
                    if(!itemStack.getItem().equals(Items.BUCKET) && itemStack.getItem() instanceof BucketItem bucket && passesFluidFilter(pedestal,getFluidStackFromItemStack(itemStack)))
                    {
                        Fluid bucketFluid = bucket.getFluid();
                        FluidStack fluidInTank = new FluidStack(bucketFluid,1000);
                        int fluidSpaceInPedestal = pedestal.spaceForFluid();

                        FluidStack fluidInPedestal = pedestal.getStoredFluid();
                        if(fluidInPedestal.isEmpty() || fluidInPedestal.isFluidEqual(fluidInTank))
                        {
                            int transferRate = 1000;
                            if(fluidSpaceInPedestal >= transferRate || pedestal.getStoredFluid().isEmpty())
                            {
                                FluidStack fluidDrained = fluidInTank.copy();
                                if(!fluidInTank.isEmpty())
                                {
                                    pedestal.addFluid(fluidDrained,IFluidHandler.FluidAction.EXECUTE);
                                    itemEntity.setItem(new ItemStack(Items.BUCKET,1));
                                }
                            }
                        }
                    }
                }
                else if (entityIn instanceof Player player) {
                    if(!player.isShiftKeyDown())
                    {
                        ItemStack bucketItemStack = IntStream.range(0,(player.getInventory().items.size()))//Int Range
                                .mapToObj((player.getInventory().items)::get)//Function being applied to each interval
                                .filter(itemStack -> !itemStack.isEmpty())
                                .filter(itemStack -> !itemStack.getItem().equals(Items.BUCKET))
                                .filter(itemStack -> itemStack.getItem() instanceof BucketItem)
                                .filter(itemStack -> passesFluidFilter(pedestal,getFluidStackFromItemStack(itemStack)))
                                .findFirst().orElse(ItemStack.EMPTY);

                        if(!bucketItemStack.isEmpty())
                        {
                            BucketItem bucket = ((BucketItem)bucketItemStack.getItem());
                            Fluid bucketFluid = bucket.getFluid();
                            FluidStack fluidInTank = new FluidStack(bucketFluid,1000);
                            int fluidSpaceInPedestal = pedestal.spaceForFluid();

                            FluidStack fluidInPedestal = pedestal.getStoredFluid();
                            if(fluidInPedestal.isEmpty() || fluidInPedestal.isFluidEqual(fluidInTank))
                            {
                                int transferRate = 1000;
                                if(fluidSpaceInPedestal >= transferRate || pedestal.getStoredFluid().isEmpty())
                                {
                                    FluidStack fluidDrained = fluidInTank.copy();
                                    if(!fluidInTank.isEmpty())
                                    {
                                        pedestal.addFluid(fluidDrained,IFluidHandler.FluidAction.EXECUTE);
                                        int slot = player.getInventory().findSlotMatchingItem(bucketItemStack);
                                        if(!player.isCreative())player.getInventory().getItem(slot).shrink(1);
                                        if(!player.isCreative())ItemHandlerHelper.giveItemToPlayer(player,new ItemStack(Items.BUCKET,1));

                                        String fluid = pedestal.getStoredFluid().getDisplayName().getString() +": " +pedestal.getStoredFluid().getAmount() +"/"+pedestal.getFluidCapacity();
                                        MowLibMessageUtils.messagePopupText(player,ChatFormatting.WHITE,fluid);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(canTransferItems(pedestal.getCoinOnPedestal()))
            {
                if(entityIn instanceof ItemEntity itemEntity)
                {
                    ItemStack itemStack = itemEntity.getItem();
                    ItemStack stackInPedestal = pedestal.getItemInPedestal();
                    boolean stacksMatch = doItemsMatch(stackInPedestal,itemStack);
                    if((!pedestal.hasItem() || stacksMatch) && passesItemFilter(pedestal,itemStack))
                    {
                        int spaceInPed = stackInPedestal.getMaxStackSize()-stackInPedestal.getCount();
                        int filterAllowedSpace = getCountItemFilter(pedestal,itemStack);
                        int actualSpaceInPed = (filterAllowedSpace>spaceInPed)?(spaceInPed):(filterAllowedSpace);
                        if(actualSpaceInPed>0)
                        {
                            int itemInCount = itemStack.getCount();
                            int countToAdd = ( itemInCount<= actualSpaceInPed)?(itemInCount):(actualSpaceInPed);
                            ItemStack stackToAdd = itemStack.copy();
                            stackToAdd.setCount(countToAdd);
                            if(pedestal.addItem(stackToAdd,true))
                            {
                                itemEntity.getItem().setCount(itemInCount-countToAdd);
                                if(itemInCount<=countToAdd)itemEntity.remove(Entity.RemovalReason.DISCARDED);
                                pedestal.addItem(stackToAdd,false);
                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ(),180,180,180));
                            }
                        }
                    }
                }
                else if (entityIn instanceof Player player)
                {
                    if(!player.isShiftKeyDown())
                    {
                        ItemStack itemFromInv = ItemStack.EMPTY;

                        itemFromInv = IntStream.range(0,(player.getInventory().items.size()))//Int Range
                                .mapToObj((player.getInventory().items)::get)//Function being applied to each interval
                                .filter(itemStack -> !itemStack.isEmpty())
                                .filter(itemStack -> passesItemFilter(pedestal,itemStack))
                                .findFirst().orElse(ItemStack.EMPTY);

                        if(!itemFromInv.isEmpty())
                        {
                            ItemStack itemStack = itemFromInv;
                            ItemStack stackInPedestal = pedestal.getItemInPedestal();
                            boolean stacksMatch = doItemsMatch(stackInPedestal,itemStack);
                            if((!pedestal.hasItem() || stacksMatch) && passesItemFilter(pedestal,itemStack))
                            {
                                int spaceInPed = stackInPedestal.getMaxStackSize()-stackInPedestal.getCount();
                                int filterAllowedSpace = getCountItemFilter(pedestal,itemStack);
                                int actualSpaceInPed = (filterAllowedSpace>spaceInPed)?(spaceInPed):(filterAllowedSpace);
                                if(actualSpaceInPed>0)
                                {
                                    int itemInCount = itemStack.getCount();
                                    int countToAdd = ( itemInCount<= actualSpaceInPed)?(itemInCount):(actualSpaceInPed);
                                    ItemStack stackToAdd = itemStack.copy();
                                    stackToAdd.setCount(countToAdd);
                                    if(pedestal.addItem(stackToAdd,true))
                                    {
                                        ItemStack newStackInPlayer = (itemInCount>countToAdd)?(itemStack.copy()):(ItemStack.EMPTY);
                                        if(!newStackInPlayer.isEmpty())newStackInPlayer.setCount(itemInCount-countToAdd);
                                        int slot = player.getInventory().findSlotMatchingItem(itemStack);
                                        player.getInventory().setItem(slot,newStackInPlayer);
                                        pedestal.addItem(stackToAdd,false);
                                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ(),180,180,0));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
