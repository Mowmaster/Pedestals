package com.mowmaster.pedestals.crafting;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.item.ItemDust;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;

import java.util.Map;

public class Crusher
{
    private static final Crusher CRUSHER = new Crusher();

    private final Map<Item, ItemStack> crusherList = Maps.<Item, ItemStack>newHashMap();

    public static Crusher instance()
    {
        return CRUSHER;
    }

    private Crusher()
    {
        this.addCrusherRecipe(new ItemStack(Blocks.STONE).getItem(),new ItemStack(Blocks.COBBLESTONE));
        this.addCrusherRecipe(new ItemStack(Blocks.COBBLESTONE).getItem(),new ItemStack(Blocks.GRAVEL));
        this.addCrusherRecipe(new ItemStack(Blocks.GRAVEL).getItem(),new ItemStack(Blocks.SAND));
        this.addCrusherRecipe(new ItemStack(Blocks.IRON_ORE).getItem(),new ItemStack(ItemDust.IRON,2));
        this.addCrusherRecipe(new ItemStack(Blocks.GOLD_ORE).getItem(),new ItemStack(ItemDust.GOLD,2));
        //Nether Gold Ore
        this.addCrusherRecipe(new ItemStack(Blocks.field_235334_I_).getItem(),new ItemStack(ItemDust.GOLD,2));
        this.addCrusherRecipe(new ItemStack(Items.BLAZE_ROD).getItem(),new ItemStack(Items.BLAZE_POWDER,3));
        this.addCrusherRecipe(new ItemStack(Items.SUGAR_CANE).getItem(),new ItemStack(Items.SUGAR,2));
        this.addCrusherRecipe(new ItemStack(Items.BEETROOT).getItem(),new ItemStack(Items.SUGAR,1));
        this.addCrusherRecipe(new ItemStack(Items.POISONOUS_POTATO).getItem(),new ItemStack(ItemDust.FLOUR,1));
        this.addCrusherRecipe(new ItemStack(Items.POTATO).getItem(),new ItemStack(ItemDust.FLOUR,1));
        this.addCrusherRecipe(new ItemStack(Items.WHEAT).getItem(),new ItemStack(ItemDust.FLOUR,1));
        this.addCrusherRecipe(new ItemStack(Items.BONE).getItem(),new ItemStack(Items.BONE_MEAL,4));

    }


    /*public void addCrusherBlockRecipe(Block blockIn, ItemStack stack)
    {
        this.addCrusherRecipe(, stack);
    }*/


    public void addCrusherRecipe(Item stackIn, ItemStack stackOut)
    {
        if (getResult(stackIn) != ItemStack.EMPTY) { return;}
        this.crusherList.put(stackIn, stackOut);
    }

    /**
     * Returns the smelting result of an item.
     */
    public ItemStack getResult(Item stackIn)
    {
        for (Map.Entry<Item, ItemStack> entry : this.crusherList.entrySet())
        {
            if (stackIn == entry.getKey())
            {
                return entry.getValue();
            }
        }

        return ItemStack.EMPTY;
    }

    public Map<Item, ItemStack> getCrushingList()
    {
        return this.crusherList;
    }

}
