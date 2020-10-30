package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeBase extends Item {

    public ItemUpgradeBase(Properties builder) {super(builder.group(PEDESTALS_TAB));}


    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {

    }

    public boolean hasEnchant(ItemStack stack)
    {
        return stack.isEnchanted();
    }

    public int getAreaModifier(ItemStack stack)
    {
        int area = 0;
        if(hasEnchant(stack))
        {
            area = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.AREA,stack);
        }
        return area;
    }

    public int getRangeModifier(ItemStack stack)
    {
        int range = 0;
        if(hasEnchant(stack))
        {
            range = (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.RANGE,stack) > 5)?(5):(EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.RANGE,stack));
        }
        return range;
    }

    public int getCapacityModifier(ItemStack stack)
    {
        int capacity = 0;
        if(hasEnchant(stack))
        {
            capacity = (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.CAPACITY,stack) > 5)?(5):(EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.CAPACITY,stack));
        }
        return capacity;
    }

    public int getAdvancedModifier(ItemStack stack)
    {
        int advanced = 0;
        if(hasEnchant(stack))
        {
            advanced = (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.ADVANCED,stack) > 1)?(1):(EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.ADVANCED,stack));
        }
        return advanced;
    }

    public boolean hasAdvancedInventoryTargeting(ItemStack stack)
    {
        return getAdvancedModifier(stack)>=1;
    }

    public int intOperationalSpeedModifier(ItemStack stack)
    {
        int rate = 0;
        if(hasEnchant(stack))
        {
            rate = (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack) > 5)?(5):(EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack));
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
        TranslationTextComponent normal = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_0");
        TranslationTextComponent twox = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_1");
        TranslationTextComponent fourx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_2");
        TranslationTextComponent sixx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_3");
        TranslationTextComponent tenx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_4");
        TranslationTextComponent twentyx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_5");
        String str = normal.getString();
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                str = normal.getString();//normal speed
                break;
            case 1:
                str = twox.getString();//2x faster
                break;
            case 2:
                str = fourx.getString();//4x faster
                break;
            case 3:
                str = sixx.getString();//6x faster
                break;
            case 4:
                str = tenx.getString();//10x faster
                break;
            case 5:
                str = twentyx.getString();//20x faster
                break;
            default: str = normal.getString();;
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

    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
    }

    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{0,0};
    }

    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
    }

    public Block getBaseBlockBelow(World world, BlockPos pedestalPos)
    {
        Block block = world.getBlockState(getPosOfBlockBelow(world,pedestalPos,1)).getBlock();

        /*ITag.INamedTag<Block> BLOCK_EMERALD = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/emerald"));
        ITag.INamedTag<Block> BLOCK_DIAMOND = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/diamond"));
        ITag.INamedTag<Block> BLOCK_GOLD = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/gold"));
        ITag.INamedTag<Block> BLOCK_LAPIS = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/lapis"));
        ITag.INamedTag<Block> BLOCK_IRON = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/iron"));
        ITag.INamedTag<Block> BLOCK_COAL = BlockTags.createOptional(new ResourceLocation("forge", "storage_blocks/coal"));*/
        ITag<Block> BLOCK_EMERALD = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/emerald"));
        ITag<Block> BLOCK_DIAMOND = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/diamond"));
        ITag<Block> BLOCK_GOLD = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/gold"));
        ITag<Block> BLOCK_LAPIS = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/lapis"));
        ITag<Block> BLOCK_IRON = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/iron"));
        ITag<Block> BLOCK_COAL = BlockTags.getCollection().get(new ResourceLocation("forge", "storage_blocks/coal"));

        //Netherite
        if(block.equals(Blocks.NETHERITE_BLOCK)) return Blocks.NETHERITE_BLOCK;
        if(BLOCK_EMERALD.contains(block)) return Blocks.EMERALD_BLOCK;//Players
        if(BLOCK_DIAMOND.contains(block)) return Blocks.DIAMOND_BLOCK;//All Monsters
        if(BLOCK_GOLD.contains(block)) return Blocks.GOLD_BLOCK;//All Animals
        if(BLOCK_LAPIS.contains(block)) return Blocks.LAPIS_BLOCK;//All Flying
        if(BLOCK_IRON.contains(block)) return Blocks.IRON_BLOCK;//All Creatures
        if(BLOCK_COAL.contains(block)) return Blocks.COAL_BLOCK;//All Mobs

        return block;
    }

    public LivingEntity getTargetEntity(World world, BlockPos pedestalPos, Entity entityIn)
    {
        if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.EMERALD_BLOCK))
        {
            if(entityIn instanceof PlayerEntity)
            {
                return (PlayerEntity)entityIn;
            }
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.DIAMOND_BLOCK))
        {
            if(entityIn instanceof MonsterEntity)
            {
                return (MonsterEntity)entityIn;
            }
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.GOLD_BLOCK))
        {
            if(entityIn instanceof AnimalEntity)
            {
                return (AnimalEntity)entityIn;
            }
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.LAPIS_BLOCK))
        {
            if(entityIn instanceof FlyingEntity)
            {
                return (FlyingEntity)entityIn;
            }
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.IRON_BLOCK))
        {
            if(entityIn instanceof CreatureEntity)
            {
                return (CreatureEntity)entityIn;
            }
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.COAL_BLOCK))
        {
            if(entityIn instanceof MobEntity)
            {
                return (MobEntity)entityIn;
            }
        }
        else
        {
            return (LivingEntity)entityIn;
        }
        return null;
    }

    public String getTargetEntity(World world, BlockPos pedestalPos)
    {
        TranslationTextComponent EMERALD = new TranslationTextComponent(getTranslationKey() + ".entity_emerald");
        TranslationTextComponent DIAMOND = new TranslationTextComponent(getTranslationKey() + ".entity_diamond");
        TranslationTextComponent GOLD = new TranslationTextComponent(getTranslationKey() + ".entity_gold");
        TranslationTextComponent LAPIS = new TranslationTextComponent(getTranslationKey() + ".entity_lapis");
        TranslationTextComponent IRON = new TranslationTextComponent(getTranslationKey() + ".entity_iron");
        TranslationTextComponent COAL = new TranslationTextComponent(getTranslationKey() + ".entity_coal");
        TranslationTextComponent ALL = new TranslationTextComponent(getTranslationKey() + ".entity_all");

        if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.EMERALD_BLOCK))
        {
            return EMERALD.getString();
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.DIAMOND_BLOCK))
        {
            return DIAMOND.getString();
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.GOLD_BLOCK))
        {
            return GOLD.getString();
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.LAPIS_BLOCK))
        {
            return LAPIS.getString();
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.IRON_BLOCK))
        {
            return IRON.getString();
        }
        else if(getBaseBlockBelow(world,pedestalPos).equals(Blocks.COAL_BLOCK))
        {
            return COAL.getString();
        }
        else {
            return ALL.getString();
        }
    }

    public ItemStack getStackInPedestal(World world, BlockPos posOfPedestal)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof PedestalTileEntity) {
            stackInPedestal = ((PedestalTileEntity) pedestalInventory).getItemInPedestal();
        }

        return stackInPedestal;
    }

    public void removeFromPedestal(World world, BlockPos posOfPedestal, int count)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof PedestalTileEntity) {
            ((PedestalTileEntity) pedestalInventory).removeItem(count);
        }
    }

    public int canAddToPedestal(World world, BlockPos posOfPedestal, ItemStack itemStackToAdd)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        int returner = 0;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof PedestalTileEntity) {
            returner =  ((PedestalTileEntity) pedestalInventory).canAcceptItems(world,posOfPedestal,itemStackToAdd);
        }

        return returner;
    }

    public void addToPedestal(World world, BlockPos posOfPedestal, ItemStack itemStackToAdd)
    {
        ItemStack stackInPedestal = ItemStack.EMPTY;
        TileEntity pedestalInventory = world.getTileEntity(posOfPedestal);
        if(pedestalInventory instanceof PedestalTileEntity) {
            ((PedestalTileEntity) pedestalInventory).addItem(itemStackToAdd);
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
        if(pedestal instanceof PedestalTileEntity) {
            ((PedestalTileEntity) pedestal).setStoredValueForUpgrades(value);
        }
    }

    public int getIntValueFromPedestal(World world, BlockPos posOfPedestal)
    {
        int value = 0;
        TileEntity pedestal = world.getTileEntity(posOfPedestal);
        if(pedestal instanceof PedestalTileEntity) {
            value = ((PedestalTileEntity) pedestal).getStoredValueForUpgrades();
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

    public int canAcceptCount(World world, BlockPos posPedestal, ItemStack inPedestal, ItemStack itemStackIncoming)
    {
        int stackabe = itemStackIncoming.getMaxStackSize();
        return stackabe;
    }

    //Info Used to create this goes to https://github.com/BluSunrize/ImmersiveEngineering/blob/f40a49da570c991e51dd96bba1d529e20da6caa6/src/main/java/blusunrize/immersiveengineering/api/ApiUtils.java#L338
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
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof BoatEntity);
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

    public static LazyOptional<IItemHandler> findItemHandlerAtPosAdvanced(World world, BlockPos pos, Direction side, boolean allowEntity)
    {
        TileEntity neighbourTile = world.getTileEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowEntity)
        {
            //Added for quark boats with inventories (i hope)
            //List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof BoatEntity);
            List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof Entity);
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

    public int getSlotWithMatchingStack(LazyOptional<IItemHandler> cap, ItemStack stackToFind)
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
                        if(stackInSlot.isItemEqual(stackToFind))
                        {
                            slot.set(i);
                            break;
                        }
                    }
                }});


        }

        return slot.get();
    }

    public int getSlotWithMatchingStackExact(LazyOptional<IItemHandler> cap, ItemStack stackToFind)
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
                        if(ItemHandlerHelper.canItemStacksStack(stackInSlot,stackToFind))//stackInSlot.isItemEqual(stackToFind)
                        {
                            slot.set(i);
                            break;
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

    //Trying to work on a way to do better block checks
    public BlockPos getPosOfNextBlock(int currentPosition, BlockPos negCorner, BlockPos posCorner)
    {
        int xRange = Math.abs(posCorner.getX() - negCorner.getX());
        int yRange = Math.abs(posCorner.getY() - negCorner.getY());
        int zRange = Math.abs(posCorner.getZ() - negCorner.getZ());
        int layerVolume = xRange*zRange;
        int addY = (int)Math.floor(currentPosition/layerVolume);
        int layerCurrentPosition = currentPosition - addY*layerVolume;
        int addZ = (int)Math.floor(layerCurrentPosition/xRange);
        int addX = layerCurrentPosition - addZ*xRange;

        return negCorner.add(addX,addY,addZ);
    }
    /*
    if we have a range of 3x3x3
    layer = x val * z val
    cube = layer * y val
    layer #1-3,4-6,7-9
    x val = 1-3,1-3,1-3
    zval = 1,2,3

    Y_Value = (int) total count / layer value : should give us which layer we're on

    LAYER_COUNT = total count - Y_Value*layer value

    Z_Value = (int) LAYER_COUNT / xRange

    X_Value = LAYER_COUNT - Z_Value*xRange

    */
    public boolean resetCurrentPosInt(int currentPosition, BlockPos negCorner, BlockPos posCorner)
    {
        int xRange = Math.abs(posCorner.getX() - negCorner.getX());
        int yRange = Math.abs(posCorner.getY() - negCorner.getY());
        int zRange = Math.abs(posCorner.getZ() - negCorner.getZ());
        int layerVolume = xRange*zRange;

        int addY = (int)Math.floor(currentPosition/layerVolume);

        return addY >= yRange;
    }

    public boolean canMineBlock(World world, BlockPos posPedestal, Block blockIn)
    {
        return false;
    }

    public int blocksToMineInArea(World world, BlockPos pedestalPos, int width, int height)
    {
        int validBlocks = 0;

        BlockPos negNums = getNegRangePos(world,pedestalPos,width,height);
        BlockPos posNums = getPosRangePos(world,pedestalPos,width,height);
        for (int x = negNums.getX(); x <= posNums.getX(); x++) {
            for (int z = negNums.getZ(); z <= posNums.getZ(); z++) {
                for (int y = negNums.getY(); y <= posNums.getY(); y++) {
                    BlockPos blockToMinePos = new BlockPos(x, y, z);
                    BlockState blockToMineState = world.getBlockState(blockToMinePos);
                    if(!blockToMineState.getBlock().isAir(blockToMineState,world,blockToMinePos) && !(blockToMineState.getBlock() instanceof PedestalBlock) && canMineBlock(world, pedestalPos, blockToMineState.getBlock())
                            && !(blockToMineState.getBlock() instanceof IFluidBlock || blockToMineState.getBlock() instanceof FlowingFluidBlock) && blockToMineState.getBlockHardness(world, blockToMinePos) != -1.0F)validBlocks++;
                }
            }
        }

        return validBlocks;
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

    public Boolean canAcceptAdvanced()
    {
        return false;
    }

    public Boolean canAcceptArea()
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

    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {

    }

    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {

    }

    public void spawnParticleAroundPedestalBase(World world, int tick, BlockPos pos, BasicParticleType parti)
    {
        double dx = (double)pos.getX();
        double dy = (double)pos.getY();
        double dz = (double)pos.getZ();

        BlockState state = world.getBlockState(pos);
        Direction enumfacing = Direction.UP;
        if(state.getBlock() instanceof PedestalBlock)
        {
            enumfacing = state.get(FACING);
        }
        switch (enumfacing)
        {
            case UP:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                return;
            case DOWN:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+.85D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+.85D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+.85D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+.85D, dz+ 0.75D,0, 0, 0);
                return;
            case NORTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+.85D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+.85D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+.85D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+.85D,0, 0, 0);
                return;
            case SOUTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+0.15D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+0.15D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+0.15D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+0.15D,0, 0, 0);
                return;
            case EAST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.25D, dz+0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.25D, dz+0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.75D, dz+0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.75D, dz+0.75D,0, 0, 0);
                return;
            case WEST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.85D, dy+0.25D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.85D, dy+0.25D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.85D, dy+0.75D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.85D, dy+0.75D, dz+ 0.75D,0, 0, 0);
                return;
            default:
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%35 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                return;
        }
    }

    public void spawnParticleAroundPedestalBase(World world,int tick, BlockPos pos, float r, float g, float b, float alpha)
    {
        double dx = (double)pos.getX();
        double dy = (double)pos.getY();
        double dz = (double)pos.getZ();

        BlockState state = world.getBlockState(pos);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = pos;
        RedstoneParticleData parti = new RedstoneParticleData(r, g, b, alpha);
        switch (enumfacing)
        {
            case UP:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                return;
            case DOWN:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+.85D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+.85D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+.85D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+.85D, dz+ 0.75D,0, 0, 0);
                return;
            case NORTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+.85D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+.85D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+.85D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+.85D,0, 0, 0);
                return;
            case SOUTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+0.15D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+0.15D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+0.15D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+0.15D,0, 0, 0);
                return;
            case EAST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.25D, dz+0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.25D, dz+0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.75D, dz+0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.15D, dy+ 0.75D, dz+0.75D,0, 0, 0);
                return;
            case WEST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.85D, dy+0.25D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.85D, dy+0.25D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.85D, dy+0.75D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.85D, dy+0.75D, dz+ 0.75D,0, 0, 0);
                return;
            default:
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%35 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.15D, dz+ 0.75D,0, 0, 0);
                return;
        }
    }

    public void spawnParticleAbovePedestal(World world, BlockPos pos, float r, float g, float b, float alpha)
    {
        double dx = (double)pos.getX();
        double dy = (double)pos.getY();
        double dz = (double)pos.getZ();
        BlockState state = world.getBlockState(pos);
        Direction enumfacing = state.get(FACING);
        RedstoneParticleData parti = new RedstoneParticleData(r, g, b, alpha);
        switch (enumfacing)
        {
            case UP:
                world.addParticle(parti, dx+ 0.5D, dy+1.0D, dz+ 0.5D,0, 0, 0);
                return;
            case DOWN:
                world.addParticle(parti, dx+ 0.5D, dy-1.0D, dz+ 0.5D,0, 0, 0);
                return;
            case NORTH:
                world.addParticle(parti, dx+ 0.5D, dy+0.5D, dz-1.0D,0, 0, 0);
                return;
            case SOUTH:
                world.addParticle(parti, dx+ 0.5D, dy+0.5D, dz+1.0D,0, 0, 0);
                return;
            case EAST:
                world.addParticle(parti, dx+1.0D, dy+ 0.5D, dz+0.5D,0, 0, 0);
                return;
            case WEST:
                world.addParticle(parti, dx-1.0D, dy+0.5D, dz+ 0.5D,0, 0, 0);
                return;
            default:
                world.addParticle(parti, dx+ 0.5D, dy+1.0D, dz+ 0.5D,0, 0, 0);
                return;
        }
    }

    public boolean canSendItem(PedestalTileEntity tile)
    {
        return true;
    }

    //Thanks to Lothrazar for this: https://github.com/Lothrazar/Cyclic/blob/5946452faedd1a59375f7813f5ec9f861914ed8a/src/main/java/com/lothrazar/cyclic/base/BlockBase.java#L59
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);
    }

}
