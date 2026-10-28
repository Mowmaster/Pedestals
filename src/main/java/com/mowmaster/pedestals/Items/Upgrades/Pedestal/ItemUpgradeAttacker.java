package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

// Attacks every mob in the work card's area each operation, as a player swinging the pedestal's tool (a stick if
// none is in): the tool's damage, enchantments, sweeping and wear all work like a player's attack. The damage
// modifier adds that much damage. The block under the pedestal can limit which mobs (see EntityTargets). Players
// are never attacked.
public class ItemUpgradeAttacker extends ItemUpgradeBase
{
    private static final Identifier BONUS_DAMAGE = Identifier.fromNamespaceAndPath(PedestalReferences.MODID, "attacker_damage_modifier");

    public ItemUpgradeAttacker(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.DAMAGE_CAPACITY, UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
    }

    @Override
    public boolean canModify(ItemStack upgrade, String modifier)
    {
        if (modifier.equals(UpgradeModifiers.AREA)) return Config.REQUIRE_SIZED_AREA.getAsBoolean();
        return super.canModify(upgrade, modifier);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.AREA;
    }

    @Override
    public ItemStack getDefaultTool()
    {
        return new ItemStack(Items.STICK);
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.ATTACKER_FUEL;
    }

    @Override
    protected @Nullable Config.ToolRules getToolRules()
    {
        return Config.ATTACKER_TOOLS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Optional<AABB> area = pedestal.getWorkArea();
        if (area.isEmpty() || !canUseTool(level, pedestal)) return;
        Predicate<Entity> targets = EntityTargets.forPedestal(level, pedestal);

        PedestalFakePlayer player = playerWithTool(level, pedestal);
        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        int bonus = getModifier(upgrade, UpgradeModifiers.DAMAGE_CAPACITY);
        if (damage != null && bonus > 0) damage.addOrUpdateTransientModifier(new AttributeModifier(BONUS_DAMAGE, bonus, AttributeModifier.Operation.ADD_VALUE));
        try
        {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area.get(), entity -> entity.isAlive() && !(entity instanceof Player)))
            {
                if (!targets.test(target)) continue;
                int distance = distance(pedestal.getBlockPos(), target.blockPosition());
                if (!useFuel(pedestal, distance, true)) return;
                if (wearsTool(pedestal) && pedestal.getTool().isDamageableItem()
                        && pedestal.getTool().getMaxDamage() - pedestal.getTool().getDamageValue() <= 1) return;
                // Turn to face it, so knockback and sweeping go the right way
                player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
                player.attack(target);
                useFuel(pedestal, distance, false);
            }
        }
        finally
        {
            if (damage != null) damage.removeModifier(BONUS_DAMAGE);
            finishWithTool(pedestal, player);
        }
    }
}
