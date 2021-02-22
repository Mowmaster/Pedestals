package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.util.PedestalFakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
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
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeChopperShrooms extends ItemUpgradeBase
{
    public ItemUpgradeChopperShrooms(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

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
        return getRangeTree(stack);
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{getRangeHeight(coin),0};
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
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            if(!world.isBlockPowered(pedestalPos))
            {
                int rangeWidth = getAreaWidth(coinInPedestal);
                int rangeHeight = getRangeHeight(coinInPedestal);
                int speed = getOperationSpeed(coinInPedestal);

                BlockState pedestalState = world.getBlockState(pedestalPos);
                Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
                BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
                BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));

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
                    writeStoredIntTwoToNBT(coinInPedestal,val-1);
                }
                else {

                    //If work queue doesnt exist, try to make one
                    if(workQueueSize(coinInPedestal)<=0)
                    {
                        buildWorkQueue(pedestal,rangeWidth,rangeHeight);
                    }

                    //
                    if(workQueueSize(coinInPedestal) > 0)
                    {
                        List<BlockPos> workQueue = readWorkQueueFromNBT(coinInPedestal);
                        if (world.getGameTime() % speed == 0) {
                            for(int i = 0;i< workQueue.size(); i++)
                            {
                                BlockPos targetPos = workQueue.get(i);
                                BlockPos blockToMinePos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                BlockState targetBlock = world.getBlockState(blockToMinePos);
                                if(canMineBlock(pedestal,blockToMinePos))
                                {
                                    workQueue.remove(i);
                                    writeWorkQueueToNBT(coinInPedestal,workQueue);
                                    upgradeAction(pedestal, targetPos, targetBlock);
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
                    else {
                        writeStoredIntTwoToNBT(coinInPedestal,(((rangeWidth*2)+1)*20)+20);
                    }
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, BlockPos blockToChopPos, BlockState blockToChop)
    {
        World world = pedestal.getWorld();
        //ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack toolInPedestal = pedestal.getToolOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();
        //wart blocks*, warped stems*, crimson stems*, shroomlight*, mushroom stems, mushroom brown and mushroom red
        FakePlayer fakePlayer = new PedestalFakePlayer((ServerWorld) world,getPlayerFromCoin(coinInPedestal),posOfPedestal,toolInPedestal.copy());
        //FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
        if(!fakePlayer.getPosition().equals(new BlockPos(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ()))) {fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());}
        ItemStack choppingAxe = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_AXE,1));

        if(!pedestal.hasTool())
        {
            choppingAxe = getToolDefaultEnchanted(coinInPedestal,choppingAxe);
        }

        if (!fakePlayer.getHeldItemMainhand().equals(choppingAxe)) {fakePlayer.setHeldItem(Hand.MAIN_HAND, choppingAxe);}

        ToolType tool = blockToChop.getHarvestTool();
        int toolLevel = fakePlayer.getHeldItemMainhand().getHarvestLevel(tool, fakePlayer, blockToChop);
        //if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToChop,true))
        //toolLevel >= blockToChop.getHarvestLevel()
        if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToChop,true))
        {
            //BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, blockToChopPos, blockToChop, fakePlayer);
            //if (!MinecraftForge.EVENT_BUS.post(e)) {
            blockToChop.getBlock().harvestBlock(world, fakePlayer, blockToChopPos, blockToChop, null, fakePlayer.getHeldItemMainhand());
            blockToChop.getBlock().onBlockHarvested(world, blockToChopPos, blockToChop, fakePlayer);
            int expdrop = blockToChop.getBlock().getExpDrop(blockToChop,world,blockToChopPos,
                    (EnchantmentHelper.getEnchantments(fakePlayer.getHeldItemMainhand()).containsKey(Enchantments.FORTUNE))?(EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE,fakePlayer.getHeldItemMainhand())):(0),
                    (EnchantmentHelper.getEnchantments(fakePlayer.getHeldItemMainhand()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH,fakePlayer.getHeldItemMainhand())):(0));
            if(expdrop>0)blockToChop.getBlock().dropXpOnBlockBreak((ServerWorld)world,posOfPedestal,expdrop);
            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,blockToChopPos.getX(),blockToChopPos.getY(),blockToChopPos.getZ(),255,164,0));
            world.removeBlock(blockToChopPos, false);
            //}
        }
    }

    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos, PlayerEntity player)
    {
        World world = pedestal.getWorld();
        ItemStack toolInPedestal = pedestal.getToolOnPedestal();
        BlockState blockStateToChop = world.getBlockState(blockToMinePos);
        Block blockToChop = blockStateToChop.getBlock();
        if(!blockToChop.isAir(blockStateToChop,world,blockToMinePos))
        {
            ItemStack axe = (pedestal.hasTool())?(toolInPedestal):(new ItemStack(Items.DIAMOND_AXE,1));
            ToolType tool = blockStateToChop.getHarvestTool();
            int toolLevel = axe.getHarvestLevel(tool, null, blockStateToChop);
            //toolLevel >= blockStateToChop.getHarvestLevel() &&
            if(
                    (
                       blockToChop.isIn(BlockTags.WART_BLOCKS)
                    || blockToChop.isIn(BlockTags.WARPED_STEMS)
                    || blockToChop.isIn(BlockTags.CRIMSON_STEMS)
                    || blockToChop.equals(Blocks.SHROOMLIGHT)
                    || blockToChop.equals(Blocks.MUSHROOM_STEM)
                    || blockToChop.equals(Blocks.BROWN_MUSHROOM_BLOCK)
                    || blockToChop.equals(Blocks.RED_MUSHROOM_BLOCK)
                    )
                    && passesFilter(world, pedestal.getPos(), blockToChop)
              )
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
        ItemStack toolInPedestal = pedestal.getToolOnPedestal();
        BlockState blockStateToChop = world.getBlockState(blockToMinePos);
        Block blockToChop = blockStateToChop.getBlock();
        if(!blockToChop.isAir(blockStateToChop,world,blockToMinePos))
        {
            ItemStack axe = (pedestal.hasTool())?(toolInPedestal):(new ItemStack(Items.DIAMOND_AXE,1));
            ToolType tool = blockStateToChop.getHarvestTool();
            int toolLevel = axe.getHarvestLevel(tool, null, blockStateToChop);
            //toolLevel >= blockStateToChop.getHarvestLevel() &&
            if(
                    (
                            blockToChop.isIn(BlockTags.WART_BLOCKS)
                                    || blockToChop.isIn(BlockTags.WARPED_STEMS)
                                    || blockToChop.isIn(BlockTags.CRIMSON_STEMS)
                                    || blockToChop.equals(Blocks.SHROOMLIGHT)
                                    || blockToChop.equals(Blocks.MUSHROOM_STEM)
                                    || blockToChop.equals(Blocks.BROWN_MUSHROOM_BLOCK)
                                    || blockToChop.equals(Blocks.RED_MUSHROOM_BLOCK)
                    )
                            && passesFilter(world, pedestal.getPos(), blockToChop)
                    )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean passesFilter(World world, BlockPos posPedestal, Block blockIn)
    {
        boolean returner = true;
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
                    .filter(itemStack -> Block.getBlockFromItem(itemStack.getItem()).equals(blockIn))
                    .findFirst().orElse(ItemStack.EMPTY);

            if(!itemFromInv.isEmpty())
            {
                returner = false;
            }
        }

        return returner;
    }

    @Override
    public void onPedestalNeighborChanged(PedestalTileEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<ItemStack> stackIn = buildFilterQueue(pedestal);
        if(filterQueueSize(coin)>0)
        {
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
            if(!doesFilterAndQueueMatch(stackIn,stackCurrent))
            {
                writeFilterQueueToNBT(coin,stackIn);
            }
        }
        else
        {
            writeFilterQueueToNBT(coin,stackIn);
        }
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
        String trr = "" + (getRangeHeight(stack)+1) + "";
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
        btm.appendString("" + ((workQueueSize(stack)>0)?(workQueueSize(stack)):(0)) + "");
        btm.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.DUMMY_UUID);

        ItemStack toolStack = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_AXE));
        TranslationTextComponent tool = new TranslationTextComponent(getTranslationKey() + ".chat_tool");
        tool.append(toolStack.getDisplayName());
        tool.mergeStyle(TextFormatting.BLUE);
        player.sendMessage(tool,Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments((pedestal.hasTool())?(pedestal.getToolOnPedestal()):(stack));
        /*if(hasAdvancedInventoryTargeting(stack))
        {
            map.put(EnchantmentRegistry.ADVANCED,1);
        }*/
        if(map.size() > 0 && getNumNonPedestalEnchants(map)>0)
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
                    player.sendMessage(enchants,Util.DUMMY_UUID);
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
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s3 = getAreaWidth(stack);
        int s4 = getRangeHeight(stack);

        String tr = "" + (s3+s3+1) + "";
        String trr = "" + (s4+1) + "";

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);

        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item CHOPPER = new ItemUpgradeChopperShrooms(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/choppershrooms"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(CHOPPER);
    }


}
