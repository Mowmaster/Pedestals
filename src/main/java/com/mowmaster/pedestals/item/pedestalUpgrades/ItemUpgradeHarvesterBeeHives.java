package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.KelpTopBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.BeehiveDispenseBehavior;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeHarvesterBeeHives extends ItemUpgradeBase
{
    public ItemUpgradeHarvesterBeeHives(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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
        return new int[]{getRangeHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    //https://github.com/Lothrazar/Cyclic/blob/trunk/1.16/src/main/java/com/lothrazar/cyclic/block/harvester/TileHarvester.java#L157
    public static IntegerProperty getBlockPropertyHoney(BlockState blockState) {
        for (Property<?> prop : blockState.getProperties()) {
            if (prop != null && prop.getName() != null && prop instanceof IntegerProperty && prop.getName().equalsIgnoreCase("honey_level")) {
                return (IntegerProperty) prop;
            }
        }
        return null;
    }

    //https://github.com/Lothrazar/Cyclic/blob/trunk/1.16/src/main/java/com/lothrazar/cyclic/block/harvester/TileHarvester.java#L113
    public boolean canHarvest(World world, BlockState state)
    {
        boolean returner = false;
        //BeehiveDispenseBehavior
        if(state.isIn(BlockTags.BEEHIVES))
        {
            IntegerProperty propInt = getBlockPropertyHoney(state);
            if (propInt == null || !(world instanceof ServerWorld)) {
                returner = false;
            }
            else
            {
                int current = state.get(propInt);
                int max = Collections.max(propInt.getAllowedValues());
                //Taked from onBlockActivated method in the BeehiveBlock class
                if(current >= 5)
                {
                    returner = true;
                }
            }
        }

        return returner;
    }

    public int ticked = 0;

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);

            int width = getAreaWidth(coinInPedestal);
            int height = getRangeHeight(coinInPedestal)+1;

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
                        upgradeAction(world, itemInPedestal,coinInPedestal, pedestalPos, targetPos, targetBlock);
                        pedestal.setStoredValueForUpgrades(currentPosition+1);
                        if(resetCurrentPosInt(currentPosition,negBlockPos,posBlockPos))
                        {
                            pedestal.setStoredValueForUpgrades(0);
                        }
                    }
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
    {

        if(canHarvest(world,target) && !target.getBlock().isAir(target,world,posTarget))
        {

            FakePlayer fakePlayer = FakePlayerFactory.get(world.getServer().func_241755_D_(),new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
            fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());
            ItemStack harvestingShears = new ItemStack(Items.SHEARS,1);
            fakePlayer.setHeldItem(Hand.MAIN_HAND,harvestingShears);
            BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
            ItemStack itemFromInv = ItemStack.EMPTY;
            LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                if(invToPullFrom instanceof PedestalTileEntity) {
                    itemFromInv = ItemStack.EMPTY;

                }
                else {
                    if(handler != null)
                    {
                        int i = getNextSlotWithItemsCap(cap,getStackInPedestal(world,posOfPedestal));
                        if(i>=0)
                        {
                            itemFromInv = handler.getStackInSlot(i);
                            if(itemFromInv.getItem().equals(Items.GLASS_BOTTLE))
                            {
                                fakePlayer.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
                                PlayerInteractEvent.RightClickBlock e = new PlayerInteractEvent.RightClickBlock(fakePlayer,Hand.MAIN_HAND,posTarget,Direction.UP);
                                if (!MinecraftForge.EVENT_BUS.post(e)) {
                                    TileEntity tile = world.getTileEntity(posOfPedestal);
                                    if(tile instanceof PedestalTileEntity)
                                    {
                                        PedestalTileEntity pedestal = ((PedestalTileEntity) tile);
                                        if(pedestal.addItem(new ItemStack(Items.HONEY_BOTTLE)))
                                        {
                                            handler.extractItem(i,1 ,false);
                                            ((BeehiveBlock)target.getBlock()).takeHoney(world,target,posTarget,fakePlayer, BeehiveTileEntity.State.BEE_RELEASED);
                                            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.HARVESTED,posTarget.getX(),posTarget.getY(),posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                                            world.playSound((PlayerEntity)null, posTarget, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                PlayerInteractEvent.RightClickBlock e = new PlayerInteractEvent.RightClickBlock(fakePlayer,Hand.MAIN_HAND,posTarget,Direction.UP);
                                if (!MinecraftForge.EVENT_BUS.post(e)) {
                                    TileEntity tile = world.getTileEntity(posOfPedestal);
                                    if(tile instanceof PedestalTileEntity)
                                    {
                                        PedestalTileEntity pedestal = ((PedestalTileEntity) tile);
                                        if(pedestal.addItem(new ItemStack(Items.HONEYCOMB,3)))
                                        {
                                            ((BeehiveBlock)target.getBlock()).takeHoney(world,target,posTarget,fakePlayer, BeehiveTileEntity.State.BEE_RELEASED);
                                            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.HARVESTED,posTarget.getX(),posTarget.getY(),posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                                            world.playSound((PlayerEntity)null, posTarget, SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
            //Is it needed? idk
            else
            {
                PlayerInteractEvent.RightClickBlock e = new PlayerInteractEvent.RightClickBlock(fakePlayer,Hand.MAIN_HAND,posTarget,Direction.UP);
                if (!MinecraftForge.EVENT_BUS.post(e)) {
                    TileEntity tile = world.getTileEntity(posOfPedestal);
                    if(tile instanceof PedestalTileEntity)
                    {
                        PedestalTileEntity pedestal = ((PedestalTileEntity) tile);
                        if(pedestal.addItem(new ItemStack(Items.HONEYCOMB,3)))
                        {
                            ((BeehiveBlock)target.getBlock()).takeHoney(world,target,posTarget,fakePlayer, BeehiveTileEntity.State.BEE_RELEASED);
                            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.HARVESTED,posTarget.getX(),posTarget.getY(),posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                            world.playSound((PlayerEntity)null, posTarget, SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
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

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + getRangeHeight(stack) + "";
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

    public static final Item HARVESTERHIVES = new ItemUpgradeHarvesterBeeHives(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/harvesterhives"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(HARVESTERHIVES);
    }


}
