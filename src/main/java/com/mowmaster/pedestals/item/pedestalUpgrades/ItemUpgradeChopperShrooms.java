package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
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
import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeChopperShrooms extends ItemUpgradeBase
{
    public ItemUpgradeChopperShrooms(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

    public int getRangeHeight(ItemStack stack)
    {
        int rangeHeight = 0;
        int rH = getRangeModifier(stack);
        rangeHeight = ((rH*6)+4);
        return rangeHeight;
    }

    public int ticked = 0;

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int rangeWidth = getRangeWidth(coinInPedestal);
            int rangeHeight = getRangeHeight(coinInPedestal);
            int speed = getOperationSpeed(coinInPedestal);

            BlockPos negNums = getNegRangePos(world,pedestalPos,rangeWidth,rangeHeight);
            BlockPos posNums = getPosRangePos(world,pedestalPos,rangeWidth,rangeHeight);

            if(!world.isBlockPowered(pedestalPos)) {
                for (int x = negNums.getX(); x <= posNums.getX(); x++) {
                    for (int z = negNums.getZ(); z <= posNums.getZ(); z++) {
                        for (int y = negNums.getY(); y <= posNums.getY(); y++) {
                            BlockPos blockToChopPos = new BlockPos(x, y, z);
                            //BlockPos blockToChopPos = this.getPos().add(x, y, z);
                            BlockState blockToChop = world.getBlockState(blockToChopPos);
                            if (tick%speed == 0) {
                                ticked++;
                            }

                            if(ticked > 84)
                            {
                                upgradeAction(world, itemInPedestal, coinInPedestal, blockToChopPos, blockToChop, pedestalPos);
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

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos blockToChopPos, BlockState blockToChop, BlockPos posOfPedestal)
    {
        //wart blocks*, wart stems*, crimson stems*, shroomlight*, mushroom stems, mushroom brown and mushroom red
        if(!blockToChop.getBlock().isAir(blockToChop,world,blockToChopPos) && blockToChop.getBlock().isIn(BlockTags.field_232874_ao_) || blockToChop.getBlock().isIn(BlockTags.field_232889_z_) || blockToChop.getBlock().isIn(BlockTags.field_232888_y_)
                || blockToChop.getBlock().equals(Blocks.field_235383_mw_) || blockToChop.getBlock().equals(Blocks.MUSHROOM_STEM) || blockToChop.getBlock().equals(Blocks.BROWN_MUSHROOM_BLOCK) || blockToChop.getBlock().equals(Blocks.RED_MUSHROOM_BLOCK))
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

            /*if(ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToChop,true))
            {
                blockToChop.getBlock().harvestBlock(world, fakePlayer, blockToChopPos, blockToChop, null, fakePlayer.getHeldItemMainhand());
                world.setBlockState(blockToChopPos, Blocks.AIR.getDefaultState());
            }*/

            if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToChop,true)) {

                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, blockToChopPos, blockToChop, fakePlayer);
                if (!MinecraftForge.EVENT_BUS.post(e)) {
                    blockToChop.getBlock().harvestBlock(world, fakePlayer, blockToChopPos, blockToChop, null, fakePlayer.getHeldItemMainhand());
                    blockToChop.getBlock().onBlockHarvested(world, blockToChopPos, blockToChop, fakePlayer);

                    world.removeBlock(blockToChopPos, false);
                }
                //world.setBlockState(posOfBlock, Blocks.AIR.getDefaultState());
            }
            //blockToChop.getBlock().removedByPlayer(blockToChop,world,blockToChopPos,fakePlayer,false,null);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.func_240699_a_(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());

        int s3 = getRangeWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + (getRangeHeight(stack)+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(trr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        area.func_240699_a_(TextFormatting.WHITE);
        player.sendMessage(area,player.getUniqueID());

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        if(map.size() > 0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_enchants");
            enchant.func_240699_a_(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,player.getUniqueID());

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.func_240699_a_(TextFormatting.GRAY);
                    player.sendMessage(enchants,player.getUniqueID());
                }
            }
        }

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));
        speed.func_240699_a_(TextFormatting.RED);
        player.sendMessage(speed,player.getUniqueID());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s3 = getRangeWidth(stack);
        int s4 = getRangeHeight(stack);

        String tr = "" + (s3+s3+1) + "";
        String trr = "" + (s4+1) + "";

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(trr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        area.func_240699_a_(TextFormatting.WHITE);
        tooltip.add(area);

        speed.func_240699_a_(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item CHOPPER = new ItemUpgradeChopperShrooms(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/choppershrooms"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(CHOPPER);
    }


}
