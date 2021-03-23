package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.item.pedestalFilters.ItemFilterBase;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.util.PedestalFakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeQuarry extends ItemUpgradeBase
{
    public ItemUpgradeQuarry(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    @Override
    public Boolean canAcceptCapacity() {
        return false;
    }

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {return true;}

    @Override
    public Boolean canAcceptMagnet() {
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
        return getRangeLargest(stack);
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

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            int width = getAreaWidth(pedestal.getCoinOnPedestal());
            int widdth = (width*2)+1;
            int height = getRangeHeight(pedestal.getCoinOnPedestal());
            int amount = workQueueSize(coin);
            int area = Math.multiplyExact(Math.multiplyExact(widdth,widdth),height);
            if(amount>0)
            {
                float f = (float)amount/(float)area;
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    @Override
    public int intOperationalSpeedModifier(ItemStack stack)
    {
        int rate = 0;
        if(hasEnchant(stack))
        {
            rate = (intOperationalSpeedModifierOverride(stack) > 10)?(10):(intOperationalSpeedModifierOverride(stack));
        }
        return rate;
    }

    @Override
    public int getOperationSpeed(ItemStack stack)
    {
        int intOperationalSpeed = 128;
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                intOperationalSpeed = 128;//normal speed
                break;
            case 1:
                intOperationalSpeed=112;//0.25x faster
                break;
            case 2:
                intOperationalSpeed = 96;//0.5x faster
                break;
            case 3:
                intOperationalSpeed = 80;//0.75x faster
                break;
            case 4:
                intOperationalSpeed = 64;//2x faster
                break;
            case 5:
                intOperationalSpeed=32;//4x faster
                break;
            case 6:
                intOperationalSpeed=16;//8x faster
                break;
            case 7:
                intOperationalSpeed=8;//16x faster
                break;
            case 8:
                intOperationalSpeed=4;//32x faster
                break;
            case 9:
                intOperationalSpeed=2;//64x faster
                break;
            case 10:
                intOperationalSpeed=1;//128x faster
                break;
            default: intOperationalSpeed=128;
        }

        return  intOperationalSpeed;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int rangeWidth = getAreaWidth(coinInPedestal);
            int rangeHeight = getRangeHeight(coinInPedestal);
            int speed = getOperationSpeed(coinInPedestal);

            BlockState pedestalState = world.getBlockState(pedestalPos);
            Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
            BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
            BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));

            if(world.isAreaLoaded(negNums,posNums))
            {
                if(!pedestal.isPedestalBlockPowered(world,pedestalPos)) {

                    if(hasMagnetEnchant(coinInPedestal))
                    {
                        //Should disable magneting when its not needed
                        AxisAlignedBB getBox = new AxisAlignedBB(negNums,posNums);
                        List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);
                        if(itemList.size()>0)
                        {
                            upgradeActionMagnet(pedestal, world, itemList, itemInPedestal, pedestalPos);
                        }
                    }

                    int val = readStoredIntTwoFromNBT(coinInPedestal);
                    if(val>0)
                    {
                        if (world.getGameTime()%5 == 0) {
                            BlockPos directionalPos = getPosOfBlockBelow(world,pedestalPos,0);
                            if(!pedestal.hasParticleDiffuser())PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,directionalPos.getX(),directionalPos.getY(),directionalPos.getZ(),145,145,145));
                        }
                        writeStoredIntTwoToNBT(coinInPedestal,val-1);
                    }
                    else {

                        //If work queue doesnt exist, try to make one
                        if(workQueueSize(coinInPedestal)<=0)
                        {
                            buildWorkQueue(pedestal,rangeWidth,rangeHeight);
                        }

                        //
                        if(workQueueSize(coinInPedestal) > 0)
                        {
                            List<BlockPos> workQueue = readWorkQueueFromNBT(coinInPedestal);
                            if (world.getGameTime() % speed == 0) {
                                for(int i = 0;i< workQueue.size(); i++)
                                {
                                    BlockPos targetPos = workQueue.get(i);
                                    BlockPos blockToMinePos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                    BlockState targetBlock = world.getBlockState(blockToMinePos);
                                    if(canMineBlock(pedestal,blockToMinePos))
                                    {
                                        workQueue.remove(i);
                                        writeWorkQueueToNBT(coinInPedestal,workQueue);
                                        upgradeAction(pedestal, world, itemInPedestal, coinInPedestal, targetPos, targetBlock, pedestalPos);
                                        break;
                                    }
                                    else
                                    {
                                        workQueue.remove(i);
                                    }
                                }
                                writeWorkQueueToNBT(coinInPedestal,workQueue);
                            }
                        }
                        else {
                            int delay = rangeWidth*rangeWidth*rangeHeight;
                            writeStoredIntTwoToNBT(coinInPedestal,(delay<100)?(100):(delay));
                        }
                    }
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos blockToMinePos, BlockState blockToMine, BlockPos posOfPedestal)
    {
        ItemStack pick = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_PICKAXE,1));
        FakePlayer fakePlayer = new PedestalFakePlayer((ServerWorld) world,getPlayerFromCoin(coinInPedestal),posOfPedestal,pick.copy());
        if(!fakePlayer.getPosition().equals(new BlockPos(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ()))) {fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());}

        if(!pedestal.hasTool())
        {
            pick = getToolDefaultEnchanted(coinInPedestal,pick);
        }

        if(!fakePlayer.getHeldItemMainhand().equals(pick))fakePlayer.setHeldItem(Hand.MAIN_HAND,pick);
        ToolType tool = blockToMine.getHarvestTool();
        int toolLevel = fakePlayer.getHeldItemMainhand().getHarvestLevel(tool, fakePlayer, blockToMine);

        if(canMineBlock(pedestal, blockToMinePos,fakePlayer))
        {
            //ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToMine,toolLevel >= blockToMine.getHarvestLevel())
            if (ForgeHooks.canHarvestBlock(blockToMine,fakePlayer,world,blockToMinePos)) {
                blockToMine.getBlock().harvestBlock(world, fakePlayer, blockToMinePos, blockToMine, null, fakePlayer.getHeldItemMainhand());
                blockToMine.getBlock().onBlockHarvested(world, blockToMinePos, blockToMine, fakePlayer);
                int expdrop = blockToMine.getBlock().getExpDrop(blockToMine,world,blockToMinePos,
                        (EnchantmentHelper.getEnchantments(fakePlayer.getHeldItemMainhand()).containsKey(Enchantments.FORTUNE))?(EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE,fakePlayer.getHeldItemMainhand())):(0),
                        (EnchantmentHelper.getEnchantments(fakePlayer.getHeldItemMainhand()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH,fakePlayer.getHeldItemMainhand())):(0));
                if(expdrop>0)blockToMine.getBlock().dropXpOnBlockBreak((ServerWorld)world,posOfPedestal,expdrop);
                world.removeBlock(blockToMinePos, false);
                if(!pedestal.hasParticleDiffuser())PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,blockToMinePos.getX(),blockToMinePos.getY(),blockToMinePos.getZ(),255,164,0));
            }
        }
    }

    @Override
    public boolean passesFilter(World world, BlockPos posPedestal, Block blockIn)
    {
        boolean returner = true;
        if(world.getTileEntity(posPedestal) instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = ((PedestalTileEntity)world.getTileEntity(posPedestal));
            if(pedestal.hasFilter())
            {
                Item filterInPedestal = pedestal.getFilterInPedestal().getItem();
                if(filterInPedestal instanceof ItemFilterBase)
                {
                    returner = ((ItemFilterBase) filterInPedestal).canAcceptItem(pedestal,new ItemStack(blockIn));
                }
            }
        }

        return returner;
    }

    @Override
    public String getOperationSpeedString(ItemStack stack)
    {
        TranslationTextComponent zero = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_0");
        TranslationTextComponent one = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_1");
        TranslationTextComponent two = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_2");
        TranslationTextComponent three = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_3");
        TranslationTextComponent four = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_4");
        TranslationTextComponent five = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_5");
        TranslationTextComponent six = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_6");
        TranslationTextComponent seven = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_7");
        TranslationTextComponent eight = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_8");
        TranslationTextComponent nine = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_9");
        TranslationTextComponent ten = new TranslationTextComponent(Reference.MODID + ".quarry_tooltips" + ".speed_10");
        String str = zero.getString();
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                str = zero.getString();
                break;
            case 1:
                str = one.getString();
                break;
            case 2:
                str = two.getString();
                break;
            case 3:
                str = three.getString();
                break;
            case 4:
                str = four.getString();
                break;
            case 5:
                str = five.getString();
                break;
            case 6:
                str = six.getString();
                break;
            case 7:
                str = seven.getString();
                break;
            case 8:
                str = eight.getString();
                break;
            case 9:
                str = nine.getString();
                break;
            case 10:
                str = ten.getString();
                break;
            default: str = zero.getString();
        }

        return  str;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        int s3 = getAreaWidth(stack);
        int s4 = getRangeHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);

        //Display Blocks To Mine Left
        TranslationTextComponent btm = new TranslationTextComponent(getTranslationKey() + ".chat_btm");
        btm.appendString("" + ((workQueueSize(stack)>0)?(workQueueSize(stack)):(0)) + "");
        btm.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.DUMMY_UUID);

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
        player.sendMessage(speed,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //super.addInformation(stack, worldIn, tooltip, flagIn);
        int s3 = getAreaWidth(stack);
        int s4 = getRangeHeight(stack);

        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);

        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item QUARRY = new ItemUpgradeQuarry(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/quarry"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(QUARRY);
    }


}
