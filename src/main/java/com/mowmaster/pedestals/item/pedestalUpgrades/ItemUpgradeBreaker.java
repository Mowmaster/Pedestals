package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeBreaker extends ItemUpgradeBase
{

    public int range = 1;

    public ItemUpgradeBreaker(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    public int getRange(ItemStack stack)
    {
        switch (getRangeModifier(stack))
        {
            case 0:
                range = 1;
                break;
            case 1:
                range = 2;
                break;
            case 2:
                range = 4;
                break;
            case 3:
                range = 8;
                break;
            case 4:
                range = 12;
                break;
            case 5:
                range = 16;
                break;
            default: range = 1;
        }

        return  range;
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

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinOnPedestal) {
        int range = getRange(coinOnPedestal);

        FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinOnPedestal),"[Pedestals]"));
        //FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().func_241755_D_());
        fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE, 1);
        BlockPos posOfBlock = getPosOfBlockBelow(world, posOfPedestal, range);
        BlockState blockToBreak = world.getBlockState(posOfBlock);

        /*
        BREAKS BLOCKS AND DROPS THEM IN WORLD FOR PICKUP LATER
         */

        if (!blockToBreak.getBlock().isAir(blockToBreak, world, posOfBlock) && !(blockToBreak.getBlock() instanceof IFluidBlock || blockToBreak.getBlock() instanceof FlowingFluidBlock) && blockToBreak.getBlockHardness(world, posOfBlock) != -1.0F) {
            if (itemInPedestal.getItem() instanceof PickaxeItem || itemInPedestal.getToolTypes().contains(ToolType.PICKAXE) && !fakePlayer.getHeldItemMainhand().equals(itemInPedestal)) {
                fakePlayer.setHeldItem(Hand.MAIN_HAND, itemInPedestal);
            }
            else {
                if (EnchantmentHelper.getEnchantments(coinOnPedestal).containsKey(Enchantments.SILK_TOUCH)) {
                    pickaxe.addEnchantment(Enchantments.SILK_TOUCH, 1);
                    fakePlayer.setHeldItem(Hand.MAIN_HAND, pickaxe);
                } else if (EnchantmentHelper.getEnchantments(coinOnPedestal).containsKey(Enchantments.FORTUNE)) {
                    int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, coinOnPedestal);
                    pickaxe.addEnchantment(Enchantments.FORTUNE, lvl);
                    fakePlayer.setHeldItem(Hand.MAIN_HAND, pickaxe);
                } else {
                    fakePlayer.setHeldItem(Hand.MAIN_HAND, pickaxe);
                }
            }

            if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToBreak,true)) {

                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, posOfBlock, blockToBreak, fakePlayer);
                if (!MinecraftForge.EVENT_BUS.post(e)) {
                    blockToBreak.getBlock().harvestBlock(world, fakePlayer, posOfBlock, blockToBreak, null, fakePlayer.getHeldItemMainhand());
                    blockToBreak.getBlock().onBlockHarvested(world, posOfBlock, blockToBreak, fakePlayer);

                    world.removeBlock(posOfBlock, false);
                }
                //world.setBlockState(posOfBlock, Blocks.AIR.getDefaultState());
            }
            //blockToBreak.getBlock().removedByPlayer(blockToBreak, world, posOfBlock, fakePlayer, false, null);
        }

    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);
        //Util.DUMMY_UUID

        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".chat_range");
        range.appendString(""+getRange(stack)+"");
        range.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(range,Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        if(map.size() > 0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_enchants");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.DUMMY_UUID);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants,Util.DUMMY_UUID);
                }
            }
        }

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
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
        tooltip.add(range);

        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item BREAKER = new ItemUpgradeBreaker(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/breaker"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(BREAKER);
    }


}
