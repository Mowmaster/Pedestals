package com.mowmaster.pedestals.item;

import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.item.pedestalFilters.ItemFilterBase;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemFilterSwapper extends Item {

    public ItemFilterSwapper() {
        super(new Properties().maxStackSize(1).containerItem(FILTERTOOL).group(PEDESTALS_TAB));
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
                    if(pedestal.hasFilter())
                    {
                        ItemFilterBase getFilter = (ItemFilterBase)pedestal.getFilterInPedestal().getItem();
                        getFilter.chatDetails(player,pedestal);
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
                if(!p_77659_1_.isRemote)
                {
                    if(p_77659_2_.isCrouching())
                    {
                        ItemStack heldItem = p_77659_2_.getHeldItem(p_77659_3_);
                        if(heldItem.getItem().equals(ItemFilterSwapper.FILTERTOOL) && !heldItem.isEnchanted())
                        {
                            p_77659_2_.setHeldItem(p_77659_3_,new ItemStack(ItemTagTool.TAG));
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


    public static final Item FILTERTOOL = new ItemFilterSwapper().setRegistryName(new ResourceLocation(MODID, "filterswapper"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FILTERTOOL);
    }




}
