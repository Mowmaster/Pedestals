package com.mowmaster.pedestals.Items.MechanicalOnlyStorage;

import com.mowmaster.mowlib.Items.BaseEnergyDropItem;
import com.mowmaster.mowlib.Items.BaseItemStackDropItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class BaseItemBulkStorageItem extends BaseItemStackDropItem implements IBulkItem {

    public BaseItemBulkStorageItem(Properties p_41383_) {
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
    ///
    // Remove later once mowlib updates
    ///

    public static int getItemColor()
    {
        return 16776960;
    }

    public int getMaxAllowedStorage()
    {
        return PedestalConfig.COMMON.bulkstorage_maxItemStorage.get();
    }

    public void setStacksList(ItemStack stack, List<ItemStack> itemStacksList)
    {
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        CompoundTag returnerTag = MowLibCompoundTagUtils.writeItemStackListToNBT(MODID, tag, itemStacksList,"_bulkitemstorage");
        stack.setTag(returnerTag);
    }

    public List<ItemStack> getStacksList(ItemStack stack)
    {
        List<ItemStack> stacked = new ArrayList<>();
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();

        if(tag.contains(MODID+"_bulkitemstorage")) { stacked = MowLibCompoundTagUtils.readItemStackListFromNBT(MODID, tag,"_bulkitemstorage"); }

        return stacked;
    }

    public int getItemVariant(ItemStack stack) {
        if (getStacksList(stack).size() > 0) {
            double renderDevider = 100.0D * (double)(this.getStacksList(stack).size() / this.getMaxAllowedStorage());
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

            if(survival && PedestalConfig.COMMON.bulkstorage_itemDischarge_toggle.get())
            {
                counter++;
                if(counter>=20)
                {
                    increaseDischargeCounter(p_41404_);
                    counter=0;
                }

                if(getDischargeCounter(p_41404_) >= PedestalConfig.COMMON.bulkstorage_itemDischarge.get())
                {
                    removeStacks(p_41405_,p_41406_.getOnPos(),p_41404_);
                    p_41404_.shrink(1);
                }
            }
        }
    }

    public void removeStacks(Level level, BlockPos pos, ItemStack packageItem) {

        List<ItemStack> stacked = getStacksList(packageItem);
        for(ItemStack stack:stacked)
        {
            MowLibItemUtils.spawnItemStack(level,pos.getX(),pos.getY(),pos.getZ(),stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        int stored = getStacksList(p_41421_).size();
        if(stored>0)
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_item"), ChatFormatting.GOLD);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal(getStacksList(p_41421_).get(0).getDisplayName().getString() + ": " + stored + " Stacks"), ChatFormatting.WHITE);
        }

        int charge = getDischargeCounter(p_41421_);
        if(charge>0)
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_item_discharge"), ChatFormatting.RED);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal("" + charge + "/" + PedestalConfig.COMMON.bulkstorage_energyDischarge.get() +""), ChatFormatting.WHITE);
        }
    }
}
