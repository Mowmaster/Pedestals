package com.mowmaster.pedestals.item.pedestalFilters;

import com.mowmaster.pedestals.api.filter.IFilterBase;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;


public class ItemFilterDurability extends ItemFilterBase
{
    public ItemFilterDurability(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    public int getPercentDamaged(ItemStack itemIn)
    {
        if(itemIn.isDamageable())
        {
            int maxDamage = itemIn.getMaxDamage();
            int damage = itemIn.getDamage();
            int durabilityCurrent = maxDamage-damage;
            int percentDurability = Math.floorDiv((durabilityCurrent*100),maxDamage);
            return percentDurability;
        }
        return 100;
    }

    public int getDurabilityTarget(ItemStack filter)
    {
        int returner = 0;
        List<ItemStack> filterQueue = readFilterQueueFromNBT(filter);
        if(filterQueue.size()>0)
        {
            for(int i=0;i<filterQueue.size();i++)
            {
                ItemStack stackGet = filterQueue.get(i);
                returner += getPercentDamaged(stackGet);
                if(returner>100)break;
            }
        }

        return (returner>100)?(100):(returner);
    }

    @Override
    public boolean canAcceptItem(PedestalTileEntity pedestal, ItemStack itemStackIn)
    {
        boolean filterBool=getFilterType(pedestal.getFilterInPedestal());
        int durabilityTarget = getDurabilityTarget(pedestal.getFilterInPedestal());

        int percentDurabilityCurrent = getPercentDamaged(itemStackIn);
        int percentToBeat = durabilityTarget;

        if(filterBool)
        {
            return (itemStackIn.isDamageable())?((percentDurabilityCurrent<=percentToBeat)?(true):(false)):(false);
        }
        else
        {
            return (itemStackIn.isDamageable())?((percentDurabilityCurrent>=percentToBeat)?(true):(false)):(false);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World p_77659_1_, PlayerEntity p_77659_2_, Hand p_77659_3_) {
        //Thankyou past self: https://github.com/Mowmaster/Ensorcelled/blob/main/src/main/java/com/mowmaster/ensorcelled/enchantments/handlers/HandlerAOEMiner.java#L53
        //RayTraceResult result = player.pick(player.getLookVec().length(),0,false); results in MISS type returns
        RayTraceResult result = p_77659_2_.pick(5,0,false);
        if(result != null)
        {
            //Assuming it it hits a block it wont work???
            if(result.getType() == RayTraceResult.Type.MISS)
            {
                if(p_77659_2_.isCrouching())
                {
                    ItemStack itemInHand = p_77659_2_.getHeldItem(p_77659_3_);
                    if(itemInHand.getItem() instanceof IFilterBase)
                    {
                        boolean getCurrentType = getFilterType(itemInHand);
                        setFilterType(itemInHand,!getCurrentType);
                        TranslationTextComponent changed = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterchange");
                        changed.mergeStyle(TextFormatting.GREEN);
                        TranslationTextComponent white = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterabove");
                        TranslationTextComponent black = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterbelow");
                        changed.append((!getCurrentType)?(black):(white));
                        p_77659_2_.sendStatusMessage(changed,true);
                        return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                    }
                    return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
                }
            }
            //Assuming it it hits a block it wont work???
            if(result.getType() == RayTraceResult.Type.BLOCK)
            {
                if(p_77659_2_.isCrouching())
                {
                    ItemStack itemInHand = p_77659_2_.getHeldItem(p_77659_3_);
                    if(itemInHand.getItem() instanceof IFilterBase)
                    {
                        ItemUseContext context = new ItemUseContext(p_77659_2_,p_77659_3_,((BlockRayTraceResult) result));
                        BlockRayTraceResult res = new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), false);
                        BlockPos posBlock = res.getPos();

                        List<ItemStack> buildQueue = buildFilterQueue(p_77659_1_,posBlock);

                        if(buildQueue.size() > 0)
                        {
                            writeFilterQueueToNBT(itemInHand,buildQueue);
                            return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                        }
                        return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
                    }
                }
            }
        }

        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        TranslationTextComponent filterList = new TranslationTextComponent(filterStack.getDisplayName().getString());
        filterList.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(filterList, Util.DUMMY_UUID);

        boolean filterType = getFilterType(filterStack);
        TranslationTextComponent filterList2 = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filtertype");
        TranslationTextComponent above = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterabove");
        TranslationTextComponent below = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterbelow");
        filterList2.append((filterType)?(below):(above));
        filterList2.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(filterList2, Util.DUMMY_UUID);

        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack);
        if(filterQueue.size()>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterlist");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant, Util.DUMMY_UUID);

            TranslationTextComponent enchants = new TranslationTextComponent(""+getDurabilityTarget(pedestal.getFilterInPedestal())+"");
            enchants.mergeStyle(TextFormatting.GRAY);
            player.sendMessage(enchants, Util.DUMMY_UUID);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        boolean filterType = getFilterType(stack);
        TranslationTextComponent filterList = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filtertype");
        TranslationTextComponent above = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterabove");
        TranslationTextComponent below = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterbelow");
        filterList.append((filterType)?(below):(above));
        filterList.mergeStyle(TextFormatting.GOLD);
        tooltip.add(filterList);

        List<ItemStack> filterQueue = readFilterQueueFromNBT(stack);
        if(filterQueue.size()>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterlist_durability");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            tooltip.add(enchant);

            TranslationTextComponent enchants = new TranslationTextComponent(""+getDurabilityTarget(stack)+"");
            enchants.mergeStyle(TextFormatting.GRAY);
            tooltip.add(enchants);
        }
    }

    public static void handleItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return getColorFromNBT(itemstack);} else {return -1;}},DURABILITYFILTER);
    }

    public static final Item DURABILITYFILTER = new ItemFilterDurability(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "filter/filterdurability"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(DURABILITYFILTER);
    }
}
