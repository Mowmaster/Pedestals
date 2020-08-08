package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnergyQuarryBlacklist extends ItemUpgradeBaseEnergyMachine
{
    public ItemUpgradeEnergyQuarryBlacklist(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    //Gets Capacity and Speed By Default

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    public int getAreaWidth(ItemStack stack)
    {
        int areaWidth = 0;
        int aW = getAreaModifier(stack);
        areaWidth = ((aW)+1);
        return  areaWidth;
    }

    public int getRangeHeight(ItemStack stack)
    {
        return getHeight(stack);
    }

    public int getHeight(ItemStack stack)
    {
        int height = 8;
        switch (getRangeModifier(stack))
        {
            case 0:
                height = 8;
                break;
            case 1:
                height=16;
                break;
            case 2:
                height = 24;
                break;
            case 3:
                height = 32;
                break;
            case 4:
                height = 48;
                break;
            case 5:
                height=64;
                break;
            default: height=8;
        }

        return  height;
    }

    public int ticked = 0;

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            //Set Default Energy Buffer
            int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
            if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}

            int rangeWidth = getAreaWidth(coinInPedestal);
            int rangeHeight = getRangeHeight(coinInPedestal)-1;
            int speed = getOperationSpeed(coinInPedestal);
            int breakspeed = Math.multiplyExact(Math.multiplyExact((int)(Math.pow((Math.multiplyExact(rangeWidth,2)+1),2)/9),20),84);

            BlockPos negNums = getNegRangePos(world,pedestalPos,rangeWidth,rangeHeight);
            BlockPos posNums = getPosRangePos(world,pedestalPos,rangeWidth,rangeHeight);
            if(world.isAreaLoaded(negNums,posNums))
            {
                if(!world.isBlockPowered(pedestalPos)) {

                    TileEntity pedestalInv = world.getTileEntity(pedestalPos);
                    if(pedestalInv instanceof TilePedestal) {
                        TilePedestal ped = ((TilePedestal) pedestalInv);
                        //Cost To Mine 1 Block
                        int fuelToConsume = rfCostPerItemSmelted;
                        if(hasEnergy(coinInPedestal) && removeEnergyFuel(ped,fuelToConsume,true)>=0)
                        {
                            upgradeActionMagnet(world, itemInPedestal, pedestalPos, rangeWidth, rangeHeight);

                            for (int x = negNums.getX(); x <= posNums.getX(); x++) {
                                for (int z = negNums.getZ(); z <= posNums.getZ(); z++) {
                                    for (int y = negNums.getY(); y <= posNums.getY(); y++) {
                                        BlockPos blockToChopPos = new BlockPos(x, y, z);
                                        //BlockPos blockToChopPos = this.getPos().add(x, y, z);
                                        BlockState blockToChop = world.getBlockState(blockToChopPos);
                                        if (tick%speed == 0) {
                                            ticked++;
                                        }

                                        if(ticked > breakspeed)
                                        {
                                            upgradeAction(world, itemInPedestal, coinInPedestal, blockToChopPos, blockToChop, pedestalPos);
                                            ticked=0;
                                        }
                                        else
                                        {
                                            ticked++;
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

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos blockToMinePos, BlockState blockToMine, BlockPos posOfPedestal)
    {
        int fuelToConsume = rfCostPerItemSmelted;
        if(!blockToMine.getBlock().isAir(blockToMine,world,blockToMinePos) && !(blockToMine.getBlock() instanceof BlockPedestalTE) && canMineBlock(world, posOfPedestal, blockToMine.getBlock())
                && !(blockToMine.getBlock() instanceof IFluidBlock || blockToMine.getBlock() instanceof FlowingFluidBlock) && blockToMine.getBlockHardness(world, blockToMinePos) != -1.0F)
        {
            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().func_241755_D_());
            fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());
            ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE,1);

            if(EnchantmentHelper.getEnchantments(coinInPedestal).containsKey(Enchantments.SILK_TOUCH))
            {
                pick.addEnchantment(Enchantments.SILK_TOUCH,1);
                fakePlayer.setHeldItem(Hand.MAIN_HAND,pick);
            }
            else if (EnchantmentHelper.getEnchantments(coinInPedestal).containsKey(Enchantments.FORTUNE))
            {
                int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE,coinInPedestal);
                pick.addEnchantment(Enchantments.FORTUNE,lvl);
                fakePlayer.setHeldItem(Hand.MAIN_HAND,pick);
            }
            else
            {
                fakePlayer.setHeldItem(Hand.MAIN_HAND,pick);
            }
            TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
            if(pedestalInv instanceof TilePedestal) {
                TilePedestal ped = ((TilePedestal) pedestalInv);
                if(removeEnergyFuel(ped,fuelToConsume,true)>=0)
                {
                    /*if(ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToMine,true))
                    {
                        blockToMine.getBlock().harvestBlock(world, fakePlayer, blockToMinePos, blockToMine, null, fakePlayer.getHeldItemMainhand());
                        removeFuel(ped,200,false);
                        world.setBlockState(blockToMinePos, Blocks.AIR.getDefaultState());
                    }*/
                    if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToMine,true)) {

                        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, blockToMinePos, blockToMine, fakePlayer);
                        if (!MinecraftForge.EVENT_BUS.post(e)) {
                            blockToMine.getBlock().harvestBlock(world, fakePlayer, blockToMinePos, blockToMine, null, fakePlayer.getHeldItemMainhand());
                            blockToMine.getBlock().onBlockHarvested(world, blockToMinePos, blockToMine, fakePlayer);
                            removeEnergyFuel(ped,fuelToConsume,false);
                            world.removeBlock(blockToMinePos, false);
                        }
                        //world.setBlockState(posOfBlock, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    public void upgradeActionMagnet(World world, ItemStack itemInPedestal, BlockPos posOfPedestal, int width, int height)
    {
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

        List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);
        for(ItemEntity getItemFromList : itemList)
        {
            if (itemInPedestal.equals(ItemStack.EMPTY))
            {
                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
                TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                if(pedestalInv instanceof TilePedestal) {
                    if(getItemFromList.getItem().getCount() <=64)
                    {
                        getItemFromList.remove();
                        ((TilePedestal) pedestalInv).addItem(getItemFromList.getItem());
                    }
                    else
                    {
                        int count = getItemFromList.getItem().getCount();
                        getItemFromList.getItem().setCount(count-64);
                        ItemStack getItemstacked = getItemFromList.getItem().copy();
                        getItemstacked.setCount(64);
                        ((TilePedestal) pedestalInv).addItem(getItemstacked);
                    }
                }
                break;
            }
        }
    }

    public boolean canMineBlock(World world, BlockPos posPedestal, Block blockIn)
    {
        boolean returner = true;
        BlockPos posInventory = getPosOfBlockBelow(world, posPedestal, 1);

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posPedestal),true);
        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            if(handler != null)
            {
                int range = handler.getSlots();

                ItemStack itemFromInv = ItemStack.EMPTY;
                itemFromInv = IntStream.range(0,range)//Int Range
                        .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                        .filter(itemStack -> Block.getBlockFromItem(itemStack.getItem()).equals(blockIn))
                        .findFirst().orElse(ItemStack.EMPTY);

                if(!itemFromInv.isEmpty())
                {
                    returner = false;
                }
            }
        }

        return returner;
    }

    public int blocksToMineInArea(World world, BlockPos pedestalPos, int width, int height)
    {
        int validBlocks = 0;

        BlockPos negNums = getNegRangePos(world,pedestalPos,width,height);
        BlockPos posNums = getPosRangePos(world,pedestalPos,width,height);
        for (int x = negNums.getX(); x <= posNums.getX(); x++) {
            for (int z = negNums.getZ(); z <= posNums.getZ(); z++) {
                for (int y = negNums.getY(); y <= posNums.getY(); y++) {
                    BlockPos blockToChopPos = new BlockPos(x, y, z);
                    //BlockPos blockToChopPos = this.getPos().add(x, y, z);
                    BlockState blockToChop = world.getBlockState(blockToChopPos);
                    if(canMineBlock(world,pedestalPos,blockToChop.getBlock()))validBlocks++;
                }
            }
        }

        return validBlocks;
    }



    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());

        int s3 = getAreaWidth(stack);
        int s4 = getRangeHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,player.getUniqueID());

        //Display Blocks To Mine Left
        TranslationTextComponent btm = new TranslationTextComponent(getTranslationKey() + ".chat_btm");
        btm.appendString("" + blocksToMineInArea(pedestal.getWorld(),pedestal.getPos(),getAreaWidth(pedestal.getCoinOnPedestal()),getRangeHeight(pedestal.getCoinOnPedestal())) + "");
        btm.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,player.getUniqueID());

        //Display Fuel Left
        int fuelValue = getEnergyStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.appendString("" + fuelValue/2500 + "");
        fuel.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,player.getUniqueID());

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        if(map.size() > 0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_enchants");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,player.getUniqueID());

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea) && !(enchantment instanceof EnchantmentCapacity))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants,player.getUniqueID());
                }
            }
        }

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getSmeltingSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,player.getUniqueID());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        int s3 = getAreaWidth(stack);
        int s4 = getRangeHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfstored");
        xpstored.appendString(""+ getEnergyStored(stack) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(xpstored);

        TranslationTextComponent xpcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfcapacity");
        xpcapacity.appendString(""+ getEnergyBuffer(stack) +"");
        xpcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(xpcapacity);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item RFQUARRYB = new ItemUpgradeEnergyQuarryBlacklist(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rfquarryb"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFQUARRYB);
    }


}
