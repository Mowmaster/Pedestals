package com.mowmaster.pedestals.Registry;

import com.mowmaster.pedestals.PedestalUtils.References;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DeferredCreativeTabRegistry
{
    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, References.MODID);

    public static final RegistryObject<CreativeModeTab> TAB = DEF_REG.register(References.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + References.MODID))
            .icon(() -> makeIcon())
            .displayItems((enabledFeatures, output) -> {
                for(RegistryObject<Item> item : DeferredRegisterItems.ITEMS.getEntries()){
                    output.accept(item.get());
                }
            })
            .build());

    private static ItemStack makeIcon() {
        ItemStack stack = new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt("3DRender", 1);
        stack.setTag(tag);
        return stack;
    }
}
