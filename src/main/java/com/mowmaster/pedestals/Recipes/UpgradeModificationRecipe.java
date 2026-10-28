package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.pedestals.Items.PedestalDataComponents;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

// Modification upgrade (1.20's "upgrademodification_global"): an upgrade from the inventory below plus the items on
// the work card's pedestals, in order, adds value to one of the upgrade's modifiers (up to max).
//   {"type": "pedestals:upgrademodification_global",
//    "ingredients": ["pedestals:upgrade_pedestal_breaker", "#c:gems/diamond", "minecraft:obsidian"],
//    "modifier": "upgradespeed", "value": 2, "max": 40}
// The first ingredient must be the upgrade; empty pedestals don't count, so ingredients line up with the full ones.
public final class UpgradeModificationRecipe extends PedestalMachineRecipe<UpgradeModificationRecipe.Input>
{
    public static final MapCodec<UpgradeModificationRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf(1, 10).fieldOf("ingredients").forGetter(UpgradeModificationRecipe::ingredients),
            Codec.STRING.fieldOf("modifier").forGetter(UpgradeModificationRecipe::modifier),
            Codec.INT.optionalFieldOf("value", 1).forGetter(UpgradeModificationRecipe::value),
            Codec.INT.optionalFieldOf("min", 0).forGetter(UpgradeModificationRecipe::min),
            Codec.INT.fieldOf("max").forGetter(UpgradeModificationRecipe::max))
            .apply(instance, UpgradeModificationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeModificationRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), UpgradeModificationRecipe::ingredients,
            ByteBufCodecs.STRING_UTF8, UpgradeModificationRecipe::modifier,
            ByteBufCodecs.VAR_INT, UpgradeModificationRecipe::value,
            ByteBufCodecs.VAR_INT, UpgradeModificationRecipe::min,
            ByteBufCodecs.VAR_INT, UpgradeModificationRecipe::max,
            UpgradeModificationRecipe::new);

    private final List<Ingredient> ingredients;
    private final String modifier;
    private final int value;
    private final int min;
    private final int max;

    public UpgradeModificationRecipe(List<Ingredient> ingredients, String modifier, int value, int min, int max)
    {
        this.ingredients = List.copyOf(ingredients);
        this.modifier = modifier;
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public List<Ingredient> ingredients() { return ingredients; }
    public String modifier() { return modifier; }
    public int value() { return value; }
    public int min() { return min; }
    public int max() { return max; }

    // Item 0 is the upgrade, the rest come from the pedestals. Every ingredient needs its item, in order.
    @Override
    public boolean matches(Input input, Level level)
    {
        List<ItemStack> items = input.items();
        if (items.size() != ingredients.size()) return false;
        for (int index = 0; index < items.size(); index++)
        {
            if (!ingredients.get(index).test(items.get(index))) return false;
        }
        return canApply(items.getFirst());
    }

    // The upgrade takes this modifier and isn't already at the max (or below the min)
    public boolean canApply(ItemStack upgrade)
    {
        if (!(upgrade.getItem() instanceof ItemUpgradeBase base) || !base.canModify(upgrade, modifier)) return false;
        int current = ItemUpgradeBase.getModifiers(upgrade).get(modifier);
        return current < max && current >= min;
    }

    // The upgrade with the modifier raised
    public ItemStack apply(ItemStack upgrade)
    {
        ItemStack modified = upgrade.copyWithCount(1);
        int current = ItemUpgradeBase.getModifiers(modified).get(modifier);
        modified.set(PedestalDataComponents.UPGRADE_MODIFIERS.get(), ItemUpgradeBase.getModifiers(modified).with(modifier, Math.min(max, current + value)));
        return modified;
    }

    @Override
    public RecipeSerializer<UpgradeModificationRecipe> getSerializer()
    {
        return PedestalRecipes.UPGRADE_MODIFICATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<UpgradeModificationRecipe> getType()
    {
        return PedestalRecipes.UPGRADE_MODIFICATION.get();
    }

    // The upgrade, then the pedestals' items (empty ones left out)
    public record Input(List<ItemStack> items) implements RecipeInput
    {
        @Override
        public ItemStack getItem(int index)
        {
            return index < items.size() ? items.get(index) : ItemStack.EMPTY;
        }

        @Override
        public int size()
        {
            return items.size();
        }
    }
}
