package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

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
        else
        {
            doTeleport(world, tilePedestal, posPedestal, state, entityIn,true);
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
                        if(canTeleportTo(world,tilePedestal,posPedestal,tilePedestal.getStoredPositionAt(i),isItemEntity))
                        {

                            if(teleportEntity(world, tilePedestal, tilePedestal.getStoredPositionAt(i), state, entityIn))
                            {
                                removeFuel(tilePedestal,getTeleportDistance(posPedestal,tilePedestal.getStoredPositionAt(i)),false);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean canTeleportTo(World world, TilePedestal tilePedestal, BlockPos posOrigPedestal, BlockPos posDestPedestal, boolean itItemEntity)
    {
        int range = getRange(tilePedestal.getCoinOnPedestal());
        BlockPos posDestBlock = getPosOfBlockBelow(world,posDestPedestal,range);

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
                        if(removeFuel(tilePedestal,getTeleportDistance(posOrigPedestal,posDestPedestal),true)>=0)
                        {
                            if(itItemEntity)
                            {
                                if(world.isAirBlock(posDestBlock) || world.getBlockState(posDestBlock).getBlock().equals(Blocks.WATER))
                                {
                                    return true;
                                }
                            }
                            else
                            {
                                if(world.isAirBlock(posDestPedestal) || world.getBlockState(posDestBlock).equals(Blocks.WATER) && world.isAirBlock(posDestPedestal.add(0,1,0)) || world.getBlockState(posDestPedestal.add(0,1,0)).equals(Blocks.WATER))
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean teleportEntity(World world, TilePedestal tilePedestal, BlockPos posPedestalDest, BlockState state, Entity entityIn)
    {
        int range = getRange(tilePedestal.getCoinOnPedestal());
        BlockPos pos = getPosOfBlockBelow(world,posPedestalDest,range);
        if(entityIn instanceof PlayerEntity)
        {
            ((PlayerEntity)entityIn).stopRiding();
            ((ServerPlayerEntity)entityIn).connection.setPlayerLocation(pos.getX(), pos.getY(), pos.getZ(), entityIn.rotationYaw, entityIn.rotationPitch);
            world.playSound(null, entityIn.getPosX()+0.5D, entityIn.getPosY(), entityIn.getPosZ()+0.5D, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }
        else if(entityIn instanceof CreatureEntity) {
            ((CreatureEntity) entityIn).stopRiding();
            if (((CreatureEntity) entityIn).attemptTeleport(pos.getX(), pos.getY(), pos.getZ(), true)) {

                world.playSound(null, posPedestalDest.getX()+0.5D, posPedestalDest.getY(), posPedestalDest.getZ()+0.5D, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                return true;
            }
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
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //super.addInformation(stack, worldIn, tooltip, flagIn);
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.func_240699_a_(TextFormatting.GOLD);
        tooltip.add(t);

        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".tooltip_range");
        range.func_240702_b_("" + getRange(stack) + "");
        range.func_240699_a_(TextFormatting.WHITE);
        tooltip.add(range);
    }

    public static final Item TELEPORTER = new ItemUpgradeTeleporter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/teleporter"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(TELEPORTER);
    }
}
