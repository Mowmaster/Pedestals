package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.*;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import com.mowmaster.pedestals.Items.ISelectableArea;
import com.mowmaster.pedestals.Items.ISelectablePoints;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardBase;
import com.mowmaster.pedestals.PedestalTab.PedestalsTab;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;

import static com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock.FACING;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ItemUpgradeBase extends Item implements IPedestalUpgrade
{
    public ItemUpgradeBase(Properties p_41383_) {
        super(new Properties().tab(PedestalsTab.TAB_ITEMS));
    }

    /*
    *
    * Methods Runs By Pedestal
    * START
    *
     */
    @Override
    public int getComparatorRedstoneLevel(Level worldIn, BlockPos pos) {
        return -1;
    }

    @Override
    public void updateAction(Level level, BasePedestalBlockEntity pedestal) {
        if(!pedestal.isPedestalBlockPowered(pedestal))
        {
            int speed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get() - getSpeedTicksReduced(pedestal.getCoinOnPedestal());
            //Make sure speed has at least a value of 1
            if(speed<=0)speed = 1;
            if(level.getGameTime()%speed == 0 )
            {
                upgradeAction(level, pedestal,pedestal.getPos(),pedestal.getCoinOnPedestal());
            }
        }
    }

    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {

    }

    @Override
    public void actionOnCollideWithBlock(BasePedestalBlockEntity pedestal) {
        if(!pedestal.isPedestalBlockPowered(pedestal))
        {
            int speed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get() - getSpeedTicksReduced(pedestal.getCoinOnPedestal());
            //Make sure speed has at least a value of 1
            if(speed<=0)speed = 1;
            if(pedestal.getLevel().getGameTime()%speed == 0 )
            {
                onCollideAction(pedestal);
            }
        }
    }


    public void onCollideAction(BasePedestalBlockEntity pedestal) {

    }

    @Override
    public void actionOnNeighborBelowChange(BasePedestalBlockEntity pedestal, BlockPos belowBlock) {

    }

    @Override
    public void actionOnAddedToPedestal(Player player, BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        MowLibOwnerUtils.writeNameToStackNBT(coinInPedestal,player.getName().getString());
        MowLibOwnerUtils.writeUUIDToNBT(coinInPedestal, player.getUUID());
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        MowLibOwnerUtils.removePlayerFromStack(coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(), "_string_last_clicked_direction");
    }

    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal)
    {
        List<String> messages = new ArrayList<>();
        if(getWorkCardType()<0)return messages;

        if(needsWorkCard() && !pedestal.hasWorkCard())
        {
            messages.add(ChatFormatting.WHITE + "Needs");
            messages.add(ChatFormatting.WHITE + "----------------");
            if(getWorkCardType() == 0)
            {
                messages.add(ChatFormatting.RED + "Work Area");
                messages.add(ChatFormatting.WHITE + "OR");
                messages.add(ChatFormatting.RED + "Work Locations");
            }
            if(getWorkCardType() == 1)
            {
                messages.add(ChatFormatting.RED + "Work Area");
            }
            if(getWorkCardType() == 2)
            {
                messages.add(ChatFormatting.RED + "Work Locations");
            }
            if(getWorkCardType() == 3)
            {
                messages.add(ChatFormatting.RED + "Pedestal Locations");
            }
        }
        else if(pedestal.hasWorkCard())
        {
            if(pedestal.getWorkCardInPedestal().getItem() instanceof WorkCardBase workCardBase)
            {
                Boolean inSelectedInRange = workCardBase.selectedAreaWithinRange(pedestal);
                boolean addmessages = false;
                if(getWorkCardType()!=workCardBase.getWorkCardType())
                {
                    addmessages=true;
                    if(getWorkCardType()==0 && (workCardBase.getWorkCardType()==1 || workCardBase.getWorkCardType()==2))
                    {
                        addmessages=false;
                    }

                    if(addmessages)
                    {
                        messages.add(ChatFormatting.RED + "Incorrect Card");
                        messages.add(ChatFormatting.WHITE + "Needs:");
                        if(getWorkCardType() != 0){messages.add(ChatFormatting.WHITE + "----------------");}

                        if(getWorkCardType() == 0)
                        {
                            messages.add(ChatFormatting.BLUE + "Work Area");
                            messages.add(ChatFormatting.WHITE + "OR");
                            messages.add(ChatFormatting.BLUE + "Work Locations");
                        }
                        if(getWorkCardType() == 1 )
                        {
                            messages.add(ChatFormatting.BLUE + "Work Area");
                        }
                        if(getWorkCardType() == 2)
                        {
                            messages.add(ChatFormatting.BLUE + "Work Locations");
                        }
                        if(getWorkCardType() == 3)
                        {
                            messages.add(ChatFormatting.BLUE + "Pedestal Locations");
                        }
                    }
                }
                else if(!inSelectedInRange && pedestal.getWorkCardInPedestal().getItem().equals(DeferredRegisterItems.WORKCARD_AREA.get()))
                {
                    messages.add(ChatFormatting.RED + "Work Selection");
                    messages.add(ChatFormatting.RED + "Is Invalid");
                }
            }
        }

        return messages;
    }

    /*
     *
     * Methods Runs By Pedestal
     * END
     *
     */


    public boolean needsWorkCard()
    {
        return false;
    }

    public int getWorkCardType()
    {
        //-1 = null
        //0 = Either
        //1 = Area
        //2 = Locations
        //3 = Pedestals
        return -1;
    }



    public int getUpgradeExperienceTransferRate(ItemStack upgradeStack)
    {
        int baseValue = PedestalConfig.COMMON.pedestal_baseXpTransferRate.get();
        int experienceTransferRateConverted = MowLibXpUtils.getExpCountByLevel(baseValue);
        int upgradeIncrease = getXPCapacityIncrease(upgradeStack);

        return  (upgradeIncrease>0)?(MowLibXpUtils.getExpCountByLevel(upgradeIncrease+baseValue)):(experienceTransferRateConverted);
    }

    public ItemStack getUpgradeDefaultTool()
    {
        return ItemStack.EMPTY;
    }

    //This is for things that have for loops, normally they break after each working loop,
    // but this would remove that break and allow it to process all in the for loop
    public boolean hasAdvancedOne(ItemStack upgradeStack)
    {
        if(canModifySuperSpeed(upgradeStack))
        {
            return getSuperSpeed(upgradeStack);
        }

        return false;
    }

    public boolean hasSuperSpeed(ItemStack upgradeStack)
    {
        if(canModifySuperSpeed(upgradeStack))
        {
            return getSuperSpeed(upgradeStack);
        }

        return false;
    }

    public boolean hasGentleHarvest(ItemStack upgradeStack)
    {
        if(canModifyGentleHarvest(upgradeStack))
        {
            return getGentleHarvest(upgradeStack);
        }

        return false;
    }

    public void runClientStuff(BasePedestalBlockEntity pedestal)
    {
        return;
    }





    //If toggled in config, this is the max allowed size of selectable area
    public int getUpgradeSelectableAreaSize(ItemStack upgradeStack)
    {
        //For a default 3x3x3 area the value is 2
        return 2 + getAreaIncrease(upgradeStack);
    }



    //Requires energy
    public boolean requiresEnergy() { return baseEnergyCostPerDistance()>0; }
    public boolean energyDistanceAsModifier() {return true;}
    public int baseEnergyCostPerDistance(){ return 0; }
    public double energyCostMultiplier(){ return 1.0D; }

    public boolean requiresXp() { return baseXpCostPerDistance()>0; }
    public boolean xpDistanceAsModifier() {return true;}
    public int baseXpCostPerDistance(){ return 0; }
    public double xpCostMultiplier(){ return 1.0D; }

    public boolean requiresDust() { return !baseDustCostPerDistance().isEmpty(); }
    public boolean dustDistanceAsModifier() {return true;}
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(-1,0); }
    public double dustCostMultiplier(){ return 1.0D; }

    //A modifier that takes the distance the selected area covers and multiplies it by the multiplier before multiplying it by the cost.
    public boolean hasSelectedAreaModifier() { return false; }
    public double selectedAreaCostMultiplier(){ return 1.0D; }

    public boolean requiresFuelForUpgradeAction()
    {
        return (requiresEnergy() || requiresXp() || requiresDust());
    }

    public boolean removeFuelForAction(BasePedestalBlockEntity pedestal, int distance, boolean simulate)
    {
        if(requiresFuelForUpgradeAction())
        {
            boolean energy = true;
            boolean xp = true;
            boolean dust = true;

            if(requiresEnergy())
            {
                int energyCost = (int)Math.round(((double)baseEnergyCostPerDistance() + ((hasSelectedAreaModifier())?((double)(((energyDistanceAsModifier())?(distance):(1)) * selectedAreaCostMultiplier())):(0.0D))) * energyCostMultiplier());
                energy = pedestal.removeEnergy(energyCost,simulate)>=energyCost;
            }

            if(requiresXp())
            {
                int xpCost = (int)Math.round(((double)baseXpCostPerDistance() + ((hasSelectedAreaModifier())?((double)(((xpDistanceAsModifier())?(distance):(1)) * selectedAreaCostMultiplier())):(0.0D))) * xpCostMultiplier());
                xp = pedestal.removeExperience(xpCost,simulate)>=xpCost;
            }

            //Need to add dust stuff to pedestal yet...
            if(requiresDust())
            {
                int dustAmountNeeded = (int)Math.round(((double)baseDustCostPerDistance().getDustAmount() + ((hasSelectedAreaModifier())?((double)(((dustDistanceAsModifier())?(distance):(1)) * selectedAreaCostMultiplier())):(0.0D))) * dustCostMultiplier());

                //dust = pedestal.removeDust(dustAmountNeeded,simulate)>=dustAmountNeeded;
            }

            return (energy && xp && dust);
        }

        return true;
    }

    public int getDistanceBetweenPoints(BlockPos pointOne, BlockPos posToCompare)
    {
        int x = pointOne.getX();
        int y = pointOne.getY();
        int z = pointOne.getZ();
        int x1 = posToCompare.getX();
        int y1 = posToCompare.getY();
        int z1 = posToCompare.getZ();
        int xF = Math.abs(Math.subtractExact(x,x1));
        int yF = Math.abs(Math.subtractExact(y,y1));
        int zF = Math.abs(Math.subtractExact(z,z1));

        return Math.max(Math.max(xF,yF),zF);
    }











    public static ChatFormatting getModeColorFormat(int mode)
    {
        ChatFormatting color;
        switch (mode)
        {
            case 0: color = ChatFormatting.GOLD; break;
            case 1: color = ChatFormatting.BLUE; break;
            case 2: color = ChatFormatting.RED; break;
            case 3: color = ChatFormatting.GREEN; break;
            case 4: color = ChatFormatting.LIGHT_PURPLE; break;
            default: color = ChatFormatting.WHITE; break;
        }

        return color;
    }

    public static String getModeStringFromInt(int mode) {

        switch(mode)
        {
            case 0: return "item";
            case 1: return "fluid";
            case 2: return "energy";
            case 3: return "xp";
            case 4: return "dust";
            default: return "item";
        }
    }

    public static String getModeLocalizedString(int mode)
    {
        String typeString = "";
        switch(mode)
        {
            case 0: typeString = ".mode_items"; break;
            case 1: typeString = ".mode_fluids"; break;
            case 2: typeString = ".mode_energy"; break;
            case 3: typeString = ".mode_experience"; break;
            case 4: typeString = ".mode_dust"; break;
            default: typeString = ".error"; break;
        }

        return typeString;
    }

    /*
    MODES

    0 - Items
    1 - Fluids
    2 - Energy
    3 - XP
    4 - Dust
     */

    public static void writeTransportModeToNBT(ItemStack filterStack, int mode, boolean allowed) {
        CompoundTag compound = new CompoundTag();
        if(filterStack.hasTag())
        {
            compound = filterStack.getTag();
        }
        compound.putBoolean(MODID + "_" + getModeStringFromInt(mode)+"_transport_mode",allowed);
        filterStack.setTag(compound);
    }

    public static boolean getTransportModeFromNBT(ItemStack filterStack, int mode) {
        boolean allowed = true;
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            String tag = MODID + "_" + getModeStringFromInt(mode)+"_transport_mode";
            if(filterStack.getTag().contains(tag))
            {
                allowed = getCompound.getBoolean(tag);
            }
        }
        return allowed;
    }

    public void toggleTransportMode(Player player, ItemStack heldItem, InteractionHand hand) {
        if(heldItem.getItem() instanceof ItemUpgradeBase baseUpgrade)
        {
            int mode = getUpgradeMode(heldItem);
            boolean getTransportMode = getTransportModeFromNBT(heldItem,mode);
            writeTransportModeToNBT(heldItem, mode, !getTransportMode);
            player.setItemInHand(hand,heldItem);

            ChatFormatting colorChange = (!getTransportMode)?(ChatFormatting.WHITE):(ChatFormatting.BLACK);
            MowLibMessageUtils.messagePopup(player,colorChange,MODID + ((!getTransportMode)?(".transport_mode_changed_true"):(".transport_mode_changed_false")));
        }
    }

    public void incrementUpgradeMode(Player player, ItemStack heldItem, InteractionHand hand)
    {
        if(heldItem.getItem() instanceof ItemUpgradeBase baseUpgrade)
        {
            int mode = getUpgradeMode(heldItem)+1;
            int setNewMode = (mode<=4)?(mode):(0);
            saveUpgradeModeToNBT(heldItem,setNewMode);
            player.setItemInHand(hand,heldItem);

            ChatFormatting colorChange = getModeColorFormat(setNewMode);
            String typeString = getModeLocalizedString(setNewMode);

            List<String> listed = new ArrayList<>();
            listed.add(MODID + typeString);
            MowLibMessageUtils.messagePopupWithAppend(MODID, player,colorChange,MODID + ".mode_changed",listed);
        }
    }

    public static void saveUpgradeModeToNBT(ItemStack augment, int mode)
    {
        CompoundTag compound = new CompoundTag();
        if(augment.hasTag())
        {
            compound = augment.getTag();
        }
        compound.putInt(MODID+"_upgrade_mode",mode);
        augment.setTag(compound);
    }

    public static int readUpgradeModeFromNBT(ItemStack augment) {
        if(augment.hasTag())
        {
            CompoundTag getCompound = augment.getTag();
            return getCompound.getInt(MODID+"_upgrade_mode");
        }
        return 0;
    }

    public static int getUpgradeMode(ItemStack stack) {

        return readUpgradeModeFromNBT(stack);
    }

    public static int getUpgradeModeForRender(ItemStack stack) {

        int mode = readUpgradeModeFromNBT(stack);
        boolean type = getTransportModeFromNBT(stack,mode);
        return (type)?(mode):(mode+5);
    }

    public static MutableComponent getUpgradeModeComponentFromInt(int mode) {

        switch(mode)
        {
            case 0: return Component.translatable(MODID + ".item_mode_component");
            case 1: return Component.translatable(MODID + ".fluid_mode_component");
            case 2: return Component.translatable(MODID + ".energy_mode_component");
            case 3: return Component.translatable(MODID + ".xp_mode_component");
            case 4: return Component.translatable(MODID + ".dust_mode_component");
            default: return Component.translatable(MODID + ".item_mode_component");
        }
    }


    //ToDo: Add to mowlib and remove from here
    public static void saveStringToNBT(ItemStack upgrade, String nbtTag, String string)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        compound.putString(MODID+nbtTag, string);
        upgrade.setTag(compound);
    }

    //ToDo: Add to mowlib and remove from here
    //returns true for an add, false for a remove.
    public static boolean addBlockPosToList(ItemStack upgrade, BlockPos posOfBlock)
    {
        List<BlockPos> currentList = readBlockPosListFromNBT(upgrade);
        if(currentList.contains(posOfBlock))
        {
            currentList.remove(posOfBlock);
            saveBlockPosListToNBT(upgrade,currentList);
            return false;
        }
        else
        {
            currentList.add(posOfBlock);
            saveBlockPosListToNBT(upgrade,currentList);
            return true;
        }
    }

    //ToDo: Add to mowlib and remove from here
    public static void saveBlockPosListToNBT(ItemStack upgrade, List<BlockPos> posListToSave)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        List<Integer> storedX = new ArrayList<Integer>();
        List<Integer> storedY = new ArrayList<Integer>();
        List<Integer> storedZ = new ArrayList<Integer>();

        for(int i=0;i<posListToSave.size();i++)
        {
            storedX.add(posListToSave.get(i).getX());
            storedY.add(posListToSave.get(i).getY());
            storedZ.add(posListToSave.get(i).getZ());
        }

        compound.putIntArray(MODID+"_intArrayXPos",storedX);
        compound.putIntArray(MODID+"_intArrayYPos",storedY);
        compound.putIntArray(MODID+"_intArrayZPos",storedZ);
        upgrade.setTag(compound);
    }

    //ToDo: Add to mowlib and remove from here
    public static List<BlockPos> readBlockPosListFromNBT(ItemStack upgrade) {
        List<BlockPos> posList = new ArrayList<>();
        if(upgrade.hasTag())
        {
            String tagX = MODID+"_intArrayXPos";
            String tagY = MODID+"_intArrayYPos";
            String tagZ = MODID+"_intArrayZPos";
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tagX) && upgrade.getTag().contains(tagY) && upgrade.getTag().contains(tagZ))
            {
                int[] storedIX = getCompound.getIntArray(tagX);
                int[] storedIY = getCompound.getIntArray(tagY);
                int[] storedIZ = getCompound.getIntArray(tagZ);

                for(int i=0;i<storedIX.length;i++)
                {
                    BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
                    posList.add(gotPos);
                }
            }
        }
        return posList;
    }

    public static void saveBlockPosListCustomToNBT(ItemStack upgrade, String tagGenericName, List<BlockPos> posListToSave)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        List<Integer> storedX = new ArrayList<Integer>();
        List<Integer> storedY = new ArrayList<Integer>();
        List<Integer> storedZ = new ArrayList<Integer>();

        for(int i=0;i<posListToSave.size();i++)
        {
            storedX.add(posListToSave.get(i).getX());
            storedY.add(posListToSave.get(i).getY());
            storedZ.add(posListToSave.get(i).getZ());
        }

        compound.putIntArray(MODID+tagGenericName+"_X",storedX);
        compound.putIntArray(MODID+tagGenericName+"_Y",storedY);
        compound.putIntArray(MODID+tagGenericName+"_Z",storedZ);
        upgrade.setTag(compound);
    }

    public static List<BlockPos> readBlockPosListCustomFromNBT(ItemStack upgrade, String tagGenericName) {
        List<BlockPos> posList = new ArrayList<>();
        if(upgrade.hasTag())
        {
            String tagX = MODID+tagGenericName+"_X";
            String tagY = MODID+tagGenericName+"_Y";
            String tagZ = MODID+tagGenericName+"_Z";
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tagX) && upgrade.getTag().contains(tagY) && upgrade.getTag().contains(tagZ))
            {
                int[] storedIX = getCompound.getIntArray(tagX);
                int[] storedIY = getCompound.getIntArray(tagY);
                int[] storedIZ = getCompound.getIntArray(tagZ);

                for(int i=0;i<storedIX.length;i++)
                {
                    BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
                    posList.add(gotPos);
                }
            }
        }
        return posList;
    }

    public void removeBlockListCustomNBTTags(ItemStack upgrade, String tagGenericName)
    {
        String tagX = MODID+tagGenericName+"_X";
        String tagY = MODID+tagGenericName+"_Y";
        String tagZ = MODID+tagGenericName+"_Z";
        CompoundTag getTags = upgrade.getTag();
        if(getTags.contains(tagX))getTags.remove(tagX);
        if(getTags.contains(tagY))getTags.remove(tagY);
        if(getTags.contains(tagZ))getTags.remove(tagZ);
        upgrade.setTag(getTags);
    }

    public boolean hasBlockListCustomNBTTags(ItemStack upgrade, String tagGenericName)
    {
        String tagX = MODID+tagGenericName+"_X";
        String tagY = MODID+tagGenericName+"_Y";
        String tagZ = MODID+tagGenericName+"_Z";
        CompoundTag getTags = upgrade.getTag();

        return getTags.contains(tagX) && getTags.contains(tagY) && getTags.contains(tagZ);
    }

    //ToDo: Add to mowlib and remove from here
    public static void saveBlockPosToNBT(ItemStack upgrade, int num, BlockPos posToSave)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        List<Integer> listed = new ArrayList<>();
        listed.add(posToSave.getX());
        listed.add(posToSave.getY());
        listed.add(posToSave.getZ());
        compound.putIntArray(MODID+"_upgrade_blockpos_"+num, listed);
        upgrade.setTag(compound);
    }

    //ToDo: Add to mowlib and remove from here
    public static BlockPos readBlockPosFromNBT(ItemStack upgrade, int num) {
        if(upgrade.hasTag())
        {
            String tag = MODID+"_upgrade_blockpos_"+num;
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tag))
            {
                int[] listed = getCompound.getIntArray(tag);
                if(listed.length>=3)return new BlockPos(listed[0],listed[1],listed[2]);
            }
        }
        return BlockPos.ZERO;
    }

    //ToDo: Add to mowlib and remove from here
    public static BlockPos getBlockPosOnUpgrade(ItemStack stack, int num) {

        return readBlockPosFromNBT(stack,num);
    }

    //ToDo: Add to mowlib and remove from here
    public boolean hasOneBlockPos(ItemStack stack) {
        return !readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO) || !readBlockPosFromNBT(stack,2).equals(BlockPos.ZERO);
    }


    public static BlockPos getExistingSingleBlockPos(ItemStack stack) {
        return (!readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO))?(readBlockPosFromNBT(stack,1)):(readBlockPosFromNBT(stack,2));
    }


    public boolean isNewBlockPosSmallerThanExisting(ItemStack stack, BlockPos posTwo) {
        BlockPos posOne = getExistingSingleBlockPos(stack);
        BlockPos toCompare = new BlockPos(Math.min(posOne.getX(), posTwo.getX()),Math.min(posOne.getY(), posTwo.getY()),Math.min(posOne.getZ(), posTwo.getZ()));

        return (posTwo.equals(toCompare))?(true):(false);
    }


    public boolean hasTwoPointsSelected(ItemStack stack)
    {
        return !readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO) && !readBlockPosFromNBT(stack,2).equals(BlockPos.ZERO);
    }

    public AABB getAABBonUpgrade(ItemStack stack)
    {
        if(stack.getItem() instanceof ISelectableArea && hasTwoPointsSelected(stack))
        {
            BlockPos posOne = readBlockPosFromNBT(stack,1);
            BlockPos posTwo = readBlockPosFromNBT(stack,2);

            return new AABB(Math.min(posOne.getX(), posTwo.getX()),Math.min(posOne.getY(), posTwo.getY()),Math.min(posOne.getZ(), posTwo.getZ()),
                    Math.max(posOne.getX(), posTwo.getX()),Math.max(posOne.getY(), posTwo.getY()),Math.max(posOne.getZ(), posTwo.getZ())).expandTowards(1D,1D,1D);
        }
        return new AABB(BlockPos.ZERO);
    }



    public int getUpgradeWorkRange(ItemStack coinUpgrade)
    {
        return PedestalConfig.COMMON.upgrades_baseSelectionRange.get() + getRangeIncrease(coinUpgrade);
    }

    //ToDo: Add to mowlib and remove from here
    public boolean isSelectionInRange(BasePedestalBlockEntity pedestalCurrent, BlockPos currentSelectedPoint)
    {

        int range = PedestalConfig.COMMON.upgrades_baseSelectionRange.get() + getUpgradeWorkRange(pedestalCurrent.getCoinOnPedestal());
        int x = currentSelectedPoint.getX();
        int y = currentSelectedPoint.getY();
        int z = currentSelectedPoint.getZ();
        int x1 = pedestalCurrent.getPos().getX();
        int y1 = pedestalCurrent.getPos().getY();
        int z1 = pedestalCurrent.getPos().getZ();
        int xF = Math.abs(Math.subtractExact(x,x1));
        int yF = Math.abs(Math.subtractExact(y,y1));
        int zF = Math.abs(Math.subtractExact(z,z1));

        if(xF>range || yF>range || zF>range)
        {
            return false;
        }
        else return true;
    }

    public boolean selectedAreaWithinRange(BasePedestalBlockEntity pedestal)
    {
        if(isSelectionInRange(pedestal, readBlockPosFromNBT(pedestal.getCoinOnPedestal(),1)) && isSelectionInRange(pedestal, readBlockPosFromNBT(pedestal.getCoinOnPedestal(),2)))
        {
            return true;
        }

        return false;
    }

    //ToDo: Add to mowlib and remove from here
    public boolean selectedPointWithinRange(BasePedestalBlockEntity pedestal, BlockPos posPoint)
    {
        if(isSelectionInRange(pedestal, posPoint))
        {
            return true;
        }

        return false;
    }

    public BlockPos getHigherByFacing(BlockPos atLocation, Direction facing)
    {
        //west north down +XYZ
        //east +YZ
        //south +XY
        //up +XZ
        BlockPos higherPos = atLocation;
        switch(facing)
        {
            case NORTH:
            default:
                higherPos = atLocation.offset(1,1,1);
                break;
            case EAST:
                higherPos = atLocation.offset(0,1,1);
                break;
            case SOUTH:
                higherPos = atLocation.offset(1,1,0);
                break;
            case UP:
                higherPos = atLocation.offset(1,0,1);
                break;
        }

        return higherPos;
    }

    public BlockPos getLowerByFacing(BlockPos atLocation, Direction facing)
    {
        //west north down +-0
        //east -x
        //south -z
        //up -y
        BlockPos lowerPos = atLocation;
        switch(facing)
        {
            case NORTH:
            default:
                break;
            case EAST:
                lowerPos = atLocation.offset(-1,0,0);
                break;
            case SOUTH:
                lowerPos = atLocation.offset(0,0,-1);
                break;
            case UP:
                lowerPos = atLocation.offset(0,-1,0);
                break;
        }

        return lowerPos;
    }

    /*
    INTERACTIONS

    MODE CHANGE:
    - (Offhand)Crouch Right Click = Mode change
    TYPE CHANGE:
    - (Offhand)Right Click = Type Change



    GET BLOCK POS FOR AREA:
    main hand crouch right click to start it and right click to end it???
     */
    public Direction getLastClickedDirectionFromUpgrade(ItemStack stack)
    {
        Direction dir = Direction.UP;
        if(stack.hasTag())
        {
            if(stack.getTag().contains(MODID + "_string_last_clicked_direction"))
            {
                String direction = stack.getTag().getString(MODID + "_string_last_clicked_direction");
                if(direction == "down")return Direction.DOWN;
                else if(direction == "up")return Direction.UP;
                else if(direction == "north")return Direction.NORTH;
                else if(direction == "south")return Direction.SOUTH;
                else if(direction == "west")return Direction.WEST;
                else if(direction == "east")return Direction.EAST;
            }
        }

        return dir;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        saveBlockPosToNBT(stack,10,context.getClickedPos());
        saveStringToNBT(stack,"_string_last_clicked_direction",context.getClickedFace().toString());
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level level = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack itemInOffhand = player.getOffhandItem();
        HitResult result = player.pick(5,player.getEyeHeight(),false);
        BlockPos atLocation = readBlockPosFromNBT(itemInHand,10);
        Direction facing = getLastClickedDirectionFromUpgrade(itemInHand);


        //result.getType().equals(HitResult.Type.MISS)

        if(itemInHand.getItem() instanceof ItemUpgradeBase)
        {
            if(hand.equals(InteractionHand.MAIN_HAND) && !player.isShiftKeyDown() && itemInHand.getItem() instanceof ISelectablePoints)
            {
                if(result.getType().equals(HitResult.Type.BLOCK))
                {
                    boolean added = addBlockPosToList(itemInHand,atLocation);
                    player.setItemInHand(hand,itemInHand);
                    MowLibMessageUtils.messagePopup(player,(added)?(ChatFormatting.WHITE):(ChatFormatting.BLACK),(added)?(MODID + ".upgrade_blockpos_added"):(MODID + ".upgrade_blockpos_removed"));
                    MowLibPacketHandler.sendToNearby(p_41432_,player.getOnPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,atLocation.getX(),atLocation.getY()+1.0D,atLocation.getZ(),0,(added)?(200):(0),0));

                }
                else if(result.getType().equals(HitResult.Type.MISS) && readBlockPosListFromNBT(itemInHand).size()>0)
                {
                    saveBlockPosListToNBT(itemInHand, new ArrayList<>());
                    MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_clear");
                }
            }

            if(hand.equals(InteractionHand.MAIN_HAND) && player.isShiftKeyDown() && itemInHand.getItem() instanceof ISelectableArea)
            {
                if(result.getType().equals(HitResult.Type.BLOCK))
                {

                    Boolean hasOnePointAlready = hasOneBlockPos(itemInHand);
                    Boolean hasTwoPointsAlready = hasTwoPointsSelected(itemInHand);

                    if(hasOnePointAlready && !hasTwoPointsAlready)
                    {
                        if(PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get())
                        {
                            if(getDistanceBetweenPoints(readBlockPosFromNBT(itemInHand,1),atLocation) <= getUpgradeSelectableAreaSize(itemInHand))
                            {
                                saveBlockPosToNBT(itemInHand,2,atLocation);
                                player.setItemInHand(hand,itemInHand);
                                MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_second");
                            }
                            else
                            {
                                MowLibMessageUtils.messagePopup(player,ChatFormatting.RED,MODID + ".upgrade_blockpos_point_out_of_range");
                            }
                        }
                        else
                        {
                            saveBlockPosToNBT(itemInHand,2,atLocation);
                            player.setItemInHand(hand,itemInHand);
                            MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_second");
                        }
                    }
                    else if(!hasTwoPointsAlready)
                    {
                        saveBlockPosToNBT(itemInHand,1,atLocation);
                        player.setItemInHand(hand,itemInHand);
                        MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_first");
                    }
                }
                else if(result.getType().equals(HitResult.Type.MISS) && hasOneBlockPos(itemInHand))
                {
                    saveBlockPosToNBT(itemInHand,1,BlockPos.ZERO);
                    saveBlockPosToNBT(itemInHand,2,BlockPos.ZERO);
                    MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_clear");
                }
            }

            if(hand.equals(InteractionHand.OFF_HAND) && itemInHand.getItem() instanceof IHasModeTypes)
            {
                if(result.getType().equals(HitResult.Type.MISS))
                {
                    if(player.isShiftKeyDown())
                    {
                        incrementUpgradeMode(player,itemInOffhand,hand);
                    }
                    else
                    {
                        toggleTransportMode(player,itemInOffhand,hand);
                    }
                }
            }

        }

        return InteractionResultHolder.fail(p_41433_.getItemInHand(p_41434_));
    }

    @Override
    public boolean canTransferItems(ItemStack upgrade)
    {
        return getTransportModeFromNBT(upgrade, 0);
    }

    @Override
    public boolean canTransferFluids(ItemStack upgrade)
    {
        return getTransportModeFromNBT(upgrade, 1);
    }

    @Override
    public boolean canTransferEnergy(ItemStack upgrade)
    {
        return getTransportModeFromNBT(upgrade, 2);
    }

    @Override
    public boolean canTransferXP(ItemStack upgrade)
    {
        return getTransportModeFromNBT(upgrade, 3);
    }

    @Override
    public boolean canTransferDust(ItemStack upgrade)
    {
        return getTransportModeFromNBT(upgrade, 4);
    }

    public BlockPos getPosOfBlockBelow(Level world, BlockPos posOfPedestal, int numBelow)
    {
        BlockState state = world.getBlockState(posOfPedestal);

        Direction enumfacing = (state.hasProperty(FACING))?(state.getValue(FACING)):(Direction.UP);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.offset(0,-numBelow,0);
            case DOWN:
                return blockBelow.offset(0,numBelow,0);
            case NORTH:
                return blockBelow.offset(0,0,numBelow);
            case SOUTH:
                return blockBelow.offset(0,0,-numBelow);
            case EAST:
                return blockBelow.offset(-numBelow,0,0);
            case WEST:
                return blockBelow.offset(numBelow,0,0);
            default:
                return blockBelow;
        }
    }

    public Direction getPedestalFacing(Level world, BlockPos posOfPedestal)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        return state.getValue(FACING);
    }

    public boolean isInventoryEmpty(LazyOptional<IItemHandler> cap)
    {
        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            if(handler != null)
            {
                int range = handler.getSlots();

                ItemStack itemFromInv = ItemStack.EMPTY;
                itemFromInv = IntStream.range(0,range)//Int Range
                        .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                        .filter(itemStack -> !itemStack.isEmpty())
                        .findFirst().orElse(ItemStack.EMPTY);

                if(!itemFromInv.isEmpty())
                {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean doItemsMatch(ItemStack stackPedestal, ItemStack itemStackIn)
    {
        return ItemHandlerHelper.canItemStacksStack(stackPedestal,itemStackIn);
    }

    public boolean doItemsMatchWithEmpty(ItemStack stackPedestal, ItemStack itemStackIn)
    {
        if(stackPedestal.isEmpty() && itemStackIn.isEmpty())return true;

        return ItemHandlerHelper.canItemStacksStack(stackPedestal,itemStackIn);
    }

    public int getNextSlotWithItemsCapFilteredMachine(BasePedestalBlockEntity pedestal, LazyOptional<IItemHandler> cap)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        if(cap.isPresent()) {

            cap.ifPresent(itemHandler -> {
                int range = itemHandler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    //find a slot with items
                    if(!stackInSlot.isEmpty())
                    {
                        //check if it could pull the item out or not
                        if(!itemHandler.extractItem(i,1 ,true ).equals(ItemStack.EMPTY))
                        {
                            //If pedestal is empty accept any items
                            if(passesMachineFilter(pedestal,stackInSlot))
                            {
                                slot.set(i);
                                break;
                            }
                        }
                    }
                }});


        }

        return slot.get();
    }

    public int getNextSlotWithItemsCapFiltered(BasePedestalBlockEntity pedestal, LazyOptional<IItemHandler> cap)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        if(cap.isPresent()) {

            cap.ifPresent(itemHandler -> {
                int range = itemHandler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    //find a slot with items
                    if(!stackInSlot.isEmpty())
                    {
                        //check if it could pull the item out or not
                        if(!itemHandler.extractItem(i,1 ,true ).equals(ItemStack.EMPTY))
                        {
                            //If pedestal is empty accept any items
                            if(passesItemFilter(pedestal,stackInSlot))
                            {
                                ItemStack itemFromPedestal = pedestal.getMatchingItemInPedestalOrEmptySlot(stackInSlot);
                                if(itemFromPedestal.isEmpty())
                                {
                                    slot.set(i);
                                    break;
                                }
                                //if stack in pedestal matches items in slot
                                else if(doItemsMatch(itemFromPedestal,stackInSlot))
                                {
                                    slot.set(i);
                                    break;
                                }
                            }
                        }
                    }
                }});


        }

        return slot.get();
    }

    public int getNextSlotEmptyOrMatching(LazyOptional<IItemHandler> cap, ItemStack stackInPedestal)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            if(handler != null)
            {
                int range = handler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = handler.getStackInSlot(i);
                    int maxSizeSlot = handler.getSlotLimit(i);
                    if(maxSizeSlot>0)
                    {
                        if(stackInSlot.getMaxStackSize()>1)
                        {
                            if(doItemsMatch(stackInSlot,stackInPedestal) && stackInSlot.getCount() < handler.getSlotLimit(i))
                            {
                                slot.set(i);
                                break;
                            }
                            else if(stackInSlot.isEmpty())
                            {
                                slot.set(i);
                                break;
                            }
                            //if chest is full
                            else if(i==range)
                            {
                                slot.set(i);
                            }
                        }
                    }
                }
            }
        }
        return slot.get();
    }

    public boolean passesMachineFilter(BasePedestalBlockEntity pedestal, ItemStack stackIn)
    {
        boolean returner = true;
        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if(filterInPedestal.getItem() instanceof IPedestalFilter filter)
            {
                if(filter.getFilterDirection().equals(IPedestalFilter.FilterDirection.NEUTRAL))
                {
                    returner = filter.canAcceptItems(filterInPedestal,stackIn);
                }
            }

        }

        return returner;
    }

    public boolean passesItemFilter(BasePedestalBlockEntity pedestal, ItemStack stackIn)
    {
        boolean returner = true;
        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if(filterInPedestal.getItem() instanceof IPedestalFilter filter)
            {
                returner = filter.canAcceptItems(filterInPedestal,stackIn);
            }

        }

        return returner;
    }

    public FluidStack getFluidStackFromItemStack(ItemStack stackIn)
    {
        if(stackIn.getItem() instanceof BucketItem bucket)
        {
            Fluid bucketFluid = bucket.getFluid();
            return new FluidStack(bucketFluid,1000);
        }

        return FluidStack.EMPTY;
    }

    public boolean passesFluidFilter(BasePedestalBlockEntity pedestal, FluidStack incomingFluidStack)
    {
        boolean returner = true;

        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if(filterInPedestal.getItem() instanceof IPedestalFilter filter)
            {
                returner = filter.canAcceptFluids(filterInPedestal, incomingFluidStack);
            }

        }
        else
        {
            return pedestal.canAcceptFluid(incomingFluidStack);
        }

        return returner;
    }

    public boolean passesDustFilter(BasePedestalBlockEntity pedestal, DustMagic incomingDust)
    {
        boolean returner = true;

        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if(filterInPedestal.getItem() instanceof IPedestalFilter filter)
            {
                returner = filter.canAcceptDust(filterInPedestal, incomingDust);
            }

        }
        else
        {
            return pedestal.canAcceptDust(incomingDust);
        }

        return returner;
    }

    public int getCountItemFilter(BasePedestalBlockEntity pedestal, ItemStack stackIn)
    {
        if(pedestal.hasFilter())
        {
            ItemStack filterStackInPedestal = pedestal.getFilterInPedestal();
            if(filterStackInPedestal.getItem() instanceof IPedestalFilter filter)
            {
                return filter.canAcceptCountItems(pedestal,filterStackInPedestal,stackIn.getMaxStackSize(),pedestal.getSlotSizeLimit(),stackIn);
            }
        }

        return stackIn.getCount();
    }














    /*public void upgradeActionMagnet(PedestalTileEntity pedestal, World world, List<ItemEntity> itemList, ItemStack itemInPedestal, BlockPos posOfPedestal)
    {
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        if(itemList.size()>0)
        {
            for(ItemEntity getItemFromList : itemList)
            {
                ItemStack copyStack = getItemFromList.getItem().copy();
                int maxSize = copyStack.getMaxStackSize();
                boolean stacksMatch = doItemsMatch(itemInPedestal,copyStack);
                if ((itemInPedestal.isEmpty() || stacksMatch ) && canThisPedestalReceiveItemStack(pedestal,world,posOfPedestal,copyStack))
                {
                    int spaceInPed = itemInPedestal.getMaxStackSize()-itemInPedestal.getCount();
                    if(stacksMatch)
                    {
                        if(spaceInPed > 0)
                        {
                            int itemInCount = getItemFromList.getItem().getCount();
                            int countToAdd = ( itemInCount<= spaceInPed)?(itemInCount):(spaceInPed);
                            getItemFromList.getItem().setCount(itemInCount-countToAdd);
                            copyStack.setCount(countToAdd);
                            pedestal.addItem(copyStack);
                            if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
                        }
                        else if(!hasAdvancedInventoryTargetingTwo(coinInPedestal))break;
                    }
                    else if(copyStack.getCount() <=maxSize)
                    {
                        getItemFromList.setItem(ItemStack.EMPTY);
                        getItemFromList.remove();
                        pedestal.addItem(copyStack);
                        if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);

                    }
                    else
                    {
                        //If an ItemStackEntity has more than 64, we subtract 64 and inset 64 into the pedestal
                        int count = getItemFromList.getItem().getCount();
                        getItemFromList.getItem().setCount(count-maxSize);
                        copyStack.setCount(maxSize);
                        pedestal.addItem(copyStack);
                        if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    }
                    if(!hasAdvancedInventoryTargetingTwo(coinInPedestal))break;
                }
            }
        }
    }*/








    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        if(p_41421_.getItem().equals(DeferredRegisterItems.PEDESTAL_UPGRADE_BASE.get()))
        {
            MutableComponent base = Component.translatable(getDescriptionId() + ".base_description");
            base.withStyle(ChatFormatting.DARK_RED);
            p_41423_.add(base);
        }

        if(p_41421_.getItem() instanceof IHasModeTypes)
        {
            //Display Current Mode
            int mode = getUpgradeMode(p_41421_);
            MutableComponent changed = Component.translatable(MODID + ".upgrade_tooltip_mode");
            ChatFormatting colorChange = ChatFormatting.GOLD;
            String typeString = "";
            switch(mode)
            {
                case 0: typeString = ".mode_tooltip_items"; break;
                case 1: typeString = ".mode_tooltip_fluids"; break;
                case 2: typeString = ".mode_tooltip_energy"; break;
                case 3: typeString = ".mode_tooltip_experience"; break;
                case 4: typeString = ".mode_tooltip_dust"; break;
                default: typeString = ".tooltip_error"; break;
            }
            changed.withStyle(colorChange);
            MutableComponent type = Component.translatable(MODID + typeString);
            changed.append(type);
            p_41423_.add(changed);

            if (!Screen.hasShiftDown()) {
                MutableComponent base = Component.translatable(MODID + ".upgrade_description_shift");
                base.withStyle(ChatFormatting.WHITE);
                p_41423_.add(base);
            }
            else
            {
                //Separator
                MutableComponent separator = Component.translatable(MODID + ".tooltip_separator");
                p_41423_.add(separator);

                // List all the current modes and their status
                for(int i=0;i<5;i++)
                {
                    MutableComponent modeIterator = getUpgradeModeComponentFromInt(i);
                    MutableComponent typeIterator = (getTransportModeFromNBT(p_41421_,i))?(Component.translatable(MODID + ".upgrade_tooltip_type_enabled")):(Component.translatable(MODID + ".upgrade_tooltip_type_disabled"));
                    modeIterator.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                    modeIterator.append(typeIterator);
                    p_41423_.add(modeIterator);
                }
            }
        }

        /*=====================================
        =======================================
        =====================================*/

        if(canModifySpeed(p_41421_))
        {
            if(getSpeedTicksReduced(p_41421_)>0)
            {
                MutableComponent speedLabel = Component.translatable(MODID + ".upgrade_tooltip_speed_label");
                speedLabel.withStyle(ChatFormatting.AQUA);
                MutableComponent speedAmount = Component.literal(""+getSpeedTicksReduced(p_41421_)+"");
                MutableComponent separator = Component.translatable(MODID + ".upgrade_tooltip_separator_slash");
                MutableComponent speedMax = Component.literal(""+PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get()+"");
                speedAmount.append(separator);
                speedAmount.append(speedMax);
                speedAmount.withStyle(ChatFormatting.WHITE);

                speedLabel.append(speedAmount);
                p_41423_.add(speedLabel);
            }
            else
            {
                MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_speed_allowed",ChatFormatting.AQUA);
            }
        }

        //Group All Capacities together unless shift is pressed
        if(canModifyDamageCapacity(p_41421_) ||
                canModifyBlockCapacity(p_41421_) ||
                canModifyItemCapacity(p_41421_) ||
                canModifyFluidCapacity(p_41421_) ||
                canModifyEnergyCapacity(p_41421_) ||
                canModifyXPCapacity(p_41421_) ||
                canModifyDustCapacity(p_41421_))
        {
            if(Screen.hasShiftDown())
            {
                if(canModifyDamageCapacity(p_41421_))
                {
                    if(getDamageCapacityIncrease(p_41421_)>0)
                    {
                        MutableComponent itemCapacityLabel = Component.translatable(MODID + ".upgrade_tooltip_damagecapacity_label");
                        itemCapacityLabel.withStyle(ChatFormatting.DARK_RED);
                        MutableComponent capacityAmount = Component.literal(""+getDamageCapacityIncrease(p_41421_)+"");
                        capacityAmount.withStyle(ChatFormatting.WHITE);
                        itemCapacityLabel.append(capacityAmount);
                        p_41423_.add(itemCapacityLabel);
                    }
                    else { MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_damagecapacity_allowed",ChatFormatting.DARK_RED); }
                }

                if(canModifyBlockCapacity(p_41421_))
                {
                    if(getBlockCapacityIncrease(p_41421_)>0)
                    {
                        MutableComponent itemCapacityLabel = Component.translatable(MODID + ".upgrade_tooltip_blockcapacity_label");
                        itemCapacityLabel.withStyle(ChatFormatting.GRAY);
                        MutableComponent capacityAmount = Component.literal(""+getBlockCapacityIncrease(p_41421_)+"");
                        capacityAmount.withStyle(ChatFormatting.WHITE);
                        itemCapacityLabel.append(capacityAmount);
                        p_41423_.add(itemCapacityLabel);
                    }
                    else { MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_blockcapacity_allowed",ChatFormatting.GRAY); }
                }

                if(canModifyItemCapacity(p_41421_))
                {
                    if(getItemCapacityIncrease(p_41421_)>0)
                    {
                        MutableComponent itemCapacityLabel = Component.translatable(MODID + ".upgrade_tooltip_itemcapacity_label");
                        itemCapacityLabel.withStyle(ChatFormatting.GOLD);
                        MutableComponent capacityAmount = Component.literal(""+getItemCapacityIncrease(p_41421_)+"");
                        capacityAmount.withStyle(ChatFormatting.WHITE);
                        itemCapacityLabel.append(capacityAmount);
                        p_41423_.add(itemCapacityLabel);
                    }
                    else { MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_itemcapacity_allowed",ChatFormatting.GOLD); }

                }

                if(canModifyFluidCapacity(p_41421_))
                {
                    if(getFluidCapacityIncrease(p_41421_)>0)
                    {
                        MutableComponent fluidCapacityLabel = Component.translatable(MODID + ".upgrade_tooltip_fluidcapacity_label");
                        fluidCapacityLabel.withStyle(ChatFormatting.BLUE);
                        MutableComponent capacityAmount = Component.literal(""+getFluidCapacityIncrease(p_41421_)+"");
                        capacityAmount.withStyle(ChatFormatting.WHITE);
                        fluidCapacityLabel.append(capacityAmount);
                        p_41423_.add(fluidCapacityLabel);
                    }
                    else { MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_fluidcapacity_allowed",ChatFormatting.BLUE); }

                }

                if(canModifyEnergyCapacity(p_41421_))
                {
                    if(getEnergyCapacityIncrease(p_41421_)>0)
                    {
                        MutableComponent energyCapacityLabel = Component.translatable(MODID + ".upgrade_tooltip_energycapacity_label");
                        energyCapacityLabel.withStyle(ChatFormatting.RED);
                        MutableComponent capacityAmount = Component.literal(""+getEnergyCapacityIncrease(p_41421_)+"");
                        capacityAmount.withStyle(ChatFormatting.WHITE);
                        energyCapacityLabel.append(capacityAmount);
                        p_41423_.add(energyCapacityLabel);
                    }
                    else { MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_energycapacity_allowed",ChatFormatting.RED); }

                }

                if(canModifyXPCapacity(p_41421_))
                {
                    if(getItemCapacityIncrease(p_41421_)>0)
                    {
                        MutableComponent xpCapacityLabel = Component.translatable(MODID + ".upgrade_tooltip_xpcapacity_label");
                        xpCapacityLabel.withStyle(ChatFormatting.GREEN);
                        MutableComponent capacityAmount = Component.literal(""+getXPCapacityIncrease(p_41421_)+"");
                        capacityAmount.withStyle(ChatFormatting.WHITE);
                        xpCapacityLabel.append(capacityAmount);
                        p_41423_.add(xpCapacityLabel);
                    }
                    else { MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_xpcapacity_allowed",ChatFormatting.GREEN); }

                }

                if(canModifyDustCapacity(p_41421_))
                {
                    if(getItemCapacityIncrease(p_41421_)>0)
                    {
                        MutableComponent dustCapacityLabel = Component.translatable(MODID + ".upgrade_tooltip_dustcapacity_label");
                        dustCapacityLabel.withStyle(ChatFormatting.LIGHT_PURPLE);
                        MutableComponent capacityAmount = Component.literal(""+getDustCapacityIncrease(p_41421_)+"");
                        capacityAmount.withStyle(ChatFormatting.WHITE);
                        dustCapacityLabel.append(capacityAmount);
                        p_41423_.add(dustCapacityLabel);
                    }
                    else { MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_dustcapacity_allowed",ChatFormatting.LIGHT_PURPLE); }
                }
            }
            else
            {
                MutableComponent capacityLabel = Component.translatable(MODID + ".upgrade_tooltip_capacity_label");
                capacityLabel.withStyle(ChatFormatting.GREEN);
                MutableComponent separator_space = Component.translatable(MODID + ".upgrade_tooltip_separator_space");

                MutableComponent capacityDamageAmount = Component.literal("?");
                MutableComponent capacityBlockAmount = Component.literal("?");
                MutableComponent capacityItemAmount = Component.literal("?");
                MutableComponent capacityFluidAmount = Component.literal("?");
                MutableComponent capacityEnergyAmount = Component.literal("?");
                MutableComponent capacityXPAmount = Component.literal("?");
                MutableComponent capacityDustAmount = Component.literal("?");

                if(getDamageCapacityIncrease(p_41421_)>0) { capacityDamageAmount = Component.literal(""+getDamageCapacityIncrease(p_41421_)+""); }
                if(getBlockCapacityIncrease(p_41421_)>0) { capacityBlockAmount = Component.literal(""+getBlockCapacityIncrease(p_41421_)+""); }
                if(getItemCapacityIncrease(p_41421_)>0) { capacityItemAmount = Component.literal(""+getItemCapacityIncrease(p_41421_)+""); }
                if(getFluidCapacityIncrease(p_41421_)>0) { capacityFluidAmount = Component.literal(""+getFluidCapacityIncrease(p_41421_)+"");}
                if(getEnergyCapacityIncrease(p_41421_)>0) { capacityEnergyAmount = Component.literal(""+getEnergyCapacityIncrease(p_41421_)+""); }
                if(getItemCapacityIncrease(p_41421_)>0) { capacityXPAmount = Component.literal(""+getXPCapacityIncrease(p_41421_)+""); }
                if(getItemCapacityIncrease(p_41421_)>0) { capacityDustAmount = Component.literal(""+getDustCapacityIncrease(p_41421_)+""); }

                capacityDamageAmount.withStyle(ChatFormatting.DARK_RED);
                capacityBlockAmount.withStyle(ChatFormatting.GRAY);
                capacityItemAmount.withStyle(ChatFormatting.GOLD);
                capacityFluidAmount.withStyle(ChatFormatting.BLUE);
                capacityEnergyAmount.withStyle(ChatFormatting.RED);
                capacityXPAmount.withStyle(ChatFormatting.GREEN);
                capacityDustAmount.withStyle(ChatFormatting.LIGHT_PURPLE);

                if(canModifyDamageCapacity(p_41421_))
                {
                    capacityLabel.append(capacityDamageAmount);
                }
                if(canModifyBlockCapacity(p_41421_))
                {
                    capacityLabel.append(separator_space);
                    capacityLabel.append(capacityBlockAmount);
                }
                if(canModifyItemCapacity(p_41421_))
                {
                    capacityLabel.append(separator_space);
                    capacityLabel.append(capacityItemAmount);
                }
                if(canModifyFluidCapacity(p_41421_))
                {
                    capacityLabel.append(separator_space);
                    capacityLabel.append(capacityFluidAmount);
                }
                if(canModifyEnergyCapacity(p_41421_))
                {
                    capacityLabel.append(separator_space);
                    capacityLabel.append(capacityEnergyAmount);
                }
                if(canModifyXPCapacity(p_41421_))
                {
                    capacityLabel.append(separator_space);
                    capacityLabel.append(capacityXPAmount);
                }
                if(canModifyDustCapacity(p_41421_))
                {
                    capacityLabel.append(separator_space);
                    capacityLabel.append(capacityDustAmount);
                }

                p_41423_.add(capacityLabel);
            }
        }

        if(canModifyArea(p_41421_))
        {
            if(getAreaIncrease(p_41421_)>0)
            {
                MutableComponent areaLabel = Component.translatable(MODID + ".upgrade_tooltip_area_label");
                areaLabel.withStyle(ChatFormatting.GRAY);
                MutableComponent areaAmount = Component.literal(""+getAreaIncrease(p_41421_)+"");
                areaAmount.withStyle(ChatFormatting.WHITE);

                areaLabel.append(areaAmount);
                p_41423_.add(areaLabel);
            }
            else
            {
                MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_area_allowed",ChatFormatting.GRAY);
            }
        }

        if(canModifyRange(p_41421_))
        {
            if(getRangeIncrease(p_41421_)>0)
            {
                MutableComponent areaLabel = Component.translatable(MODID + ".upgrade_tooltip_range_label");
                areaLabel.withStyle(ChatFormatting.GOLD);
                MutableComponent areaAmount = Component.literal(""+getRangeIncrease(p_41421_)+"");
                areaAmount.withStyle(ChatFormatting.WHITE);

                areaLabel.append(areaAmount);
                p_41423_.add(areaLabel);
            }
            else
            {
                MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_range_allowed",ChatFormatting.GOLD);
            }
        }

        if(canModifyMagnet(p_41421_))
        {
            if(getMagnet(p_41421_))
            {
                MutableComponent areaLabel = Component.translatable(MODID + ".upgrade_tooltip_magnet_label");
                areaLabel.withStyle(ChatFormatting.DARK_RED);
                p_41423_.add(areaLabel);
            }
            else
            {
                MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_magnet_allowed",ChatFormatting.DARK_RED);
            }
        }

        if(canModifyGentleHarvest(p_41421_))
        {
            if(getGentleHarvest(p_41421_))
            {
                MutableComponent areaLabel = Component.translatable(MODID + ".upgrade_tooltip_gentle_label");
                areaLabel.withStyle(ChatFormatting.YELLOW);
                p_41423_.add(areaLabel);
            }
            else
            {
                MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_gentle_allowed",ChatFormatting.YELLOW);
            }
        }

        if(canModifySuperSpeed(p_41421_))
        {
            if(getSuperSpeed(p_41421_))
            {
                MutableComponent areaLabel = Component.translatable(MODID + ".upgrade_tooltip_superspeed_label");
                areaLabel.withStyle(ChatFormatting.DARK_AQUA);
                p_41423_.add(areaLabel);
            }
            else
            {
                MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,MODID + ".upgrade_tooltip_superspeed_allowed",ChatFormatting.DARK_AQUA);
            }
        }

        /*=====================================
        =======================================
        =====================================*/

        if(Screen.hasShiftDown() && Screen.hasAltDown())
        {
            //Add a new Line if both are present
            p_41423_.add(Component.literal(""));
        }

        if(p_41421_.getItem() instanceof ISelectableArea)
        {
            if(hasOneBlockPos(p_41421_))
            {
                if (!Screen.hasAltDown()) {
                    MutableComponent base = Component.translatable(MODID + ".upgrade_description_alt");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".upgrade_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".tooltip_separator");
                    p_41423_.add(separator);

                    MutableComponent posOne = Component.translatable(MODID + ".upgrade_tooltip_blockpos_one");
                    BlockPos blockPosOne = readBlockPosFromNBT(p_41421_,1);
                    MutableComponent posOnePos = Component.literal(blockPosOne.getX() + "x " + blockPosOne.getY() + "y " + blockPosOne.getZ()+ "z");
                    posOnePos.withStyle(ChatFormatting.GRAY);
                    posOne.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                    posOne.append(posOnePos);

                    p_41423_.add(posOne);

                    MutableComponent posTwo = Component.translatable(MODID + ".upgrade_tooltip_blockpos_two");
                    BlockPos blockPosTwo = readBlockPosFromNBT(p_41421_,2);
                    MutableComponent posTwoPos = Component.literal(blockPosTwo.getX() + "x " + blockPosTwo.getY() + "y " + blockPosTwo.getZ()+ "z");
                    posTwoPos.withStyle(ChatFormatting.GRAY);
                    posTwo.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                    posTwo.append(posTwoPos);
                    p_41423_.add(posTwo);
                }
            }
        }

        if(p_41421_.getItem() instanceof ISelectablePoints && !hasTwoPointsSelected(p_41421_))
        {
            List<BlockPos> getList = readBlockPosListFromNBT(p_41421_);
            if(getList.size()>0)
            {
                if (!Screen.hasAltDown()) {
                    MutableComponent base = Component.translatable(MODID + ".upgrade_description_alt");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".upgrade_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".tooltip_separator");
                    p_41423_.add(separator);

                    for (BlockPos pos : getList) {
                        MutableComponent posOnePos = Component.literal(pos.getX() + "x " + pos.getY() + "y " + pos.getZ()+ "z");
                        posOnePos.withStyle(ChatFormatting.GRAY);
                        p_41423_.add(posOnePos);
                    }
                }
            }
        }

        if(requiresFuelForUpgradeAction())
        {
            if(requiresEnergy())
            {
                MutableComponent base = Component.translatable(MODID + ".upgrade_fuel_energy");
                base.withStyle(ChatFormatting.RED);
                p_41423_.add(base);
            }
            if(requiresXp())
            {
                MutableComponent base = Component.translatable(MODID + ".upgrade_fuel_xp");
                base.withStyle(ChatFormatting.GREEN);
                p_41423_.add(base);
            }
            if(requiresDust())
            {
                MutableComponent base = Component.translatable(MODID + ".upgrade_fuel_dust");
                base.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                base.withStyle(ChatFormatting.LIGHT_PURPLE);
                MutableComponent dust = Component.translatable(MowLibReferences.MODID + "." + MowLibColorReference.getColorName(baseDustCostPerDistance().getDustColor()));
                dust.withStyle(ChatFormatting.WHITE);
                base.append(dust);
                p_41423_.add(base);
            }
        }

    }



    /*============================================================================
    ==============================================================================
    =========================    FAKE PLAYER  START    ===========================
    ==============================================================================
    ============================================================================*/

    public static void tryEquipItem(ItemStack stack, WeakReference<FakePlayer> pedestalPlayer, InteractionHand hand)
    {
        if (pedestalPlayer == null) {
            return;
        }

        pedestalPlayer.get().setItemInHand(hand, stack);
    }

    public WeakReference<FakePlayer> fakeUpgradePlayer(BasePedestalBlockEntity pedestal)
    {
        Level world = pedestal.getLevel();
        ItemStack upgrade = pedestal.getCoinOnPedestal();
        if(world instanceof ServerLevel slevel)
        {
            return new WeakReference<FakePlayer>(new MowLibFakePlayer(slevel , MowLibOwnerUtils.getPlayerFromStack(upgrade), MowLibOwnerUtils.getPlayerNameFromStack(upgrade),pedestal.getPos(),pedestal.getToolStack(),"[Pedestal_"+ pedestal.getPos().getX() + pedestal.getPos().getY() + pedestal.getPos().getZ() +"]"));
        }
        else return null;
    }

    /*============================================================================
    ==============================================================================
    =========================     FAKE PLAYER  END     ===========================
    ==============================================================================
    ============================================================================*/


    /*============================================================================
    ==============================================================================
    =========================    Modifications Start   ===========================
    ==============================================================================
    ============================================================================*/

    //Allow Upgrades to Individually allow various modifications
    public boolean canModifySpeed(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyDamageCapacity(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyBlockCapacity(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyItemCapacity(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyFluidCapacity(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyEnergyCapacity(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyXPCapacity(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyDustCapacity(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyArea(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyRange(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyMagnet(ItemStack upgradeItemStack) { return false; }
    public boolean canModifyGentleHarvest(ItemStack upgradeItemStack) { return false; }
    public boolean canModifySuperSpeed(ItemStack upgradeItemStack) { return false; }

    public boolean canAddModifierToUpgrade(ItemStack upgradeItemStack, String nbtTagString)
    {
        switch(nbtTagString)
        {
            case "upgradespeed": return canModifySpeed(upgradeItemStack);
            case "upgradedamagecapacity": return canModifyDamageCapacity(upgradeItemStack);
            case "upgradeblockcapacity": return canModifyDamageCapacity(upgradeItemStack);
            case "upgradeitemcapacity": return canModifyItemCapacity(upgradeItemStack);
            case "upgradefluidcapacity": return canModifyFluidCapacity(upgradeItemStack);
            case "upgradeenergycapacity": return canModifyEnergyCapacity(upgradeItemStack);
            case "upgradexpcapacity": return canModifyXPCapacity(upgradeItemStack);
            case "upgradedustcapacity": return canModifyDustCapacity(upgradeItemStack);
            case "upgradearea": return canModifyArea(upgradeItemStack);
            case "upgraderange": return canModifyRange(upgradeItemStack);
            case "upgrademagnet": return canModifyMagnet(upgradeItemStack);
            case "upgradegentle": return canModifyGentleHarvest(upgradeItemStack);
            case "upgradesuperspeed": return canModifySuperSpeed(upgradeItemStack);
            default: return false;
        }
    }

    public int getSpeedTicksReduced(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradespeed"); }

    public int getDamageCapacityIncrease(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradedamagecapacity"); }
    public int getBlockCapacityIncrease(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradeblockcapacity"); }
    public int getItemCapacityIncrease(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradeitemcapacity"); }
    public int getFluidCapacityIncrease(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradefluidcapacity"); }
    public int getEnergyCapacityIncrease(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradeenergycapacity"); }
    public int getXPCapacityIncrease(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradexpcapacity"); }
    public int getDustCapacityIncrease(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradedustcapacity"); }

    public int getAreaIncrease(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradearea"); }
    public int getRangeIncrease(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgraderange"); }

    public boolean getMagnet(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgrademagnet")>=1; }
    public boolean getGentleHarvest(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradegentle")>=1; }
    public boolean getSuperSpeed(ItemStack upgradeItemStack) { return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,upgradeItemStack.getOrCreateTag(),"upgradesuperspeed")>=1; }

    /*============================================================================
    ==============================================================================
    =========================     Modifications End    ===========================
    ==============================================================================
    ============================================================================*/


}
