package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeQuarryBlacklist extends ItemUpgradeBaseMachine
{
    public ItemUpgradeQuarryBlacklist(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    @Override
    public Boolean canAcceptCapacity() {
        return false;
    }

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

    @Override
    public int getRangeModifier(ItemStack stack)
    {
        int range = 0;
        if(hasEnchant(stack))
        {
            range = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.RANGE,stack);
        }
        return range;
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
            default: height=(getRangeModifier(stack)*12);
        }

        return  height;
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
            int height = getRangeHeight(pedestal.getCoinOnPedestal());
            int amount = blocksToMineInArea(pedestal.getWorld(),pedestal.getPos(),width,height);
            int area = Math.multiplyExact(Math.multiplyExact(amount,amount),height);
            if(amount>0)
            {
                float f = (float)amount/(float)area;
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public int ticked = 0;

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int getMaxFuelValue = 2000000000;
            if(!hasMaxFuelSet(coinInPedestal) || readMaxFuelFromNBT(coinInPedestal) != getMaxFuelValue) {setMaxFuel(coinInPedestal, getMaxFuelValue);}

            int rangeWidth = getAreaWidth(coinInPedestal);
            int rangeHeight = getRangeHeight(coinInPedestal);
            int speed = getOperationSpeed(coinInPedestal);
            //int breakspeed = Math.multiplyExact(Math.multiplyExact((int)(Math.pow((Math.multiplyExact(rangeWidth,2)+1),2)/9),20),84);

            BlockState pedestalState = world.getBlockState(pedestalPos);
            Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
            BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
            BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
            //System.out.println("World Loaded: " + world.isAreaLoaded(negNums,posNums));

            if(world.isAreaLoaded(negNums,posNums))
            {
                if(!world.isBlockPowered(pedestalPos)) {

                    //Should disable magneting when its not needed
                    AxisAlignedBB getBox = new AxisAlignedBB(negNums,posNums);
                    List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);
                    if(itemList.size()>0)
                    {
                        upgradeActionMagnet(world, itemList, itemInPedestal, pedestalPos, rangeWidth, rangeHeight);
                    }

                    if(removeFuel(pedestal,200,true))
                    {
                        if(blocksToMineInArea(world,pedestalPos,rangeWidth,rangeHeight) > 0)
                        {
                            if(!world.isBlockPowered(pedestalPos)) {
                                if (world.getGameTime() % speed == 0) {
                                    int currentPosition = 0;
                                    for(currentPosition = pedestal.getStoredValueForUpgrades();!resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));currentPosition++)
                                    {
                                        BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                        BlockPos blockToMinePos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                        BlockState blockToMineState = world.getBlockState(blockToMinePos);
                                        Block blockToMine = blockToMineState.getBlock();
                                        if(!blockToMine.isAir(blockToMineState,world,blockToMinePos) && !(blockToMine instanceof PedestalBlock) && canMineBlock(world, pedestalPos, blockToMine)
                                                && !(blockToMine instanceof IFluidBlock || blockToMine instanceof FlowingFluidBlock) && blockToMineState.getBlockHardness(world, blockToMinePos) != -1.0F)
                                        {
                                            pedestal.setStoredValueForUpgrades(currentPosition);
                                            break;
                                        }
                                    }
                                    BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                    BlockState targetBlock = world.getBlockState(targetPos);
                                    /*BlockPos getNextTargetPos = getNextMineableBlock(world,pedestalPos,rangeWidth,rangeHeight);
                                    BlockState getNextTargetBlock = world.getBlockState(getNextTargetPos);
                                    upgradeAction(world, itemInPedestal, coinInPedestal, getNextTargetPos, getNextTargetBlock, pedestalPos);*/
                                    upgradeAction(world, itemInPedestal, coinInPedestal, targetPos, targetBlock, pedestalPos);
                                    if(resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums)))
                                    {
                                        pedestal.setStoredValueForUpgrades(0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            /*else {
                if(pedestal.getStoredValueForUpgrades()!=0)
                {
                    pedestal.setStoredValueForUpgrades(0);
                }
            }*/
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos blockToMinePos, BlockState blockToMine, BlockPos posOfPedestal)
    {
        if(!blockToMine.getBlock().isAir(blockToMine,world,blockToMinePos) && !(blockToMine.getBlock() instanceof PedestalBlock) && canMineBlock(world, posOfPedestal, blockToMine.getBlock())
                && !(blockToMine.getBlock() instanceof IFluidBlock || blockToMine.getBlock() instanceof FlowingFluidBlock) && blockToMine.getBlockHardness(world, blockToMinePos) != -1.0F)
        {
            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
            //FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().func_241755_D_());
            fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());
            ItemStack pick = new ItemStack(Items.NETHERITE_PICKAXE,1);

            if(!fakePlayer.getHeldItemMainhand().equals(itemInPedestal))
            {
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
            }
            TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
            if(pedestalInv instanceof PedestalTileEntity) {
                PedestalTileEntity ped = ((PedestalTileEntity) pedestalInv);
                if(removeFuel(ped,200,true))
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
                            removeFuel(ped,200,false);
                            world.removeBlock(blockToMinePos, false);
                            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,blockToMinePos.getX(),blockToMinePos.getY(),blockToMinePos.getZ(),255,164,0));
                            //PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.HARVESTED,blockToMinePos.getX(),blockToMinePos.getY()-0.5f,blockToMinePos.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                        }
                        //world.setBlockState(posOfBlock, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    public void upgradeActionMagnet(World world, List<ItemEntity> itemList, ItemStack itemInPedestal, BlockPos posOfPedestal, int width, int height)
    {
        for(ItemEntity getItemFromList : itemList)
        {
            ItemStack copyStack = getItemFromList.getItem().copy();
            if (itemInPedestal.equals(ItemStack.EMPTY))
            {
                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
                TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                if(pedestalInv instanceof PedestalTileEntity) {
                    if(copyStack.getCount() <=64)
                    {
                        getItemFromList.setItem(ItemStack.EMPTY);
                        getItemFromList.remove();
                        ((PedestalTileEntity) pedestalInv).addItem(copyStack);
                    }
                    else
                    {
                        //If an ItemStackEntity has more than 64, we subtract 64 and inset 64 into the pedestal
                        int count = getItemFromList.getItem().getCount();
                        getItemFromList.getItem().setCount(count-64);
                        copyStack.setCount(64);
                        ((PedestalTileEntity) pedestalInv).addItem(copyStack);
                    }
                }
                break;
            }
        }

    }

    public int blocksToMineInArea(World world, BlockPos pedestalPos, int width, int height)
    {
        int validBlocks = 0;
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToMinePos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            BlockState blockToMineState = world.getBlockState(blockToMinePos);
            Block blockToMine = blockToMineState.getBlock();
            if(!blockToMine.isAir(blockToMineState,world,blockToMinePos) && !(blockToMine instanceof PedestalBlock) && canMineBlock(world, pedestalPos, blockToMine)
                    && !(blockToMine instanceof IFluidBlock || blockToMine instanceof FlowingFluidBlock) && blockToMineState.getBlockHardness(world, blockToMinePos) != -1.0F)
            {
                validBlocks++;
            }
        }

        return validBlocks;
    }

    public BlockPos getNextMineableBlock(World world, BlockPos pedestalPos, int width, int height)
    {
        BlockPos returnBlockPos = pedestalPos;
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        /*int xRange = Math.abs(posNums.getX() - negNums.getX());
        int yRange = Math.abs(posNums.getY() - negNums.getY());
        int zRange = Math.abs(posNums.getZ() - negNums.getZ());
        int largeVal = xRange * yRange * zRange;

        returnBlockPos = IntStream.range(0,largeVal)
                .mapToObj(i -> getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums)))
                .filter(targetPos -> !world.getBlockState(targetPos).getBlock().isAir(world.getBlockState(targetPos),world,targetPos))
                .filter(targetPos -> !(world.getBlockState(targetPos).getBlock() instanceof PedestalBlock))
                .filter(targetPos -> canMineBlock(world, pedestalPos, world.getBlockState(targetPos).getBlock()))
                .filter(targetPos -> !(world.getBlockState(targetPos).getBlock() instanceof IFluidBlock || world.getBlockState(targetPos).getBlock() instanceof FlowingFluidBlock))
                .filter(targetPos -> world.getBlockState(targetPos).getBlockHardness(world, targetPos) != -1.0F)
                .findFirst().orElse(pedestalPos);

        Most Optimized IntStream solution offered by @sciwhiz12#1286 in forge discord
        Im just too dumb to get it to work...
        Optional<BlockPos> pos = BlockPos.getAllInBoxMutable(firstPos, secondPos)
                .filter(<your filter here, recommending you use a method reference>)
        .findFirst()
            .map(pos -> pos.toImmutable()); // makes sure it's an immutable blockpos
        */

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToMinePos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            BlockState blockToMineState = world.getBlockState(blockToMinePos);
            Block blockToMine = blockToMineState.getBlock();
            if(!blockToMine.isAir(blockToMineState,world,blockToMinePos) && !(blockToMine instanceof PedestalBlock) && canMineBlock(world, pedestalPos, blockToMine)
                    && !(blockToMine instanceof IFluidBlock || blockToMine instanceof FlowingFluidBlock) && blockToMineState.getBlockHardness(world, blockToMinePos) != -1.0F)
            {
                return blockToMinePos;
            }
        }

        return returnBlockPos;
    }

    @Override
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

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

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
        player.sendMessage(area,Util.DUMMY_UUID);

        //Display Blocks To Mine Left
        TranslationTextComponent btm = new TranslationTextComponent(getTranslationKey() + ".chat_btm");
        btm.appendString("" + blocksToMineInArea(pedestal.getWorld(),pedestal.getPos(),getAreaWidth(pedestal.getCoinOnPedestal()),getRangeHeight(pedestal.getCoinOnPedestal())) + "");
        btm.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.DUMMY_UUID);

        //Display Fuel Left
        int fuelLeft = getFuelStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        TranslationTextComponent fuel2 = new TranslationTextComponent(getTranslationKey() + ".chat_fuel2");
        fuel.appendString("" + fuelLeft/200 + "");
        fuel.appendString(fuel2.getString());
        fuel.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        if(map.size() > 0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_enchants");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.DUMMY_UUID);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants, Util.DUMMY_UUID);
                }
            }
        }

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //super.addInformation(stack, worldIn, tooltip, flagIn);
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
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);

        TranslationTextComponent fuelStored = new TranslationTextComponent(getTranslationKey() + ".tooltip_fuelstored");
        fuelStored.appendString(""+ getFuelStored(stack) +"");
        fuelStored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(fuelStored);

        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item QUARRY = new ItemUpgradeQuarryBlacklist(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/quarryb"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(QUARRY);
    }


}
