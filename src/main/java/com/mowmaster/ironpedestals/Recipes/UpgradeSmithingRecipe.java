package com.mowmaster.ironpedestals.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.ironpedestals.Items.PedestalDataComponents;
import com.mowmaster.ironpedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.ironpedestals.Items.Upgrades.UpgradeModifiers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

// The Upgrade smithing template unlocking a special modifier: template + upgrade + addition in a smithing table ->
// the same upgrade with that modifier at its full value (UpgradeModifiers.SPECIALS). Only matches upgrades that use
// the modifier and don't have it yet. One recipe per modifier (recipe/upgrade_smithing/):
//   {"type": "ironpedestals:upgrade_smithing", "template": "ironpedestals:smithing_template_upgrade",
//    "base": "#ironpedestals:upgrades", "addition": "minecraft:ender_eye", "modifier": "upgradeentitystorage"}
// The smithing table only lets items into its slots that some smithing recipe's template / base / addition
// ingredient accepts, so these three ingredients are also what opens the slots for them.
public class UpgradeSmithingRecipe implements SmithingRecipe
{
    public static final MapCodec<UpgradeSmithingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("template").forGetter(UpgradeSmithingRecipe::template),
                    Ingredient.CODEC.fieldOf("base").forGetter(UpgradeSmithingRecipe::base),
                    Ingredient.CODEC.fieldOf("addition").forGetter(UpgradeSmithingRecipe::addition),
                    Codec.STRING.fieldOf("modifier").forGetter(UpgradeSmithingRecipe::modifier))
            .apply(instance, UpgradeSmithingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeSmithingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, UpgradeSmithingRecipe::template,
            Ingredient.CONTENTS_STREAM_CODEC, UpgradeSmithingRecipe::base,
            Ingredient.CONTENTS_STREAM_CODEC, UpgradeSmithingRecipe::addition,
            ByteBufCodecs.STRING_UTF8, UpgradeSmithingRecipe::modifier,
            UpgradeSmithingRecipe::new);

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final String modifier;
    private PlacementInfo placementInfo;

    public UpgradeSmithingRecipe(Ingredient template, Ingredient base, Ingredient addition, String modifier)
    {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.modifier = modifier;
    }

    public Ingredient template() { return template; }
    public Ingredient base() { return base; }
    public Ingredient addition() { return addition; }
    public String modifier() { return modifier; }

    // ---- SmithingRecipe: which items go in which slot ----

    @Override
    public Optional<Ingredient> templateIngredient()
    {
        return Optional.of(template);
    }

    @Override
    public Ingredient baseIngredient()
    {
        return base;
    }

    @Override
    public Optional<Ingredient> additionIngredient()
    {
        return Optional.of(addition);
    }

    // The three ingredients match (SmithingRecipe's own check), and the upgrade can take the modifier
    @Override
    public boolean matches(SmithingRecipeInput input, Level level)
    {
        return SmithingRecipe.super.matches(input, level) && canApply(input.base());
    }

    // The upgrade uses this modifier and hasn't got it at full value yet
    public boolean canApply(ItemStack upgrade)
    {
        Integer full = UpgradeModifiers.SPECIALS.get(modifier);
        if (full == null || !(upgrade.getItem() instanceof ItemUpgradeBase upgradeItem) || !upgradeItem.canModify(upgrade, modifier)) return false;
        return ItemUpgradeBase.getModifiers(upgrade).get(modifier) < full;
    }

    // The upgrade, everything kept, with the modifier at full value
    @Override
    public ItemStack assemble(SmithingRecipeInput input)
    {
        ItemStack upgrade = input.base().copyWithCount(1);
        int full = UpgradeModifiers.SPECIALS.getOrDefault(modifier, 1);
        upgrade.set(PedestalDataComponents.UPGRADE_MODIFIERS.get(), ItemUpgradeBase.getModifiers(upgrade).with(modifier, full));
        return upgrade;
    }

    // ---- Recipe bookkeeping ----

    @Override
    public RecipeSerializer<UpgradeSmithingRecipe> getSerializer()
    {
        return PedestalRecipes.UPGRADE_SMITHING_SERIALIZER.get();
    }

    // The ingredients in slot order; worked out once (like vanilla's smithing recipes)
    @Override
    public PlacementInfo placementInfo()
    {
        if (placementInfo == null) placementInfo = PlacementInfo.create(List.of(template, base, addition));
        return placementInfo;
    }

    @Override
    public boolean showNotification()
    {
        return false;
    }

    @Override
    public String group()
    {
        return "";
    }
}
