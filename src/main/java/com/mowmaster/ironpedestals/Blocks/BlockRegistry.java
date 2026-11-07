package com.mowmaster.ironpedestals.Blocks;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.ironpedestals.Blocks.Pedestal.PedestalBlockItem;
import com.mowmaster.ironpedestals.Blocks.Pedestal.PedestalTier;
import com.mowmaster.ironpedestals.Items.ItemRegistry;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class BlockRegistry
{
    public static final DeferredRegister.Blocks BCBLOCKS = DeferredRegister.createBlocks(IronPedestalReferences.MODID);

    // ---- Pedestals: one block per tier (see PedestalTier). Tier 0 keeps 1.20's id, "block_pedestal". ----
    public static final DeferredBlock<BasePedestalBlock> BLOCK_PEDESTAL = registerPedestal(PedestalTier.BASE);
    public static final DeferredBlock<BasePedestalBlock> BLOCK_PEDESTAL_COPPER = registerPedestal(PedestalTier.COPPER);
    public static final DeferredBlock<BasePedestalBlock> BLOCK_PEDESTAL_IRON = registerPedestal(PedestalTier.IRON);
    public static final DeferredBlock<BasePedestalBlock> BLOCK_PEDESTAL_GOLD = registerPedestal(PedestalTier.GOLD);
    public static final DeferredBlock<BasePedestalBlock> BLOCK_PEDESTAL_DIAMOND = registerPedestal(PedestalTier.DIAMOND);
    public static final DeferredBlock<BasePedestalBlock> BLOCK_PEDESTAL_NETHERITE = registerPedestal(PedestalTier.NETHERITE);
    public static final DeferredBlock<BasePedestalBlock> BLOCK_PEDESTAL_NETHERSTAR = registerPedestal(PedestalTier.NETHERSTAR);

    // Every pedestal, tier 0 first (the block entity type and the creative tab use this)
    public static final List<DeferredBlock<BasePedestalBlock>> PEDESTALS = List.of(BLOCK_PEDESTAL, BLOCK_PEDESTAL_COPPER,
            BLOCK_PEDESTAL_IRON, BLOCK_PEDESTAL_GOLD, BLOCK_PEDESTAL_DIAMOND, BLOCK_PEDESTAL_NETHERITE, BLOCK_PEDESTAL_NETHERSTAR);

    // lightLevel replaces 1.20's getLightEmission override: full light while glowstone is in it.
    // The item is a PedestalBlockItem (the tier's numbers in its tooltip).
    private static DeferredBlock<BasePedestalBlock> registerPedestal(PedestalTier tier)
    {
        DeferredBlock<BasePedestalBlock> block = BCBLOCKS.registerBlock(tier.id(),
                properties -> new BasePedestalBlock(properties.strength(2.0f).sound(SoundType.STONE).noOcclusion()
                        .lightLevel(state -> state.getValue(BasePedestalBlock.LIT) ? 15 : 0), tier));
        ItemRegistry.PEDESTAL_ITEMS.registerItem(tier.id(), properties -> new PedestalBlockItem(block.get(), properties.useBlockDescriptionPrefix()));
        return block;
    }

    // ---- Books: items per page by cover (as in 1.20), 8 pages unless the recipe gives more ----

    // Books can't be mined (strength -1, like bedrock; left-click takes items out instead) or blown up,
    // aren't pushed by pistons, and don't hide the faces of blocks next to them (noOcclusion)
    private static BlockBehaviour.Properties bookProperties(BlockBehaviour.Properties properties, SoundType sound)
    {
        return properties.strength(-1f, 3600000f).sound(sound).noOcclusion().pushReaction(PushReaction.PUSH_PULL);
    }

    public static void register(IEventBus eventBus)
    {
        BCBLOCKS.register(eventBus);
    }
}
