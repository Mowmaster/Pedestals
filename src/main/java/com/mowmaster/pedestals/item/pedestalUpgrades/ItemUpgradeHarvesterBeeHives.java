package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.util.PedestalFakePlayer;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
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
            int amount = workQueueSize(coin);
            int area = workQueueTwoSize(coin);
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
            if(!world.isBlockPowered(pedestalPos))
            {
                int rangeWidth = getAreaWidth(coinInPedestal);
                int rangeHeight = getRangeHeight(coinInPedestal);
                BlockState pedestalState = world.getBlockState(pedestalPos);
                Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
                BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
                BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));

                if(world.isAreaLoaded(negNums,posNums))
                {

                    int speed = getOperationSpeed(coinInPedestal);

                    //Wont Magnet anything

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
                            buildWorkQueueTwo(pedestal,rangeWidth,rangeHeight);
                            //Update Pedestal so the comparator can update just incase the pedestal inv never changes
                            pedestal.update();
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
                                        upgradeAction(world, itemInPedestal,coinInPedestal, pedestalPos, targetPos, targetBlock);
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
                            //5 second cooldown
                            writeStoredIntTwoToNBT(coinInPedestal,100);
                        }
                    }
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
    {
        if(canHarvest(world,target) && !target.getBlock().isAir(target,world,posTarget))
        {
            ItemStack harvestingShears = (itemInPedestal.isEmpty())?(new ItemStack(Items.SHEARS,1)):(itemInPedestal);
            FakePlayer fakePlayer = new PedestalFakePlayer((ServerWorld) world,getPlayerFromCoin(coinInPedestal),posOfPedestal,harvestingShears.copy());
            if(!fakePlayer.getPosition().equals(new BlockPos(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ()))) {fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());}
            if(!fakePlayer.getHeldItemMainhand().equals(harvestingShears)) {fakePlayer.setHeldItem(Hand.MAIN_HAND,harvestingShears);}

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
                }
            }
        }
    }

    //Can Harvest Hives (hives available to harvest)
    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos)
    {
        World world = pedestal.getWorld();
        BlockPos targetPos = blockToMinePos;
        BlockPos blockToHarvestPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        BlockState blockToHarvestState = world.getBlockState(blockToHarvestPos);
        Block blockToHarvest = blockToHarvestState.getBlock();
        if(canHarvest(world,blockToHarvestState) && !blockToHarvest.isAir(blockToHarvestState,world,blockToHarvestPos))
        {
            return true;
        }

        return false;
    }

    //All Hives In Area
    @Override
    public boolean canMineBlockTwo(PedestalTileEntity pedestal, BlockPos blockToMinePos)
    {
        World world = pedestal.getWorld();
        BlockPos targetPos = blockToMinePos;
        BlockPos blockToHarvestPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        BlockState blockToHarvestState = world.getBlockState(blockToHarvestPos);
        Block blockToHarvest = blockToHarvestState.getBlock();
        if(blockToHarvestState.isIn(BlockTags.BEEHIVES))
        {
            return true;
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
        btm.appendString("" + workQueueSize(stack) + "");
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
