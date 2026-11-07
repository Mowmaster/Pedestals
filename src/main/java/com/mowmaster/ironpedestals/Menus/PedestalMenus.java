package com.mowmaster.ironpedestals.Menus;

import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Menu types. IMenuTypeExtension.create makes a menu type whose client side gets extra data from the server when
// it opens (here: the pedestal's position, written by player.openMenu(provider, pos)). The screen for it is
// registered on the client (IronPedestalsClient, RegisterMenuScreensEvent).
public class PedestalMenus
{
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, IronPedestalReferences.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PedestalMenu>> PEDESTAL =
            MENUS.register("pedestal", () -> IMenuTypeExtension.create(PedestalMenu::fromNetwork));

    public static void register(IEventBus eventBus)
    {
        MENUS.register(eventBus);
    }
}
