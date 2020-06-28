package com.mowmaster.pedestals.crafting;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class CraftingPedestals
{
    private static final CraftingPedestals CRAFTING_PEDESTALS = new CraftingPedestals();

    private final Map<Integer, BlockState> craftingPedestalsList = Maps.<Integer, BlockState>newHashMap();

    public static CraftingPedestals instance()
    {
        return CRAFTING_PEDESTALS;
    }

    private CraftingPedestals()
    {
        this.addPedestalCraftingRecipe(0,BlockPedestalTE.PEDESTAL_000.getDefaultState());
        this.addPedestalCraftingRecipe(85,BlockPedestalTE.PEDESTAL_001.getDefaultState());
        this.addPedestalCraftingRecipe(170,BlockPedestalTE.PEDESTAL_002.getDefaultState());
        this.addPedestalCraftingRecipe(255,BlockPedestalTE.PEDESTAL_003.getDefaultState());
        this.addPedestalCraftingRecipe(21760,BlockPedestalTE.PEDESTAL_010.getDefaultState());
        this.addPedestalCraftingRecipe(21845,BlockPedestalTE.PEDESTAL_011.getDefaultState());
        this.addPedestalCraftingRecipe(21930,BlockPedestalTE.PEDESTAL_012.getDefaultState());
        this.addPedestalCraftingRecipe(22015,BlockPedestalTE.PEDESTAL_013.getDefaultState());
        this.addPedestalCraftingRecipe(43520,BlockPedestalTE.PEDESTAL_020.getDefaultState());
        this.addPedestalCraftingRecipe(43605,BlockPedestalTE.PEDESTAL_021.getDefaultState());
        this.addPedestalCraftingRecipe(43690,BlockPedestalTE.PEDESTAL_022.getDefaultState());
        this.addPedestalCraftingRecipe(43775,BlockPedestalTE.PEDESTAL_023.getDefaultState());
        this.addPedestalCraftingRecipe(65280,BlockPedestalTE.PEDESTAL_030.getDefaultState());
        this.addPedestalCraftingRecipe(65365,BlockPedestalTE.PEDESTAL_031.getDefaultState());
        this.addPedestalCraftingRecipe(65450,BlockPedestalTE.PEDESTAL_032.getDefaultState());
        this.addPedestalCraftingRecipe(65535,BlockPedestalTE.PEDESTAL_033.getDefaultState());

        this.addPedestalCraftingRecipe(5570560,BlockPedestalTE.PEDESTAL_100.getDefaultState());
        this.addPedestalCraftingRecipe(5570645,BlockPedestalTE.PEDESTAL_101.getDefaultState());
        this.addPedestalCraftingRecipe(5570730,BlockPedestalTE.PEDESTAL_102.getDefaultState());
        this.addPedestalCraftingRecipe(5570815,BlockPedestalTE.PEDESTAL_103.getDefaultState());
        this.addPedestalCraftingRecipe(5592320,BlockPedestalTE.PEDESTAL_110.getDefaultState());
        this.addPedestalCraftingRecipe(5592405,BlockPedestalTE.PEDESTAL_111.getDefaultState());
        this.addPedestalCraftingRecipe(5592490,BlockPedestalTE.PEDESTAL_112.getDefaultState());
        this.addPedestalCraftingRecipe(5592575,BlockPedestalTE.PEDESTAL_113.getDefaultState());
        this.addPedestalCraftingRecipe(5614080,BlockPedestalTE.PEDESTAL_120.getDefaultState());
        this.addPedestalCraftingRecipe(5614165,BlockPedestalTE.PEDESTAL_121.getDefaultState());
        this.addPedestalCraftingRecipe(5614250,BlockPedestalTE.PEDESTAL_122.getDefaultState());
        this.addPedestalCraftingRecipe(5614335,BlockPedestalTE.PEDESTAL_123.getDefaultState());
        this.addPedestalCraftingRecipe(5635840,BlockPedestalTE.PEDESTAL_130.getDefaultState());
        this.addPedestalCraftingRecipe(5635925,BlockPedestalTE.PEDESTAL_131.getDefaultState());
        this.addPedestalCraftingRecipe(5636010,BlockPedestalTE.PEDESTAL_132.getDefaultState());
        this.addPedestalCraftingRecipe(5636095,BlockPedestalTE.PEDESTAL_133.getDefaultState());

        this.addPedestalCraftingRecipe(11141120,BlockPedestalTE.PEDESTAL_200.getDefaultState());
        this.addPedestalCraftingRecipe(11141205,BlockPedestalTE.PEDESTAL_201.getDefaultState());
        this.addPedestalCraftingRecipe(11141290,BlockPedestalTE.PEDESTAL_202.getDefaultState());
        this.addPedestalCraftingRecipe(11141375,BlockPedestalTE.PEDESTAL_203.getDefaultState());
        this.addPedestalCraftingRecipe(11162880,BlockPedestalTE.PEDESTAL_210.getDefaultState());
        this.addPedestalCraftingRecipe(11162965,BlockPedestalTE.PEDESTAL_211.getDefaultState());
        this.addPedestalCraftingRecipe(11163050,BlockPedestalTE.PEDESTAL_212.getDefaultState());
        this.addPedestalCraftingRecipe(11163135,BlockPedestalTE.PEDESTAL_213.getDefaultState());
        this.addPedestalCraftingRecipe(11184640,BlockPedestalTE.PEDESTAL_220.getDefaultState());
        this.addPedestalCraftingRecipe(11184725,BlockPedestalTE.PEDESTAL_221.getDefaultState());
        this.addPedestalCraftingRecipe(11184810,BlockPedestalTE.PEDESTAL_222.getDefaultState());
        this.addPedestalCraftingRecipe(11184895,BlockPedestalTE.PEDESTAL_223.getDefaultState());
        this.addPedestalCraftingRecipe(11206400,BlockPedestalTE.PEDESTAL_230.getDefaultState());
        this.addPedestalCraftingRecipe(11206485,BlockPedestalTE.PEDESTAL_231.getDefaultState());
        this.addPedestalCraftingRecipe(11206570,BlockPedestalTE.PEDESTAL_232.getDefaultState());
        this.addPedestalCraftingRecipe(11206655,BlockPedestalTE.PEDESTAL_233.getDefaultState());

        this.addPedestalCraftingRecipe(16711680,BlockPedestalTE.PEDESTAL_300.getDefaultState());
        this.addPedestalCraftingRecipe(16711765,BlockPedestalTE.PEDESTAL_301.getDefaultState());
        this.addPedestalCraftingRecipe(16711850,BlockPedestalTE.PEDESTAL_302.getDefaultState());
        this.addPedestalCraftingRecipe(16711935,BlockPedestalTE.PEDESTAL_303.getDefaultState());
        this.addPedestalCraftingRecipe(16733440,BlockPedestalTE.PEDESTAL_310.getDefaultState());
        this.addPedestalCraftingRecipe(16733525,BlockPedestalTE.PEDESTAL_311.getDefaultState());
        this.addPedestalCraftingRecipe(16733610,BlockPedestalTE.PEDESTAL_312.getDefaultState());
        this.addPedestalCraftingRecipe(16733695,BlockPedestalTE.PEDESTAL_313.getDefaultState());
        this.addPedestalCraftingRecipe(16755200,BlockPedestalTE.PEDESTAL_320.getDefaultState());
        this.addPedestalCraftingRecipe(16755285,BlockPedestalTE.PEDESTAL_321.getDefaultState());
        this.addPedestalCraftingRecipe(16755370,BlockPedestalTE.PEDESTAL_322.getDefaultState());
        this.addPedestalCraftingRecipe(16755455,BlockPedestalTE.PEDESTAL_323.getDefaultState());
        this.addPedestalCraftingRecipe(16776960,BlockPedestalTE.PEDESTAL_330.getDefaultState());
        this.addPedestalCraftingRecipe(16777045,BlockPedestalTE.PEDESTAL_331.getDefaultState());
        this.addPedestalCraftingRecipe(16777130,BlockPedestalTE.PEDESTAL_332.getDefaultState());
        this.addPedestalCraftingRecipe(16777215,BlockPedestalTE.PEDESTAL_333.getDefaultState());
    }


    public void addColorRecipe(int colorIn, BlockState state)
    {
        this.addPedestalCraftingRecipe(colorIn, state);
    }


    public void addPedestalCraftingRecipe(int colorIn, BlockState state)
    {
        if (getResult(colorIn) != Blocks.AIR.getDefaultState()) { return;}
        this.craftingPedestalsList.put(colorIn, state);
    }

    public BlockState getResult(int colorIn)
    {
        for (Map.Entry<Integer, BlockState> entry : this.craftingPedestalsList.entrySet())
        {
            if (colorIn == entry.getKey())
            {
                return entry.getValue();
            }
        }

        return Blocks.AIR.getDefaultState();
    }

    public Map<Integer, BlockState> getCrushingList()
    {
        return this.craftingPedestalsList;
    }

}
