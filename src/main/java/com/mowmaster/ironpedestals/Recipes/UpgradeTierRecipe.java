package com.mowmaster.ironpedestals.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.ironpedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.ironpedestals.Items.Upgrades.UpgradeTier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

// An upgrade crafted up one tier, like the pedestals: 8 of the tier's material around an upgrade of the tier before
// it, in a crafting table -> the same upgrade (mode, specials, everything kept) one tier higher. One recipe per tier,
// for every upgrade at once (recipe/upgrade_tiers/):
//   {"type": "ironpedestals:upgrade_tier", "material": "#c:ingots/copper", "tier": 1}
// A CustomRecipe ("special" recipe) because the result depends on which upgrade goes in; it has no recipe book entry.
public class UpgradeTierRecipe extends CustomRecipe
{
    public static final MapCodec<UpgradeTierRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("material").forGetter(UpgradeTierRecipe::material),
                    Codec.intRange(1, UpgradeTier.MAX).fieldOf("tier").forGetter(UpgradeTierRecipe::tier))
            .apply(instance, UpgradeTierRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeTierRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, UpgradeTierRecipe::material,
            ByteBufCodecs.VAR_INT, UpgradeTierRecipe::tier,
            UpgradeTierRecipe::new);

    // The middle of a 3x3 grid (slots are counted row by row)
    private static final int CENTRE = 4;

    private final Ingredient material;
    private final int tier;

    public UpgradeTierRecipe(Ingredient material, int tier)
    {
        this.material = material;
        this.tier = tier;
    }

    public Ingredient material()
    {
        return material;
    }

    public int tier()
    {
        return tier;
    }

    // A full 3x3: the material all round, an upgrade of the tier before in the middle
    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        if (input.width() != 3 || input.height() != 3 || input.ingredientCount() != 9) return false;
        for (int slot = 0; slot < 9; slot++)
        {
            ItemStack stack = input.getItem(slot);
            if (slot == CENTRE)
            {
                if (!(stack.getItem() instanceof ItemUpgradeBase) || UpgradeTier.of(stack).tier() != tier - 1) return false;
            }
            else if (!material.test(stack))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        return UpgradeTier.byTier(tier).applyTo(input.getItem(CENTRE));
    }

    @Override
    public RecipeSerializer<UpgradeTierRecipe> getSerializer()
    {
        return PedestalRecipes.UPGRADE_TIER_SERIALIZER.get();
    }
}
