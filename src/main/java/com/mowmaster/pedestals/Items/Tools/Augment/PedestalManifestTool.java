package com.mowmaster.pedestals.Items.Tools.Augment;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Items.Tools.IPedestalTool;
import com.mowmaster.pedestals.PedestalUtils.References;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        boolean hasManifest = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),"_hasmanifest");
        return (hasManifest)?(1):(0);
    }

    public void clearManifest(ItemStack stackInHand, Player player)
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

        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,getTagOnItem,"_hasmanifest");
        MowLibMessageUtils.messagePopup(player,ChatFormatting.GRAY,MODID + ".manifest.clear");
    }



    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level level = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInHand = player.getItemInHand(hand);

        HitResult result = player.pick(5,0,false);
        BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
        if(!level.isClientSide())
        {
            if(result.getType().equals(HitResult.Type.MISS))
            {
                clearManifest(stackInHand, player);
            }
            else if(result.getType().equals(HitResult.Type.BLOCK))
            {
                BlockState getBlockState = level.getBlockState(pos);
                if(getBlockState.getBlock() instanceof BasePedestalBlock)
                {
                    BlockEntity tile = level.getBlockEntity(pos);
                    if(tile instanceof BasePedestalBlockEntity ped)
                    {
                        CompoundTag getTagOnItem = stackInHand.getOrCreateTag();

                        //Create Manifest of Augments
                        if(player.isShiftKeyDown())
                        {
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

                            boolean hasRoundRobin = ped.hasRRobin();
                            boolean hasCollide = ped.hasNoCollide();
                            boolean hasTransferToggle = ped.hasTransferToggleAugment();
                            MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasRoundRobin,"_hasrobin");
                            MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasCollide,"_hascollide");
                            MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,hasTransferToggle,"_hastransfertoggle");


                            MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,ped.numAugmentsSpeed(),"_intaugmentspeed");
                            MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,ped.numAugmentsCapacity(),"_intaugmentcapacity");
                            MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,ped.numAugmentsStorage(),"_intaugmentstorage");
                            MowLibCompoundTagUtils.writeIntegerToNBT(MODID,getTagOnItem,ped.numAugmentsRange(),"_intaugmentrange");

                            MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".manifest.create");
                            MowLibPacketHandler.sendToNearby(p_41432_,player.getOnPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pos.getX(),pos.getY()+1.0D,pos.getZ(),0,0,200));
                        }

                        MowLibCompoundTagUtils.writeBooleanToNBT(MODID,getTagOnItem,true,"_hasmanifest");


                        // " "
                        MutableComponent separator = Component.translatable(MODID + ".upgrade_modification.separatortwo");
                        MutableComponent baseMessage = Component.translatable(MODID + ".manifest.description.augments.num");
                        baseMessage.withStyle(ChatFormatting.WHITE);
                        MutableComponent speedAugments = Component.literal(""+ped.numAugmentsSpeed()+"");
                        speedAugments.withStyle(ChatFormatting.AQUA);
                        baseMessage.append(speedAugments);
                        MutableComponent capacityAugments = Component.literal(""+ped.numAugmentsCapacity()+"");
                        capacityAugments.withStyle(ChatFormatting.GREEN);
                        baseMessage.append(separator);
                        baseMessage.append(capacityAugments);
                        MutableComponent storageAugments = Component.literal(""+ped.numAugmentsStorage()+"");
                        storageAugments.withStyle(ChatFormatting.GRAY);
                        baseMessage.append(separator);
                        baseMessage.append(storageAugments);
                        MutableComponent rangeAugments = Component.literal(""+ped.numAugmentsRange()+"");
                        rangeAugments.withStyle(ChatFormatting.GOLD);
                        baseMessage.append(separator);
                        baseMessage.append(rangeAugments);

                        player.displayClientMessage(baseMessage, false);
                    }
                }
                else
                {
                    clearManifest(stackInHand, player);
                }
            }
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }


    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        CompoundTag getTagOnItem = p_41421_.getOrCreateTag();
        boolean hasManifest = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hasmanifest");

        if(hasManifest)
        {
            MutableComponent separator = Component.translatable(MODID + ".upgrade_modification.separatortwo");
            MutableComponent separator2 = Component.translatable(MODID + ".upgrade_tooltip_separator_slash");


            boolean hasRender = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hasrender");
            boolean hasRedstone = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hasredstone");
            boolean hasSignal = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hassignal");
            boolean hasRobin = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hasrobin");
            boolean hasCollide = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hascollide");
            boolean hasTransferToggle = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hastransfertoggle");

            boolean hasSpeed = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hasspeed");
            boolean hasCapacity = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hascapacity");
            boolean hasStorage = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hasstorage");
            boolean hasRange = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,getTagOnItem,"_hasrange");

            if(!Screen.hasShiftDown() && !Screen.hasAltDown())
            {
                if(hasRender)
                {
                    MutableComponent hasAugmentRender = Component.translatable(MODID + ".manifest.description.augments.has_render");
                    hasAugmentRender.withStyle(ChatFormatting.LIGHT_PURPLE);
                    p_41423_.add(hasAugmentRender);
                }
                if(hasRedstone)
                {
                    MutableComponent hasInsertedRedstone = Component.translatable(MODID + ".manifest.description.augments.has_redstone");
                    hasInsertedRedstone.withStyle(ChatFormatting.RED);
                    p_41423_.add(hasInsertedRedstone);
                }
                if(hasSignal)
                {
                    MutableComponent hasRedstoneSignal = Component.translatable(MODID + ".manifest.description.augments.has_signal");
                    hasRedstoneSignal.withStyle(ChatFormatting.DARK_RED);
                    p_41423_.add(hasRedstoneSignal);
                }
                if(hasRobin)
                {
                    MutableComponent hasAugmentRobin = Component.translatable(MODID + ".manifest.description.augments.has_robin");
                    hasAugmentRobin.withStyle(ChatFormatting.BLUE);
                    p_41423_.add(hasAugmentRobin);
                }
                if(hasCollide)
                {
                    MutableComponent hasAugmentCollide = Component.translatable(MODID + ".manifest.description.augments.has_collide");
                    hasAugmentCollide.withStyle(ChatFormatting.DARK_PURPLE);
                    p_41423_.add(hasAugmentCollide);
                }
                if(hasTransferToggle)
                {
                    MutableComponent hasTransferToggleComp = Component.translatable(MODID + ".manifest.description.augments.has_transfertoggle");
                    hasTransferToggleComp.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(hasTransferToggleComp);
                }
                if(hasTransferToggle)
                {
                    MutableComponent hasTransferToggleComp = Component.translatable(MODID + ".manifest.description.augments.has_transfertoggle");
                    hasTransferToggleComp.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(hasTransferToggleComp);
                }
            }




            if(!Screen.hasAltDown())
            {
                if (!Screen.hasShiftDown()) {
                    MutableComponent augmentsNum = Component.translatable(MODID + ".manifest.description.augments.num");

                    int speedNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intaugmentspeed");
                    MutableComponent augmentsSpeedNum = Component.literal(""+ ((speedNum>0)?(speedNum):(0)) +"");
                    augmentsSpeedNum.withStyle(ChatFormatting.AQUA);

                    int capacityNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intaugmentcapacity");
                    MutableComponent augmentsCapacityNum = Component.literal(""+ ((capacityNum>0)?(capacityNum):(0)) +"");
                    augmentsCapacityNum.withStyle(ChatFormatting.GREEN);

                    int storageNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intaugmentstorage");
                    MutableComponent augmentsStorageNum = Component.literal(""+ ((storageNum>0)?(storageNum):(0)) +"");
                    augmentsStorageNum.withStyle(ChatFormatting.GRAY);

                    int rangeNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intaugmentrange");
                    MutableComponent augmentsRangeNum = Component.literal(""+ ((rangeNum>0)?(rangeNum):(0)) +"");
                    augmentsRangeNum.withStyle(ChatFormatting.GOLD);

                    augmentsNum.append(augmentsSpeedNum);
                    augmentsNum.append(separator);
                    augmentsNum.append(augmentsCapacityNum);
                    augmentsNum.append(separator);
                    augmentsNum.append(augmentsStorageNum);
                    augmentsNum.append(separator);
                    augmentsNum.append(augmentsRangeNum);

                    p_41423_.add(augmentsNum);

                    MutableComponent base = Component.translatable(MODID + ".manifest.description.shift");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                }
                else
                {
                    if(hasSpeed || hasCapacity || hasStorage || hasRange)
                    {
                        MutableComponent numberOf = Component.translatable(MODID + ".manifest.description.augments.numberof");
                        numberOf.withStyle(ChatFormatting.GOLD);
                        p_41423_.add(numberOf);
                    }
                    if(hasSpeed)
                    {
                        MutableComponent hasAugmentSpeed = Component.translatable(MODID + ".manifest.description.augments.augment_speed");
                        hasAugmentSpeed.withStyle(ChatFormatting.AQUA);

                        int speedNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intaugmentspeed");
                        MutableComponent augmentsSpeedNum = Component.literal(""+ ((speedNum>0)?(speedNum):(0)) +"");
                        augmentsSpeedNum.withStyle(ChatFormatting.WHITE);

                        hasAugmentSpeed.append(augmentsSpeedNum);
                        p_41423_.add(hasAugmentSpeed);
                    }
                    if(hasCapacity)
                    {
                        MutableComponent hasAugmentCapacity = Component.translatable(MODID + ".manifest.description.augments.augment_capacity");
                        hasAugmentCapacity.withStyle(ChatFormatting.GREEN);

                        int capacityNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intaugmentcapacity");
                        MutableComponent augmentsCapacityNum = Component.literal(""+ ((capacityNum>0)?(capacityNum):(0)) +"");
                        augmentsCapacityNum.withStyle(ChatFormatting.WHITE);

                        hasAugmentCapacity.append(augmentsCapacityNum);
                        p_41423_.add(hasAugmentCapacity);
                    }
                    if(hasStorage)
                    {
                        MutableComponent hasAugmentStorage = Component.translatable(MODID + ".manifest.description.augments.augment_storage");
                        hasAugmentStorage.withStyle(ChatFormatting.GRAY);

                        int storageNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intaugmentstorage");
                        MutableComponent augmentsStorageNum = Component.literal(""+ ((storageNum>0)?(storageNum):(0)) +"");
                        augmentsStorageNum.withStyle(ChatFormatting.WHITE);

                        hasAugmentStorage.append(augmentsStorageNum);
                        p_41423_.add(hasAugmentStorage);
                    }
                    if(hasRange)
                    {
                        MutableComponent hasAugmentRange = Component.translatable(MODID + ".manifest.description.augments.augment_range");
                        hasAugmentRange.withStyle(ChatFormatting.YELLOW);

                        int rangeNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intaugmentrange");
                        MutableComponent augmentsRangeNum = Component.literal(""+ ((rangeNum>0)?(rangeNum):(0)) +"");
                        augmentsRangeNum.withStyle(ChatFormatting.WHITE);

                        hasAugmentRange.append(augmentsRangeNum);
                        p_41423_.add(hasAugmentRange);
                    }
                }
            }

            if(!Screen.hasShiftDown())
            {
                if (!Screen.hasAltDown()) {

                    MutableComponent base = Component.translatable(MODID + ".manifest.description.alt");
                    base.withStyle(ChatFormatting.GRAY);
                    p_41423_.add(base);
                }
                else
                {
                    if(hasRedstone || hasSignal)
                    {
                        MutableComponent augmentsNumRedstone = Component.translatable(MODID + ".manifest.description.augments.redstoneneeded_alt");
                        separator2.withStyle(ChatFormatting.WHITE);

                        //input signal
                        int signalStrength = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalsignal");
                        MutableComponent inputSignalStrength = Component.literal(""+ ((signalStrength>0)?(signalStrength):(0)) +"");
                        inputSignalStrength.withStyle(ChatFormatting.RED);

                        //Redstone Needed
                        int redstoneNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalredstone");
                        MutableComponent inputRedstoneRequired = Component.literal(""+ ((redstoneNeeded>0)?(redstoneNeeded):(0)) +"");
                        inputRedstoneRequired.withStyle(ChatFormatting.DARK_RED);

                        augmentsNumRedstone.append(inputSignalStrength);
                        augmentsNumRedstone.append(separator2);
                        augmentsNumRedstone.append(inputRedstoneRequired);
                        p_41423_.add(augmentsNumRedstone);
                    }

                    //Pedestal Stats
                    MutableComponent augmentsNumAlt = Component.translatable(MODID + ".manifest.description.augments.num_alt");
                    p_41423_.add(augmentsNumAlt);

                    MutableComponent augmentsSpeedAlt = Component.translatable(MODID + ".manifest.description.augments.num_speed_alt");
                    augmentsSpeedAlt.withStyle(ChatFormatting.AQUA);
                    //ticks reduced
                    int speedNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalspeed");
                    MutableComponent augmentsSpeedNum = Component.literal(""+ ((speedNum>0)?(speedNum):(0)) +"");
                    augmentsSpeedNum.withStyle(ChatFormatting.WHITE);
                    augmentsSpeedAlt.append(augmentsSpeedNum);
                    p_41423_.add(augmentsSpeedAlt);

                    MutableComponent augmentsRangeAlt = Component.translatable(MODID + ".manifest.description.augments.num_range_alt");
                    augmentsRangeAlt.withStyle(ChatFormatting.GOLD);
                    int rangeNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestaltotalrange");
                    MutableComponent augmentsRangeNum = Component.literal(""+ ((rangeNum>0)?(rangeNum):(0)) +"");
                    augmentsRangeNum.withStyle(ChatFormatting.WHITE);
                    augmentsRangeAlt.append(augmentsRangeNum);
                    p_41423_.add(augmentsRangeAlt);

                    MutableComponent augmentsNumItemAlt = Component.translatable(MODID + ".manifest.description.augments.num_capstor_alt_item");
                    augmentsNumItemAlt.withStyle(ChatFormatting.YELLOW);
                    int itemRate = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalitemtransfer");
                    int itemStacks = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalstorageitem");
                    MutableComponent itemsR = Component.literal(""+ itemRate +" ");
                    MutableComponent itemsC = Component.literal(" "+ itemStacks +"");
                    itemsR.append(separator2);
                    itemsR.append(itemsC);
                    itemsR.withStyle(ChatFormatting.WHITE);

                    augmentsNumItemAlt.append(itemsR);
                    p_41423_.add(augmentsNumItemAlt);

                    MutableComponent augmentsNumFluidAlt = Component.translatable(MODID + ".manifest.description.augments.num_capstor_alt_fluid");
                    augmentsNumFluidAlt.withStyle(ChatFormatting.BLUE);
                    int fluidRate = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalfluidtransfer");
                    int fluidCapacity = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalstoragefluid");
                    MutableComponent fluidR = Component.literal(""+ fluidRate +" ");
                    MutableComponent fluidC = Component.literal(" "+ fluidCapacity +"");
                    fluidR.append(separator2);
                    fluidR.append(fluidC);
                    fluidR.withStyle(ChatFormatting.WHITE);

                    augmentsNumFluidAlt.append(fluidR);
                    p_41423_.add(augmentsNumFluidAlt);

                    MutableComponent augmentsNumEnergyAlt = Component.translatable(MODID + ".manifest.description.augments.num_capstor_alt_energy");
                    augmentsNumEnergyAlt.withStyle(ChatFormatting.RED);
                    int energyRate = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalenergytransfer");
                    int energyCapacity = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalstorageenergy");
                    MutableComponent energyR = Component.literal(""+ energyRate +" ");
                    MutableComponent energyC = Component.literal(" "+ energyCapacity +"");
                    energyR.append(separator2);
                    energyR.append(energyC);
                    energyR.withStyle(ChatFormatting.WHITE);

                    augmentsNumEnergyAlt.append(energyR);
                    p_41423_.add(augmentsNumEnergyAlt);

                    MutableComponent augmentsNumExpAlt = Component.translatable(MODID + ".manifest.description.augments.num_capstor_alt_xp");
                    augmentsNumExpAlt.withStyle(ChatFormatting.GREEN);
                    int xpRate = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalexptransfer");
                    int xpCapacity = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalstorageexp");
                    MutableComponent expR = Component.literal(""+ xpRate +" ");
                    MutableComponent expC = Component.literal(" "+ xpCapacity +"");
                    expR.append(separator2);
                    expR.append(expC);
                    expR.withStyle(ChatFormatting.WHITE);

                    augmentsNumExpAlt.append(expR);
                    p_41423_.add(augmentsNumExpAlt);

                    if(References.isDustLoaded())
                    {
                        MutableComponent augmentsNumDustAlt = Component.translatable(MODID + ".manifest.description.augments.num_capstor_alt_dust");
                        augmentsNumDustAlt.withStyle(ChatFormatting.LIGHT_PURPLE);
                        int dustRate = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestaldusttransfer");
                        int dustCapacity = MowLibCompoundTagUtils.readIntegerFromNBT(MODID,getTagOnItem,"_intpedestalstoragedust");
                        MutableComponent dustR = Component.literal(""+ dustRate +" ");
                        MutableComponent dustC = Component.literal(" "+ dustCapacity +"");
                        dustR.append(separator2);
                        dustR.append(dustC);
                        dustR.withStyle(ChatFormatting.WHITE);

                        augmentsNumDustAlt.append(dustR);
                        p_41423_.add(augmentsNumDustAlt);
                    }
                }
            }
        }
    }
}
