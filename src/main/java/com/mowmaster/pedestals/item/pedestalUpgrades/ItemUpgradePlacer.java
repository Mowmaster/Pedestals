package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.util.PedestalFakePlayer;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.block.SkullBlock.ROTATION;
import static net.minecraft.state.properties.BlockStateProperties.FACING;
import static net.minecraft.state.properties.BlockStateProperties.ROTATION_0_15;

public class ItemUpgradePlacer extends ItemUpgradeBase
{
    public ItemUpgradePlacer(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    public BlockState getState(Block getBlock, ItemStack itemForBlock)
    {
        BlockState stated = Blocks.AIR.getDefaultState();

        //Redstone
        if(itemForBlock.getItem() == Items.REDSTONE)
        {
            stated = Blocks.REDSTONE_WIRE.getDefaultState();
        }
        else
        {
            stated = getBlock.getDefaultState();
        }

        return stated;
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRangeSmall(coin);
        BlockPos posOfBlock = getPosOfBlockBelow(world, pos, range);
        return posOfBlock.getX();
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRangeSmall(coin);
        BlockPos posOfBlock = getPosOfBlockBelow(world, pos, range);
        return new int[]{posOfBlock.getY(),1};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRangeSmall(coin);
        BlockPos posOfBlock = getPosOfBlockBelow(world, pos, range);
        return posOfBlock.getZ();
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    int range = getRangeSmall(coinInPedestal);
                    BlockPos blockPosBelow = getPosOfBlockBelow(world,pedestalPos,range);
                    placeBlock(pedestal,blockPosBelow);
                }
            }
        }
    }

    public void placeBlock(PedestalTileEntity pedestal, BlockPos targetPos)
    {
        World world = pedestal.getWorld();
        BlockPos pedPos = pedestal.getPos();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinOnPedestal = pedestal.getCoinOnPedestal();
        if(!itemInPedestal.isEmpty())
        {
            Block blockBelow = world.getBlockState(targetPos).getBlock();
            Item singleItemInPedestal = itemInPedestal.getItem();

            if(blockBelow.equals(Blocks.AIR) && !singleItemInPedestal.equals(Items.AIR)) {
                if(singleItemInPedestal instanceof BlockItem)
                {
                    if (((BlockItem) singleItemInPedestal).getBlock() instanceof Block)
                    {
                        if (!itemInPedestal.isEmpty() && itemInPedestal.getItem() instanceof BlockItem && ((BlockItem) itemInPedestal.getItem()).getBlock() instanceof Block) {

                            FakePlayer fakePlayer = new PedestalFakePlayer((ServerWorld) world,getPlayerFromCoin(coinOnPedestal),pedPos,itemInPedestal);
                            if(!fakePlayer.getPosition().equals(new BlockPos(pedPos.getX(), pedPos.getY(), pedPos.getZ()))) {fakePlayer.setPosition(pedPos.getX(), pedPos.getY(), pedPos.getZ());}

                            BlockItemUseContext blockContext = new BlockItemUseContext(fakePlayer, Hand.MAIN_HAND, itemInPedestal.copy(), new BlockRayTraceResult(Vector3d.ZERO, getPedestalFacing(world,pedPos), targetPos, false));

                            ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                            if (result == ActionResultType.CONSUME) {
                                this.removeFromPedestal(world,pedPos,1);
                                world.playSound((PlayerEntity) null, targetPos.getX(), targetPos.getY(), targetPos.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    public static int getRotFromSide(Direction side)
    {
        switch (side)
        {
            case UP:return 0;
            case DOWN:return 8;
            case NORTH:return 0;
            case SOUTH:return 8;
            case EAST:return 4;
            case WEST:return 12;
        }
        return 0;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".chat_range");
        range.appendString(""+getRangeSmall(stack)+"");
        range.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(range,Util.DUMMY_UUID);

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

        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".tooltip_range");
        range.appendString("" + getRangeSmall(stack) + "");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        range.mergeStyle(TextFormatting.WHITE);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(range);
        tooltip.add(speed);
    }

    public static final Item PLACER = new ItemUpgradePlacer(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/placer"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(PLACER);
    }


}
