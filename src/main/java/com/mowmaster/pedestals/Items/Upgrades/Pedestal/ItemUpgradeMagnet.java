package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardArea;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardBase;
import com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibXpUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class ItemUpgradeMagnet extends ItemUpgradeBase implements IHasModeTypes
{
    public ItemUpgradeMagnet(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifySuperSpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyXPCapacity(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyEnergyCapacity(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyRange(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyArea(ItemStack upgradeItemStack) {
        return PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get();
    }

    @Override
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 1; }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_magnet_baseEnergyCost.get(); }
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_magnet_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_magnet_baseXpCost.get(); }
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_magnet_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_magnet_dustColor.get(),PedestalConfig.COMMON.upgrade_magnet_baseDustAmount.get()); }
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_magnet_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_magnet_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_magnet_selectedMultiplier.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {

        List<String> messages = super.getUpgradeHUD(pedestal);

        if(messages.size()<=0)
        {
            if(baseEnergyCostPerDistance()>0)
            {
                if(pedestal.getStoredEnergy()<baseEnergyCostPerDistance())
                {
                    messages.add(ChatFormatting.RED + "Needs Energy");
                    messages.add(ChatFormatting.RED + "To Operate");
                }
            }
            if(baseXpCostPerDistance()>0)
            {
                if(pedestal.getStoredExperience()<baseXpCostPerDistance())
                {
                    messages.add(ChatFormatting.GREEN + "Needs Experience");
                    messages.add(ChatFormatting.GREEN + "To Operate");
                }
            }
            if(baseDustCostPerDistance().getDustAmount()>0)
            {
                if(pedestal.getStoredEnergy()<baseEnergyCostPerDistance())
                {
                    messages.add(ChatFormatting.LIGHT_PURPLE + "Needs Dust");
                    messages.add(ChatFormatting.LIGHT_PURPLE + "To Operate");
                }
            }
        }

        return messages;
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {

        if(pedestal.hasWorkCard())
        {
            ItemStack card = pedestal.getWorkCardInPedestal();
            if(card.getItem() instanceof WorkCardBase workCardBase)
            {
                if(workCardBase.hasTwoPointsSelected(card))
                {
                    if(MowLibBlockPosUtils.selectedAreaWithinRange(pedestal,getUpgradeWorkRange(coin)))
                    {
                        magnetAction(pedestal, level,pedestal.getPos(),pedestal.getCoinOnPedestal());
                    }
                }
            }
        }
    }

    public void magnetAction(BasePedestalBlockEntity pedestal, Level world, BlockPos posOfPedestal, ItemStack coinInPedestal) {
        ItemStack workCardItemStack = pedestal.getWorkCardInPedestal();
        if (workCardItemStack.getItem() instanceof WorkCardArea) {
            WorkCardArea.getAABBIfDefinedAndInRange(workCardItemStack, pedestal, getUpgradeWorkRange(coinInPedestal)).ifPresent(aabb -> {
                AABB expandedAABB = aabb.expandTowards(1.0D, 1.0D, 1.0D);
                boolean needsEnergy = requiresFuelForUpgradeAction();
                boolean actionDone = false;

                List<ItemEntity> list = world.getEntitiesOfClass(ItemEntity.class, expandedAABB);
                for (ItemEntity item : list)
                {
                    if(needsEnergy) { if(!removeFuelForAction(pedestal,getDistanceBetweenPoints(posOfPedestal,item.getOnPos()),true))break; }

                    ItemStack itemStack = item.getItem();
                    //Fluid
                    if(canTransferFluids(coinInPedestal))
                    {
                        if(!itemStack.getItem().equals(Items.BUCKET) && itemStack.getItem() instanceof BucketItem bucket)
                        {
                            Fluid bucketFluid = bucket.getFluid();
                            FluidStack fluidInTank = new FluidStack(bucketFluid,1000);
                            if(passesFluidFilter(pedestal,fluidInTank))
                            {
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
                                            pedestal.addFluid(fluidDrained, IFluidHandler.FluidAction.EXECUTE);
                                            //Turn bucket back into an empty one
                                            itemStack = new ItemStack(Items.BUCKET,1);
                                            item.setItem(itemStack);
                                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,item.getX(),item.getY(),item.getZ(),0,0,180));                                    actionDone = true;
                                            actionDone = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //Energy
                    if(canTransferEnergy(coinInPedestal))
                    {
                        int getMaxEnergyValue = pedestal.getEnergyCapacity();
                        LazyOptional<IEnergyStorage> cap = itemStack.getCapability(ForgeCapabilities.ENERGY);
                        if(cap.isPresent())
                        {
                            IEnergyStorage handler = cap.orElse(null);

                            if(handler != null)
                            {
                                if(handler.canExtract())
                                {
                                    int containerCurrentEnergy = handler.getEnergyStored();
                                    int getMaxEnergy = getMaxEnergyValue;
                                    int getCurrentEnergy = pedestal.getStoredEnergy();
                                    int getSpaceForEnergy = getMaxEnergy - getCurrentEnergy;
                                    int baseRate = PedestalConfig.COMMON.upgrade_import_baseEnergyTransferSpeed.get();
                                    int maxRate = baseRate + getEnergyCapacityIncrease(pedestal.getCoinOnPedestal());
                                    int transferRate = (getSpaceForEnergy >= maxRate)?(maxRate):(getSpaceForEnergy);
                                    if (containerCurrentEnergy < transferRate) {transferRate = containerCurrentEnergy;}

                                    //transferRate at this point is equal to what we can send.
                                    if(handler.extractEnergy(transferRate,true) > 0)
                                    {
                                        pedestal.addEnergy(transferRate,false);
                                        handler.extractEnergy(transferRate,false);
                                        actionDone = true;
                                    }
                                }
                            }
                        }
                    }
                    //XP
                    if(canTransferXP(coinInPedestal))
                    {
                        //maybe also get xp custom items???
                        if(itemStack.getItem().equals(Items.EXPERIENCE_BOTTLE))
                        {
                            int currentlyStoredExp = pedestal.getStoredExperience();
                            if(currentlyStoredExp < pedestal.getExperienceCapacity())
                            {
                                //set value per bottle from 3 to 11?
                                Random r = new Random();
                                int low = 3;
                                int high = 11;
                                int result = r.nextInt(high-low) + low;
                                int value = result * itemStack.getCount();
                                if(value > 0 && pedestal.addExperience(value,true)>= value)
                                {
                                    pedestal.addExperience(value,false);
                                    itemStack = new ItemStack(Items.GLASS_BOTTLE,itemStack.getCount());
                                    item.setItem(itemStack);
                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,item.getX(),item.getY(),item.getZ(),0,180,0));
                                    actionDone = true;
                                }
                            }
                        }
                    }
                    //Dust
                    /*if(canTransferDust(coinInPedestal))
                    {
                        //get custom dust items only, and maybe jars???
                    }*/

                    //Handle items last so that the others can do their action first
                    //Item
                    if(canTransferItems(coinInPedestal))
                    {
                        // if(canTransferDust(coinInPedestal)) then dont pick up dust items???

                        ItemStack stackInPedestal = pedestal.getItemInPedestal();
                        boolean stacksMatch = doItemsMatch(stackInPedestal,itemStack);
                        if((pedestal.addItem(itemStack,true)) && passesItemFilter(pedestal,itemStack))
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
                                    item.getItem().setCount(itemInCount-countToAdd);
                                    if(itemInCount<=countToAdd)item.remove(Entity.RemovalReason.DISCARDED);
                                    pedestal.addItem(stackToAdd,false);
                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,item.getX(),item.getY(),item.getZ(),180,180,0));
                                    actionDone = true;
                                }
                            }
                        }
                    }

                    if(needsEnergy && actionDone)removeFuelForAction(pedestal,getDistanceBetweenPoints(posOfPedestal,item.getOnPos()),false);
                    if(!hasAdvancedOne(coinInPedestal) && actionDone)break;
                }

                List<ExperienceOrb> listXP = world.getEntitiesOfClass(ExperienceOrb.class, expandedAABB);
                for (ExperienceOrb orb : listXP)
                {
                    if(needsEnergy) { if(!removeFuelForAction(pedestal,getDistanceBetweenPoints(posOfPedestal,orb.getOnPos()),true))break; }

                    if(canTransferXP(coinInPedestal))
                    {
                        int currentlyStoredExp = pedestal.getStoredExperience();
                        if(currentlyStoredExp < pedestal.getExperienceCapacity())
                        {
                            //set value per bottle from 3 to 11?
                            Random r = new Random();
                            int low = 3;
                            int high = 11;
                            int result = r.nextInt(high-low) + low;
                            int value = result * orb.getValue();
                            if(value > 0 && pedestal.addExperience(value,true)>= value)
                            {
                                int added = pedestal.addExperience(value,false);
                                orb.value = value-added;
                                if(value<=added)orb.remove(Entity.RemovalReason.DISCARDED);
                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ(),0,255,0));
                                if(needsEnergy && !actionDone)removeFuelForAction(pedestal,getDistanceBetweenPoints(posOfPedestal,orb.getOnPos()),false);
                                if(!hasAdvancedOne(coinInPedestal))break;
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onCollideAction(BasePedestalBlockEntity pedestal) {

        List<Entity> entitiesColliding = pedestal.getLevel().getEntitiesOfClass(Entity.class,new AABB(pedestal.getPos()));
        for(Entity entityIn : entitiesColliding)
        {
            if(canTransferItems(pedestal.getCoinOnPedestal()))
            {
                if(entityIn instanceof ItemEntity)
                {
                    ItemEntity itemEntity = ((ItemEntity) entityIn);
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
                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ(),180,180,180));
                            }
                        }
                    }
                }
                else if (entityIn instanceof Player)
                {
                    Player player = ((Player) entityIn);
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
                                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ(),180,180,0));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if(canTransferXP(pedestal.getCoinOnPedestal()) && pedestal.canAcceptExperience())
            {
                if (entityIn instanceof Player) {
                    Player player = ((Player) entityIn);
                    if(!player.isShiftKeyDown())
                    {
                        int currentlyStoredExp = pedestal.getStoredExperience();
                        if(currentlyStoredExp < pedestal.getExperienceCapacity())
                        {
                            int baseRate = PedestalConfig.COMMON.pedestal_baseXpTransferRate.get();
                            int transferRate = baseRate + MowLibXpUtils.getExpCountByLevel(getXPCapacityIncrease(pedestal.getCoinOnPedestal()));
                            int value = MowLibXpUtils.removeXp(player, transferRate);
                            if(value > 0)
                            {
                                pedestal.addExperience(value,false);
                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ(),0,255,0));
                            }
                        }
                    }
                }
            }

            if(canTransferFluids(pedestal.getCoinOnPedestal()))
            {
                if (entityIn instanceof Player) {
                    Player player = ((Player) entityIn);
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
                            FluidStack fluidInTank = getFluidStackFromItemStack(bucketItemStack);
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
                                        pedestal.addFluid(fluidDrained, IFluidHandler.FluidAction.EXECUTE);
                                        int slot = player.getInventory().findSlotMatchingItem(bucketItemStack);
                                        if(!player.isCreative())player.getInventory().getItem(slot).shrink(1);
                                        if(!player.isCreative()) ItemHandlerHelper.giveItemToPlayer(player,new ItemStack(Items.BUCKET,1));

                                        String fluid = pedestal.getStoredFluid().getDisplayName().getString() +": " +pedestal.getStoredFluid().getAmount() +"/"+pedestal.getFluidCapacity();
                                        MowLibMessageUtils.messagePopupText(player, ChatFormatting.WHITE,fluid);
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
