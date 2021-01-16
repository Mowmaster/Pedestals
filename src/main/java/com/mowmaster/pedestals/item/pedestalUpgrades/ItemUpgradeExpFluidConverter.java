package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.recipes.CrusherRecipe;
import com.mowmaster.pedestals.recipes.FluidtoExpConverterRecipe;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeExpFluidConverter extends ItemUpgradeBaseFluid
{
    public ItemUpgradeExpFluidConverter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    @Override
    public boolean canSendItem(PedestalTileEntity tile)
    {
        if(tile !=null)
        {
            FluidStack fluidInItem = getFluidInItem(tile.getItemInPedestal());
            return fluidInItem.isEmpty() || !getFluidStored(tile.getCoinOnPedestal()).isFluidEqual(fluidInItem);
        }
        return true;
    }

    @Override
    public int canAcceptCount(World world, BlockPos pos,ItemStack inPedestal, ItemStack itemStackIncoming) {

        //If incoming item has a fluid then set max stack to 1, if the pedestal has an item then 0, else allow normal transferring
        return (!getFluidInItem(itemStackIncoming).isEmpty())?(1):((inPedestal.isEmpty())?(itemStackIncoming.getMaxStackSize()):(0));
    }

    /*@Override
    public boolean canRecieveFluid(World world, BlockPos posPedestal, FluidStack fluidIncoming)
    {
        boolean returner = false;
        if(world.getTileEntity(posPedestal) instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = (PedestalTileEntity)world.getTileEntity(posPedestal);
            ItemStack coin = pedestal.getCoinOnPedestal();
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
            if(!(stackCurrent.size()>0))
            {
                stackCurrent = buildFilterQueue(pedestal);
                writeFilterQueueToNBT(coin,stackCurrent);
            }

            int range = stackCurrent.size();

            ItemStack itemFromInv = ItemStack.EMPTY;
            itemFromInv = IntStream.range(0,range)//Int Range
                    .mapToObj((stackCurrent)::get)//Function being applied to each interval
                    .filter(itemStack -> doesFluidBucketMatch(itemStack,fluidIncoming))
                    .findFirst().orElse(ItemStack.EMPTY);

            if(!itemFromInv.isEmpty())
            {
                returner = true;
            }
        }

        return returner;
    }*/

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{0,0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int speed = getOperationSpeed(coinInPedestal);

            if(getXPStored(coinInPedestal)>0)
            {
                upgradeActionSendExp(pedestal);
            }

            if(!world.isBlockPowered(pedestalPos)) {

                if (world.getGameTime() % speed == 0) {

                    if(hasFluidInCoin(coinInPedestal))
                    {
                        upgradeAction(pedestal);
                    }

                    upgradeActionItem(pedestal);
                    upgradeActionBlock(pedestal);
                }
            }
        }
    }

    public FluidStack getFluidInItem(ItemStack itemInPedestal)
    {
        //TODO: Maybe have a fluid recipe thingy for people to add other tiems that 'contain' fluids??? have an input, fluid, amount, and output for the recipe???
        if(FluidUtil.getFluidHandler(itemInPedestal).isPresent())
        {
            FluidStack fluidInItem = FluidUtil.getFluidContained(itemInPedestal).get();
            return fluidInItem;
        }
        return FluidStack.EMPTY;
    }

//https://github.com/mekanism/Mekanism/blob/be11c0df7d6ffece12da666b3100fc5e6d8ce0ab/src/main/java/mekanism/common/inventory/slot/IFluidHandlerSlot.java#L137
    public void upgradeActionItem(PedestalTileEntity pedestal)
    {
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();

        int getMaxXpValue = getExpCountByLevel(getExpBuffer(coinInPedestal));
        if(!hasMaxXpSet(coinInPedestal) || readMaxXpFromNBT(coinInPedestal) != getMaxXpValue) {setMaxXP(coinInPedestal, getMaxXpValue);}

        if(!itemInPedestal.isEmpty())
        {
            FluidStack fluidIn = getFluidInItem(itemInPedestal);
            if(!fluidIn.isEmpty())
            {
                FluidStack fluidInCoin = getFluidInItem(coinInPedestal);

                //https://github.com/mekanism/Mekanism/blob/be11c0df7d6ffece12da666b3100fc5e6d8ce0ab/src/main/java/mekanism/common/inventory/slot/IFluidHandlerSlot.java#L44
                Optional<IFluidHandlerItem> fluidContainerItemIn = FluidUtil.getFluidHandler(itemInPedestal).resolve();
                if(fluidContainerItemIn.isPresent())
                {
                    IFluidHandlerItem fluidHandlerItem = fluidContainerItemIn.get();
                    int tanks = fluidContainerItemIn.get().getTanks();

                    if(tanks > 1)
                    {
                        if(!fluidInCoin.isEmpty())
                        {
                            //Default grab from first tank
                            FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(0);
                            int amountIn = fluidInTank.getAmount();
                            int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                            int rate = getFluidTransferRate(coinInPedestal);
                            int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                            int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                            if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                            {
                                FluidStack estFluidToDrain = new FluidStack(fluidInTank,transferRate);
                                FluidStack fluidToActuallyDrain = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                if(!fluidInTank.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                {
                                    FluidStack fluidDrained = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                    addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                    ItemStack returnerStack = fluidHandlerItem.getContainer();
                                    pedestal.removeItemOverride();
                                    pedestal.addItem(returnerStack);
                                }
                            }
                        }
                        else
                        {
                            FluidStack fluidMatching = FluidStack.EMPTY;
                            fluidMatching = IntStream.range(0,tanks)//Int Range
                                    .mapToObj((fluidHandlerItem)::getFluidInTank)//Function being applied to each interval
                                    .filter(fluidStack -> fluidInCoin.isFluidEqual(fluidStack))
                                    .findFirst().orElse(FluidStack.EMPTY);

                            if(!fluidMatching.isEmpty())
                            {
                                int amountIn = fluidMatching.getAmount();
                                int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                int rate = getFluidTransferRate(coinInPedestal);
                                int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                {
                                    FluidStack estFluidToDrain = new FluidStack(fluidMatching,transferRate);
                                    FluidStack fluidToActuallyDrain = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                    if(!fluidMatching.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                    {
                                        FluidStack fluidDrained = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                        addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                        ItemStack returnerStack = fluidHandlerItem.getContainer();
                                        pedestal.removeItemOverride();
                                        pedestal.addItem(returnerStack);
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        //should i just set this to zero???
                        FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tanks-1);
                        if(fluidInCoin.isEmpty() || fluidInCoin.isFluidEqual(fluidInTank))
                        {
                            int amountIn = fluidInTank.getAmount();
                            int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                            int rate = getFluidTransferRate(coinInPedestal);
                            int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                            int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                            if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                            {
                                FluidStack estFluidToDrain = new FluidStack(fluidInTank,transferRate);
                                FluidStack fluidToActuallyDrain = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                if(!fluidInTank.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                {
                                    FluidStack fluidDrained = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                    addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                    ItemStack returnerStack = fluidHandlerItem.getContainer();
                                    pedestal.removeItemOverride();
                                    pedestal.addItem(returnerStack);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void upgradeActionBlock(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        BlockPos posPedestal = pedestal.getPos();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();

        BlockPos posInventory = getPosOfBlockBelow(world,posPedestal,1);
        ItemStack itemFromPedestal = ItemStack.EMPTY;

        LazyOptional<IFluidHandler> cap = findFluidHandlerAtPos(world,posInventory,getPedestalFacing(world, posPedestal),true);

        TileEntity invToPushTo = world.getTileEntity(posInventory);
        if(invToPushTo instanceof PedestalTileEntity) {
            itemFromPedestal = ItemStack.EMPTY;
        }
        else {
            if(cap.isPresent())
            {
                IFluidHandler handler = cap.orElse(null);
                if(handler != null)
                {
                    int tanks = handler.getTanks();
                    FluidStack fluidCheckedMatching = FluidStack.EMPTY;
                    fluidCheckedMatching = IntStream.range(0,tanks)//Int Range
                            .mapToObj((handler)::getFluidInTank)//Function being applied to each interval
                            .filter(fluidStack -> !fluidStack.isEmpty())
                            .findFirst().orElse(FluidStack.EMPTY);

                    if(!fluidCheckedMatching.isEmpty())
                    {
                        FluidStack fluidInCoin = getFluidStored(coinInPedestal);
                        if(tanks > 1)
                        {
                            if(!fluidInCoin.isEmpty())
                            {
                                //Default grab from first tank
                                FluidStack fluidInTank = handler.getFluidInTank(0);
                                int amountIn = fluidInTank.getAmount();
                                int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                int rate = getFluidTransferRate(coinInPedestal);
                                int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                {
                                    FluidStack estFluidToDrain = new FluidStack(fluidInTank,transferRate);
                                    FluidStack fluidToActuallyDrain = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                    if(!fluidInTank.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                    {
                                        FluidStack fluidDrained = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                        addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                    }
                                }
                            }
                            else
                            {
                                FluidStack fluidMatching = FluidStack.EMPTY;
                                fluidMatching = IntStream.range(0,tanks)//Int Range
                                        .mapToObj((handler)::getFluidInTank)//Function being applied to each interval
                                        .filter(fluidStack -> fluidInCoin.isFluidEqual(fluidStack))
                                        .findFirst().orElse(FluidStack.EMPTY);

                                if(!fluidMatching.isEmpty())
                                {
                                    int amountIn = fluidMatching.getAmount();
                                    int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                    int rate = getFluidTransferRate(coinInPedestal);
                                    int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                    int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                    if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                    {
                                        FluidStack estFluidToDrain = new FluidStack(fluidMatching,transferRate);
                                        FluidStack fluidToActuallyDrain = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                        if(!fluidMatching.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                        {
                                            FluidStack fluidDrained = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                            addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            //should i just set this to zero???
                            FluidStack fluidInTank = handler.getFluidInTank(tanks-1);
                            if(fluidInCoin.isEmpty() || fluidInCoin.isFluidEqual(fluidInTank))
                            {
                                int amountIn = fluidInTank.getAmount();
                                int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                int rate = getFluidTransferRate(coinInPedestal);
                                int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                {
                                    FluidStack estFluidToDrain = new FluidStack(fluidInTank,transferRate);
                                    FluidStack fluidToActuallyDrain = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                    if(!fluidInTank.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                    {
                                        FluidStack fluidDrained = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                        addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    protected FluidtoExpConverterRecipe getRecipe(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        //System.out.println(world == null ? null : world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, inv, world).orElse(null));
        return world == null ? null : world.getRecipeManager().getRecipe(FluidtoExpConverterRecipe.recipeType, inv, world).orElse(null);
    }

    protected Collection<ItemStack> getProcessResults(FluidtoExpConverterRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResult()));
    }

    public void upgradeAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();

        ItemStack bucketStack = new ItemStack(Items.BARRIER);
        if(removeFluid(pedestal,coinInPedestal, FluidAttributes.BUCKET_VOLUME,true))
        {
            FluidStack fluidToBucket = getFluidStored(coinInPedestal);
            Item bucket = fluidToBucket.getFluid().getFilledBucket();
            bucketStack = new ItemStack(bucket);
        }
        System.out.println(bucketStack);
        Collection<ItemStack> jsonResults = getProcessResults(getRecipe(world,bucketStack));
        //Just check to make sure our recipe output isnt air
        ItemStack resultSmelted = (jsonResults.iterator().next().isEmpty())?(ItemStack.EMPTY):(jsonResults.iterator().next());
        System.out.println(resultSmelted);
        if(!resultSmelted.isEmpty() && resultSmelted.getItem().equals(Items.EXPERIENCE_BOTTLE))
        {
            int outputCount = resultSmelted.getCount();
            int max = readMaxXpFromNBT(coinInPedestal);
            int current = getXPStored(coinInPedestal);
            int spaceXP = max-current;
            if(spaceXP>=outputCount)
            {
                setXPStored(coinInPedestal,current+outputCount);
                removeFluid(pedestal,coinInPedestal,FluidAttributes.BUCKET_VOLUME,false);
            }
        }
    }

    /*
    ============================
    EXP STUFF
    =============================
    */

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            if(getXPStored(coin)>0)
            {
                float f = (float)getXPStored(coin)/(float)readMaxXpFromNBT(coin);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public void setMaxXP(ItemStack stack, int value)
    {
        writeMaxXpToNBT(stack, value);
    }

    //Just set to 30 levels worth for all sending
    public int getExpTransferRate(ItemStack stack)
    {
        /*int summonRate = 55;
        switch (getCapacityModifier(stack))
        {

            case 0:
                summonRate = 55;//5
                break;
            case 1:
                summonRate=160;//10
                break;
            case 2:
                summonRate = 315;//15
                break;
            case 3:
                summonRate = 550;//20
                break;
            case 4:
                summonRate = 910;//25
                break;
            case 5:
                summonRate=1395;//30
                break;
            default: summonRate=55;
        }*/
        //Clearly capped at 1995 when everything else can exceed it wasnt gonna cut it
        int overEnchanted = getExpCountByLevel(((getCapacityModifierOverEnchanted(stack)*5)+5));

        //21,863 being the max before we get close to int overflow
        return  (overEnchanted>=20000)?(20000):(overEnchanted);
    }

    public int getExpTransferRateLevel(ItemStack stack)
    {
        /*int summonRate = 55;
        switch (getCapacityModifier(stack))
        {

            case 0:
                summonRate = 55;//5
                break;
            case 1:
                summonRate=160;//10
                break;
            case 2:
                summonRate = 315;//15
                break;
            case 3:
                summonRate = 550;//20
                break;
            case 4:
                summonRate = 910;//25
                break;
            case 5:
                summonRate=1395;//30
                break;
            default: summonRate=55;
        }*/
        //Clearly capped at 1995 when everything else can exceed it wasnt gonna cut it
        int overEnchanted = ((getCapacityModifierOverEnchanted(stack)*5)+5);

        //21,863 being the max before we get close to int overflow
        return  (overEnchanted>=20000)?(20000):(overEnchanted);
    }

    public String getExpTransferRateString(ItemStack stack)
    {
        return  ""+getExpTransferRateLevel(stack)+"";
    }


    public static int removeXp(PlayerEntity player, int amount) {
        //Someday consider using player.addExpierence()
        int startAmount = amount;
        while(amount > 0) {
            int barCap = player.xpBarCap();
            int barXp = (int) (barCap * player.experience);
            int removeXp = Math.min(barXp, amount);
            int newBarXp = barXp - removeXp;
            amount -= removeXp;//amount = amount-removeXp

            player.experienceTotal -= removeXp;
            if(player.experienceTotal < 0) {
                player.experienceTotal = 0;
            }
            if(newBarXp == 0 && amount > 0) {
                player.experienceLevel--;
                if(player.experienceLevel < 0) {
                    player.experienceLevel = 0;
                    player.experienceTotal = 0;
                    player.experience = 0;
                    break;
                } else {
                    player.experience = 1.0F;
                }
            } else {
                player.experience = newBarXp / (float) barCap;
            }
        }
        return startAmount - amount;
    }

    public void upgradeActionSendExp(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinMainPedestal = pedestal.getCoinOnPedestal();
        BlockPos posMainPedestal = pedestal.getPos();

        int xpMainPedestal = getXPStored(coinMainPedestal);
        if(xpMainPedestal>0)
        {
            //Grab the connected pedestals to send to
            if(pedestal.getNumberOfStoredLocations()>0)
            {
                for(int i=0; i<pedestal.getNumberOfStoredLocations();i++)
                {
                    BlockPos posStoredPedestal = pedestal.getStoredPositionAt(i);
                    //Make sure pedestal ISNOT powered and IS loaded in world
                    if(!world.isBlockPowered(posStoredPedestal) && world.isBlockLoaded(posStoredPedestal))
                    {
                        if(posStoredPedestal != posMainPedestal)
                        {
                            TileEntity storedPedestal = world.getTileEntity(posStoredPedestal);
                            if(storedPedestal instanceof PedestalTileEntity) {
                                PedestalTileEntity tileStoredPedestal = ((PedestalTileEntity) storedPedestal);
                                ItemStack coinStoredPedestal = tileStoredPedestal.getCoinOnPedestal();
                                //Check if pedestal to send to can even be sent exp
                                if(coinStoredPedestal.getItem() instanceof ItemUpgradeBaseExp)
                                {
                                    int xpMaxStoredPedestal = ((ItemUpgradeBaseExp)coinStoredPedestal.getItem()).readMaxXpFromNBT(coinStoredPedestal);
                                    int xpStoredPedestal = getXPStored(coinStoredPedestal);
                                    //if Stored Pedestal has room for exp (will be lazy sending exp here)
                                    if(xpStoredPedestal < xpMaxStoredPedestal)
                                    {
                                        int transferRate = getExpTransferRate(coinMainPedestal);
                                        //If we have more then X levels in the pedestal we're sending from
                                        if(xpMainPedestal >= transferRate)
                                        {
                                            int xpRemainingMainPedestal = xpMainPedestal - transferRate;
                                            int xpRemainingStoredPedestal = xpStoredPedestal + transferRate;
                                            world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.05F, 1.0F);
                                            setXPStored(coinMainPedestal,xpRemainingMainPedestal);
                                            pedestal.update();
                                            world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.05F, 1.0F);
                                            setXPStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                            tileStoredPedestal.update();
                                        }
                                        else
                                        {
                                            //If we have less then X levels, just send them all.
                                            int xpRemainingMainPedestal = 0;
                                            int xpRemainingStoredPedestal = xpStoredPedestal + xpMainPedestal;
                                            world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.05F, 1.0F);
                                            setXPStored(coinMainPedestal,xpRemainingMainPedestal);
                                            pedestal.update();
                                            world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.05F, 1.0F);
                                            setXPStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                            tileStoredPedestal.update();
                                        }

                                        break;
                                    }
                                }
                                else if(coinStoredPedestal.getItem() instanceof ItemUpgradeBaseExpFilter)
                                {
                                    int xpMaxStoredPedestal = ((ItemUpgradeBaseExpFilter)coinStoredPedestal.getItem()).readMaxXpFromNBT(coinStoredPedestal);
                                    int xpStoredPedestal = getXPStored(coinStoredPedestal);
                                    //if Stored Pedestal has room for exp (will be lazy sending exp here)
                                    if(xpStoredPedestal < xpMaxStoredPedestal)
                                    {
                                        int transferRate = getExpTransferRate(coinMainPedestal);
                                        //If we have more then X levels in the pedestal we're sending from
                                        if(xpMainPedestal >= transferRate)
                                        {
                                            int xpRemainingMainPedestal = xpMainPedestal - transferRate;
                                            int xpRemainingStoredPedestal = xpStoredPedestal + transferRate;
                                            world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.05F, 1.0F);
                                            setXPStored(coinMainPedestal,xpRemainingMainPedestal);
                                            pedestal.update();
                                            world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.05F, 1.0F);
                                            setXPStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                            tileStoredPedestal.update();
                                        }
                                        else
                                        {
                                            //If we have less then X levels, just send them all.
                                            int xpRemainingMainPedestal = 0;
                                            int xpRemainingStoredPedestal = xpStoredPedestal + xpMainPedestal;
                                            world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.05F, 1.0F);
                                            setXPStored(coinMainPedestal,xpRemainingMainPedestal);
                                            pedestal.update();
                                            world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.05F, 1.0F);
                                            setXPStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                            tileStoredPedestal.update();
                                        }

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ExperienceOrbEntity)
        {
            ItemStack coin = tilePedestal.getCoinOnPedestal();
            ExperienceOrbEntity getXPFromList = ((ExperienceOrbEntity)entityIn);
            world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.15F, 1.0F);
            int currentlyStoredExp = getXPStored(coin);
            if(currentlyStoredExp < readMaxXpFromNBT(coin))
            {
                int value = getXPFromList.getXpValue();
                getXPFromList.remove();
                setXPStored(coin, currentlyStoredExp + value);
                tilePedestal.update();
            }
        }
    }

    public int getExpCountByLevel(int level)
    {
        int expUsed = 0;

        if(level <= 16)
        {
            expUsed = (level*level) + (6 * level);
        }
        else if(level > 16 && level <=31)
        {
            expUsed = (int)(((2.5 * (level*level)) - (40.5 * level))+360);
        }
        else if(level > 31)
        {
            expUsed = (int)(((4.5 * (level*level)) - (162.5 * level))+2220);
        }

        return expUsed;
    }

    public int getExpLevelFromCount(int value)
    {
        int level = 0;
        long maths = 0;
        int i = 0;
        int j = 0;

        if(value > 0 && value <= 352)
        {
            maths = (long)Math.sqrt(Math.addExact((long)36, Math.addExact((long)4,(long)value )));
            i = (int)(Math.round(Math.addExact((long)-6 , maths) / 2));
        }
        if(value > 352 && value <= 1507)
        {
            maths = (long)Math.sqrt(Math.subtractExact((long)164025, Math.multiplyExact((long)100,Math.subtractExact((long)3600,Math.multiplyExact((long)10,(long)value)))));

            i = (int)(Math.addExact((long)405 , maths) / 50);
        }
        if(value > 1507)
        {

            maths = (long)Math.sqrt(Math.subtractExact((long)2640625,Math.multiplyExact((long)180, Math.subtractExact((long)22200,Math.multiplyExact((long)10,(long)value)))));
            i = (int)(Math.addExact((long)1625 , maths) / 90);
        }

        return Math.abs(i);
    }

    public int spaceForXP(ItemStack coin)
    {
        int max = readMaxXpFromNBT(coin);
        int stored = getXPStored(coin);
        int space = max-stored;
        return (space>0)?(space):(0);
    }

    public void setXPStored(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("xpstored",value);
        stack.setTag(compound);
    }

    public int getXPStored(ItemStack stack)
    {
        int storedxp = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            storedxp = getCompound.getInt("xpstored");
        }
        return storedxp;
    }

    public boolean hasMaxXpSet(ItemStack stack)
    {
        boolean returner = false;
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("maxxp"))
            {
                returner = true;
            }
        }
        return returner;
    }


    public void writeMaxXpToNBT(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("maxxp",value);
        stack.setTag(compound);
    }

    public int readMaxXpFromNBT(ItemStack stack)
    {
        int maxxp = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            maxxp = getCompound.getInt("maxxp");
        }
        return maxxp;
    }

    public int getExpBuffer(ItemStack stack)
    {
        return  30;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_xp");
        xpstored.appendString(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored, Util.DUMMY_UUID);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getTranslationKey() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getTranslationKey() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getTranslationKey() + ".chat_fluidseperator");
            fluid.appendString("" + fluidStored.getDisplayName().getString() + "");
            fluid.appendString(fluidSplit.getString());
            fluid.appendString("" + fluidStored.getAmount() + "");
            fluid.appendString(fluidLabel.getString());
            fluid.mergeStyle(TextFormatting.BLUE);
            player.sendMessage(fluid,Util.DUMMY_UUID);
        }

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getTranslationKey() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getTranslationKey() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getTranslationKey() + ".chat_fluidseperator");
            fluid.appendString("" + fluidStored.getDisplayName().getString() + "");
            fluid.appendString(fluidSplit.getString());
            fluid.appendString("" + fluidStored.getAmount() + "");
            fluid.appendString(fluidLabel.getString());
            fluid.mergeStyle(TextFormatting.BLUE);
            tooltip.add(fluid);
        }

        TranslationTextComponent fluidcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_fluidcapacity");
        fluidcapacity.appendString(""+ getFluidbuffer(stack) +"");
        fluidcapacity.appendString(fluidLabel.getString());
        fluidcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(fluidcapacity);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.appendString(fluidLabel.getString());
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item FLUIDXPCONVERTER = new ItemUpgradeExpFluidConverter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidxpconverter"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDXPCONVERTER);
    }


}
