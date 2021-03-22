package com.mowmaster.pedestals.item.pedestalFilters;

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


public class ItemFilterEnchantedCount extends ItemFilterBase
{
    public ItemFilterEnchantedCount(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean getFilterType()
    {
        //state 0|
        //state 1|false = whitelist
        //state 2|true = blacklist
        return false;
    }

    @Override
    public boolean getFilterType(ItemStack filterItem)
    {
        //false = whitelist
        //true = blacklist
        return getFilterType();
    }

    @Override
    public boolean canAcceptItem(PedestalTileEntity pedestal, ItemStack itemStackIn)
    {
        //This only works one way regardless of white or blacklist
        boolean returner = false;

        ItemStack filter = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter);
        int range = stackCurrent.size();


        int count = 0;
        for(int i=0;i<range;i++)
        {
            ItemStack stackGet = stackCurrent.get(i);
            if(stackGet.isEnchanted() || stackGet.getItem().equals(Items.ENCHANTED_BOOK))
            {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stackGet);
                count +=map.size();
            }
        }

        if(itemStackIn.isEnchanted() || itemStackIn.getItem().equals(Items.ENCHANTED_BOOK))
        {
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStackIn);
            if(map.size() == count)
            {
                returner = true;
            }
        }

        return returner;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World p_77659_1_, PlayerEntity p_77659_2_, Hand p_77659_3_) {
        //Thankyou past self: https://github.com/Mowmaster/Ensorcelled/blob/main/src/main/java/com/mowmaster/ensorcelled/enchantments/handlers/HandlerAOEMiner.java#L53
        //RayTraceResult result = player.pick(player.getLookVec().length(),0,false); results in MISS type returns
        RayTraceResult result = p_77659_2_.pick(5,0,false);
        if(result != null)
        {
            //Assuming it it hits a block it wont work???
            if(result.getType() == RayTraceResult.Type.BLOCK)
            {
                if(p_77659_2_.isCrouching())
                {
                    ItemStack itemInHand = p_77659_2_.getHeldItem(p_77659_3_);
                    if(itemInHand.getItem() instanceof ItemFilterBase)
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

        return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        TranslationTextComponent filterList = new TranslationTextComponent(filterStack.getDisplayName().getString());
        filterList.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(filterList, Util.DUMMY_UUID);

        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack);
        if(filterQueue.size()>0)
        {

            TranslationTextComponent enchant = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterlist_count");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant, Util.DUMMY_UUID);

            int count = 0;
            for(int i=0;i<filterQueue.size();i++)
            {
                ItemStack stackGet = filterQueue.get(i);
                if(stackGet.isEnchanted() || stackGet.getItem().equals(Items.ENCHANTED_BOOK))
                {
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stackGet);
                    count +=map.size();
                }
            }

            TranslationTextComponent enchants = new TranslationTextComponent(""+count+"");
            enchants.mergeStyle(TextFormatting.GRAY);
            player.sendMessage(enchants, Util.DUMMY_UUID);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        List<ItemStack> filterQueue = readFilterQueueFromNBT(stack);
        if(filterQueue.size()>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterlist_count");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            tooltip.add(enchant);

            int count = 0;
            for(int i=0;i<filterQueue.size();i++)
            {
                ItemStack stackGet = filterQueue.get(i);
                if(stackGet.isEnchanted() || stackGet.getItem().equals(Items.ENCHANTED_BOOK))
                {
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stackGet);
                    count +=map.size();
                }
            }
            TranslationTextComponent enchants = new TranslationTextComponent(""+count+"");
            enchants.mergeStyle(TextFormatting.GRAY);
            tooltip.add(enchants);
        }
    }

    public static void handleItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 8388736;} else {return -1;}},ENCHANTEDCOUNTFILTER);
    }

    public static final Item ENCHANTEDCOUNTFILTER = new ItemFilterEnchantedCount(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "filter/filterenchantedcount"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENCHANTEDCOUNTFILTER);
    }
}
