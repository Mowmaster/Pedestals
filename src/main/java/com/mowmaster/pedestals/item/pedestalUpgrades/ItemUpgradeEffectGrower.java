package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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

public class ItemUpgradeEffectGrower extends ItemUpgradeBase
{
    public ItemUpgradeEffectGrower(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

    public int ticked = 0;

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);
            int width = getAreaWidth(coinInPedestal);
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

    }

    public void upgradeAction(World world, ItemStack itemInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
    {
        ServerWorld sworld = world.getServer().func_241755_D_();
        ItemStack bonemeal = new ItemStack(Items.BONE_MEAL);
        Random rand = new Random();

        if(target.getBlock() instanceof IGrowable || target.getBlock() instanceof IPlantable)
        {
            if (target.getBlock() instanceof IGrowable) {
                if(((IGrowable) target.getBlock()).canGrow(world,posTarget,target,false))
                {
                    if(doItemsMatch(itemInPedestal,bonemeal))
                    {
                        ((IGrowable) target.getBlock()).grow(sworld,rand,posTarget,target);
                        TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                        if(pedestalInv instanceof TilePedestal) {
                            ((TilePedestal) pedestalInv).removeItem(1);
                        }
                    }
                    else
                    {
                        target.tick(sworld,posTarget,rand);
                    }
                }
            }
            else
            {
                target.getBlock().tick(target,sworld,posTarget,rand);
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,player.getUniqueID());

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,player.getUniqueID());
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
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(speed);
    }

    public static final Item GROWER = new ItemUpgradeEffectGrower(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/grower"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(GROWER);
    }


}
