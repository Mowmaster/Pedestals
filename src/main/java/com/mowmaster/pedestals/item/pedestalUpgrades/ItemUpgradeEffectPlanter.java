package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeEffectPlanter extends ItemUpgradeBase
{
    public ItemUpgradeEffectPlanter(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

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
            int amount = blocksToPlantInArea(pedestal.getWorld(),pedestal.getPos(),pedestal.getItemInPedestal(),width,height);
            int area = plantableSpotsInArea(pedestal.getWorld(),pedestal.getPos(),pedestal.getItemInPedestal(),width,height);
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
                        if(!world.isBlockPowered(pedestalPos) && !itemInPedestal.isEmpty()) {
                            if(blocksToPlantInArea(world,pedestalPos,itemInPedestal,rangeWidth,rangeHeight) > 0)
                            {
                                if (world.getGameTime() % speed == 0) {
                                    int currentPosition = 0;
                                    for(currentPosition = getStoredInt(coinInPedestal);!resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));currentPosition++)
                                    {
                                        BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                        BlockPos blockToPlantPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                        BlockState blockToPlantState = world.getBlockState(blockToPlantPos);
                                        Block blockToPlant = blockToPlantState.getBlock();
                                        Item singleItemInPedestal = itemInPedestal.getItem();

                                        if(blockToPlant.equals(Blocks.AIR) && !singleItemInPedestal.equals(Items.AIR)) {
                                            if (singleItemInPedestal instanceof BlockItem) {
                                                if (((BlockItem) singleItemInPedestal).getBlock() instanceof IPlantable) {
                                                    if (!itemInPedestal.isEmpty() && itemInPedestal.getItem() instanceof BlockItem && ((BlockItem) itemInPedestal.getItem()).getBlock() instanceof IPlantable) {
                                                        Block block = ((BlockItem) itemInPedestal.getItem()).getBlock();
                                                        if (world.getBlockState(blockToPlantPos.down()).canSustainPlant(world, blockToPlantPos.down(), Direction.UP, (IPlantable) block)) {
                                                            writeStoredIntToNBT(coinInPedestal, currentPosition);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                    BlockState targetBlock = world.getBlockState(targetPos);
                                    upgradeAction(world, pedestal, itemInPedestal, pedestalPos, targetPos, targetBlock);
                                    if(resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums)))
                                    {
                                        writeStoredIntToNBT(coinInPedestal,0);
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

    public void upgradeAction(World world, PedestalTileEntity pedestal, ItemStack itemInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
    {
        Random rand = new Random();
        Item singleItemInPedestal = itemInPedestal.getItem();

        if(world.getBlockState(posTarget).getBlock().equals(Blocks.AIR) && !singleItemInPedestal.equals(Items.AIR))
        {
            if(singleItemInPedestal instanceof BlockItem)
            {
                if(((BlockItem) singleItemInPedestal).getBlock() instanceof IPlantable)
                {
                    if (!itemInPedestal.isEmpty() && itemInPedestal.getItem() instanceof BlockItem && ((BlockItem) itemInPedestal.getItem()).getBlock() instanceof IPlantable) {
                        Block block = ((BlockItem) itemInPedestal.getItem()).getBlock();


                        if (world.getBlockState(posTarget.down()).canSustainPlant(world,posTarget.down(), Direction.UP,(IPlantable) block)) {
                            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(pedestal.getCoinOnPedestal()),"[Pedestals]"));
                            fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());

                            BlockItemUseContext blockContext = new BlockItemUseContext(fakePlayer, Hand.MAIN_HAND, itemInPedestal.copy(), new BlockRayTraceResult(Vector3d.ZERO, getPedestalFacing(world,posOfPedestal), posTarget.down(), false));

                            ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                            if (result == ActionResultType.CONSUME) {
                                this.removeFromPedestal(world,posOfPedestal,1);
                                world.playSound((PlayerEntity) null, posTarget.getX(), posTarget.getY(), posTarget.getZ(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    public int plantableSpotsInArea(World world, BlockPos pedestalPos, ItemStack itemInPedestal, int width, int height)
    {
        int validBlocks = 0;
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToPlantPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            BlockState blockToPlantState = world.getBlockState(blockToPlantPos);
            Block blockToPlant = blockToPlantState.getBlock();
            Item singleItemInPedestal = itemInPedestal.getItem();

            if(!singleItemInPedestal.equals(Items.AIR)) {
                if (singleItemInPedestal instanceof BlockItem) {
                    if (((BlockItem) singleItemInPedestal).getBlock() instanceof IPlantable) {
                        if (!itemInPedestal.isEmpty() && itemInPedestal.getItem() instanceof BlockItem && ((BlockItem) itemInPedestal.getItem()).getBlock() instanceof IPlantable) {
                            Block block = ((BlockItem) itemInPedestal.getItem()).getBlock();
                            if (world.getBlockState(blockToPlantPos.down()).canSustainPlant(world, blockToPlantPos.down(), Direction.UP, (IPlantable) block)) {
                                validBlocks++;
                            }
                        }
                    }
                }
            }
        }

        return validBlocks;
    }

    public int blocksToPlantInArea(World world, BlockPos pedestalPos, ItemStack itemInPedestal, int width, int height)
    {
        int validBlocks = 0;
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToPlantPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            BlockState blockToPlantState = world.getBlockState(blockToPlantPos);
            Block blockToPlant = blockToPlantState.getBlock();
            Item singleItemInPedestal = itemInPedestal.getItem();

            if(blockToPlant.equals(Blocks.AIR) && !singleItemInPedestal.equals(Items.AIR)) {
                if (singleItemInPedestal instanceof BlockItem) {
                    if (((BlockItem) singleItemInPedestal).getBlock() instanceof IPlantable) {
                        if (!itemInPedestal.isEmpty() && itemInPedestal.getItem() instanceof BlockItem && ((BlockItem) itemInPedestal.getItem()).getBlock() instanceof IPlantable) {
                            Block block = ((BlockItem) itemInPedestal.getItem()).getBlock();
                            if (world.getBlockState(blockToPlantPos.down()).canSustainPlant(world, blockToPlantPos.down(), Direction.UP, (IPlantable) block)) {
                                validBlocks++;
                            }
                        }
                    }
                }
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
        btm.appendString("" + blocksToPlantInArea(pedestal.getWorld(),pedestal.getPos(),pedestal.getItemInPedestal(),getAreaWidth(pedestal.getCoinOnPedestal()),getHeight(pedestal.getCoinOnPedestal())) + "");
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

    public static final Item PLANTER = new ItemUpgradeEffectPlanter(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/planter"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(PLANTER);
    }


}
