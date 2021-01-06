package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
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
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeBreaker extends ItemUpgradeBase
{

    public int range = 1;

    public ItemUpgradeBreaker(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {return true;}

    public int getRange(ItemStack stack)
    {
        return  getRangeSmall(stack);
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
                //Should disable magneting when its not needed
                int range = getRange(coinInPedestal);
                BlockState pedestalState = world.getBlockState(pedestalPos);
                Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
                BlockPos negNums = getNegRangePosEntity(world,pedestalPos,1,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(-range-1):(-range));
                BlockPos posNums = getPosRangePosEntity(world,pedestalPos,1,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(-range-1):(-range));
                AxisAlignedBB getBox = new AxisAlignedBB(negNums,posNums);
                List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);
                if(itemList.size()>0)
                {
                    upgradeActionMagnet(world, itemList, itemInPedestal, pedestalPos, 1, 1);
                }

                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal);
                }
            }
        }

    }

    public void upgradeAction(PedestalTileEntity pedestal) {
        World world = pedestal.getWorld();
        BlockPos posOfPedestal = pedestal.getPos();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        int range = getRange(coinInPedestal);

        FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
        //FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().func_241755_D_());
        fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());
        ItemStack pickaxe = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_PICKAXE,1));
        BlockPos posOfBlock = getPosOfBlockBelow(world, posOfPedestal, range);
        BlockState blockToBreak = world.getBlockState(posOfBlock);

        /*
        BREAKS BLOCKS AND DROPS THEM IN WORLD FOR PICKUP LATER
         */
        if(!pedestal.hasTool())
        {
            pickaxe = getToolDefaultEnchanted(coinInPedestal,pickaxe);
        }

        ToolType tool = blockToBreak.getHarvestTool();
        int toolLevel = pickaxe.getHarvestLevel(tool, null, blockToBreak);
        //if (!blockToBreak.getBlock().isAir(blockToBreak, world, posOfBlock) && !(blockToBreak.getBlock() instanceof IFluidBlock || blockToBreak.getBlock() instanceof FlowingFluidBlock) && toolLevel >= blockToBreak.getHarvestLevel() &&blockToBreak.getBlockHardness(world, posOfBlock) != -1.0F) {
        if(canMineBlock(pedestal, posOfBlock))
        {
            if (!fakePlayer.getHeldItemMainhand().equals(pickaxe)) {
                fakePlayer.setHeldItem(Hand.MAIN_HAND, pickaxe);
            }

            tool = blockToBreak.getHarvestTool();
            toolLevel = fakePlayer.getHeldItemMainhand().getHarvestLevel(tool, fakePlayer, blockToBreak);
            if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToBreak,toolLevel >= blockToBreak.getHarvestLevel())) {
                //This event is already called in the Event factory doPlayerHarvestCheck
                //BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, posOfBlock, blockToBreak, fakePlayer);
                //if (MinecraftForge.EVENT_BUS.post(e)) {
                    blockToBreak.getBlock().harvestBlock(world, fakePlayer, posOfBlock, blockToBreak, null, fakePlayer.getHeldItemMainhand());
                    blockToBreak.getBlock().onBlockHarvested(world, posOfBlock, blockToBreak, fakePlayer);
                    int expdrop = blockToBreak.getBlock().getExpDrop(blockToBreak,world,posOfBlock,
                            (EnchantmentHelper.getEnchantments(fakePlayer.getHeldItemMainhand()).containsKey(Enchantments.FORTUNE))?(EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE,fakePlayer.getHeldItemMainhand())):(0),
                            (EnchantmentHelper.getEnchantments(fakePlayer.getHeldItemMainhand()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH,fakePlayer.getHeldItemMainhand())):(0));
                    if(expdrop>0)blockToBreak.getBlock().dropXpOnBlockBreak((ServerWorld)world,posOfPedestal,expdrop);
                    world.removeBlock(posOfBlock, false);
                //}
            }
        }
    }

    @Override
    public boolean hasAcceptableTool(ItemStack tool)
    {
        if(tool.getItem() instanceof ToolItem
                || tool.getItem() instanceof SwordItem
                || tool.getToolTypes().contains(ToolType.PICKAXE)
                || tool.getToolTypes().contains(ToolType.HOE)
                || tool.getToolTypes().contains(ToolType.AXE)
                || tool.getToolTypes().contains(ToolType.SHOVEL))
        {
            return true;
        }

        return false;
    }

    //No filter here, Jokes on YOU!
    @Override
    public boolean passesFilter(World world, BlockPos posPedestal, Block blockIn)
    {
        return true;
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

        ItemStack toolStack = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_PICKAXE));
        TranslationTextComponent tool = new TranslationTextComponent(getTranslationKey() + ".chat_tool");
        tool.append(toolStack.getDisplayName());
        tool.mergeStyle(TextFormatting.BLUE);
        player.sendMessage(tool,Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments((pedestal.hasTool())?(pedestal.getToolOnPedestal()):(stack));
        if(hasAdvancedInventoryTargeting(stack))
        {
            map.put(EnchantmentRegistry.ADVANCED,1);
        }
        if(map.size() > 0 && getNumNonPedestalEnchants(map)>0)
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
