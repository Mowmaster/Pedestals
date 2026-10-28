package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.PedestalDataMaps;
import com.mowmaster.pedestals.PedestalTags;
import com.mowmaster.pedestals.Recipes.RecyclingMaterials;
import com.mowmaster.pedestals.Transport.ExperienceTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 1.16's experience grindstone and recycler in one upgrade. Takes enchanted things, tools and armour from the
// inventory below, one at a time. Turn the two steps on and off like the transport upgrades (only items and
// experience do anything):
//
// Experience (the grindstone), always done first: removes every enchantment except curses, like the vanilla
// grindstone, and puts the experience in the pedestal. Skipped if the pedestal can't hold all of it.
// Enchanted books with nothing left become books.
//
// Items (the recycler): breaks the item down into what it was crafted from, using the pedestals:recycling data map
// (see RecyclingMaterials). Each material has a 50% chance to come back, scaled by the durability left: a full
// diamond pickaxe can give 3 diamonds and 2 sticks, at half durability 1 diamond and 1 stick. A material that scales
// to less than one comes back as one, with a 25% chance. Under 5% durability it gives the salvage instead (its
// furnace nugget, or a stick). Items not in the map are smelted (iron sword -> iron nugget), or passed through.
// The drops go into the pedestal in the map's order; what doesn't fit is lost.
//
// Modifiers: speed; drop chance (+5% per level to every material chance, up to +50%, only while the item has at
// least a quarter of its durability); knowledge (+10% experience per level, up to double).
public class ItemUpgradeRecycler extends ItemUpgradeBase
{
    // Recipe experience below one point is kept here (1.20's _storedExpUnderOne), in hundredths
    private static final String XP_FRACTION = "xp_fraction";

    public ItemUpgradeRecycler(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.DROP_CHANCE, UpgradeModifiers.KNOWLEDGE);
    }

    @Override
    public boolean usesTransportModes()
    {
        return true;
    }

    private static boolean canProcess(ItemStack stack)
    {
        if (stack.isEmpty() || stack.is(PedestalTags.CANNOT_RECYCLE)) return false;
        return EnchantmentHelper.hasAnyEnchantments(stack) || materials(stack) != null
                || stack.has(DataComponents.EQUIPPABLE) || stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON);
    }

    private static @Nullable RecyclingMaterials materials(ItemStack stack)
    {
        return stack.typeHolder().getData(PedestalDataMaps.RECYCLING);
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        ResourceHandler<ItemResource> inventory = getInventoryBelow(level, pedestal);
        if (inventory == null) return;
        boolean items = canTransport(upgrade, PedestalTransportTypes.ITEMS.get());
        boolean experience = canTransport(upgrade, PedestalTransportTypes.EXPERIENCE.get());

        for (int index = 0; index < inventory.size(); index++)
        {
            ItemResource resource = inventory.getResource(index);
            if (resource.isEmpty()) continue;
            ItemStack input = resource.toStack(1);
            if (!canProcess(input) || !pedestal.machineFilterAllows(input)) continue;

            // Grindstone step: strip the enchantments, if the experience fits
            int points = 0;
            ItemStack item = input;
            if (experience)
            {
                int value = grindstoneExperience(level.getRandom(), input, getModifier(upgrade, UpgradeModifiers.KNOWLEDGE));
                if (value > 0 && experienceFits(pedestal, value))
                {
                    points = value;
                    item = removeNonCurses(input.copy());
                }
            }

            // Recycling step (books just come back as books)
            List<ItemStack> outputs = List.of(item);
            int recipeHundredths = 0;
            if (items && !item.is(Items.BOOK) && !item.is(Items.ENCHANTED_BOOK))
            {
                RecyclingMaterials materials = materials(item);
                if (materials != null)
                {
                    outputs = rollDrops(level, item, materials, getModifier(upgrade, UpgradeModifiers.DROP_CHANCE));
                }
                else
                {
                    Optional<RecipeHolder<? extends AbstractCookingRecipe>> recipe = findRecipe(level, item);
                    if (recipe.isPresent())
                    {
                        recipeHundredths = Math.round(recipe.get().value().experience() * 100);
                        outputs = List.of(recipe.get().value().assemble(new SingleRecipeInput(item)));
                    }
                }
            }
            else if (!items && points == 0)
            {
                continue; // nothing this mode can do with it
            }

            if (process(pedestal, inventory, index, resource, outputs, points))
            {
                if (recipeHundredths > 0) addRecipeExperience(pedestal, recipeHundredths);
                level.playSound(null, pedestal.getBlockPos(), points > 0 ? SoundEvents.GRINDSTONE_USE : SoundEvents.ANVIL_DESTROY,
                        SoundSource.BLOCKS, points > 0 ? 0.25F : 0.15F, 1.0F);
                return; // one item per operation
            }
        }
    }

    // In one transaction: take the item, put the outputs and experience in the pedestal.
    // Outputs go in order and whatever doesn't fit is lost (as the material generator does), but at least the first
    // has to fit, so nothing is destroyed while the pedestal is full. outputs may be empty (every roll failed).
    private static boolean process(BasePedestalBlockEntity pedestal, ResourceHandler<ItemResource> inventory, int index,
                                   ItemResource resource, List<ItemStack> outputs, int points)
    {
        try (Transaction transaction = Transaction.openRoot())
        {
            if (inventory.extract(index, resource, 1, transaction) != 1) return false;
            if (outputs.isEmpty())
            {
                // Still needs room for something, or a full pedestal would eat items with nothing to show
                if (ResourceHandlerUtil.isFull(pedestal.getItemStorage())) return false;
            }
            for (int output = 0; output < outputs.size(); output++)
            {
                ItemStack stack = outputs.get(output);
                if (stack.isEmpty()) continue;
                int inserted = ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(stack), stack.getCount(), transaction);
                if (output == 0 && inserted <= 0) return false;
            }
            if (points > 0 && pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).insert(ExperienceTransportType.EXPERIENCE, points, transaction) != points) return false;
            transaction.commit();
            return true;
        }
    }

    // ---- Grindstone ----

    // Vanilla's grindstone: each non-curse enchantment is worth its minimum enchanting cost, and the grindstone gives
    // between half and all of the total. Knowledge adds 10% per level.
    private static int grindstoneExperience(RandomSource random, ItemStack stack, int knowledge)
    {
        int total = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet())
        {
            if (!entry.getKey().is(EnchantmentTags.CURSE)) total += entry.getKey().value().getMinCost(entry.getIntValue());
        }
        if (total <= 0) return 0;
        int half = (int) Math.ceil(total / 2.0);
        int points = half + random.nextInt(half);
        return points * (100 + 10 * knowledge) / 100;
    }

    private static boolean experienceFits(BasePedestalBlockEntity pedestal, int points)
    {
        try (Transaction transaction = Transaction.openRoot())
        {
            return pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).insert(ExperienceTransportType.EXPERIENCE, points, transaction) == points;
        } // not committed: only a test
    }

    // GrindstoneMenu.removeNonCursesFrom: updateEnchantments changes the stored enchantments of books and the
    // enchantments of anything else; the repair cost drops to what the remaining curses need
    private static ItemStack removeNonCurses(ItemStack item)
    {
        ItemEnchantments remaining = EnchantmentHelper.updateEnchantments(item, enchantments -> enchantments.removeIf(enchantment -> !enchantment.is(EnchantmentTags.CURSE)));
        if (item.is(Items.ENCHANTED_BOOK) && remaining.isEmpty()) item = item.transmuteCopy(Items.BOOK);
        int repairCost = 0;
        for (int curse = 0; curse < remaining.size(); curse++) repairCost = AnvilMenu.calculateIncreasedRepairCost(repairCost);
        item.set(DataComponents.REPAIR_COST, repairCost);
        return item;
    }

    // ---- Recycling ----

    private static List<ItemStack> rollDrops(ServerLevel level, ItemStack item, RecyclingMaterials materials, int dropChance)
    {
        double durability = 1.0;
        if (item.isDamageableItem() && item.getMaxDamage() > 0)
        {
            durability = (double) (item.getMaxDamage() - item.getDamageValue()) / item.getMaxDamage();
        }

        List<ItemStack> drops = new ArrayList<>();
        if (durability < 0.05)
        {
            drops.add(salvage(level, item, materials));
            return drops;
        }

        RandomSource random = level.getRandom();
        int bonus = durability >= 0.25 ? 5 * dropChance : 0;
        for (ItemStackTemplate material : materials.results())
        {
            double share = material.count() * durability;
            int count = share >= 1 ? (int) share : 1;
            int chance = Math.min(100, (share >= 1 ? 50 : 25) + bonus);
            if (random.nextInt(100) < chance) drops.add(material.withCount(count).create());
        }
        return drops;
    }

    // An almost broken item: the map's salvage, else its furnace result (nuggets), else a stick
    private static ItemStack salvage(ServerLevel level, ItemStack item, RecyclingMaterials materials)
    {
        if (materials.salvage().isPresent()) return materials.salvage().get().create();
        Optional<RecipeHolder<? extends AbstractCookingRecipe>> recipe = findRecipe(level, item);
        if (recipe.isPresent())
        {
            ItemStack smelted = recipe.get().value().assemble(new SingleRecipeInput(item));
            if (!smelted.isEmpty()) return smelted;
        }
        return new ItemStack(Items.STICK);
    }

    // Blast furnace first (ores, tools, armour), then furnace
    private static Optional<RecipeHolder<? extends AbstractCookingRecipe>> findRecipe(ServerLevel level, ItemStack stack)
    {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        Optional<RecipeHolder<? extends AbstractCookingRecipe>> blasting = level.recipeAccess().getRecipeFor(RecipeType.BLASTING, input, level).map(holder -> holder);
        if (blasting.isPresent()) return blasting;
        return level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level).map(holder -> holder);
    }

    // Furnace recipes give fractions of a point: keep the remainder for next time
    private static void addRecipeExperience(BasePedestalBlockEntity pedestal, int hundredths)
    {
        int total = pedestal.getUpgradeData().getIntOr(XP_FRACTION, 0) + hundredths;
        int points = total / 100;
        if (points > 0)
        {
            try (Transaction transaction = Transaction.openRoot())
            {
                pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).insert(ExperienceTransportType.EXPERIENCE, points, transaction);
                transaction.commit();
            }
        }
        pedestal.getUpgradeData().putInt(XP_FRACTION, total % 100);
        pedestal.upgradeDataChanged();
    }
}
