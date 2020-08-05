package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.tiles.TilePedestal;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (tick%speed == 0) {
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
        if(state.getBlock() instanceof BlockPedestalTE)
        {
            TilePedestal pedestal = ((TilePedestal)world.getTileEntity(posOfPedestal));

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
    public void actionOnCollideWithBlock(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            if(getItemStack.getItem().equals(Items.ENDER_PEARL))
            {
                int CurrentBurnTime = tilePedestal.getStoredValueForUpgrades();
                int getBurnTimeForStack = 16 * getItemStack.getCount();
                tilePedestal.setStoredValueForUpgrades(CurrentBurnTime + getBurnTimeForStack);

                entityIn.remove();
            }
            else if(getItemStack.getItem().equals(Items.CHORUS_FRUIT))
            {
                int CurrentBurnTime = tilePedestal.getStoredValueForUpgrades();
                int getBurnTimeForStack = 4 * getItemStack.getCount();
                tilePedestal.setStoredValueForUpgrades(CurrentBurnTime + getBurnTimeForStack);

                entityIn.remove();
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

    public boolean doTeleport(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn, boolean isItemEntity)
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
                        else if (canTeleportTo(world,tilePedestal,posPedestal,tilePedestal.getStoredPositionAt(i),isItemEntity) == 2 && removeFuel(tilePedestal,0,true) > 0)
                        {
                            int range = getRange(tilePedestal.getCoinOnPedestal());
                            int remainingFuel = removeFuel(tilePedestal,0,true);
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

    public int canTeleportTo(World world, TilePedestal tilePedestal, BlockPos posOrigPedestal, BlockPos posDestPedestal, boolean isItemEntity)
    {
        if(world.isAreaLoaded(posDestPedestal,1))
        {
            //If block ISNT powered
            if(!world.isBlockPowered(posDestPedestal))
            {
                //Make sure its a pedestal before getting the tile
                if(world.getBlockState(posDestPedestal).getBlock() instanceof BlockPedestalTE)
                {
                    //Get the tile before checking other things
                    if(world.getTileEntity(posDestPedestal) instanceof TilePedestal)
                    {
                        if(getTeleportDistance(posOrigPedestal,posDestPedestal) <= tilePedestal.getStoredValueForUpgrades())
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

    public boolean teleportEntity(World world, TilePedestal tilePedestal, BlockPos posPedestalDest, Entity entityIn)
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
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(TilePedestal pedestal, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            int fuelValue = pedestal.getStoredValueForUpgrades();

            double d0 = (double)getPosOfBlockBelow(world,pos,-1 ).getX() + 0.55D - (double)(rand.nextFloat() * 0.1F);
            double d1 = (double)getPosOfBlockBelow(world,pos,-1 ).getY() + 0.0D - (double)(rand.nextFloat() * 0.1F);
            double d2 = (double)getPosOfBlockBelow(world,pos,-1 ).getZ() + 0.55D - (double)(rand.nextFloat() * 0.1F);
            double d3 = (double)(0.4F - (rand.nextFloat() + rand.nextFloat()) * 0.4F);

            if(fuelValue > 0)
            {
                world.addParticle(ParticleTypes.PORTAL, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.0D, (double)pos.getZ() + 0.5D,0, 0, 0);
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

        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".chat_range");
        range.appendString(""+getRange(stack)+"");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));

        range.mergeStyle(TextFormatting.WHITE);
        speed.mergeStyle(TextFormatting.RED);

        player.sendMessage(range,player.getUniqueID());


        //Display Fuel Left
        int fuelLeft = pedestal.getStoredValueForUpgrades();
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.appendString("" + fuelLeft + "");
        fuel.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,player.getUniqueID());

        //Display Speed Last Like on Tooltips
        player.sendMessage(speed,player.getUniqueID());
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
