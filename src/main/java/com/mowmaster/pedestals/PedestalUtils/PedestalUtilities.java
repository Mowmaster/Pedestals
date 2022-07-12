package com.mowmaster.pedestals.PedestalUtils;

import com.mowmaster.mowlib.Capabilities.Dust.CapabilityDust;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Capability.Experience.CapabilityExperience;
import com.mowmaster.pedestals.Capability.Experience.IExperienceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeAbstractMinecart;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

import static com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock.FACING;

public class PedestalUtilities
{
    public static int getRedstoneLevelPedestal(Level worldIn, BlockPos pos)
    {
        int hasItem=0;
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if(blockEntity instanceof BasePedestalBlockEntity) {
            BasePedestalBlockEntity pedestal = ((BasePedestalBlockEntity) blockEntity);
            ItemStack itemstack = pedestal.getItemInPedestal();
            ItemStack coin = pedestal.getCoinOnPedestal();
            /*if(coin.getItem() instanceof IPedestalUpgrade)
            {
                return ((IPedestalUpgrade)coin.getItem()).getComparatorRedstoneLevel(worldIn,pos);
            }*/
            if(!itemstack.isEmpty())
            {
                float f = (float)itemstack.getCount()/(float)itemstack.getMaxStackSize();
                hasItem = (int)Math.floor(f*14.0F)+1;
            }
        }

        return hasItem;
    }

    public static LazyOptional<IItemHandler> findItemHandlerAtPos(Level world, BlockPos pos, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {

                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos.above()), entity -> entity instanceof Boat);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IItemHandler> findItemHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
            if(!list.isEmpty())
            {
                LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                if(cap.isPresent())
                    return cap;
            }
            /*if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
                System.out.println(list);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }*/
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IFluidHandler> findFluidHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IFluidHandler> cap = neighbourTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
            if(!list.isEmpty())
            {
                LazyOptional<IFluidHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                if(cap.isPresent())
                    return cap;
            }
            /*if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IFluidHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof Boat);
                if(!list.isEmpty())
                {
                    LazyOptional<IFluidHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }*/
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IEnergyStorage> findEnergyHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IEnergyStorage> cap = neighbourTile.getCapability(CapabilityEnergy.ENERGY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
            if(!list.isEmpty())
            {
                LazyOptional<IEnergyStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityEnergy.ENERGY);
                if(cap.isPresent())
                    return cap;
            }
            /*if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IEnergyStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityEnergy.ENERGY);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof Boat);
                if(!list.isEmpty())
                {
                    LazyOptional<IEnergyStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityEnergy.ENERGY);
                    if(cap.isPresent())
                        return cap;
                }
            }*/
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IExperienceStorage> findExperienceHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IExperienceStorage> cap = neighbourTile.getCapability(CapabilityExperience.EXPERIENCE, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
            if(!list.isEmpty())
            {
                LazyOptional<IExperienceStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityExperience.EXPERIENCE);
                if(cap.isPresent())
                    return cap;
            }
            /*if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IExperienceStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityExperience.EXPERIENCE);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof Boat);
                if(!list.isEmpty())
                {
                    LazyOptional<IExperienceStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityExperience.EXPERIENCE);
                    if(cap.isPresent())
                        return cap;
                }
            }*/
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IDustHandler> findDustHandlerAtPos(Level world, BlockPos pos, Direction side)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IDustHandler> cap = neighbourTile.getCapability(CapabilityDust.DUST_HANDLER, side);
            if(cap.isPresent())
                return cap;
        }

        return LazyOptional.empty();
    }

    public static int removeXp(Player player, int amount) {
        //Someday consider using player.addExpierence()
        int startAmount = amount;
        while(amount > 0) {
            int barCap = player.getXpNeededForNextLevel();
            int barXp = (int) (barCap * player.experienceProgress);
            int removeXp = Math.min(barXp, amount);
            int newBarXp = barXp - removeXp;
            amount -= removeXp;//amount = amount-removeXp

            player.totalExperience -= removeXp;
            if(player.totalExperience < 0) {
                player.totalExperience = 0;
            }
            if(newBarXp == 0 && amount > 0) {
                player.experienceLevel--;
                if(player.experienceLevel < 0) {
                    player.experienceLevel = 0;
                    player.totalExperience = 0;
                    player.experienceProgress = 0;
                    break;
                } else {
                    player.experienceProgress = 1.0F;
                }
            } else {
                player.experienceProgress = newBarXp / (float) barCap;
            }
        }
        return startAmount - amount;
    }

    public BlockPos getPosOfBlockBelow(Level level, BlockPos posOfPedestal, int numBelow)
    {
        BlockState state = level.getBlockState(posOfPedestal);

        Direction enumfacing = (state.hasProperty(FACING))?(state.getValue(FACING)):(Direction.UP);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.offset(0,-numBelow,0);
            case DOWN:
                return blockBelow.offset(0,numBelow,0);
            case NORTH:
                return blockBelow.offset(0,0,numBelow);
            case SOUTH:
                return blockBelow.offset(0,0,-numBelow);
            case EAST:
                return blockBelow.offset(-numBelow,0,0);
            case WEST:
                return blockBelow.offset(numBelow,0,0);
            default:
                return blockBelow;
        }
    }
}
