package com.mowmaster.pedestals.crafting;

import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

public class Crusher
{
    //Old class, see recipes/CrusherRecipe for updated Crusher code
    /*private static final Crusher CRUSHER = new Crusher();

    private final Map<Item, ItemStack> crusherList = Maps.<Item, ItemStack>newHashMap();

    public static Crusher instance()
    {
        return CRUSHER;
    }

    private Crusher()
    {
        //this.addCrusherRecipe(new ItemStack(Blocks.STONE).getItem(),new ItemStack(Blocks.COBBLESTONE));
        //this.addCrusherRecipe(new ItemStack(Blocks.COBBLESTONE).getItem(),new ItemStack(Blocks.GRAVEL));
        //this.addCrusherRecipe(new ItemStack(Blocks.GRAVEL).getItem(),new ItemStack(Blocks.SAND));
        //this.addCrusherRecipe(new ItemStack(Blocks.IRON_ORE).getItem(),new ItemStack(ItemDust.IRON,2));
        //this.addCrusherRecipe(new ItemStack(Blocks.GOLD_ORE).getItem(),new ItemStack(ItemDust.GOLD,2));
        //this.addCrusherTagRecipe("forge:ores/gold",new ItemStack(ItemDust.GOLD,2));
        //this.addCrusherTagRecipe("forge:ores/iron",new ItemStack(ItemDust.IRON,2));
        //this.addCrusherTagRecipe("forge:ores/copper",new ItemStack(ItemDust.COPPER,2));
        //this.addCrusherTagRecipe("forge:ores/tin",new ItemStack(ItemDust.TIN,2));
        //this.addCrusherTagRecipe("forge:ores/osmium",new ItemStack(ItemDust.OSMIUM,2));
        //this.addCrusherTagRecipe("forge:ores/uranium",new ItemStack(ItemDust.URANIUM,2));
        //this.addCrusherTagRecipe("forge:ores/lead",new ItemStack(ItemDust.LEAD,2));
        //this.addCrusherTagRecipe("forge:ores/aluminum",new ItemStack(ItemDust.ALUMINUM,2));
        //this.addCrusherTagRecipe("forge:ores/silver",new ItemStack(ItemDust.SILVER,2));
        //this.addCrusherTagRecipe("forge:ores/nickel",new ItemStack(ItemDust.NICKEL,2));

        ////this.addCrusherTagRecipe("forge:ores/platinum",new ItemStack(ItemDust.PLATINUM,2));
        ////this.addCrusherTagRecipe("forge:ores/zinc",new ItemStack(ItemDust.ZINC,2));
        ////this.addCrusherTagRecipe("forge:ores/bismuth",new ItemStack(ItemDust.BISMUTH,2));
        ////this.addCrusherTagRecipe("forge:ores/tungsten",new ItemStack(ItemDust.TUNGSTEN,2));
        ////this.addCrusherTagRecipe("forge:ores/allthemodium",new ItemStack(ItemDust.ALLTHEMODIUM,2));

        //Nether Gold Ore
        //this.addCrusherRecipe(new ItemStack(Blocks.field_235334_I_).getItem(),new ItemStack(ItemDust.GOLD,2));

        //this.addCrusherRecipe(new ItemStack(Items.BLAZE_ROD).getItem(),new ItemStack(Items.BLAZE_POWDER,3));
        //this.addCrusherRecipe(new ItemStack(Items.SUGAR_CANE).getItem(),new ItemStack(Items.SUGAR,2));
        //this.addCrusherRecipe(new ItemStack(Items.BEETROOT).getItem(),new ItemStack(Items.SUGAR,1));
        //this.addCrusherRecipe(new ItemStack(Items.POISONOUS_POTATO).getItem(),new ItemStack(ItemDust.FLOUR,1));
        //this.addCrusherRecipe(new ItemStack(Items.POTATO).getItem(),new ItemStack(ItemDust.FLOUR,1));
        //this.addCrusherRecipe(new ItemStack(Items.WHEAT).getItem(),new ItemStack(ItemDust.FLOUR,1));
        //this.addCrusherRecipe(new ItemStack(Items.BONE).getItem(),new ItemStack(Items.BONE_MEAL,4));

        //Flowers to Dye
        //Red Tulip,Poppy,RoseBush(2) = red_dye
        //this.addCrusherRecipe(new ItemStack(Items.RED_TULIP).getItem(),new ItemStack(Items.RED_DYE,2));
        //this.addCrusherRecipe(new ItemStack(Items.POPPY).getItem(),new ItemStack(Items.RED_DYE,2));
        //this.addCrusherRecipe(new ItemStack(Items.ROSE_BUSH).getItem(),new ItemStack(Items.RED_DYE,4));
        //Cactus = green dye
        //this.addCrusherRecipe(new ItemStack(Items.CACTUS).getItem(),new ItemStack(Items.GREEN_DYE,2));
        //Oxeye, Azure, white tulip = lightgrey
        //this.addCrusherRecipe(new ItemStack(Items.OXEYE_DAISY).getItem(),new ItemStack(Items.LIGHT_GRAY_DYE,2));
        //this.addCrusherRecipe(new ItemStack(Items.AZURE_BLUET).getItem(),new ItemStack(Items.LIGHT_GRAY_DYE,2));
        //this.addCrusherRecipe(new ItemStack(Items.WHITE_TULIP).getItem(),new ItemStack(Items.LIGHT_GRAY_DYE,2));
        //pink tulup, peony(2) = pink dye
        //this.addCrusherRecipe(new ItemStack(Items.PINK_TULIP).getItem(),new ItemStack(Items.PINK_DYE,2));
        //this.addCrusherRecipe(new ItemStack(Items.PEONY).getItem(),new ItemStack(Items.PINK_DYE,4));
        //sea pickle = lime dye
        //this.addCrusherRecipe(new ItemStack(Items.SEA_PICKLE).getItem(),new ItemStack(Items.LIME_DYE,2));
        //dandelion,sunflower(2) = yellow dye
        //this.addCrusherRecipe(new ItemStack(Items.DANDELION).getItem(),new ItemStack(Items.YELLOW_DYE,2));
        //this.addCrusherRecipe(new ItemStack(Items.SUNFLOWER).getItem(),new ItemStack(Items.YELLOW_DYE,4));
        //blue orchid = light blue dye
        //this.addCrusherRecipe(new ItemStack(Items.BLUE_ORCHID).getItem(),new ItemStack(Items.LIGHT_BLUE_DYE,2));
        //lilac(2), allium = magenta dye
        //this.addCrusherRecipe(new ItemStack(Items.LILAC).getItem(),new ItemStack(Items.MAGENTA_DYE,4));
        //this.addCrusherRecipe(new ItemStack(Items.ALLIUM).getItem(),new ItemStack(Items.MAGENTA_DYE,2));
        //orange tulip = orange dye
        //this.addCrusherRecipe(new ItemStack(Items.ORANGE_TULIP).getItem(),new ItemStack(Items.ORANGE_DYE,2));
        //corn flower, lapis = blue dye
        //this.addCrusherRecipe(new ItemStack(Items.CORNFLOWER).getItem(),new ItemStack(Items.BLUE_DYE,2));
        //this.addCrusherRecipe(new ItemStack(Items.LAPIS_LAZULI).getItem(),new ItemStack(Items.BLUE_DYE,2));
        //cocoa beans = brown dye
        //this.addCrusherRecipe(new ItemStack(Items.COCOA_BEANS).getItem(),new ItemStack(Items.BROWN_DYE,2));
        //ink sac,wither rose = black dye
        //this.addCrusherRecipe(new ItemStack(Items.INK_SAC).getItem(),new ItemStack(Items.BLACK_DYE,2));
        //this.addCrusherRecipe(new ItemStack(Items.WITHER_ROSE).getItem(),new ItemStack(Items.BLACK_DYE,2));
        //lilly of the valey, bonemeal = white dye
        //this.addCrusherRecipe(new ItemStack(Items.LILY_OF_THE_VALLEY).getItem(),new ItemStack(Items.WHITE_DYE,2));
        //this.addCrusherRecipe(new ItemStack(Items.BONE_MEAL).getItem(),new ItemStack(Items.WHITE_DYE,2));

    }


    *//*public void addCrusherBlockRecipe(Block blockIn, ItemStack stack)
    {
        this.addCrusherRecipe(, stack);
    }*//*
    public void addCrusherTagRecipe(String tagIn, ItemStack stackOut)
    {
        ResourceLocation grabTags = new ResourceLocation(tagIn.split(":")[0], tagIn.split(":")[1]);
        List<Item> itemList = ItemTags.getAllTags().getTagOrCreate(grabTags).getAllElements();
        for(Item item : itemList)
        {
            if (getResult(item) != ItemStack.EMPTY) { return;}
            this.crusherList.put(item, stackOut);
        }
    }

    public void addCrusherRecipe(Item stackIn, ItemStack stackOut)
    {
        if (getResult(stackIn) != ItemStack.EMPTY) { return;}
        this.crusherList.put(stackIn, stackOut);
    }

    *//**
     * Returns the smelting result of an item.
     *//*
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
    }*/

}
