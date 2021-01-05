package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.references.Reference;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
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
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeEnergyQuarryBlacklist extends ItemUpgradeBaseEnergyMachine
{
    public ItemUpgradeEnergyQuarryBlacklist(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    //Gets Capacity and Speed By Default

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {return true;}

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
        return getRangeLarge(stack);
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
            int width = getAreaWidth(coin);
            int widthModified = (width*2)+1;
            int height = getRangeHeight(coin);
            int amount = blocksToMineInArea(pedestal,width,height);
            int area = Math.multiplyExact(Math.multiplyExact(widthModified,widthModified),height);
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
            rate = (EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack) > 5)?(5):(EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.OPERATIONSPEED,stack));
        }
        return rate;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            //Set Default Energy Buffer
            int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
            if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}

            int rangeWidth = getAreaWidth(coinInPedestal);
            int rangeHeight = getRangeHeight(coinInPedestal);
            int speed = getOperationSpeed(coinInPedestal);

            BlockState pedestalState = world.getBlockState(pedestalPos);
            Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
            BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
            BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));

            if(world.isAreaLoaded(negNums,posNums))
            {
                if(!world.isBlockPowered(pedestalPos)) {

                    //Should disable magneting when its not needed
                    AxisAlignedBB getBox = new AxisAlignedBB(negNums,posNums);
                    List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);
                    if(itemList.size()>0)
                    {
                        upgradeActionMagnet(world, itemList, itemInPedestal, pedestalPos, rangeWidth, rangeHeight);
                    }

                    int fuelToConsume = rfCostPerItemSmelted;
                    if(hasEnergy(coinInPedestal) && removeEnergyFuel(pedestal,fuelToConsume,true)>=0)
                    {
                        if(blocksToMineInArea(pedestal,rangeWidth,rangeHeight) > 0)
                        {
                            if (world.getGameTime() % speed == 0) {
                                int currentPosition = 0;
                                for(currentPosition = getStoredInt(coinInPedestal);!resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));currentPosition++)
                                {
                                    BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                    BlockPos blockToMinePos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                    if(canMineBlock(pedestal,blockToMinePos))
                                    {
                                        writeStoredIntToNBT(coinInPedestal,currentPosition);
                                        break;
                                    }
                                }
                                BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                BlockState targetBlock = world.getBlockState(targetPos);
                                upgradeAction(pedestal, world, itemInPedestal, coinInPedestal, targetPos, targetBlock, pedestalPos);
                                if(resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums)))
                                {
                                    writeStoredIntToNBT(coinInPedestal,0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos blockToMinePos, BlockState blockToMine, BlockPos posOfPedestal)
    {
        int fuelToConsume = rfCostPerItemSmelted;
        if(canMineBlock(pedestal, blockToMinePos))
        {
            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
            fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());
            ItemStack pick = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_PICKAXE,1));

            if(!pedestal.hasTool())
            {
                if(EnchantmentHelper.getEnchantments(coinInPedestal).containsKey(Enchantments.SILK_TOUCH))
                {
                    pick.addEnchantment(Enchantments.SILK_TOUCH,1);
                }
                else if (EnchantmentHelper.getEnchantments(coinInPedestal).containsKey(Enchantments.FORTUNE))
                {
                    int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE,coinInPedestal);
                    pick.addEnchantment(Enchantments.FORTUNE,lvl);
                }
            }

            if(removeEnergyFuel(pedestal,fuelToConsume,true)>=0)
            {
                if(!fakePlayer.getHeldItemMainhand().equals(pick))fakePlayer.setHeldItem(Hand.MAIN_HAND,pick);
                ToolType tool = blockToMine.getHarvestTool();
                int toolLevel = fakePlayer.getHeldItemMainhand().getHarvestLevel(tool, fakePlayer, blockToMine);
                if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer,blockToMine,toolLevel >= blockToMine.getHarvestLevel())) {
                    blockToMine.getBlock().harvestBlock(world, fakePlayer, blockToMinePos, blockToMine, null, fakePlayer.getHeldItemMainhand());
                    blockToMine.getBlock().onBlockHarvested(world, blockToMinePos, blockToMine, fakePlayer);
                    removeEnergyFuel(pedestal,fuelToConsume,false);
                    world.removeBlock(blockToMinePos, false);
                    PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,blockToMinePos.getX(),blockToMinePos.getY(),blockToMinePos.getZ(),255,164,0));
                }
            }
        }
    }

    @Override
    public boolean passesFilter(World world, BlockPos posPedestal, Block blockIn)
    {
        boolean returner = true;
        BlockPos posInventory = getPosOfBlockBelow(world, posPedestal, 1);

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posPedestal),true);
        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            if(handler != null)
            {
                int range = handler.getSlots();

                ItemStack itemFromInv = ItemStack.EMPTY;
                itemFromInv = IntStream.range(0,range)//Int Range
                        .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                        .filter(itemStack -> Block.getBlockFromItem(itemStack.getItem()).equals(blockIn))
                        .findFirst().orElse(ItemStack.EMPTY);

                if(!itemFromInv.isEmpty())
                {
                    returner = false;
                }
            }
        }

        return returner;
    }

    @Override
    public String getOperationSpeedString(ItemStack stack)
    {
        TranslationTextComponent normal = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_0");
        TranslationTextComponent twox = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_1");
        TranslationTextComponent fourx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_2");
        TranslationTextComponent sixx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_3");
        TranslationTextComponent tenx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_4");
        TranslationTextComponent twentyx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_5");
        String str = normal.getString();
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                str = normal.getString();//normal speed
                break;
            case 1:
                str = twox.getString();//2x faster
                break;
            case 2:
                str = fourx.getString();//4x faster
                break;
            case 3:
                str = sixx.getString();//6x faster
                break;
            case 4:
                str = tenx.getString();//10x faster
                break;
            case 5:
                str = twentyx.getString();//20x faster
                break;
            default: str = normal.getString();;
        }

        return  str;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.DUMMY_UUID);

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
        btm.appendString("" + blocksToMineInArea(pedestal,getAreaWidth(pedestal.getCoinOnPedestal()),getRangeHeight(pedestal.getCoinOnPedestal())) + "");
        btm.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.DUMMY_UUID);

        //Display Fuel Left
        int fuelValue = getEnergyStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.appendString("" + fuelValue/2500 + "");
        fuel.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,Util.DUMMY_UUID);

        ItemStack toolStack = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(new ItemStack(Items.DIAMOND_PICKAXE));
        TranslationTextComponent tool = new TranslationTextComponent(getTranslationKey() + ".chat_tool");
        tool.append(toolStack.getDisplayName());
        tool.mergeStyle(TextFormatting.BLUE);
        player.sendMessage(tool,Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        if(map.size() > 0 && getNumNonPedestalEnchants(map)>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_enchants");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.DUMMY_UUID);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea) && !(enchantment instanceof EnchantmentCapacity))
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

        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

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
        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfstored");
        xpstored.appendString(""+ getEnergyStored(stack) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(xpstored);

        TranslationTextComponent xpcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfcapacity");
        xpcapacity.appendString(""+ getEnergyBuffer(stack) +"");
        xpcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(xpcapacity);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item RFQUARRYB = new ItemUpgradeEnergyQuarryBlacklist(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rfquarryb"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFQUARRYB);
    }


}
