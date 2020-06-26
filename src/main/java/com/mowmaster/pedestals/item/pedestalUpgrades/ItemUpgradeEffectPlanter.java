package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.pedestals;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
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
    public Boolean canAcceptRange() {
        return true;
    }

    public int getRangeWidth(ItemStack stack)
    {
        int rangeWidth = 0;
        int rW = getRangeModifier(stack);
        rangeWidth = ((rW)+1);
        return  rangeWidth;
    }


    public int ticked = 0;

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        int speed = getOperationSpeed(coinInPedestal);

        int width = getRangeWidth(coinInPedestal);
        int height = (2*width)+1;

        BlockPos negBlockPos = getNegRangePos(world,pedestalPos,width,height);
        BlockPos posBlockPos = getPosRangePos(world,pedestalPos,width,height);

        if(!world.isBlockPowered(pedestalPos)) {
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
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
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
                            if (world.setBlockState(posTarget, ((IPlantable) block).getPlant(world, posTarget))) {
                                this.removeFromPedestal(world,posOfPedestal,1);
                                world.setBlockState(posTarget,((IPlantable) block).getPlant(world, posTarget));
                                world.playSound((PlayerEntity) null, posTarget.getX(), posTarget.getY(), posTarget.getZ(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s3 = getRangeWidth(stack);
        String tr = "" + (s3+s3+1) + "";

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        area.func_240699_a_(TextFormatting.WHITE);
        speed.func_240699_a_(TextFormatting.RED);

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
