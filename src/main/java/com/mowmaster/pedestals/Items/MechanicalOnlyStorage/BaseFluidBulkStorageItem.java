package com.mowmaster.pedestals.Items.MechanicalOnlyStorage;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.BaseDustDropItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibFluidUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class BaseFluidBulkStorageItem extends BaseDustDropItem {

    public BaseFluidBulkStorageItem(Properties p_41383_) {
        super(p_41383_);
    }

    private int getDischargeCounter(ItemStack stack){
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_dischargeCounter")){
            return readIntegerFromNBT(MODID,tag,"_dischargeCounter");
        }
        else {
            setDischargeCounter(stack);
            return 0;
        }
    }
    private void increaseDischargeCounter(ItemStack stack){
        int current = 0;
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_dischargeCounter"))current = readIntegerFromNBT(MODID,tag,"_dischargeCounter");
        writeIntegerToNBT(MODID,tag,(current+1),"_dischargeCounter");
    }
    private void setDischargeCounter(ItemStack stack){
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        writeIntegerToNBT(MODID,tag,0,"_dischargeCounter");
    }

    ///
    // Remove later once mowlib updates
    ///
    public static CompoundTag writeIntegerToNBT(String ModID, @Nullable CompoundTag inputNBT, int input, String intName) {
        CompoundTag compound = inputNBT != null ? inputNBT : new CompoundTag();
        compound.putInt(ModID + intName, input);
        return compound;
    }

    public static int readIntegerFromNBT(String ModID, CompoundTag inputNBT, String intName)
    {
        if(inputNBT.contains(ModID + intName))
        {
            return inputNBT.getInt(ModID + intName);
        }
        return 0;
    }

    public static CompoundTag writeFluidStackToNBT(String ModID, @Nullable CompoundTag inputNBT, FluidStack fluidStack) {
        CompoundTag compound = inputNBT != null ? inputNBT : new CompoundTag();
        if (fluidStack.isEmpty()) {
            return compound;
        } else {
            compound.put(ModID + "_storedFluid", fluidStack.writeToNBT(new CompoundTag()));
            return compound;
        }
    }

    public static FluidStack readFluidStackFromNBT(String ModID, CompoundTag inputNBT) {
        FluidStack fluidStack = FluidStack.EMPTY;
        if (inputNBT.contains(ModID + "_storedFluid")) {
            return FluidStack.loadFluidStackFromNBT(inputNBT.getCompound(ModID + "_storedFluid"));
        } else {
            return fluidStack;
        }
    }
    ///
    // Remove later once mowlib updates
    ///

    public static int getItemColor()
    {
        return 2518783;
    }

    public int getMaxAllowedStorage()
    {
        return PedestalConfig.COMMON.pedestal_baseFluidStorage.get() + (PedestalConfig.COMMON.augment_t4StorageFluid.get() * PedestalConfig.COMMON.augment_t4StorageInsertSize.get());
    }

    public void setFluidStack(ItemStack stack, FluidStack stackFluid)
    {
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        writeFluidStackToNBT(MODID, tag, stackFluid);
        stack.setTag(tag);
    }

    public FluidStack getFluid(ItemStack stack)
    {
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        return readFluidStackFromNBT(MODID,tag);
    }

    public void addFluid(ItemStack stack, FluidStack stackFluid)
    {
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        FluidStack currentStack = readFluidStackFromNBT(MODID,tag);
        if(currentStack.isFluidEqual(stackFluid))
        {
            int difference = currentStack.getAmount() + stackFluid.getAmount();
            currentStack.setAmount((difference>getMaxAllowedStorage())?(getMaxAllowedStorage()):(difference));
            writeFluidStackToNBT(MODID, tag, currentStack);
            stack.setTag(tag);
        }
    }

    public void removeFluid(ItemStack stack, int removed)
    {
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        FluidStack currentStack = readFluidStackFromNBT(MODID,tag);
        int difference = currentStack.getAmount() - removed;
        if(difference>0) { currentStack.setAmount(difference); }
        else { currentStack = FluidStack.EMPTY; }
        writeFluidStackToNBT(MODID, tag, currentStack);
        stack.setTag(tag);
    }

    public int getItemVariant(ItemStack stack) {
        FluidStack fluidStack = getFluid(stack).copy();
        if (!fluidStack.isEmpty()) {
            double renderDevider = 100.0D * (double)(fluidStack.getAmount() / this.getMaxAllowedStorage());
            if (renderDevider <= 25.0D) {
                return 0;
            }

            if (renderDevider <= 50.0D) {
                return 1;
            }

            if (renderDevider <= 75.0D) {
                return 2;
            }

            if (renderDevider >= 100.0D) {
                return 3;
            }
        }

        return 0;
    }

    private int counter = 0;
    @Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) {
        super.inventoryTick(p_41404_, p_41405_, p_41406_, p_41407_, p_41408_);

        if(!p_41405_.isClientSide())
        {
            boolean survival = true;
            if(p_41406_ instanceof Player player)
            {
                if(player.isCreative()) survival = false;
            }

            if(survival && PedestalConfig.COMMON.bulkstorage_fluidDischarge_toggle.get())
            {
                FluidStack fluidIn = getFluid(p_41404_);
                if(!fluidIn.isEmpty())
                {
                    counter++;
                    if(counter>=20)
                    {
                        increaseDischargeCounter(p_41404_);
                        counter=0;
                    }
                }

                if(getDischargeCounter(p_41404_) >= PedestalConfig.COMMON.bulkstorage_fluidDischarge.get())
                {
                    removeFluid(p_41405_,p_41406_.getOnPos(),p_41404_);
                    p_41404_.shrink(1);
                }
            }
        }
    }

    public void removeFluid(Level worldIn, BlockPos pos, ItemStack stack) {
        FluidStack fluidStack = getFluid(stack).copy();
        int radius = getItemVariant(stack)+1;
        Item item = fluidStack.getFluid().getBucket();
        int x = -radius;
        int z = -radius;
        int y = 0;

        while(fluidStack.getAmount() >= 1000) {
            if (item instanceof BucketItem) {
                BucketItem bucketItem = (BucketItem)item;
                BlockState state = worldIn.getBlockState(pos.offset(x, y, z));
                if (state.getBlock().equals(Blocks.AIR) && bucketItem.emptyContents((Player)null, worldIn, pos.offset(x, y, z), (BlockHitResult)null)) {
                    fluidStack.grow(-1000);
                }

                if (x >= 1 && z >= 1) {
                    ++y;
                    x = -1;
                    z = -1;
                }

                if (x >= 1) {
                    x = -1;
                    ++z;
                }

                ++x;
            }

            if(y>radius)break;
        }
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        FluidStack fluid = getFluid(p_41421_);
        if(!fluid.isEmpty())
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_fluid"), ChatFormatting.GOLD);
            MutableComponent comp = fluid.getDisplayName().copy();
            comp.append(": " + fluid.getAmount() + "mb");
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,comp, ChatFormatting.WHITE);
        }

        int charge = getDischargeCounter(p_41421_);
        if(charge>0)
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_fluid_discharge"), ChatFormatting.RED);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal("" + charge + "/" + PedestalConfig.COMMON.bulkstorage_fluidDischarge.get() +""), ChatFormatting.WHITE);
        }
    }
}
