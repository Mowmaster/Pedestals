package com.mowmaster.pedestals.item;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemDevTool extends Item {

    public ItemDevTool() {
        super(new Properties().maxStackSize(1).containerItem(DEV).group(PEDESTALS_TAB));
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
    public ActionResult<ItemStack> onItemRightClick(World p_77659_1_, PlayerEntity p_77659_2_, Hand p_77659_3_) {

        if(!p_77659_1_.isRemote)
        {
            RayTraceResult result = p_77659_2_.pick(5,0,false);
            if(result != null)
            {
                //Assuming it it hits a block it wont work???
                if(result.getType() == RayTraceResult.Type.MISS)
                {
                    if(!p_77659_2_.getHeldItemOffhand().isEmpty())
                    {
                        //int getAmp = PotionUtils.getEffectsFromStack(p_77659_2_.getHeldItemOffhand()).get(0).getAmplifier();
                        //TranslationTextComponent name = new TranslationTextComponent(""+getAmp+"");
                        if(p_77659_2_.getHeldItemOffhand().hasTag())
                        {
                            TranslationTextComponent name = new TranslationTextComponent(""+p_77659_2_.getHeldItemOffhand().getTag().toString()+"");
                            name.mergeStyle(TextFormatting.GOLD);
                            p_77659_2_.sendMessage(name,p_77659_2_.getUniqueID());
                            return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                        }
                        return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
                    }
                }
                else if(result.getType() == RayTraceResult.Type.BLOCK)
                {
                    ItemUseContext context = new ItemUseContext(p_77659_2_,p_77659_3_,((BlockRayTraceResult) result));
                    BlockRayTraceResult res = new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), false);
                    BlockPos hit = res.getPos();

                    if(p_77659_1_.getTileEntity(hit) instanceof PedestalTileEntity)
                    {
                        PedestalTileEntity pedestal = (PedestalTileEntity)p_77659_1_.getTileEntity(hit);
                        if(p_77659_2_.isCrouching())
                        {
                            if(pedestal.getCoinOnPedestal().hasTag())
                            {
                                TranslationTextComponent name = new TranslationTextComponent(""+pedestal.getCoinOnPedestal().getTag().toString()+"");
                                name.mergeStyle(TextFormatting.GOLD);
                                p_77659_2_.sendMessage(name,p_77659_2_.getUniqueID());
                                return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                            }
                        }
                        else
                        {
                            TranslationTextComponent name = new TranslationTextComponent(""+pedestal.getTileData().toString()+"");
                            name.mergeStyle(TextFormatting.WHITE);
                            p_77659_2_.sendMessage(name,p_77659_2_.getUniqueID());
                            return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                        }

                    }

                    return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
                }
            }
        }

        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
    }

    public static final Item DEV = new ItemDevTool().setRegistryName(new ResourceLocation(MODID, "devtool"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(DEV);
    }




}
