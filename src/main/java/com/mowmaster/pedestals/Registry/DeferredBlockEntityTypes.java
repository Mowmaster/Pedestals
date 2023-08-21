package com.mowmaster.pedestals.Registry;


import com.mowmaster.pedestals.Blocks.Pedestal.CatStatuePedestal.CatStatuePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.ColorablePedestal.ColorablePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.GoblinStatuePedestal.GoblinStatuePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.RatStatuePedestal.RatStatuePedestalBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraftforge.registries.RegistryObject;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class DeferredBlockEntityTypes
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final RegistryObject<BlockEntityType<ColorablePedestalBlockEntity>> PEDESTAL = BLOCK_ENTITIES.register(
            "block_entity_pedestal",
            () -> BlockEntityType.Builder.of(ColorablePedestalBlockEntity::new, DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<RatStatuePedestalBlockEntity>> RATSTATUE_PEDESTAL = BLOCK_ENTITIES.register(
            "block_entity_ratstatue_pedestal",
            () -> BlockEntityType.Builder.of(RatStatuePedestalBlockEntity::new, DeferredRegisterTileBlocks.BLOCK_RATSTATUE_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<CatStatuePedestalBlockEntity>> CATSTATUE_PEDESTAL = BLOCK_ENTITIES.register(
            "block_entity_catstatue_pedestal",
            () -> BlockEntityType.Builder.of(CatStatuePedestalBlockEntity::new, DeferredRegisterTileBlocks.BLOCK_CATSTATUE_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<GoblinStatuePedestalBlockEntity>> GOBLINSTATUE_PEDESTAL = BLOCK_ENTITIES.register(
            "block_entity_goblinstatue_pedestal",
            () -> BlockEntityType.Builder.of(GoblinStatuePedestalBlockEntity::new, DeferredRegisterTileBlocks.BLOCK_GOBLINSTATUE_PEDESTAL.get()).build(null));

    private DeferredBlockEntityTypes() {
    }
}
