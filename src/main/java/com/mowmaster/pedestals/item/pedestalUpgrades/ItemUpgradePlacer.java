package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.datafixers.util.Pair;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.util.PedestalFakePlayer;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradePlacer extends ItemUpgradeBase
{
    public ItemUpgradePlacer(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptRange() {
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

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int speed = getOperationSpeed(coinInPedestal);
            if (world.getGameTime()%speed == 0) {
                int range = getRangeSmall(coinInPedestal);
                BlockPos blockPosBelow = getPosOfBlockBelow(world, pedestalPos, range);
                BlockState blockBelow = world.getBlockState(blockPosBelow);
                if (!blockBelow.isAir()) {
                    return;
                }

                if (!pedestal.isPedestalBlockPowered(world, pedestalPos)) {
                    placeBlock(pedestal,blockPosBelow,blockBelow.getBlock());
                }
            }
        }
    }

    public void placeBlock(PedestalTileEntity pedestal, BlockPos targetPos, Block blockBelow)
    {
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        Item singleItemInPedestal = itemInPedestal.getItem();

        ItemStack coinOnPedestal = pedestal.getCoinOnPedestal();
        if (itemInPedestal.isEmpty() || singleItemInPedestal.equals(Items.AIR) || !(singleItemInPedestal instanceof BlockItem)) {
            return;
        }

        BlockPos pedPos = pedestal.getPos();

        FakePlayer fakePlayer = fakePedestalPlayer(pedestal).get();
        if (fakePlayer != null) {
            fakePlayer.setSilent(true);
            if (!fakePlayer.getPosition().equals(new BlockPos(pedPos.getX(), pedPos.getY(), pedPos.getZ()))) {
                fakePlayer.setPosition(pedPos.getX(), pedPos.getY(), pedPos.getZ());
            }

            World world = pedestal.getWorld();
            BlockItemUseContext blockContext = new BlockItemUseContext(fakePlayer, Hand.MAIN_HAND, itemInPedestal.copy(), new BlockRayTraceResult(Vector3d.ZERO, getPedestalFacing(world, pedPos), targetPos, false));

            ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
            if (result == ActionResultType.CONSUME) {
                this.removeFromPedestal(world, pedPos, 1);
                if (!pedestal.hasMuffler())
                    world.playSound((PlayerEntity) null, targetPos.getX(), targetPos.getY(), targetPos.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
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
