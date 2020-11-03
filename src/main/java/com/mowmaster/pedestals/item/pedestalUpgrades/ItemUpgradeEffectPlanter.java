package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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

    public int getRangeHeight(ItemStack stack)
    {
        return getHeight(stack);
    }

    public int getHeight(ItemStack stack)
    {
        int height = 3;
        switch (getRangeModifier(stack))
        {
            case 0:
                height = 3;
                break;
            case 1:
                height=5;
                break;
            case 2:
                height = 7;
                break;
            case 3:
                height = 9;
                break;
            case 4:
                height = 11;
                break;
            case 5:
                height=13;
                break;
            default: height=3;
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

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        int speed = getOperationSpeed(coinInPedestal);

        int width = getAreaWidth(coinInPedestal);
        int height = getRangeHeight(coinInPedestal);

        BlockPos negBlockPos = getNegRangePosEntity(world,pedestalPos,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,pedestalPos,width,height);

        if(!world.isBlockPowered(pedestalPos)) {
            if (world.getGameTime() % speed == 0) {
                TileEntity tile = world.getTileEntity(pedestalPos);
                if(tile instanceof PedestalTileEntity)
                {
                    PedestalTileEntity pedestal = (PedestalTileEntity) tile;
                    int currentPosition = pedestal.getStoredValueForUpgrades();
                    BlockPos targetPos = getPosOfNextBlock(currentPosition,negBlockPos,posBlockPos);
                    BlockState targetBlock = world.getBlockState(targetPos);
                    upgradeAction(world, pedestal, itemInPedestal, pedestalPos, targetPos, targetBlock);
                    pedestal.setStoredValueForUpgrades(currentPosition+1);
                    if(resetCurrentPosInt(currentPosition,negBlockPos,posBlockPos))
                    {
                        pedestal.setStoredValueForUpgrades(0);
                    }
                }
            }
        }

        /*if(!world.isBlockPowered(pedestalPos)) {
            for (int x = negBlockPos.getX(); x <= posBlockPos.getX(); x++) {
                for (int z = negBlockPos.getZ(); z <= posBlockPos.getZ(); z++) {
                    for (int y = negBlockPos.getY(); y <= posBlockPos.getY(); y++) {
                        BlockPos posTargetBlock = new BlockPos(x, y, z);
                        BlockState targetBlock = world.getBlockState(posTargetBlock);
                        if (tick%speed == 0) {
                            ticked++;
                        }

                        if(ticked > 84)
                        {
                            upgradeAction(world, itemInPedestal, pedestalPos, posTargetBlock, targetBlock);
                            ticked=0;
                        }
                        else
                        {
                            ticked++;
                        }
                    }
                }
            }
        }*/
    }

    public void upgradeAction(World world, PedestalTileEntity pedestal, ItemStack itemInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
    {
        if(!world.isRemote)
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
                                FakePlayer fakePlayer = FakePlayerFactory.get(world.getServer().func_241755_D_(),new GameProfile(getPlayerFromCoin(pedestal.getCoinOnPedestal()),"[Pedestals]"));
                                //FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().func_241755_D_());
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
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);
        String trr = "" + getRangeHeight(stack) + "";
        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);

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

    public static final Item PLANTER = new ItemUpgradeEffectPlanter(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/planter"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(PLANTER);
    }


}
