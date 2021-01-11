package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeHarvesterBeeHives extends ItemUpgradeBase
{
    public ItemUpgradeHarvesterBeeHives(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptRange() {
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
        return new int[]{getRangeHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    //https://github.com/Lothrazar/Cyclic/blob/trunk/1.16/src/main/java/com/lothrazar/cyclic/block/harvester/TileHarvester.java#L157
    public static IntegerProperty getBlockPropertyHoney(BlockState blockState) {
        for (Property<?> prop : blockState.getProperties()) {
            if (prop != null && prop.getName() != null && prop instanceof IntegerProperty && prop.getName().equalsIgnoreCase("honey_level")) {
                return (IntegerProperty) prop;
            }
        }
        return null;
    }

    //https://github.com/Lothrazar/Cyclic/blob/trunk/1.16/src/main/java/com/lothrazar/cyclic/block/harvester/TileHarvester.java#L113
    public boolean canHarvest(World world, BlockState state)
    {
        boolean returner = false;
        //BeehiveDispenseBehavior
        if(state.isIn(BlockTags.BEEHIVES))
        {
            IntegerProperty propInt = getBlockPropertyHoney(state);
            if (propInt == null || !(world instanceof ServerWorld)) {
                returner = false;
            }
            else
            {
                int current = state.get(propInt);
                int max = Collections.max(propInt.getAllowedValues());
                //Taked from onBlockActivated method in the BeehiveBlock class
                if(current >= 5)
                {
                    returner = true;
                }
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
            int area = hivesInArea(pedestal.getWorld(),pedestal.getPos(),width,height);
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

            int val = pedestal.getStoredValueForUpgrades();
            if(val>0)
            {
                pedestal.setStoredValueForUpgrades(val-1);
            }
            else {
                if(blocksToMineInArea(pedestal,rangeWidth,rangeHeight) > 0)
                {
                    if(world.isAreaLoaded(negNums,posNums))
                    {
                        if(!world.isBlockPowered(pedestalPos)) {
                            int hivesToHarvest = blocksToHarvestInArea(world,pedestalPos,rangeWidth,rangeHeight);
                            if(hivesToHarvest > 0)
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
                                    //basically if the harvester runs in "passive" mode it never does block updates, so i need to force them for the comparator to work
                                    if(itemInPedestal.isEmpty())
                                    {
                                        if(pedestal.getStoredValueForUpgrades() != hivesToHarvest)
                                        {
                                            pedestal.setStoredValueForUpgrades(hivesToHarvest);
                                        }
                                    }
                                    upgradeAction(world, itemInPedestal,coinInPedestal, pedestalPos, targetPos, targetBlock);
                                    if(resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums)))
                                    {
                                        writeStoredIntToNBT(coinInPedestal,0);
                                    }
                                }
                            }
                            else
                            {
                                //basically if the harvester runs in "passive" mode it never does block updates, so i need to force them for the comparator to work
                                if(itemInPedestal.isEmpty())
                                {
                                    if(pedestal.getStoredValueForUpgrades()!= 0)
                                    {
                                        pedestal.setStoredValueForUpgrades(0);
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    pedestal.setStoredValueForUpgrades((rangeWidth*20)+20);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
    {

        if(canHarvest(world,target) && !target.getBlock().isAir(target,world,posTarget))
        {

            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
            fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());
            ItemStack harvestingShears = (itemInPedestal.isEmpty())?(new ItemStack(Items.SHEARS,1)):(itemInPedestal);
            if(!fakePlayer.getHeldItemMainhand().equals(harvestingShears))
            {
                fakePlayer.setHeldItem(Hand.MAIN_HAND,harvestingShears);
            }
            BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
            ItemStack itemFromInv = ItemStack.EMPTY;
            /*LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                if(invToPullFrom instanceof PedestalTileEntity) {
                    itemFromInv = ItemStack.EMPTY;

                }
                else {
                    if(handler != null)
                    {
                        int i = getNextSlotWithItemsCap(cap,getStackInPedestal(world,posOfPedestal));
                        if(i>=0)
                        {
                            itemFromInv = handler.getStackInSlot(i);
                            if(itemFromInv.getItem().equals(Items.GLASS_BOTTLE))
                            {
                                if(!fakePlayer.getHeldItemMainhand().equals(new ItemStack(Items.GLASS_BOTTLE)))
                                {
                                    fakePlayer.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
                                }
                                PlayerInteractEvent.RightClickBlock e = new PlayerInteractEvent.RightClickBlock(fakePlayer,Hand.MAIN_HAND,posTarget,Direction.UP);
                                if (!MinecraftForge.EVENT_BUS.post(e)) {
                                    TileEntity tile = world.getTileEntity(posOfPedestal);
                                    if(tile instanceof PedestalTileEntity)
                                    {
                                        PedestalTileEntity pedestal = ((PedestalTileEntity) tile);
                                        if(pedestal.addItem(new ItemStack(Items.HONEY_BOTTLE)))
                                        {
                                            handler.extractItem(i,1 ,false);
                                            ((BeehiveBlock)target.getBlock()).onBlockActivated(target,world,posTarget,fakePlayer,Hand.MAIN_HAND,new BlockRayTraceResult(new Vector3d(posTarget.getX(),posTarget.getY(),posTarget.getZ()),Direction.UP,posTarget,false));
                                            //((BeehiveBlock)target.getBlock()).takeHoney(world,target,posTarget,fakePlayer, BeehiveTileEntity.State.BEE_RELEASED);
                                            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),255,178,35));
                                            //PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.HARVESTED,posTarget.getX(),posTarget.getY(),posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                                            world.playSound((PlayerEntity)null, posTarget, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                PlayerInteractEvent.RightClickBlock e = new PlayerInteractEvent.RightClickBlock(fakePlayer,Hand.MAIN_HAND,posTarget,Direction.UP);
                                if (!MinecraftForge.EVENT_BUS.post(e)) {
                                    TileEntity tile = world.getTileEntity(posOfPedestal);
                                    if(tile instanceof PedestalTileEntity)
                                    {
                                        PedestalTileEntity pedestal = ((PedestalTileEntity) tile);
                                        if(pedestal.addItem(new ItemStack(Items.HONEYCOMB,3)))
                                        {
                                            ((BeehiveBlock)target.getBlock()).takeHoney(world,target,posTarget,fakePlayer, BeehiveTileEntity.State.BEE_RELEASED);
                                            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),255,178,35));
                                            //PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.HARVESTED,posTarget.getX(),posTarget.getY(),posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                                            world.playSound((PlayerEntity)null, posTarget, SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }*/
            //Is it needed? idk
            //else
            //{
                PlayerInteractEvent.RightClickBlock e = new PlayerInteractEvent.RightClickBlock(fakePlayer,Hand.MAIN_HAND,posTarget,Direction.UP);
                if (!MinecraftForge.EVENT_BUS.post(e)) {
                    TileEntity tile = world.getTileEntity(posOfPedestal);
                    if(tile instanceof PedestalTileEntity)
                    {
                        PedestalTileEntity pedestal = ((PedestalTileEntity) tile);
                        ActionResultType type = ((BeehiveBlock)target.getBlock()).onBlockActivated(target,world,posTarget,fakePlayer,Hand.MAIN_HAND,new BlockRayTraceResult(new Vector3d(posTarget.getX(),posTarget.getY(),posTarget.getZ()),Direction.UP,posTarget,true));
                        if(type == ActionResultType.CONSUME || type == ActionResultType.SUCCESS)
                        {
                            if(!itemInPedestal.isEmpty())
                            {
                                ItemStack itemInFakeBoy = ItemStack.EMPTY;
                                itemInFakeBoy = IntStream.range(0,fakePlayer.inventory.getSizeInventory())//Int Range
                                        .mapToObj((fakePlayer.inventory)::getStackInSlot)//Function being applied to each interval
                                        .filter(itemStack -> !itemStack.isEmpty())
                                        .filter(itemStack -> !itemStack.getItem().equals(itemInPedestal.getItem()))
                                        .findFirst().orElse(ItemStack.EMPTY);
                                BlockPos spawnItemHere = getPosOfBlockBelow(world,posTarget,-1);
                                pedestal.spawnItemStack(world,spawnItemHere.getX(),spawnItemHere.getY(),spawnItemHere.getZ(),itemInFakeBoy);
                                int getSlot = getPlayerSlotWithMatchingStackExact(fakePlayer.inventory,itemInFakeBoy);
                                if(getSlot>=0 && !fakePlayer.inventory.getStackInSlot(0).getItem().equals(itemInPedestal.getItem()))
                                {
                                    fakePlayer.inventory.removeStackFromSlot(getSlot);
                                }
                            }
                        }
                        /*if(pedestal.addItem(new ItemStack(Items.HONEYCOMB,3)))
                        {
                            ((BeehiveBlock)target.getBlock()).takeHoney(world,target,posTarget,fakePlayer, BeehiveTileEntity.State.BEE_RELEASED);
                            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),255,178,35));
                            //PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.HARVESTED,posTarget.getX(),posTarget.getY(),posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                            world.playSound((PlayerEntity)null, posTarget, SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }*/
                    }
                }
            //}
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

    public int hivesInArea(World world, BlockPos pedestalPos, int width, int height)
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
            if(blockToHarvestState.isIn(BlockTags.BEEHIVES))
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
        String trr = "" + getRangeHeight(stack) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);

        TranslationTextComponent btm = new TranslationTextComponent(getTranslationKey() + ".chat_btm");
        btm.appendString("" + blocksToHarvestInArea(pedestal.getWorld(),pedestal.getPos(),getAreaWidth(pedestal.getCoinOnPedestal()),getHeight(pedestal.getCoinOnPedestal())) + "");
        btm.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.DUMMY_UUID);

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
        String trr = "" + getRangeHeight(stack) + "";
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
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(speed);
    }

    public static final Item HARVESTERHIVES = new ItemUpgradeHarvesterBeeHives(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/harvesterhives"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(HARVESTERHIVES);
    }

}
