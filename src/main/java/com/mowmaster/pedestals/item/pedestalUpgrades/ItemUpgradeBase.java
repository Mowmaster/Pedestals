package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.pedestals;
import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.horse.DonkeyEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.passive.horse.MuleEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeEntityMinecart;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeBase extends Item {

    public ItemUpgradeBase(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    public void onRandomDisplayTick(TilePedestal pedestal, BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {

    }

    public boolean hasEnchant(ItemStack stack)
    {
        return stack.isEnchanted();
    }

    public int getRangeModifier(ItemStack stack)
    {
        int range = 0;
        if(hasEnchant(stack))
        {
            range = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.RANGE,stack);
        }
        return range;
    }

    public int getCapacityModifier(ItemStack stack)
    {
        int capacity = 0;
        if(hasEnchant(stack))
        {
            capacity = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.CAPACITY,stack);
        }
        return capacity;
    }

    public int intOperationalSpeedModifier(ItemStack stack)
    {
        int rate = 0;
        if(hasEnchant(stack))
        {
            rate = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack);
        }
        return rate;
    }

    public int getOperationSpeed(ItemStack stack)
    {
        int intOperationalSpeed = 20;
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                intOperationalSpeed = 20;//normal speed
                break;
            case 1:
                intOperationalSpeed=10;//2x faster
                break;
            case 2:
                intOperationalSpeed = 5;//4x faster
                break;
            case 3:
                intOperationalSpeed = 3;//6x faster
                break;
            case 4:
                intOperationalSpeed = 2;//10x faster
                break;
            case 5:
                intOperationalSpeed=1;//20x faster
                break;
            default: intOperationalSpeed=20;
        }

        return  intOperationalSpeed;
    }

    public String getOperationSpeedString(ItemStack stack)
    {
        String str = "Normal Speed";
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                str = "Normal Speed";//normal speed
                break;
            case 1:
                str = "2x Faster";//2x faster
                break;
            case 2:
                str = "4x Faster";//4x faster
                break;
            case 3:
                str = "6x Faster";//6x faster
                break;
            case 4:
                str = "10x Faster";//10x faster
                break;
            case 5:
                str = "20x Faster";//20x faster
                break;
            default: str = "Normal Speed";;
        }

        return  str;
    }

    public int getItemTransferRate(ItemStack stack)
    {
        int transferRate = 1;
        switch (getCapacityModifier(stack))
        {
            case 0:
                transferRate = 1;
                break;
            case 1:
                transferRate=4;
                break;
            case 2:
                transferRate = 8;
                break;
            case 3:
                transferRate = 16;
                break;
            case 4:
                transferRate = 32;
                break;
            case 5:
                transferRate=64;
                break;
            default: transferRate=1;
        }

        return  transferRate;
    }

    public int getRange(ItemStack stack)
    {
        int range = 1;
        switch (getRangeModifier(stack))
        {
            case 0:
                range = 1;
                break;
            case 1:
                range = 2;
                break;
            case 2:
                range = 4;
                break;
            case 3:
                range = 8;
                break;
            case 4:
                range = 12;
                break;
            case 5:
                range = 16;
                break;
            default: range = 1;
        }

        return  range;
    }


    public ItemStack getStackInPedestal(World world, BlockPos posOfPedestal)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof TilePedestal) {
            stackInPedestal = ((TilePedestal) pedestalInventory).getItemInPedestal();
        }

        return stackInPedestal;
    }

    public void removeFromPedestal(World world, BlockPos posOfPedestal, int count)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof TilePedestal) {
            ((TilePedestal) pedestalInventory).removeItem(count);
        }
    }

    public int canAddToPedestal(World world, BlockPos posOfPedestal, ItemStack itemStackToAdd)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        int returner = 0;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof TilePedestal) {
            returner =  ((TilePedestal) pedestalInventory).canAcceptItems(itemStackToAdd);
        }

        return returner;
    }

    public void addToPedestal(World world, BlockPos posOfPedestal, ItemStack itemStackToAdd)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof TilePedestal) {
            ((TilePedestal) pedestalInventory).addItem(itemStackToAdd);
        }
    }

    /*public void addExpToPedestal(World world, BlockPos posOfPedestal,int expToAdd)
    {
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof TilePedestal) {
            ((TilePedestal) pedestalInventory).addExpToPedestal(expToAdd);
        }
    }
    public void removeExpFromPedestal(World world, BlockPos posOfPedestal,int expToRemove)
    {
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof TilePedestal) {
            ((TilePedestal) pedestalInventory).removeExpFromPedestal(expToRemove);
        }
    }*/

    public void setIntValueToPedestal(World world, BlockPos posOfPedestal, int value)
    {
        TileEntity pedestal = world.getTileEntity(posOfPedestal);
        if(pedestal instanceof TilePedestal) {
            ((TilePedestal) pedestal).setStoredValueForUpgrades(value);
        }
    }

    public int getIntValueFromPedestal(World world, BlockPos posOfPedestal)
    {
        int value = 0;
        TileEntity pedestal = world.getTileEntity(posOfPedestal);
        if(pedestal instanceof TilePedestal) {
            value = ((TilePedestal) pedestal).getStoredValueForUpgrades();
        }

        return value;
    }

    public int intSpaceLeftInStack (ItemStack stack)
    {
        int value = 0;
        if(stack.equals(ItemStack.EMPTY))
        {
            value = 64;
        }
        else
        {
            int maxSize = stack.getMaxStackSize();
            int currentSize = stack.getCount();
            value = maxSize-currentSize;
        }

        return value;
    }

    public boolean doItemsMatch(ItemStack stackPedestal, ItemStack itemStackIn)
    {
        /*if(!stackPedestal.equals(ItemStack.EMPTY))
        {
            if(itemStackIn.hasTag())
            {
                CompoundNBT itemIn = itemStackIn.getTag();
                CompoundNBT itemStored = stackPedestal.getTag();
                if(itemIn.equals(itemStored) && itemStackIn.getItem().equals(stackPedestal.getItem()))
                {
                    return true;
                }
                else return false;
            }
            *//*else if(itemStackIn.isDamaged())
            {
                if(itemStackIn.isDamaged()==stackPedestal.getDamage() && itemStackIn.getMetadata()==stackPedestal.getMetadata())
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }*//*
            else
            {
                if(itemStackIn.getItem().equals(stackPedestal.getItem()))
                {
                    return true;
                }
            }
        }*/

        return ItemHandlerHelper.canItemStacksStack(stackPedestal,itemStackIn);
    }

    //For Filters to return if they can or cannot allow items to pass
    //Will probably need overwritten
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        return false;
    }

    public int canAcceptCount(ItemStack inPedestal, ItemStack itemStackIncoming)
    {
        int stackabe = itemStackIncoming.getMaxStackSize();
        return stackabe;
    }

    //All credit for this goes to https://github.com/BluSunrize/ImmersiveEngineering/blob/f40a49da570c991e51dd96bba1d529e20da6caa6/src/main/java/blusunrize/immersiveengineering/api/ApiUtils.java#L338
    //TODO: Alter later to fit style in refactoring
    public static LazyOptional<IItemHandler> findItemHandlerAtPos(World world, BlockPos pos, Direction side, boolean allowCart)
    {
        TileEntity neighbourTile = world.getTileEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            if(AbstractRailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof IForgeEntityMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IItemHandler> findItemHandlerAtPosBlockAndEntity(World world, BlockPos pos, Direction side, boolean allowCart)
    {
        TileEntity neighbourTile = world.getTileEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof IForgeEntityMinecart || entity instanceof DonkeyEntity || entity instanceof LlamaEntity || entity instanceof MuleEntity);
            if(!list.isEmpty())
            {
                LazyOptional<IItemHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                if(cap.isPresent())
                    return cap;
            }
        }
        return LazyOptional.empty();
    }

    //Mainly Used in the Import, Furnace, and  Milker Upgrades
    /*
        This Method gets the next slot with items in the given tile
     */

    public int getNextSlotWithItemsCap(LazyOptional<IItemHandler> cap, ItemStack stackInPedestal)
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
                            if(stackInPedestal.isEmpty())
                            {
                                slot.set(i);
                                break;
                            }
                            //if stack in pedestal matches items in slot
                            else if(doItemsMatch(stackInPedestal,stackInSlot))
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




    public int getNextSlotWithItems(TileEntity invBeingChecked, Direction sideSlot, ItemStack stackInPedestal)
    {
        int slot = -1;
        if(invBeingChecked.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,sideSlot ).isPresent()) {
            IItemHandler handler = (IItemHandler) invBeingChecked.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, sideSlot).orElse(null);
            int range = handler.getSlots();
            for(int i=0;i<range;i++)
            {
                ItemStack stackInSlot = handler.getStackInSlot(i);
                //find a slot with items
                if(!stackInSlot.isEmpty())
                {
                    //check if it could pull the item out or not
                    if(!handler.extractItem(i,1 ,true ).equals(ItemStack.EMPTY))
                    {
                        //If pedestal is empty accept any items
                        if(stackInPedestal.isEmpty())
                        {
                            slot=i;
                            break;
                        }
                        //if stack in pedestal matches items in slot
                        else if(doItemsMatch(stackInPedestal,stackInSlot))
                        {
                            slot=i;
                            break;
                        }
                    }
                }
            }
        }

        return slot;
    }


    /**
     * Can this hopper insert the specified item from the specified slot on the specified side?
     */
    public static boolean canInsertItemInSlot(IInventory inventoryIn, ItemStack stack, int index, Direction side)
    {
        if (!inventoryIn.isItemValidForSlot(index, stack))
        {
            return false;
        }
        else
        {
            return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canInsertItem(index, stack, side);
        }
    }

    /**
     * Can this hopper extract the specified item from the specified slot on the specified side?
     */
    public static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, Direction side)
    {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canExtractItem(index, stack, side);
    }

    public int[] getSlotsForSide(World world, BlockPos posOfPedestal, IInventory inventory)
    {
        int[] slots = new int[]{};

        if(inventory instanceof ISidedInventory)
        {
            slots= ((ISidedInventory) inventory).getSlotsForFace(getPedestalFacing(world, posOfPedestal));
        }

        return slots;
    }


    /*public boolean canInsertIntoSide(World world, BlockPos posOfPedestal, IInventory inventory, ItemStack itemFromPedestal, int slot)
    {
        boolean value = false;
        if(inventory instanceof ISidedInventory)
        {
            int[] slots= ((ISidedInventory) inventory).getSlotsForFace(getPedestalFacing(world, posOfPedestal));
            for(int i=0;i<slots.length;i++)
            {
                if(canInsertItemInSlot(inventory,itemFromPedestal,slots[i],getPedestalFacing(world, posOfPedestal)))
                {
                    value=true;
                }
                else
                {
                    value=false;
                    break;
                }
            }
        }
        else
        {
            if(canInsertItemInSlot(inventory,itemFromPedestal,slot,getPedestalFacing(world, posOfPedestal))) value=true;
        }
        return value;
    }*/

    public BlockPos getPosOfBlockBelow(World world, BlockPos posOfPedestal, int numBelow)
    {
        BlockState state = world.getBlockState(posOfPedestal);

        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(0,-numBelow,0);
            case DOWN:
                return blockBelow.add(0,numBelow,0);
            case NORTH:
                return blockBelow.add(0,0,numBelow);
            case SOUTH:
                return blockBelow.add(0,0,-numBelow);
            case EAST:
                return blockBelow.add(-numBelow,0,0);
            case WEST:
                return blockBelow.add(numBelow,0,0);
            default:
                return blockBelow;
        }
    }



    public BlockPos getNegRangePos(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(-intWidth,0,-intWidth);
            case DOWN:
                return blockBelow.add(-intWidth,-intHeight,-intWidth);
            case NORTH:
                return blockBelow.add(-intWidth,-intWidth,-intHeight);
            case SOUTH:
                return blockBelow.add(-intWidth,-intWidth,0);
            case EAST:
                return blockBelow.add(0,-intWidth,-intWidth);
            case WEST:
                return blockBelow.add(-intHeight,-intWidth,-intWidth);
            default:
                return blockBelow;
        }
    }

    public BlockPos getPosRangePos(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(intWidth,intHeight,intWidth);
            case DOWN:
                return blockBelow.add(intWidth,0,intWidth);
            case NORTH:
                return blockBelow.add(intWidth,intWidth,0);
            case SOUTH:
                return blockBelow.add(intWidth,intWidth,intHeight);
            case EAST:
                return blockBelow.add(intHeight,intWidth,intWidth);
            case WEST:
                return blockBelow.add(0,intWidth,intWidth);
            default:
                return blockBelow;
        }
    }

    public BlockPos getNegRangePosEntity(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(-intWidth,0,-intWidth);
            case DOWN:
                return blockBelow.add(-intWidth,-intHeight,-intWidth);
            case NORTH:
                return blockBelow.add(-intWidth,-intWidth,-intHeight);
            case SOUTH:
                return blockBelow.add(-intWidth,-intWidth,0);
            case EAST:
                return blockBelow.add(0,-intWidth,-intWidth);
            case WEST:
                return blockBelow.add(-intHeight,-intWidth,-intWidth);
            default:
                return blockBelow;
        }
    }

    public BlockPos getPosRangePosEntity(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(intWidth+1,intHeight,intWidth+1);
            case DOWN:
                return blockBelow.add(intWidth+1,0,intWidth+1);
            case NORTH:
                return blockBelow.add(intWidth+1,intWidth,0+1);
            case SOUTH:
                return blockBelow.add(intWidth+1,intWidth,intHeight+1);
            case EAST:
                return blockBelow.add(intHeight+1,intWidth,intWidth+1);
            case WEST:
                return blockBelow.add(0+1,intWidth,intWidth+1);
            default:
                return blockBelow;
        }
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return !EnchantmentRegistry.COINUPGRADE.equals(enchantment.type) && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }


    public Boolean canAcceptOpSpeed()
    {
        return true;
    }

    public Boolean canAcceptCapacity()
    {
        return false;
    }

    public Boolean canAcceptRange()
    {
        return false;
    }

    public Direction getPedestalFacing(World world, BlockPos posOfPedestal)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        return state.get(FACING);
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {

    }

    public void actionOnCollideWithBlock(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {

    }

    public boolean canSendItem()
    {
        return true;
    }

    //Thanks to Lothrazar for this: https://github.com/Lothrazar/Cyclic/blob/5946452faedd1a59375f7813f5ec9f861914ed8a/src/main/java/com/lothrazar/cyclic/base/BlockBase.java#L59
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.func_240699_a_(TextFormatting.GOLD);
        tooltip.add(t);
    }

}
