package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
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
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeEffectHarvester extends ItemUpgradeBase
{
    public ItemUpgradeEffectHarvester(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptRange() {
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

    //https://github.com/Lothrazar/Cyclic/blob/trunk/1.16/src/main/java/com/lothrazar/cyclic/block/harvester/TileHarvester.java#L157
    public static IntegerProperty getBlockPropertyAge(BlockState blockState) {
        for (Property<?> prop : blockState.getProperties()) {
            if (prop != null && prop.getName() != null && prop instanceof IntegerProperty && prop.getName().equalsIgnoreCase("age")) {
                return (IntegerProperty) prop;
            }
        }
        return null;
    }

    //https://github.com/Lothrazar/Cyclic/blob/trunk/1.16/src/main/java/com/lothrazar/cyclic/block/harvester/TileHarvester.java#L113
    public boolean canHarvest(World world, BlockState state)
    {
        boolean returner = false;
        IntegerProperty propInt = getBlockPropertyAge(state);
        if (propInt == null || !(world instanceof ServerWorld)) {
            returner = false;
        }
        else if (state.getBlock() instanceof KelpTopBlock)
        {
            returner = true;
        }
        else if (state.getBlock() instanceof StemBlock)
        {
            returner = false;
        }
        else
        {
            int current = state.get(propInt);
            int min = Collections.min(propInt.getAllowedValues());
            int max = Collections.max(propInt.getAllowedValues());
            if(current == max)
            {
                returner = true;
            }
        }

        return returner;
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
            int height = getHeight(pedestal.getCoinOnPedestal());
            int amount = blocksToHarvestInArea(pedestal.getWorld(),pedestal.getPos(),width,height);
            int area = blocksCropsInArea(pedestal.getWorld(),pedestal.getPos(),width,height);
            if(amount>0)
            {
                float f = (float)amount/(float)area;
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();

        if(!world.isRemote)
        {
            int rangeWidth = getAreaWidth(coinInPedestal);
            int rangeHeight = getHeight(coinInPedestal);
            int speed = getOperationSpeed(coinInPedestal);

            BlockState pedestalState = world.getBlockState(pedestalPos);
            Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
            BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
            BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));

            int val = readStoredIntTwoFromNBT(coinInPedestal);
            if(val>0)
            {
                writeStoredIntTwoToNBT(coinInPedestal,val-1);
            }
            else {
                if(world.isAreaLoaded(negNums,posNums))
                {
                    if(!world.isBlockPowered(pedestalPos)) {

                        AxisAlignedBB getBox = new AxisAlignedBB(negNums,posNums);
                        List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);
                        if(itemList.size()>0)
                        {
                            upgradeActionMagnet(world, itemList, itemInPedestal, pedestalPos, rangeWidth, rangeHeight);
                        }

                        if(blocksToHarvestInAreaGetOne(world,pedestalPos,rangeWidth,rangeHeight) > 0)
                        {
                            int leftToHarvest = blocksToHarvestInArea(world,pedestalPos,rangeWidth,rangeHeight);
                            if(leftToHarvest > 0)
                            {
                                if (world.getGameTime() % speed == 0) {
                                    int currentPosition = 0;
                                    for(currentPosition = getStoredInt(coinInPedestal);!resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));currentPosition++)
                                    {
                                        BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                        BlockPos blockToHarvestPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                        BlockState blockToHarvestState = world.getBlockState(blockToHarvestPos);
                                        Block blockToHarvest = blockToHarvestState.getBlock();
                                        if(canHarvest(world,blockToHarvestState) && !blockToHarvest.isAir(blockToHarvestState,world,blockToHarvestPos))
                                        {
                                            writeStoredIntToNBT(coinInPedestal,currentPosition);
                                            break;
                                        }
                                    }
                                    BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                    BlockState targetBlock = world.getBlockState(targetPos);
                                    //basically the harvester never does block updates, so i need to force them for the comparator to work
                                    //This is probably not ideal though...
                                    if(pedestal.getStoredValueForUpgrades() != leftToHarvest)
                                    {
                                        pedestal.setStoredValueForUpgrades(leftToHarvest);
                                    }
                                    upgradeAction(pedestal, targetPos, targetBlock);
                                    if(resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums)))
                                    {
                                        writeStoredIntToNBT(coinInPedestal,0);
                                    }
                                }
                            }
                            else
                            {
                                //basically the harvester never does block updates, so i need to force them for the comparator to work
                                if(pedestal.getStoredValueForUpgrades()!= 0)
                                {
                                    pedestal.setStoredValueForUpgrades(0);
                                }
                            }
                        }
                        else {
                            writeStoredIntTwoToNBT(coinInPedestal,(rangeWidth*20)+20);
                        }
                    }
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, BlockPos posTarget, BlockState target)
    {
        World world = pedestal.getWorld();
        //ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack toolInPedestal = pedestal.getToolOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();

        if(canHarvest(world,target) && !target.getBlock().isAir(target,world,posTarget))
        {
            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
            fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());
            //Changed to a stick by default since atm6 has a dumb mod installed that modifies default vanilla hoe behavior...
            ItemStack harvestingHoe = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_AXE,1));
            if (
                    (
                            harvestingHoe.getItem() instanceof HoeItem
                            || harvestingHoe.getToolTypes().contains(ToolType.HOE)
                            || harvestingHoe.getItem() instanceof AxeItem
                            || harvestingHoe.getToolTypes().contains(ToolType.AXE)
                    )
                    && !fakePlayer.getHeldItemMainhand().equals(harvestingHoe)
               ) {
                fakePlayer.setHeldItem(Hand.MAIN_HAND, harvestingHoe);
            }

            if(!pedestal.hasTool())
            {
                harvestingHoe = getToolDefaultEnchanted(coinInPedestal,harvestingHoe);
            }

            ToolType tool = target.getHarvestTool();
            int toolLevel = fakePlayer.getHeldItemMainhand().getHarvestLevel(tool, fakePlayer, target);
            /*System.out.println(tool.getName());
            System.out.println(toolLevel);
            System.out.println(target.getHarvestLevel());*/

            if(hasAdvancedInventoryTargeting(coinInPedestal))
            {
                //TODO: Make this do gentle harvesting SOMEDAY
                //https://github.com/Lothrazar/Cyclic/blob/trunk/1.16/src/main/java/com/lothrazar/cyclic/block/harvester/TileHarvester.java
                //toolLevel >= target.getHarvestLevel()
                if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,target,true))
                {
                    //BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, posTarget, target, fakePlayer);
                    //if (!MinecraftForge.EVENT_BUS.post(e)) {
                        target.getBlock().harvestBlock(world, fakePlayer, posTarget, target, null, fakePlayer.getHeldItemMainhand());
                        target.getBlock().onBlockHarvested(world, posTarget, target, fakePlayer);
                        //PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.HARVESTED,posTarget.getX(),posTarget.getY()-0.5f,posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                        PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),255,164,0));
                        world.removeBlock(posTarget, false);
                    //}
                }
            }
            else
            {
                //toolLevel >= target.getHarvestLevel()
                if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,target,true))
                {
                    //BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, posTarget, target, fakePlayer);
                    //if (!MinecraftForge.EVENT_BUS.post(e)) {
                        target.getBlock().harvestBlock(world, fakePlayer, posTarget, target, null, fakePlayer.getHeldItemMainhand());
                        target.getBlock().onBlockHarvested(world, posTarget, target, fakePlayer);
                        //PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.HARVESTED,posTarget.getX(),posTarget.getY()-0.5f,posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                        PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),255,164,0));
                        world.removeBlock(posTarget, false);
                    //}
                }
            }
        }
    }

    public int blocksToHarvestInArea(World world, BlockPos pedestalPos, int width, int height)
    {
        int validBlocks = 0;
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToHarvestPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            BlockState blockToHarvestState = world.getBlockState(blockToHarvestPos);
            Block blockToHarvest = blockToHarvestState.getBlock();
            if(canHarvest(world,blockToHarvestState) && !blockToHarvest.isAir(blockToHarvestState,world,blockToHarvestPos))
            {
                validBlocks++;
            }
        }

        return validBlocks;
    }

    public int blocksToHarvestInAreaGetOne(World world, BlockPos pedestalPos, int width, int height)
    {
        int validBlocks = 0;
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToHarvestPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            BlockState blockToHarvestState = world.getBlockState(blockToHarvestPos);
            Block blockToHarvest = blockToHarvestState.getBlock();
            if(canHarvest(world,blockToHarvestState) && !blockToHarvest.isAir(blockToHarvestState,world,blockToHarvestPos))
            {
                validBlocks++;
                break;
            }
        }

        return validBlocks;
    }

    public int blocksCropsInArea(World world, BlockPos pedestalPos, int width, int height)
    {
        int validBlocks = 0;
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToGrowPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            BlockState blockToGrowState = world.getBlockState(blockToGrowPos);
            Block blockToGrow = blockToGrowState.getBlock();
            if(blockToGrow instanceof IGrowable || blockToGrow instanceof IPlantable)
            {
                validBlocks++;
            }
        }

        return validBlocks;
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
        player.sendMessage(area,Util.DUMMY_UUID);

        TranslationTextComponent btm = new TranslationTextComponent(getTranslationKey() + ".chat_btm");
        btm.appendString("" + blocksToHarvestInArea(pedestal.getWorld(),pedestal.getPos(),getAreaWidth(pedestal.getCoinOnPedestal()),getHeight(pedestal.getCoinOnPedestal())) + "");
        btm.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.DUMMY_UUID);

        //Hoe isnt actually used, but lets just pretend
        ItemStack toolStack = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_HOE));
        TranslationTextComponent tool = new TranslationTextComponent(getTranslationKey() + ".chat_tool");
        tool.append(toolStack.getDisplayName());
        tool.mergeStyle(TextFormatting.BLUE);
        player.sendMessage(tool,Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments((pedestal.hasTool())?(pedestal.getToolOnPedestal()):(stack));
        if(hasAdvancedInventoryTargeting(stack))
        {
            map.put(EnchantmentRegistry.ADVANCED,1);
        }
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
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("" + getHeight(stack) + "");
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(speed);
    }

    public static final Item HARVESTER = new ItemUpgradeEffectHarvester(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/harvester"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(HARVESTER);
    }


}
