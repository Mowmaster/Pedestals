package com.mowmaster.pedestals.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemTagTool extends Item {

    public ItemTagTool() {
        super(new Properties().maxStackSize(1).containerItem(TAG).group(PEDESTALS_TAB));
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(this.getItem());
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
                    String tags = p_77659_2_.getHeldItemOffhand().getItem().getTags().toString();
                    if(!p_77659_2_.getHeldItemOffhand().isEmpty())
                    {
                        TranslationTextComponent output = new TranslationTextComponent("Tags: ");
                        output.appendString(tags);
                        output.mergeStyle(TextFormatting.WHITE);
                        p_77659_2_.sendMessage(output,p_77659_2_.getUniqueID());
                    }

                    if(p_77659_2_.isCrouching())
                    {
                        ItemStack heldItem = p_77659_2_.getHeldItem(p_77659_3_);
                        if(heldItem.getItem().equals(ItemTagTool.TAG) && !heldItem.isEnchanted())
                        {
                            p_77659_2_.setHeldItem(p_77659_3_,new ItemStack(ItemUpgradeTool.UPGRADE));
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

    public static final Item TAG = new ItemTagTool().setRegistryName(new ResourceLocation(MODID, "tagtool"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(TAG);
    }




}
