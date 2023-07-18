package com.mowmaster.pedestals.items.tools.upgrade;

import com.mowmaster.pedestals.items.tools.BaseTool;
import com.mowmaster.pedestals.items.tools.IPedestalTool;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.world.item.ItemStack;

public class WorkCardTool extends BaseTool implements IPedestalTool
{
    public WorkCardTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_WORKTOOL.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_TAGTOOL.get().getDefaultInstance(); }
}
