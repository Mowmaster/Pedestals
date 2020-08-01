package com.mowmaster.pedestals.crafting;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.item.ItemDust;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Map;

public class SawMill
{
    private static final SawMill SAWMILL = new SawMill();

    private final Map<Item, ItemStack> sawList = Maps.<Item, ItemStack>newHashMap();

    public static SawMill instance()
    {
        return SAWMILL;
    }

    private SawMill()
    {
        //this.addSawMillRecipe(new ItemStack(Blocks.OAK_LOG).getItem(),new ItemStack(Blocks.OAK_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.BIRCH_LOG).getItem(),new ItemStack(Blocks.BIRCH_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.JUNGLE_LOG).getItem(),new ItemStack(Blocks.JUNGLE_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.ACACIA_LOG).getItem(),new ItemStack(Blocks.ACACIA_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.SPRUCE_LOG).getItem(),new ItemStack(Blocks.SPRUCE_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.DARK_OAK_LOG).getItem(),new ItemStack(Blocks.DARK_OAK_PLANKS,5));
        //Warped
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235368_mh_).getItem(),new ItemStack(Blocks.field_235345_mD_,5));
        //Crimson
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235377_mq_).getItem(),new ItemStack(Blocks.field_235344_mC_,5));

        //this.addSawMillRecipe(new ItemStack(Blocks.STRIPPED_OAK_LOG).getItem(),new ItemStack(Blocks.OAK_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.STRIPPED_BIRCH_LOG).getItem(),new ItemStack(Blocks.BIRCH_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.STRIPPED_JUNGLE_LOG).getItem(),new ItemStack(Blocks.JUNGLE_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.STRIPPED_ACACIA_LOG).getItem(),new ItemStack(Blocks.ACACIA_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.STRIPPED_SPRUCE_LOG).getItem(),new ItemStack(Blocks.SPRUCE_PLANKS,5));
        //this.addSawMillRecipe(new ItemStack(Blocks.STRIPPED_DARK_OAK_LOG).getItem(),new ItemStack(Blocks.DARK_OAK_PLANKS,5));
        //Warped
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235369_mi_).getItem(),new ItemStack(Blocks.field_235345_mD_,5));
        //Crimson
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235378_mr_).getItem(),new ItemStack(Blocks.field_235344_mC_,5));

        //this.addSawMillRecipe(new ItemStack(Blocks.OAK_PLANKS).getItem(),new ItemStack(Blocks.OAK_SLAB,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.BIRCH_PLANKS).getItem(),new ItemStack(Blocks.BIRCH_SLAB,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.JUNGLE_PLANKS).getItem(),new ItemStack(Blocks.JUNGLE_SLAB,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.ACACIA_PLANKS).getItem(),new ItemStack(Blocks.ACACIA_SLAB,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.SPRUCE_PLANKS).getItem(),new ItemStack(Blocks.SPRUCE_SLAB,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.DARK_OAK_PLANKS).getItem(),new ItemStack(Blocks.DARK_OAK_SLAB,2));
        //Warped
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235345_mD_).getItem(),new ItemStack(Blocks.field_235347_mF_,2));
        //Crimson
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235344_mC_).getItem(),new ItemStack(Blocks.field_235346_mE_,2));

        //this.addSawMillRecipe(new ItemStack(Blocks.OAK_FENCE).getItem(),new ItemStack(Items.STICK,6));
        //this.addSawMillRecipe(new ItemStack(Blocks.BIRCH_FENCE).getItem(),new ItemStack(Items.STICK,6));
        //this.addSawMillRecipe(new ItemStack(Blocks.JUNGLE_FENCE).getItem(),new ItemStack(Items.STICK,6));
        //this.addSawMillRecipe(new ItemStack(Blocks.ACACIA_FENCE).getItem(),new ItemStack(Items.STICK,6));
        //this.addSawMillRecipe(new ItemStack(Blocks.SPRUCE_FENCE).getItem(),new ItemStack(Items.STICK,6));
        //this.addSawMillRecipe(new ItemStack(Blocks.DARK_OAK_FENCE).getItem(),new ItemStack(Items.STICK,6));
        //Warped
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235351_mJ_).getItem(),new ItemStack(Items.STICK,6));
        //Crimson
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235350_mI_).getItem(),new ItemStack(Items.STICK,6));

        //this.addSawMillRecipe(new ItemStack(Blocks.OAK_STAIRS).getItem(),new ItemStack(Items.STICK,3));
        //this.addSawMillRecipe(new ItemStack(Blocks.BIRCH_STAIRS).getItem(),new ItemStack(Items.STICK,3));
        //this.addSawMillRecipe(new ItemStack(Blocks.JUNGLE_STAIRS).getItem(),new ItemStack(Items.STICK,3));
        //this.addSawMillRecipe(new ItemStack(Blocks.ACACIA_STAIRS).getItem(),new ItemStack(Items.STICK,3));
        //this.addSawMillRecipe(new ItemStack(Blocks.SPRUCE_STAIRS).getItem(),new ItemStack(Items.STICK,3));
        //this.addSawMillRecipe(new ItemStack(Blocks.DARK_OAK_STAIRS).getItem(),new ItemStack(Items.STICK,3));
        //Warped
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235356_mO_).getItem(),new ItemStack(Items.STICK,3));
        //Crimson
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235357_mP_).getItem(),new ItemStack(Items.STICK,3));

        //this.addSawMillRecipe(new ItemStack(Blocks.OAK_SLAB).getItem(),new ItemStack(Blocks.OAK_PRESSURE_PLATE,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.BIRCH_SLAB).getItem(),new ItemStack(Blocks.BIRCH_PRESSURE_PLATE,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.JUNGLE_SLAB).getItem(),new ItemStack(Blocks.JUNGLE_PRESSURE_PLATE,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.ACACIA_SLAB).getItem(),new ItemStack(Blocks.ACACIA_PRESSURE_PLATE,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.SPRUCE_SLAB).getItem(),new ItemStack(Blocks.SPRUCE_PRESSURE_PLATE,2));
        //this.addSawMillRecipe(new ItemStack(Blocks.DARK_OAK_SLAB).getItem(),new ItemStack(Blocks.DARK_OAK_PRESSURE_PLATE,2));
        //Warped
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235347_mF_).getItem(),new ItemStack(Blocks.field_235349_mH_,2));
        //Crimson
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235346_mE_).getItem(),new ItemStack(Blocks.field_235348_mG_,2));

        //this.addSawMillRecipe(new ItemStack(Blocks.OAK_PRESSURE_PLATE).getItem(),new ItemStack(Items.STICK,1));
        //this.addSawMillRecipe(new ItemStack(Blocks.BIRCH_PRESSURE_PLATE).getItem(),new ItemStack(Items.STICK,1));
        //this.addSawMillRecipe(new ItemStack(Blocks.JUNGLE_PRESSURE_PLATE).getItem(),new ItemStack(Items.STICK,1));
        //this.addSawMillRecipe(new ItemStack(Blocks.ACACIA_PRESSURE_PLATE).getItem(),new ItemStack(Items.STICK,1));
        //this.addSawMillRecipe(new ItemStack(Blocks.SPRUCE_PRESSURE_PLATE).getItem(),new ItemStack(Items.STICK,1));
        //this.addSawMillRecipe(new ItemStack(Blocks.DARK_OAK_PRESSURE_PLATE).getItem(),new ItemStack(Items.STICK,1));
        //Warped
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235349_mH_).getItem(),new ItemStack(Items.STICK,1));
        //Crimson
        //this.addSawMillRecipe(new ItemStack(Blocks.field_235348_mG_).getItem(),new ItemStack(Items.STICK,1));

        //this.addSawMillRecipe(new ItemStack(Items.BAMBOO).getItem(),new ItemStack(Items.STICK,2));
    }

    public void addSawMillRecipe(Item stackIn, ItemStack stackOut)
    {
        if (getResult(stackIn) != ItemStack.EMPTY) { return;}
        this.sawList.put(stackIn, stackOut);
    }

    /**
     * Returns the sawing result of an item.
     */
    public ItemStack getResult(Item stackIn)
    {
        for (Map.Entry<Item, ItemStack> entry : this.sawList.entrySet())
        {
            if (stackIn == entry.getKey())
            {
                return entry.getValue();
            }
        }

        return ItemStack.EMPTY;
    }

    public Map<Item, ItemStack> getSawingList()
    {
        return this.sawList;
    }

}
