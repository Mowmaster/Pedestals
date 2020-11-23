package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraftforge.common.util.FakePlayerFactory;
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
        int range = getRange(coin);
        BlockPos posOfBlock = getPosOfBlockBelow(world, pos, range);
        return posOfBlock.getX();
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRange(coin);
        BlockPos posOfBlock = getPosOfBlockBelow(world, pos, range);
        return new int[]{posOfBlock.getY(),1};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRange(coin);
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
                    upgradeAction(world,pedestalPos,itemInPedestal,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinOnPedestal)
    {
        int range = getRange(coinOnPedestal);
        if(!itemInPedestal.isEmpty())//hasItem
        {
            /*
            Block Placing stuff here!
             */
            BlockPos blockPosBelow = getPosOfBlockBelow(world,posOfPedestal,range);//Check if its replaceable instead of checking for air
            Block blockBelow = world.getBlockState(blockPosBelow).getBlock();

            Item singleItemInPedestal = itemInPedestal.getItem();

            if(blockBelow.equals(Blocks.AIR) && !singleItemInPedestal.equals(Items.AIR)) {
                if(singleItemInPedestal instanceof BlockItem)
                {
                    if (((BlockItem) singleItemInPedestal).getBlock() instanceof Block)
                    {
                        if (!itemInPedestal.isEmpty() && itemInPedestal.getItem() instanceof BlockItem && ((BlockItem) itemInPedestal.getItem()).getBlock() instanceof Block) {
                            Block block = ((BlockItem) itemInPedestal.getItem()).getBlock();
                            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinOnPedestal),"[Pedestals]"));
                            //FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().func_241755_D_());
                            fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());

                            //BlockItemUseContext blockContext = new BlockItemUseContext(fakePlayer, Hand.MAIN_HAND, itemInPedestal, new BlockRayTraceResult(Vector3d.ZERO, getPedestalFacing(world,posOfPedestal), blockPosBelow, false));
                            BlockItemUseContext blockContext = new BlockItemUseContext(fakePlayer, Hand.MAIN_HAND, itemInPedestal.copy(), new BlockRayTraceResult(Vector3d.ZERO, getPedestalFacing(world,posOfPedestal), blockPosBelow, false));

                            /*ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                            if (result == ActionResultType.CONSUME) {
                                this.removeFromPedestal(world,posOfPedestal,1);
                                world.playSound((PlayerEntity) null, blockPosBelow.getX(), blockPosBelow.getY(), blockPosBelow.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }*/

                            ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                            if (result == ActionResultType.CONSUME) {
                                this.removeFromPedestal(world,posOfPedestal,1);
                                world.playSound((PlayerEntity) null, blockPosBelow.getX(), blockPosBelow.getY(), blockPosBelow.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }
                            /*world.setBlockState(blockPosBelow,block.getDefaultState());
                            this.removeFromPedestal(world,posOfPedestal,1);
                            world.playSound((PlayerEntity) null, blockPosBelow.getX(), blockPosBelow.getY(), blockPosBelow.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);*/
                        }
                    }
                }
            }

            /*if(blockBelow.isAir(blockBelow.getDefaultState(),world,blockPosBelow))
            {
                FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().getWorld(world.getDimension().getType()));
                fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());
                fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());
                fakePlayer.setHeldItem(Hand.MAIN_HAND,itemInPedestal.copy());
                //, blockPosBelow, getPedestalFacing(world,posOfPedestal).getOpposite(), 0.5F, 0.5F, 0.5F
                if(fakePlayer.interactionManager.processRightClick(fakePlayer, world, fakePlayer.getHeldItemMainhand(), Hand.MAIN_HAND).equals(ActionResultType.SUCCESS))
                {
                    this.removeFromPedestal(world,posOfPedestal,1);
                    world.playSound((PlayerEntity) null, blockPosBelow.getX(), blockPosBelow.getY(), blockPosBelow.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                }
            }*/
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".chat_range");
        range.appendString(""+getRange(stack)+"");
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
        range.appendString("" + getRange(stack) + "");
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
