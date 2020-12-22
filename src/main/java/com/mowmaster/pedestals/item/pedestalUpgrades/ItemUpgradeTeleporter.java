package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nullable;
import java.util.*;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeTeleporter extends ItemUpgradeBaseMachine
{
    public ItemUpgradeTeleporter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    @Override
    public Boolean canAcceptCapacity() {
        return false;
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRange(coin);
        BlockPos posOfBlock = pos;
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof PedestalTileEntity)
        {
            if(((PedestalTileEntity)tile).getNumberOfStoredLocations()>0)
            {
                BlockPos posBlock = ((PedestalTileEntity)tile).getStoredPositionAt(0);

                posOfBlock = getPosOfBlockBelow(world, posBlock, range);

            }
        }

        return posOfBlock.getX();
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRange(coin);
        BlockPos posOfBlock = pos;
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof PedestalTileEntity)
        {
            if(((PedestalTileEntity)tile).getNumberOfStoredLocations()>0)
            {
                BlockPos posBlock = ((PedestalTileEntity)tile).getStoredPositionAt(0);

                posOfBlock = getPosOfBlockBelow(world, posBlock, range);

            }
        }

        return new int[]{posOfBlock.getY(),1};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRange(coin);
        BlockPos posOfBlock = pos;
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof PedestalTileEntity)
        {
            if(((PedestalTileEntity)tile).getNumberOfStoredLocations()>0)
            {
                BlockPos posBlock = ((PedestalTileEntity)tile).getStoredPositionAt(0);
                posOfBlock = getPosOfBlockBelow(world, posBlock, range);
            }
        }

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
            int getMaxFuelValue = 2000000000;
            if(!hasMaxFuelSet(coinInPedestal) || readMaxFuelFromNBT(coinInPedestal) != getMaxFuelValue) {setMaxFuel(coinInPedestal, getMaxFuelValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(world, itemInPedestal, coinInPedestal, pedestalPos);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int width = 0;
        int height = 1;
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);
        BlockState state = world.getBlockState(posOfPedestal);
        if(state.getBlock() instanceof PedestalBlock)
        {
            PedestalTileEntity pedestal = ((PedestalTileEntity)world.getTileEntity(posOfPedestal));

            AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

            List<Entity> entityList = world.getEntitiesWithinAABB(Entity.class,getBox);
            for(Entity getFromList : entityList)
            {
                if(!(getFromList instanceof ItemEntity))
                {
                    doTeleport(world, pedestal, posOfPedestal, state, getFromList,false);
                }
            }
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            if(getItemStack.getItem().equals(Items.ENDER_PEARL))
            {
                int getBurnTimeForStack = 16 * getItemStack.getCount();
                if(addFuel(tilePedestal,getBurnTimeForStack,true))
                {
                    addFuel(tilePedestal,getBurnTimeForStack,false);
                    //tilePedestal.setStoredValueForUpgrades(CurrentBurnTime + getBurnTimeForStack);

                    entityIn.remove();
                }
            }
            else if(getItemStack.getItem().equals(Items.CHORUS_FRUIT))
            {
                //int CurrentBurnTime = tilePedestal.getStoredValueForUpgrades();
                int getBurnTimeForStack = 4 * getItemStack.getCount();
                if(addFuel(tilePedestal,getBurnTimeForStack,true))
                {
                    addFuel(tilePedestal,getBurnTimeForStack,false);
                    //tilePedestal.setStoredValueForUpgrades(CurrentBurnTime + getBurnTimeForStack);
                    entityIn.remove();
                }
            }
            else
            {
                doTeleport(world, tilePedestal, posPedestal, state, entityIn,true);
            }
        }
    }

    public int getTeleportDistance(BlockPos pedestalOrigin, BlockPos pedestalDestination)
    {
        int x = pedestalDestination.getX();
        int y = pedestalDestination.getY();
        int z = pedestalDestination.getZ();
        int x1 = pedestalOrigin.getX();
        int y1 = pedestalOrigin.getY();
        int z1 = pedestalOrigin.getZ();
        int xF = Math.abs(Math.subtractExact(x,x1));
        int yF = Math.abs(Math.subtractExact(y,y1));
        int zF = Math.abs(Math.subtractExact(z,z1));

        return xF+yF+zF;
    }

    public boolean doTeleport(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn, boolean isItemEntity)
    {
        if(!world.isBlockPowered(posPedestal))
        {
            if(tilePedestal.getNumberOfStoredLocations()>0)
            {
                for(int i=0; i<tilePedestal.getNumberOfStoredLocations();i++)
                {
                    if(tilePedestal.getStoredPositionAt(i) != posPedestal)
                    {
                        if(canTeleportTo(world,tilePedestal,posPedestal,tilePedestal.getStoredPositionAt(i),isItemEntity) == 1)
                        {
                            if(teleportEntity(world, tilePedestal, tilePedestal.getStoredPositionAt(i), entityIn))
                            {
                                world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                removeFuel(tilePedestal,getTeleportDistance(posPedestal,tilePedestal.getStoredPositionAt(i)),false);
                            }
                            break;
                        }
                        //Random teleport to use up remaining fuel???
                        else if (canTeleportTo(world,tilePedestal,posPedestal,tilePedestal.getStoredPositionAt(i),isItemEntity) == 2 && hasFuel(tilePedestal.getCoinOnPedestal()))
                        {
                            int range = getRange(tilePedestal.getCoinOnPedestal());
                            int remainingFuel = getFuelStored(tilePedestal.getCoinOnPedestal());
                            BlockPos randomPos = world.getBlockRandomPos((int)entityIn.getPosX(),(int)entityIn.getPosY(),(int)entityIn.getPosZ(),range*remainingFuel);
                            if(teleportEntityRandom(world, randomPos, entityIn))
                            {
                                world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                removeFuel(tilePedestal,remainingFuel,false);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int canTeleportTo(World world, PedestalTileEntity tilePedestal, BlockPos posOrigPedestal, BlockPos posDestPedestal, boolean isItemEntity)
    {
        if(world.isAreaLoaded(posDestPedestal,1))
        {
            //If block ISNT powered
            if(!world.isBlockPowered(posDestPedestal))
            {
                //Make sure its a pedestal before getting the tile
                if(world.getBlockState(posDestPedestal).getBlock() instanceof PedestalBlock)
                {
                    //Get the tile before checking other things
                    if(world.getTileEntity(posDestPedestal) instanceof PedestalTileEntity)
                    {
                        if(getTeleportDistance(posOrigPedestal,posDestPedestal) <= getFuelStored(tilePedestal.getCoinOnPedestal()))
                        {
                            int range = getRange(tilePedestal.getCoinOnPedestal());
                            BlockPos posDestBlock = getPosOfBlockBelow(world,posDestPedestal,range);
                            BlockState blocktoTPto = world.getBlockState(posDestBlock);
                            if(isItemEntity)
                            {
                                //why this works this way, idk
                                if(world.isAirBlock(posDestBlock) || (blocktoTPto.getBlock() instanceof IFluidBlock || blocktoTPto.getBlock() instanceof FlowingFluidBlock))
                                {
                                    return 1;
                                }
                            }
                            else
                            {
                                if(world.isAirBlock(posDestBlock) || (blocktoTPto.getBlock() instanceof IFluidBlock || blocktoTPto.getBlock() instanceof FlowingFluidBlock)
                                        && world.isAirBlock(posDestBlock.add(0,1,0)) || (blocktoTPto.getBlock() instanceof IFluidBlock || blocktoTPto.getBlock() instanceof FlowingFluidBlock))
                                {
                                    return 1;
                                }
                            }
                        }
                        //return 2 means not enough fuel so random teleport
                        else return 2;
                    }
                }
            }
        }
        return 0;
    }

    public boolean teleportEntity(World world, PedestalTileEntity tilePedestal, BlockPos posPedestalDest, Entity entityIn)
    {
        int range = getRange(tilePedestal.getCoinOnPedestal());
        BlockPos pos = getPosOfBlockBelow(world,posPedestalDest,range);
        if(entityIn instanceof PlayerEntity)
        {
            ((PlayerEntity)entityIn).stopRiding();
            ((ServerPlayerEntity)entityIn).connection.setPlayerLocation(pos.getX()+0.5D, pos.getY(), pos.getZ()+0.5D, entityIn.rotationYaw, entityIn.rotationPitch);
            world.playSound(null, entityIn.getPosX(), entityIn.getPosY(), entityIn.getPosZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }
        else if(entityIn instanceof CreatureEntity) {
            ((CreatureEntity) entityIn).stopRiding();
            ((CreatureEntity) entityIn).teleportKeepLoaded(pos.getX()+0.5D, pos.getY(), pos.getZ()+0.5D);
            world.playSound(null, posPedestalDest.getX(), posPedestalDest.getY(), posPedestalDest.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }
        else if(entityIn instanceof ItemEntity)
        {
            ((ItemEntity)entityIn).stopRiding();
            ((ItemEntity)entityIn).teleportKeepLoaded(pos.getX()+0.5D, pos.getY(), pos.getZ()+0.5D);
            world.playSound(null, posPedestalDest.getX(), posPedestalDest.getY(), posPedestalDest.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

    public boolean teleportEntityRandom(World world, BlockPos posPedestalDest, Entity entityIn)
    {
        if(entityIn instanceof PlayerEntity)
        {
            ((PlayerEntity)entityIn).stopRiding();
            ((ServerPlayerEntity)entityIn).connection.setPlayerLocation(posPedestalDest.getX(), posPedestalDest.getY(), posPedestalDest.getZ(), entityIn.rotationYaw, entityIn.rotationPitch);
            world.playSound(null, entityIn.getPosX()+0.5D, entityIn.getPosY(), entityIn.getPosZ()+0.5D, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }
        else if(entityIn instanceof CreatureEntity) {
            ((CreatureEntity) entityIn).stopRiding();
            if (((CreatureEntity) entityIn).attemptTeleport(posPedestalDest.getX(), posPedestalDest.getY(), posPedestalDest.getZ(), true)) {

                world.playSound(null, posPedestalDest.getX()+0.5D, posPedestalDest.getY(), posPedestalDest.getZ()+0.5D, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                return true;
            }
        }
        else if(entityIn instanceof ItemEntity)
        {
            ((ItemEntity)entityIn).stopRiding();
            ((ItemEntity)entityIn).teleportKeepLoaded(posPedestalDest.getX()+0.5D, posPedestalDest.getY(), posPedestalDest.getZ()+0.5D);
            world.playSound(null, posPedestalDest.getX(), posPedestalDest.getY(), posPedestalDest.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        return false;
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
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            int fuelValue = getFuelStored(pedestal.getCoinOnPedestal());

            //More than 1 smelt worth
            if(fuelValue > 0)
            {
                spawnParticleAroundPedestalBase(world,tick,pos, ParticleTypes.PORTAL);
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

        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".chat_range");
        range.appendString(""+getRange(stack)+"");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));

        range.mergeStyle(TextFormatting.WHITE);
        speed.mergeStyle(TextFormatting.RED);

        player.sendMessage(range,Util.DUMMY_UUID);


        //Display Fuel Left
        int fuelLeft = getFuelStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.appendString("" + fuelLeft + "");
        fuel.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //super.addInformation(stack, worldIn, tooltip, flagIn);
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".tooltip_range");
        range.appendString("" + getRange(stack) + "");
        range.mergeStyle(TextFormatting.WHITE);
        tooltip.add(range);

        TranslationTextComponent fuelStored = new TranslationTextComponent(getTranslationKey() + ".tooltip_fuelstored");
        fuelStored.appendString(""+ getFuelStored(stack) +"");
        fuelStored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(fuelStored);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item TELEPORTER = new ItemUpgradeTeleporter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/teleporter"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(TELEPORTER);
    }
}
