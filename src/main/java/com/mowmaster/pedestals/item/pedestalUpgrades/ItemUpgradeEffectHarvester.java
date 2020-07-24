package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.pedestals;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ai.brain.task.VillagerTasks;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEffectHarvester extends ItemUpgradeBase
{
    public ItemUpgradeEffectHarvester(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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
        if(!world.isRemote)
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
                                upgradeAction(world, itemInPedestal,coinInPedestal, pedestalPos, posTargetBlock, targetBlock);
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

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
    {
        if(target.getBlock() instanceof IGrowable && !target.getBlock().isAir(target,world,posTarget))
        {
            if(!((IGrowable) target.getBlock()).canGrow(world,posTarget,target,false))
            {
                FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().func_241755_D_());
                fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());
                ItemStack choppingAxe = new ItemStack(Items.DIAMOND_AXE,1);
                if(!itemInPedestal.isEmpty())
                {
                    fakePlayer.setHeldItem(Hand.MAIN_HAND,itemInPedestal);
                }
                else
                {
                    if(EnchantmentHelper.getEnchantments(coinInPedestal).containsKey(Enchantments.SILK_TOUCH))
                    {
                        choppingAxe.addEnchantment(Enchantments.SILK_TOUCH,1);
                        fakePlayer.setHeldItem(Hand.MAIN_HAND,choppingAxe);
                    }
                    else if (EnchantmentHelper.getEnchantments(coinInPedestal).containsKey(Enchantments.FORTUNE))
                    {
                        int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE,coinInPedestal);
                        choppingAxe.addEnchantment(Enchantments.FORTUNE,lvl);
                        fakePlayer.setHeldItem(Hand.MAIN_HAND,choppingAxe);
                    }
                    else
                    {
                        fakePlayer.setHeldItem(Hand.MAIN_HAND,choppingAxe);
                    }
                }

                if(ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,target,true))
                {
                    if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,target,true)) {

                        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, posTarget, target, fakePlayer);
                        if (!MinecraftForge.EVENT_BUS.post(e)) {
                            target.getBlock().harvestBlock(world, fakePlayer, posTarget, target, null, fakePlayer.getHeldItemMainhand());
                            target.getBlock().onBlockHarvested(world, posTarget, target, fakePlayer);

                            world.removeBlock(posTarget, false);
                        }
                        //world.setBlockState(posOfBlock, Blocks.AIR.getDefaultState());
                    }
                    /*target.getBlock().harvestBlock(world, fakePlayer, posTarget, target, null, fakePlayer.getHeldItemMainhand());
                    world.setBlockState(posTarget, Blocks.AIR.getDefaultState());*/
                }

                //target.getBlock().removedByPlayer(target,world,posTarget,fakePlayer,false,null);
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

    public static final Item HARVESTER = new ItemUpgradeEffectHarvester(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/harvester"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(HARVESTER);
    }


}
