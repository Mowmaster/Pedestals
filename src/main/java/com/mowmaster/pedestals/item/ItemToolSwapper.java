package com.mowmaster.pedestals.item;

import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.item.pedestalFilters.ItemFilterBase;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemToolSwapper extends Item {

    public ItemToolSwapper() {
        super(new Properties().maxStackSize(1).containerItem(QUARRYTOOL).group(PEDESTALS_TAB));
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(this.getItem());
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World worldIn = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getPos();
        ItemStack stackInMainHand = player.getHeldItemMainhand();
        ItemStack stackInOffHand = player.getHeldItemOffhand();

        TranslationTextComponent filterRemove = new TranslationTextComponent(Reference.MODID + ".filters.insert_remove");
        TranslationTextComponent filterSwitch = new TranslationTextComponent(Reference.MODID + ".filters.insert_switch");
        TranslationTextComponent filterInsert = new TranslationTextComponent(Reference.MODID + ".filters.insert_insert");

        if(!worldIn.isRemote)
        {
            BlockState getBlockState = worldIn.getBlockState(pos);
            if(getBlockState.getBlock() instanceof PedestalBlock) {
                TileEntity tile = worldIn.getTileEntity(pos);
                if(tile instanceof PedestalTileEntity)
                {
                    PedestalTileEntity pedestal = ((PedestalTileEntity)worldIn.getTileEntity(pos));
                    if(pedestal.hasTool())
                    {
                        chatDetails(player,pedestal);
                        return ActionResultType.SUCCESS;
                    }

                    return ActionResultType.FAIL;
                }
            }
        }

        return super.onItemUse(context);
    }

    //Thanks to TheBoo on the e6 Discord for this suggestion
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
                if(p_77659_1_.isRemote)
                {
                    if(p_77659_2_.isCrouching())
                    {
                        ItemStack heldItem = p_77659_2_.getHeldItem(p_77659_3_);
                        if(heldItem.getItem().equals(ItemToolSwapper.QUARRYTOOL) && !heldItem.isEnchanted())
                        {
                            p_77659_2_.setHeldItem(p_77659_3_,new ItemStack(ItemFilterSwapper.FILTERTOOL));
                            TranslationTextComponent range = new TranslationTextComponent(MODID + ".tool_change");
                            range.mergeStyle(TextFormatting.GREEN);
                            p_77659_2_.sendStatusMessage(range,true);
                            return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                        }
                        return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
                    }
                }
            }
        }

        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
    }

    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        if(pedestal.hasTool())
        {
            ItemStack itemTool = pedestal.getToolOnPedestal();
            TranslationTextComponent tool = new TranslationTextComponent(getTranslationKey() + ".tool_stored");
            tool.append(itemTool.getDisplayName());
            tool.mergeStyle(TextFormatting.WHITE);
            player.sendMessage(tool,Util.DUMMY_UUID);

            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(pedestal.getToolOnPedestal());
            if(map.size() > 0)
            {
                TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".tool_enchants");
                enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
                player.sendMessage(enchant,Util.DUMMY_UUID);

                for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    Integer integer = entry.getValue();
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants,Util.DUMMY_UUID);
                }
            }
        }
    }


    public static final Item QUARRYTOOL = new ItemToolSwapper().setRegistryName(new ResourceLocation(MODID, "toolswapper"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(QUARRYTOOL);
    }




}
