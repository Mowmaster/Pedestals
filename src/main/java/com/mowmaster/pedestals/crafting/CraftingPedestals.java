package com.mowmaster.pedestals.crafting;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

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
        this.addPedestalCraftingRecipe(0, PedestalBlock.PEDESTAL_000.getDefaultState());
        this.addPedestalCraftingRecipe(85, PedestalBlock.PEDESTAL_001.getDefaultState());
        this.addPedestalCraftingRecipe(170, PedestalBlock.PEDESTAL_002.getDefaultState());
        this.addPedestalCraftingRecipe(255, PedestalBlock.PEDESTAL_003.getDefaultState());
        this.addPedestalCraftingRecipe(21760, PedestalBlock.PEDESTAL_010.getDefaultState());
        this.addPedestalCraftingRecipe(21845, PedestalBlock.PEDESTAL_011.getDefaultState());
        this.addPedestalCraftingRecipe(21930, PedestalBlock.PEDESTAL_012.getDefaultState());
        this.addPedestalCraftingRecipe(22015, PedestalBlock.PEDESTAL_013.getDefaultState());
        this.addPedestalCraftingRecipe(43520, PedestalBlock.PEDESTAL_020.getDefaultState());
        this.addPedestalCraftingRecipe(43605, PedestalBlock.PEDESTAL_021.getDefaultState());
        this.addPedestalCraftingRecipe(43690, PedestalBlock.PEDESTAL_022.getDefaultState());
        this.addPedestalCraftingRecipe(43775, PedestalBlock.PEDESTAL_023.getDefaultState());
        this.addPedestalCraftingRecipe(65280, PedestalBlock.PEDESTAL_030.getDefaultState());
        this.addPedestalCraftingRecipe(65365, PedestalBlock.PEDESTAL_031.getDefaultState());
        this.addPedestalCraftingRecipe(65450, PedestalBlock.PEDESTAL_032.getDefaultState());
        this.addPedestalCraftingRecipe(65535, PedestalBlock.PEDESTAL_033.getDefaultState());

        this.addPedestalCraftingRecipe(5570560, PedestalBlock.PEDESTAL_100.getDefaultState());
        this.addPedestalCraftingRecipe(5570645, PedestalBlock.PEDESTAL_101.getDefaultState());
        this.addPedestalCraftingRecipe(5570730, PedestalBlock.PEDESTAL_102.getDefaultState());
        this.addPedestalCraftingRecipe(5570815, PedestalBlock.PEDESTAL_103.getDefaultState());
        this.addPedestalCraftingRecipe(5592320, PedestalBlock.PEDESTAL_110.getDefaultState());
        this.addPedestalCraftingRecipe(5592405, PedestalBlock.PEDESTAL_111.getDefaultState());
        this.addPedestalCraftingRecipe(5592490, PedestalBlock.PEDESTAL_112.getDefaultState());
        this.addPedestalCraftingRecipe(5592575, PedestalBlock.PEDESTAL_113.getDefaultState());
        this.addPedestalCraftingRecipe(5614080, PedestalBlock.PEDESTAL_120.getDefaultState());
        this.addPedestalCraftingRecipe(5614165, PedestalBlock.PEDESTAL_121.getDefaultState());
        this.addPedestalCraftingRecipe(5614250, PedestalBlock.PEDESTAL_122.getDefaultState());
        this.addPedestalCraftingRecipe(5614335, PedestalBlock.PEDESTAL_123.getDefaultState());
        this.addPedestalCraftingRecipe(5635840, PedestalBlock.PEDESTAL_130.getDefaultState());
        this.addPedestalCraftingRecipe(5635925, PedestalBlock.PEDESTAL_131.getDefaultState());
        this.addPedestalCraftingRecipe(5636010, PedestalBlock.PEDESTAL_132.getDefaultState());
        this.addPedestalCraftingRecipe(5636095, PedestalBlock.PEDESTAL_133.getDefaultState());

        this.addPedestalCraftingRecipe(11141120, PedestalBlock.PEDESTAL_200.getDefaultState());
        this.addPedestalCraftingRecipe(11141205, PedestalBlock.PEDESTAL_201.getDefaultState());
        this.addPedestalCraftingRecipe(11141290, PedestalBlock.PEDESTAL_202.getDefaultState());
        this.addPedestalCraftingRecipe(11141375, PedestalBlock.PEDESTAL_203.getDefaultState());
        this.addPedestalCraftingRecipe(11162880, PedestalBlock.PEDESTAL_210.getDefaultState());
        this.addPedestalCraftingRecipe(11162965, PedestalBlock.PEDESTAL_211.getDefaultState());
        this.addPedestalCraftingRecipe(11163050, PedestalBlock.PEDESTAL_212.getDefaultState());
        this.addPedestalCraftingRecipe(11163135, PedestalBlock.PEDESTAL_213.getDefaultState());
        this.addPedestalCraftingRecipe(11184640, PedestalBlock.PEDESTAL_220.getDefaultState());
        this.addPedestalCraftingRecipe(11184725, PedestalBlock.PEDESTAL_221.getDefaultState());
        this.addPedestalCraftingRecipe(11184810, PedestalBlock.PEDESTAL_222.getDefaultState());
        this.addPedestalCraftingRecipe(11184895, PedestalBlock.PEDESTAL_223.getDefaultState());
        this.addPedestalCraftingRecipe(11206400, PedestalBlock.PEDESTAL_230.getDefaultState());
        this.addPedestalCraftingRecipe(11206485, PedestalBlock.PEDESTAL_231.getDefaultState());
        this.addPedestalCraftingRecipe(11206570, PedestalBlock.PEDESTAL_232.getDefaultState());
        this.addPedestalCraftingRecipe(11206655, PedestalBlock.PEDESTAL_233.getDefaultState());

        this.addPedestalCraftingRecipe(16711680, PedestalBlock.PEDESTAL_300.getDefaultState());
        this.addPedestalCraftingRecipe(16711765, PedestalBlock.PEDESTAL_301.getDefaultState());
        this.addPedestalCraftingRecipe(16711850, PedestalBlock.PEDESTAL_302.getDefaultState());
        this.addPedestalCraftingRecipe(16711935, PedestalBlock.PEDESTAL_303.getDefaultState());
        this.addPedestalCraftingRecipe(16733440, PedestalBlock.PEDESTAL_310.getDefaultState());
        this.addPedestalCraftingRecipe(16733525, PedestalBlock.PEDESTAL_311.getDefaultState());
        this.addPedestalCraftingRecipe(16733610, PedestalBlock.PEDESTAL_312.getDefaultState());
        this.addPedestalCraftingRecipe(16733695, PedestalBlock.PEDESTAL_313.getDefaultState());
        this.addPedestalCraftingRecipe(16755200, PedestalBlock.PEDESTAL_320.getDefaultState());
        this.addPedestalCraftingRecipe(16755285, PedestalBlock.PEDESTAL_321.getDefaultState());
        this.addPedestalCraftingRecipe(16755370, PedestalBlock.PEDESTAL_322.getDefaultState());
        this.addPedestalCraftingRecipe(16755455, PedestalBlock.PEDESTAL_323.getDefaultState());
        this.addPedestalCraftingRecipe(16776960, PedestalBlock.PEDESTAL_330.getDefaultState());
        this.addPedestalCraftingRecipe(16777045, PedestalBlock.PEDESTAL_331.getDefaultState());
        this.addPedestalCraftingRecipe(16777130, PedestalBlock.PEDESTAL_332.getDefaultState());
        this.addPedestalCraftingRecipe(16777215, PedestalBlock.PEDESTAL_333.getDefaultState());
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
