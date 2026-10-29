package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

// What the XP anvil charges for taking an enchantment past its normal maximum level. The cost (see AnvilCost) is
// multiplied by how far past the maximum the result is (Sharpness X is 5 past V: five times the cost); ingredients
// with "levels" are only needed when the result's level is in that range.
//   {"type": "pedestals:anvil_overlevel", "enchantments": "minecraft:sharpness",
//    "ingredients": [{"ingredient": "minecraft:netherite_ingot", "levels": "5-10"}, {"ingredient": "minecraft:nether_star", "levels": "11-20"}],
//    "fluid": {"ingredient": "minecraft:lava", "amount": 1000}, "experience_levels": 5}
// "enchantments" takes one id, a list, or a tag ("#minecraft:non_treasure"). An enchantment with no recipe can't go
// past its maximum.
public final class AnvilOverlevelRecipe extends PedestalMachineRecipe<AnvilOverlevelRecipe.Input>
{
    public static final MapCodec<AnvilOverlevelRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.holderSet(Registries.ENCHANTMENT).fieldOf("enchantments").forGetter(AnvilOverlevelRecipe::enchantments),
            AnvilCost.MAP_CODEC.forGetter(AnvilOverlevelRecipe::cost))
            .apply(instance, AnvilOverlevelRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnvilOverlevelRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.ENCHANTMENT), AnvilOverlevelRecipe::enchantments,
            AnvilCost.STREAM_CODEC, AnvilOverlevelRecipe::cost,
            AnvilOverlevelRecipe::new);

    private final HolderSet<Enchantment> enchantments;
    private final AnvilCost cost;

    public AnvilOverlevelRecipe(HolderSet<Enchantment> enchantments, AnvilCost cost)
    {
        this.enchantments = enchantments;
        this.cost = cost;
    }

    public HolderSet<Enchantment> enchantments() { return enchantments; }
    public AnvilCost cost() { return cost; }

    @Override
    public boolean matches(Input input, Level level)
    {
        return enchantments.contains(input.enchantment());
    }

    @Override
    public RecipeSerializer<AnvilOverlevelRecipe> getSerializer()
    {
        return PedestalRecipes.ANVIL_OVERLEVEL_SERIALIZER.get();
    }

    @Override
    public RecipeType<AnvilOverlevelRecipe> getType()
    {
        return PedestalRecipes.ANVIL_OVERLEVEL.get();
    }

    // Recipes are looked up by the enchantment only
    public record Input(Holder<Enchantment> enchantment) implements RecipeInput
    {
        @Override
        public ItemStack getItem(int index)
        {
            return ItemStack.EMPTY;
        }

        @Override
        public int size()
        {
            return 0;
        }

        // Recipe lookups skip inputs that count as empty (no items), and this input never has items
        @Override
        public boolean isEmpty()
        {
            return false;
        }
    }
}
