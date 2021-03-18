package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.util.PedestalFakePlayer;
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
import net.minecraft.item.PickaxeItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
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
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeEnergyQuarry extends ItemUpgradeBaseEnergyMachine
{
    public ItemUpgradeEnergyQuarry(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    //Gets Capacity and Speed By Default

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {return true;}

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
        return getRangeLargest(stack);
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

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            int width = getAreaWidth(pedestal.getCoinOnPedestal());
            int widdth = (width*2)+1;
            int height = getRangeHeight(pedestal.getCoinOnPedestal());
            int amount = workQueueSize(coin);
            int area = Math.multiplyExact(Math.multiplyExact(widdth,widdth),height);
            if(amount>0)
            {
                float f = (float)amount/(float)area;
                System.out.println(f);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    @Override
    public int intOperationalSpeedModifier(ItemStack stack)
    {
        int rate = 0;
        if(hasEnchant(stack))
        {
            rate = (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack) > 5)?(5):(EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack));
        }
        return rate;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isClientSide)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getBlockPos();

            //Set Default Energy Buffer
            int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
            if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}

            int rangeWidth = getAreaWidth(coinInPedestal);
            int rangeHeight = getRangeHeight(coinInPedestal);
            int speed = getOperationSpeed(coinInPedestal);

            BlockState pedestalState = world.getBlockState(pedestalPos);
            Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
            BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
            BlockPos posNums = getBlockPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));

            if(world.isAreaLoaded(negNums,posNums))
            {
                if(!world.hasNeighborSignal(pedestalPos)) {

                    if(hasEnergy(coinInPedestal))
                    {
                        //Should disable magneting when its not needed
                        AxisAlignedBB getBox = new AxisAlignedBB(negNums,posNums);
                        List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);
                        if(itemList.size()>0)
                        {
                            upgradeActionMagnet(world, itemList, itemInPedestal, pedestalPos);
                        }

                        int val = readStoredIntTwoFromNBT(coinInPedestal);
                        if(val>0)
                        {
                            if (world.getGameTime()%5 == 0) {
                                BlockPos directionalPos = getBlockPosOfBlockBelow(world,pedestalPos,0);
                                PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,directionalPos.getX(),directionalPos.getY(),directionalPos.getZ(),145,145,145));
                            }
                            writeStoredIntTwoToNBT(coinInPedestal,val-1);
                        }
                        else {

                            //If work queue doesnt exist, try to make one
                            if(workQueueSize(coinInPedestal)<=0)
                            {
                                buildWorkQueue(pedestal,rangeWidth,rangeHeight);
                            }

                            if(workQueueSize(coinInPedestal) > 0)
                            {
                                List<BlockPos> workQueue = readWorkQueueFromNBT(coinInPedestal);

                                int fuelToConsume = rfCostPerItemSmelted;
                                if(hasEnergy(coinInPedestal) && removeEnergyFuel(pedestal,fuelToConsume,true)>=0)
                                {
                                    if (world.getGameTime() % speed == 0) {
                                        for(int i = 0;i< workQueue.size(); i++)
                                        {
                                            BlockPos targetBlockPos = workQueue.get(i);
                                            BlockPos blockToMinePos = new BlockPos(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());
                                            BlockState targetBlock = world.getBlockState(targetBlockPos);
                                            if(canMineBlock(pedestal,blockToMinePos))
                                            {
                                                workQueue.remove(i);
                                                writeWorkQueueToNBT(coinInPedestal,workQueue);
                                                upgradeAction(pedestal, world, itemInPedestal, coinInPedestal, targetBlockPos, targetBlock, pedestalPos);
                                                break;
                                            }
                                            else
                                            {
                                                workQueue.remove(i);
                                            }
                                        }
                                        writeWorkQueueToNBT(coinInPedestal,workQueue);
                                    }
                                }
                            }
                            else {
                                int delay = rangeWidth*rangeWidth*rangeHeight;
                                writeStoredIntTwoToNBT(coinInPedestal,(delay<100)?(100):(delay));
                            }
                        }
                    }
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos blockToMinePos, BlockState blockToMine, BlockPos posOfPedestal)
    {
        int fuelToConsume = rfCostPerItemSmelted;
        if(canMineBlock(pedestal, blockToMinePos))
        {
            ItemStack pick = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_PICKAXE,1));
            FakePlayer fakePlayer = new PedestalFakePlayer((ServerWorld) world,getPlayerFromCoin(coinInPedestal),posOfPedestal,pick.copy());
            if(!fakePlayer.blockPosition().equals(new BlockPos(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ()))) {fakePlayer.setPos(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());}

            if(!pedestal.hasTool())
            {
                pick = getToolDefaultEnchanted(coinInPedestal,pick);
            }

            if(removeEnergyFuel(pedestal,fuelToConsume,true)>=0)
            {
                if(!fakePlayer.getHeldItemMainhand().equals(pick))fakePlayer.setHeldItem(Hand.MAIN_HAND,pick);
                ToolType tool = blockToMine.getHarvestTool();
                int toolLevel = fakePlayer.getHeldItemMainhand().getHarvestLevel(tool, fakePlayer, blockToMine);
                if (ForgeHooks.canHarvestBlock(blockToMine,fakePlayer,world,blockToMinePos)) {
                    blockToMine.getBlock().harvestBlock(world, fakePlayer, blockToMinePos, blockToMine, null, fakePlayer.getHeldItemMainhand());
                    blockToMine.getBlock().onBlockHarvested(world, blockToMinePos, blockToMine, fakePlayer);
                    int expdrop = blockToMine.getBlock().getExpDrop(blockToMine,world,blockToMinePos,
                            (EnchantmentHelper.getEnchantments(fakePlayer.getHeldItemMainhand()).containsKey(Enchantments.FORTUNE))?(EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE,fakePlayer.getHeldItemMainhand())):(0),
                            (EnchantmentHelper.getEnchantments(fakePlayer.getHeldItemMainhand()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH,fakePlayer.getHeldItemMainhand())):(0));
                    if(expdrop>0)blockToMine.getBlock().dropXpOnBlockBreak((ServerWorld)world,posOfPedestal,expdrop);
                    removeEnergyFuel(pedestal,fuelToConsume,false);
                    world.removeBlock(blockToMinePos, false);
                    PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,blockToMinePos.getX(),blockToMinePos.getY(),blockToMinePos.getZ(),255,164,0));
                }
            }
        }
    }

    @Override
    public boolean passesFilter(World world, BlockPos posPedestal, Block blockIn)
    {
        boolean returner = false;
        BlockPos posInventory = getBlockPosOfBlockBelow(world, posPedestal, 1);

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
                    returner = true;
                }
            }
        }

        return returner;
    }


    @Override
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

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.NIL_UUID);

        int s3 = getAreaWidth(stack);
        int s4 = getRangeHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";
        TranslationTextComponent area = new TranslationTextComponent(getDescriptionId() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getDescriptionId() + ".chat_areax");
        area.append(tr);
        area.append(areax.getString());
        area.append(trr);
        area.append(areax.getString());
        area.append(tr);
        area.withStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.NIL_UUID);

        //Display Blocks To Mine Left
        TranslationTextComponent btm = new TranslationTextComponent(getDescriptionId() + ".chat_btm");
        btm.append("" + ((workQueueSize(stack)>0)?(workQueueSize(stack)):(0)) + "");
        btm.withStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.NIL_UUID);

        //Display Fuel Left
        int fuelValue = getEnergyStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getDescriptionId() + ".chat_fuel");
        fuel.append("" + fuelValue/2500 + "");
        fuel.withStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,Util.NIL_UUID);

        ItemStack toolStack = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_PICKAXE));
        TranslationTextComponent tool = new TranslationTextComponent(getDescriptionId() + ".chat_tool");
        tool.append(toolStack.getDisplayName());
        tool.withStyle(TextFormatting.BLUE);
        player.sendMessage(tool,Util.NIL_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments((pedestal.hasTool())?(pedestal.getToolOnPedestal()):(stack));
        if(hasAdvancedInventoryTargeting(stack))
        {
            map.put(EnchantmentRegistry.ADVANCED,1);
        }
        if(map.size() > 0 && getNumNonPedestalEnchants(map)>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getDescriptionId() + ".chat_enchants");
            enchant.withStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.NIL_UUID);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea) && !(enchantment instanceof EnchantmentCapacity))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.withStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants,Util.NIL_UUID);
                }
            }
        }

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".chat_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent t = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        t.withStyle(TextFormatting.GOLD);
        tooltip.add(t);

        int s3 = getAreaWidth(stack);
        int s4 = getRangeHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";
        TranslationTextComponent area = new TranslationTextComponent(getDescriptionId() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getDescriptionId() + ".tooltip_areax");
        area.append(tr);
        area.append(areax.getString());
        area.append(trr);
        area.append(areax.getString());
        area.append(tr);
        area.withStyle(TextFormatting.WHITE);
        tooltip.add(area);

        TranslationTextComponent xpstored = new TranslationTextComponent(getDescriptionId() + ".tooltip_rfstored");
        xpstored.append(""+ getEnergyStored(stack) +"");
        xpstored.withStyle(TextFormatting.GREEN);
        tooltip.add(xpstored);

        TranslationTextComponent xpcapacity = new TranslationTextComponent(getDescriptionId() + ".tooltip_rfcapacity");
        xpcapacity.append(""+ getEnergyBuffer(stack) +"");
        xpcapacity.withStyle(TextFormatting.AQUA);
        tooltip.add(xpcapacity);

        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".tooltip_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item RFQUARRY = new ItemUpgradeEnergyQuarry(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rfquarry"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFQUARRY);
    }


}
