package com.mowmaster.pedestals.item.augments;

import com.mowmaster.pedestals.api.filter.IFilterBase;
import com.mowmaster.pedestals.item.ItemFilterSwapper;
import com.mowmaster.pedestals.item.pedestalFilters.ItemFilterBase;
import com.mowmaster.pedestals.item.pedestalFilters.ItemFilterItem;
import com.mowmaster.pedestals.references.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemPedestalRenderAugment extends ItemPedestalUpgrades {

    public int pedestalRenderType = 0;

    public ItemPedestalRenderAugment() {
        super();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World p_77659_1_, PlayerEntity p_77659_2_, Hand p_77659_3_) {
        RayTraceResult result = p_77659_2_.pick(5,0,false);
        if(result != null)
        {
            if(result.getType() == RayTraceResult.Type.MISS)
            {
                if(p_77659_2_.isCrouching())
                {
                    ItemStack itemInHand = p_77659_2_.getHeldItem(p_77659_3_);
                    //Should prevent it from its nbt changing???
                    if(itemInHand.getItem() instanceof ItemPedestalRenderAugment)
                    {
                        int next = nextAugmentType(itemInHand);
                        setAugmentType(itemInHand,next);
                        TranslationTextComponent changed = new TranslationTextComponent(Reference.MODID + ".augments.chat_augmentchange");
                        switch(next)
                        {
                            case 0: changed = new TranslationTextComponent(Reference.MODID + ".augments.chat_augment_both");
                                changed.mergeStyle(TextFormatting.RED);
                                break;
                            case 1: changed = new TranslationTextComponent(Reference.MODID + ".augments.chat_augment_upgrade");
                                changed.mergeStyle(TextFormatting.GREEN);
                                break;
                            case 2: changed = new TranslationTextComponent(Reference.MODID + ".augments.chat_augment_item");
                                changed.mergeStyle(TextFormatting.BLUE);
                                break;
                            default: changed = new TranslationTextComponent(Reference.MODID + ".augments.chat_augment_both");
                                changed.mergeStyle(TextFormatting.RED);
                                break;
                        }

                        p_77659_2_.sendStatusMessage(changed,true);
                        return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                    }

                    return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
                }
            }
        }

        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
    }



    public int getAugmentType(ItemStack augmentItem)
    {
        getAugmentTypeFromNBT(augmentItem);
        return pedestalRenderType;
    }

    public int nextAugmentType(ItemStack augmentItem)
    {
        // 0 = BOTH
        // 1 = UPGRADE
        // 2 = ITEM

        int next = getAugmentType(augmentItem)+1;
        int nextFinal = (next>2)?(0):(next);
        return nextFinal;
    }

    public void setAugmentType(ItemStack augmentItem, int augmentSet)
    {
        pedestalRenderType = augmentSet;
        writeAugmentTypeToNBT(augmentItem);
    }

    public static int getColorFromNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            if(stack.getTag().contains("augment_type"))
            {
                CompoundNBT getCompound = stack.getTag();
                int renderType = getCompound.getInt("augment_type");
                if(renderType == 0) return 255;
                if(renderType == 1) return 65280;
                if(renderType == 2) return 16711680;
            }
        }

        return 255;
    }



    private void writeAugmentTypeToNBT(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }
        compound.putInt("augment_type",pedestalRenderType);
        stack.setTag(compound);
    }

    private int getAugmentTypeFromNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            this.pedestalRenderType = getCompound.getInt("augment_type");
        }
        return pedestalRenderType;
    }

    public static void handleItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return getColorFromNBT(itemstack);} else {return -1;}},RENDERAUGMENT);
    }

    public static final Item RENDERAUGMENT = new ItemPedestalRenderAugment().setRegistryName(new ResourceLocation(MODID, "upgraderenderaugment"));

    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RENDERAUGMENT);
    }

}
