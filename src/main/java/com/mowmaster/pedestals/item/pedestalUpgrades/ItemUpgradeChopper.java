package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.pedestals;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
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
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeChopper extends ItemUpgradeBase
{
    public ItemUpgradeChopper(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos blockToChopPos, BlockState blockToChop, BlockPos posOfPedestal)
    {
        if(!blockToChop.getBlock().isAir(blockToChop,world,blockToChopPos) && blockToChop.getBlock().isIn(BlockTags.LOGS) || blockToChop.getBlock().isIn(BlockTags.LEAVES))
        {
            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().getWorld(world.getDimension().getType()));
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

            if(fakePlayer.canHarvestBlock(blockToChop))
            {
                blockToChop.getBlock().harvestBlock(world, fakePlayer, blockToChopPos, blockToChop, null, fakePlayer.getHeldItemMainhand());
            }
            blockToChop.getBlock().removedByPlayer(blockToChop,world,blockToChopPos,fakePlayer,false,null);
        }
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
        area.appendText(tr);
        area.appendText(areax.getFormattedText());
        area.appendText(tr);
        area.appendText(areax.getFormattedText());
        area.appendText(trr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendText(getOperationSpeedString(stack));

        area.applyTextStyle(TextFormatting.WHITE);
        tooltip.add(area);

        speed.applyTextStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item CHOPPER = new ItemUpgradeChopper(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/chopper"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(CHOPPER);
    }


}
