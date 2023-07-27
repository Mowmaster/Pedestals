package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.ISelectableArea;
import com.mowmaster.pedestals.Items.ISelectablePoints;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.PedestalUtils.MoveToMowLibUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class WorkCardBase extends Item implements IPedestalWorkCard {
    public WorkCardBase(Properties p_41383_) {
        super(p_41383_);
    }

    public int getWorkCardType() {
        return -1;
    }

    //ToDo: Add to mowlib and remove from here
    public static void saveStringToNBT(ItemStack upgrade, String nbtTag, String string) {
        CompoundTag compound = upgrade.getOrCreateTag();
        compound.putString(MODID + nbtTag, string);
    }

    //ToDo: Add to mowlib and remove from here
    //returns true for an add, false for a remove.
    public static boolean addBlockPosToList(ItemStack upgrade, BlockPos posOfBlock) {
        List<BlockPos> currentList = readBlockPosListFromNBT(upgrade);
        if(currentList.contains(posOfBlock)) {
            currentList.remove(posOfBlock);
            saveBlockPosListToNBT(upgrade, currentList);
            return false;
        } else {
            currentList.add(posOfBlock);
            saveBlockPosListToNBT(upgrade, currentList);
            return true;
        }
    }

    //ToDo: Add to mowlib and remove from here
    public static void saveBlockPosListToNBT(ItemStack upgrade, List<BlockPos> posListToSave) {
        CompoundTag compound = upgrade.getOrCreateTag();
        List<Integer> storedX = new ArrayList<>();
        List<Integer> storedY = new ArrayList<>();
        List<Integer> storedZ = new ArrayList<>();
        for (BlockPos blockPos : posListToSave) {
            storedX.add(blockPos.getX());
            storedY.add(blockPos.getY());
            storedZ.add(blockPos.getZ());
        }

        compound.putIntArray(MODID+"_intArrayXPos",storedX);
        compound.putIntArray(MODID+"_intArrayYPos",storedY);
        compound.putIntArray(MODID+"_intArrayZPos",storedZ);
    }

    //ToDo: Add to mowlib and remove from here
    public static List<BlockPos> readBlockPosListFromNBT(ItemStack upgrade) {
        List<BlockPos> posList = new ArrayList<>();
        if(upgrade.hasTag()) {
            String tagX = MODID+"_intArrayXPos";
            String tagY = MODID+"_intArrayYPos";
            String tagZ = MODID+"_intArrayZPos";
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tagX) && upgrade.getTag().contains(tagY) && upgrade.getTag().contains(tagZ)) {
                int[] storedIX = getCompound.getIntArray(tagX);
                int[] storedIY = getCompound.getIntArray(tagY);
                int[] storedIZ = getCompound.getIntArray(tagZ);
                for (int i = 0; i < storedIX.length; i++) {
                    BlockPos gotPos = new BlockPos(storedIX[i], storedIY[i], storedIZ[i]);
                    posList.add(gotPos);
                }
            }
        }
        return posList;
    }

    //ToDo: Add to mowlib and remove from here
    public static void saveBlockPosToNBT(ItemStack upgrade, int num, BlockPos posToSave) {
        CompoundTag compound = upgrade.getOrCreateTag();
        List<Integer> listed = new ArrayList<>();
        listed.add(posToSave.getX());
        listed.add(posToSave.getY());
        listed.add(posToSave.getZ());
        compound.putIntArray(MODID+"_upgrade_blockpos_" + num, listed);
    }

    //ToDo: Add to mowlib and remove from here
    public static BlockPos readBlockPosFromNBT(ItemStack upgrade, int num) {
        if(upgrade.hasTag()) {
            String tag = MODID + "_upgrade_blockpos_" + num;
            CompoundTag compound = upgrade.getTag();
            if (compound.contains(tag)) {
                int[] listed = compound.getIntArray(tag);
                if (listed.length >= 3) return new BlockPos(listed[0], listed[1], listed[2]);
            }
        }
        return BlockPos.ZERO;
    }

    //ToDo: Add to mowlib and remove from here
    public boolean hasOneBlockPos(ItemStack stack) {
        return !readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO) || !readBlockPosFromNBT(stack,2).equals(BlockPos.ZERO);
    }

    public boolean hasTwoPointsSelected(ItemStack stack) {
        return !readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO) && !readBlockPosFromNBT(stack,2).equals(BlockPos.ZERO);
    }

    //ToDo: Add to mowlib and remove from here
    public boolean isSelectionInRange(BasePedestalBlockEntity pedestal, BlockPos pos) {
        int range = PedestalConfig.COMMON.upgrades_baseSelectionRange.get();
        if(pedestal.getCoinOnPedestal().getItem() instanceof ItemUpgradeBase upgrade) {
            range += upgrade.getRangeIncrease(pedestal.getCoinOnPedestal());
        }
        return MoveToMowLibUtils.arePositionsInRange(pos, pedestal.getPos(), range);
    }

    public boolean selectedAreaWithinRange(BasePedestalBlockEntity pedestal) {
        if(isSelectionInRange(pedestal, readBlockPosFromNBT(pedestal.getWorkCardInPedestal(),1)) && isSelectionInRange(pedestal, readBlockPosFromNBT(pedestal.getWorkCardInPedestal(),2))) {
            return true;
        } else {
            return false;
        }
    }

    //ToDo: Add to mowlib and remove from here
    public boolean selectedPointWithinRange(BasePedestalBlockEntity pedestal, BlockPos posPoint) {
        if (isSelectionInRange(pedestal, posPoint)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        saveBlockPosToNBT(stack, 10, context.getClickedPos());
        saveStringToNBT(stack, "_string_last_clicked_direction", context.getClickedFace().toString());
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        HitResult result = player.pick(5, player.getEyeHeight(), false);
        BlockPos atLocation = readBlockPosFromNBT(itemInHand, 10);

        if (hand.equals(InteractionHand.MAIN_HAND) && itemInHand.getItem() instanceof WorkCardBase baseCard) {
            if (itemInHand.getItem() instanceof ISelectablePoints) {
                if(result.getType().equals(HitResult.Type.BLOCK)) {
                    if(baseCard.getWorkCardType() == 3) {
                        if(!(level.getBlockState(atLocation).getBlock() instanceof BasePedestalBlock)) {
                            return InteractionResultHolder.fail(itemInHand);
                        }
                    }
                    boolean added = addBlockPosToList(itemInHand, atLocation);
                    MowLibMessageUtils.messagePopup(player, added ? ChatFormatting.WHITE : ChatFormatting.BLACK, added ? MODID + ".upgrade_blockpos_added" : MODID + ".upgrade_blockpos_removed");
                    MowLibPacketHandler.sendToNearby(level, player.getOnPos(), new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, atLocation.getX(), atLocation.getY() + 1.0D,atLocation.getZ(), 0, added ? 200 : 0, 0));
                } else if(result.getType().equals(HitResult.Type.MISS) && readBlockPosListFromNBT(itemInHand).size() > 0) {
                    itemInHand.setTag(new CompoundTag());
                    MowLibMessageUtils.messagePopup(player, ChatFormatting.WHITE, MODID + ".upgrade_blockpos_clear");
                }
            } else if(itemInHand.getItem() instanceof ISelectableArea) {
                if (result.getType().equals(HitResult.Type.BLOCK)) {
                    if (!hasOneBlockPos(itemInHand)) {
                        saveBlockPosToNBT(itemInHand,1,atLocation);
                        player.setItemInHand(hand,itemInHand);
                        MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_first");
                    } else if (!hasTwoPointsSelected(itemInHand)) {
                        saveBlockPosToNBT(itemInHand, 2, atLocation);
                        player.setItemInHand(hand, itemInHand);
                        MowLibMessageUtils.messagePopup(player, ChatFormatting.WHITE, MODID + ".upgrade_blockpos_second");
                    }
                } else if(result.getType().equals(HitResult.Type.MISS) && hasOneBlockPos(itemInHand)) {
                    itemInHand.setTag(new CompoundTag());
                    MowLibMessageUtils.messagePopup(player, ChatFormatting.WHITE, MODID + ".upgrade_blockpos_clear");
                }
            }
        }

        return InteractionResultHolder.fail(itemInHand);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        if(Screen.hasShiftDown() && Screen.hasAltDown()) {
            //Add a new Line if both are present
            p_41423_.add(Component.literal(""));
        }

        if(p_41421_.getItem() instanceof WorkCardBase && p_41421_.getItem() instanceof ISelectableArea) {
            if(hasOneBlockPos(p_41421_)) {
                if (!Screen.hasShiftDown()) {
                    MutableComponent base = Component.translatable(MODID + ".workcard_description_shift");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".upgrade_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".tooltip_separator");
                    p_41423_.add(separator);

                    MutableComponent posOne = Component.translatable(MODID + ".upgrade_tooltip_blockpos_one");
                    BlockPos blockPosOne = readBlockPosFromNBT(p_41421_,1);
                    MutableComponent posOnePos = Component.literal(blockPosOne.getX() + "x " + blockPosOne.getY() + "y " + blockPosOne.getZ()+ "z");
                    posOnePos.withStyle(ChatFormatting.GRAY);
                    posOne.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                    posOne.append(posOnePos);

                    p_41423_.add(posOne);

                    MutableComponent posTwo = Component.translatable(MODID + ".upgrade_tooltip_blockpos_two");
                    BlockPos blockPosTwo = readBlockPosFromNBT(p_41421_,2);
                    MutableComponent posTwoPos = Component.literal(blockPosTwo.getX() + "x " + blockPosTwo.getY() + "y " + blockPosTwo.getZ()+ "z");
                    posTwoPos.withStyle(ChatFormatting.GRAY);
                    posTwo.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                    posTwo.append(posTwoPos);
                    p_41423_.add(posTwo);
                }
            }
        }

        if(p_41421_.getItem() instanceof WorkCardBase && p_41421_.getItem() instanceof ISelectablePoints && !hasTwoPointsSelected(p_41421_)) {
            List<BlockPos> getList = readBlockPosListFromNBT(p_41421_);
            if(getList.size()>0) {
                if (!Screen.hasShiftDown()) {
                    MutableComponent base = Component.translatable(MODID + ".workcard_description_shift");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".upgrade_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".tooltip_separator");
                    p_41423_.add(separator);

                    for (BlockPos pos : getList) {
                        MutableComponent posOnePos = Component.literal(pos.getX() + "x " + pos.getY() + "y " + pos.getZ()+ "z");
                        posOnePos.withStyle(ChatFormatting.GRAY);
                        p_41423_.add(posOnePos);
                    }
                }
            }
        }
    }
}
