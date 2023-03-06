package com.mowmaster.pedestals.Items.Tools;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class PedestalManifestTool extends BaseTool implements IPedestalTool
{
    public PedestalManifestTool(Properties p_41383_) {
        super(p_41383_);
    }

    public static int getManifestType(ItemStack stack) {

        //This will be a true or false thing
        //0= false, 1 = true
        //texture will change and thats it basically, blank manifest page to one with lines is all

        return 0;
    }



    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level level = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInHand = player.getItemInHand(hand);

        HitResult result = player.pick(5,0,false);
        BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
        if(result.getType().equals(HitResult.Type.MISS))
        {
            //Clear Manifest
            CompoundTag getTagOnItem = stackInHand.getOrCreateTag();
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hasrender");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hasredstone");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hassignal");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_haslight");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hasspeed");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hascapacity");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hasstorage");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hasrange");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hasrobin");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hascollide");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intaugmentspeed");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intaugmentcapacity");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intaugmentstorage");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intaugmentrange");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalrender");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalredstone");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalsignal");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalspeed");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalitemtransfer");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalfluidtransfer");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalenergytransfer");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalexptransfer");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestaldusttransfer");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalstorageitem");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalstoragefluid");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalstorageenergy");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalstorageexp");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestalstoragedust");
            MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_intpedestaltotalrange");
        }
        else if(result.getType().equals(HitResult.Type.BLOCK))
        {
            BlockState getBlockState = level.getBlockState(pos);
            if(getBlockState.getBlock() instanceof BasePedestalBlock)
            {
                BlockEntity tile = level.getBlockEntity(pos);
                if(tile instanceof BasePedestalBlockEntity)
                {
                    BasePedestalBlockEntity ped = ((BasePedestalBlockEntity)tile);
                    CompoundTag getTagOnItem = stackInHand.getOrCreateTag();

                    boolean hasRenderer = ped.hasRenderAugment();
                    boolean hasRedstone = ped.hasRedstone();
                    boolean isCurrentlyPowered = ped.isPedestalBlockPowered(ped);
                    boolean hasGlowstone = ped.hasLight();
                    boolean hasSpeed = ped.hasSpeed();
                    boolean hasCapacity = ped.hasCapacity();
                    boolean hasStorage = ped.hasStorage();
                    boolean hasRange = ped.hasRange();

                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasRenderer,"_hasrender");
                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasRedstone,"_hasredstone");
                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,isCurrentlyPowered,"_hassignal");
                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasGlowstone,"_haslight");
                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasSpeed,"_hasspeed");
                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasCapacity,"_hascapacity");
                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasStorage,"_hasstorage");
                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasRange,"_hasrange");

                    //Create Manifest of Augments
                    if(player.isCrouching())
                    {
                        int renderType = ped.getRendererType();
                        int getRedstone = ped.getRedstonePowerNeeded();
                        int getCurrentPower = ped.getRedstonePower();
                        int getCurrentSpeed = ped.getCurrentSpeed();
                        int getCapacityItem = ped.getItemTransferRate();
                        int getCapacityFluid = ped.getFluidTransferRate();
                        int getCapacityEnergy = ped.getEnergyTransferRate();
                        int getCapacityExp = ped.getExperienceTransferRate();
                        int getCapacityDust = ped.getDustTransferRate();
                        int getStorageItem = ped.getItemSlotIncreaseFromStorage()+1;
                        int getStorageFluid = ped.getFluidCapacity();
                        int getStorageEnergy = ped.getEnergyCapacity();
                        int getStorageExp = ped.getExperienceCapacity();
                        int getStorageDust = ped.getDustCapacity();
                        int getRangeIncrease = ped.getLinkingRange();
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,renderType,"_intpedestalrender");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getRedstone,"_intpedestalredstone");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getCurrentPower,"_intpedestalsignal");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getCurrentSpeed,"_intpedestalspeed");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getCapacityItem,"_intpedestalitemtransfer");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getCapacityFluid,"_intpedestalfluidtransfer");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getCapacityEnergy,"_intpedestalenergytransfer");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getCapacityExp,"_intpedestalexptransfer");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getCapacityDust,"_intpedestaldusttransfer");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getStorageItem,"_intpedestalstorageitem");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getStorageFluid,"_intpedestalstoragefluid");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getStorageEnergy,"_intpedestalstorageenergy");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getStorageExp,"_intpedestalstorageexp");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getStorageDust,"_intpedestalstoragedust");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getRangeIncrease,"_intpedestaltotalrange");
                    }
                    else
                    {
                        //include renderer in tooltip
                        boolean hasRoundRobin = ped.hasRRobin();
                        boolean hasCollide = ped.hasNoCollide();
                        MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasRoundRobin,"_hasrobin");
                        MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasCollide,"_hascollide");
                        int getNumAugmentsSpeed = (hasSpeed)?(ped.getSpeed()):(0);
                        int getNumAugmentsCapacity = (hasCapacity)?(ped.getCapacity()):(0);
                        int getNumAugmentsStorage = (hasStorage)?(ped.getStorage()):(0);
                        int getNumAugmentsRange = (hasRange)?(ped.getRange()):(0);

                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getNumAugmentsSpeed,"_intaugmentspeed");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getNumAugmentsCapacity,"_intaugmentcapacity");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getNumAugmentsStorage,"_intaugmentstorage");
                        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,getNumAugmentsRange,"_intaugmentrange");
                    }
                }
            }
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }
}
