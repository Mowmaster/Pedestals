package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnergyField extends ItemUpgradeBaseEnergy
{
    public ItemUpgradeEnergyField(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public boolean canAcceptArea() {
        return true;
    }

    @Override
    public boolean canAcceptRange() {return true;}

    @Override
    public boolean canAcceptAdvanced() {
        return true;
    }

    public int getAreaWidth(ItemStack stack)
    {
        int areaWidth = 0;
        int aW = getAreaModifier(stack);
        areaWidth = ((aW)+1);
        return  areaWidth;
    }

    public int getHeight(ItemStack stack)
    {
        return  getRangeTiny(stack);
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{getHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int speed = getOperationSpeed(coinInPedestal);
            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
                if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}

                if (world.getGameTime()%200 == 0) {
                    writeStoredIntTwoToNBT(coinInPedestal,-1);
                }

                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal, world, itemInPedestal, coinInPedestal, pedestalPos);
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int width = getAreaWidth(coinInPedestal);
        int height = getHeight(coinInPedestal);

        //Cooldown
        int val = readStoredIntTwoFromNBT(coinInPedestal);
        if(val<0)
        {
            //Trigger a rebuild of Queue
            removeWorkQueueFromCoin(coinInPedestal);
            writeStoredIntTwoToNBT(coinInPedestal,0);
        }

        //If work queue doesnt exist, try to make one
        if(workQueueSize(coinInPedestal)<=0)
        {
            buildWorkQueue(pedestal,width,height);
        }


        //Cooldown if workqueue is ever 0
        if(val>0)
        {
            writeStoredIntTwoToNBT(coinInPedestal,val-1);
        }
        else
        {
            if(workQueueSize(coinInPedestal) > 0)
            {
                List<BlockPos> workQueue = readWorkQueueFromNBT(coinInPedestal);
                for(int i = 0;i< workQueue.size(); i++)
                {
                    BlockPos targetPos = workQueue.get(i);
                    BlockPos blockToMinePos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                    BlockState targetBlock = world.getBlockState(blockToMinePos);
                    if(canMineBlock(pedestal,blockToMinePos))
                    {
                        sendEnergyToBlocks(pedestal,blockToMinePos);
                    }
                    else
                    {
                        workQueue.remove(i);
                    }
                }
                writeWorkQueueToNBT(coinInPedestal,workQueue);
            }
            else {
                //5 second cooldown
                writeStoredIntTwoToNBT(coinInPedestal,100);
            }
        }
    }

    private boolean sendEnergyToBlocks(PedestalTileEntity pedestal, BlockPos target)
    {
        World world = pedestal.getWorld();
        BlockPos posOfPedestal = pedestal.getPos();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        if(world.getTileEntity(target) instanceof PedestalTileEntity)
        {
            //Already pre checked if its a compatable upgraded one
            sendEnergyToPedestal(pedestal,((PedestalTileEntity)world.getTileEntity(target)));
            return true;
        }
        LazyOptional<IEnergyStorage> cap = findEnergyHandlerAtPos(world,target,getPedestalFacing(world, posOfPedestal),true);
        if(cap.isPresent())
        {
            IEnergyStorage handler = cap.orElse(null);

            if(handler != null)
            {
                if(handler.canReceive())
                {
                    int containerMaxEnergy = handler.getMaxEnergyStored();
                    int containerCurrentEnergy = handler.getEnergyStored();
                    int containerEnergySpace = containerMaxEnergy - containerCurrentEnergy;
                    int getCurrentEnergy = getEnergyStored(coinInPedestal);
                    int transferRate = (containerEnergySpace >= getEnergyTransferRate(coinInPedestal))?(getEnergyTransferRate(coinInPedestal)):(containerEnergySpace);
                    if (getCurrentEnergy < transferRate) {transferRate = getCurrentEnergy;}

                    //transferRate at this point is equal to what we can send.
                    if(handler.receiveEnergy(transferRate,true) > 0)
                    {
                        int energyRemainingInUpgrade = getCurrentEnergy - transferRate;
                        setEnergyStored(coinInPedestal,energyRemainingInUpgrade);
                        return handler.receiveEnergy(transferRate,false) > 0;
                    }
                }
            }
        }

        return false;
    }

    private boolean isEnergyPedestal(PedestalTileEntity pedestal, BlockPos blockToCheck)
    {
        boolean returner = false;
        World world = pedestal.getWorld();
        if(world.getBlockState(blockToCheck).getBlock() instanceof PedestalBlock)
        {
            if(world.getTileEntity(blockToCheck) instanceof PedestalTileEntity)
            {
                PedestalTileEntity pedestalBeingChecked = ((PedestalTileEntity)world.getTileEntity(blockToCheck));
                ItemStack coin = pedestalBeingChecked.getCoinOnPedestal();
                if(coin.getItem() instanceof ItemUpgradeBaseEnergy)
                {
                    returner = true;
                }
            }
        }
        return returner;
    }


    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos, PlayerEntity player)
    {
        World world = pedestal.getWorld();
        BlockState blockStateEnergyConsumer = world.getBlockState(blockToMinePos);
        Block blockEnergyConsumer = blockStateEnergyConsumer.getBlock();
        if(!blockEnergyConsumer.isAir(blockStateEnergyConsumer,world,blockToMinePos))
        {
            LazyOptional<IEnergyStorage> cap = findEnergyHandlerAtPos(world,blockToMinePos,getPedestalFacing(world, pedestal.getPos()),false);
            if(cap.isPresent() || isEnergyPedestal(pedestal,blockToMinePos))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos)
    {
        World world = pedestal.getWorld();
        BlockState blockStateEnergyConsumer = world.getBlockState(blockToMinePos);
        Block blockEnergyConsumer = blockStateEnergyConsumer.getBlock();
        BlockPos pedestalPos = pedestal.getPos();

        if(!blockEnergyConsumer.isAir(blockStateEnergyConsumer,world,blockToMinePos) || !pedestalPos.equals(blockToMinePos))
        {
            LazyOptional<IEnergyStorage> cap = findEnergyHandlerAtPos(world,blockToMinePos,getPedestalFacing(world, pedestal.getPos()),false);
            if(cap.isPresent() || isEnergyPedestal(pedestal,blockToMinePos))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("" + getHeight(stack) + "");
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area, Util.DUMMY_UUID);

        TranslationTextComponent rfstored = new TranslationTextComponent(getTranslationKey() + ".chat_rfstored");
        rfstored.appendString(""+ getEnergyStored(stack) +"");
        rfstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(rfstored,Util.DUMMY_UUID);

        TranslationTextComponent energyRate = new TranslationTextComponent(getTranslationKey() + ".chat_rfrate");
        energyRate.appendString(""+ getEnergyTransferRate(stack) +"");
        energyRate.mergeStyle(TextFormatting.AQUA);
        player.sendMessage(energyRate,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("" + getHeight(stack) + "");
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);
    }

    public static final Item ENERGYFIELD = new ItemUpgradeEnergyField(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rffield"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENERGYFIELD);
    }


}
