package com.mowmaster.pedestals.Items.Tools.Upgrade;

import com.mowmaster.pedestals.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Items.Tools.IPedestalTool;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.world.item.ItemStack;

public class WorkCardTool extends BaseTool implements IPedestalTool {
    public WorkCardTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_WORKTOOL.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_TAGTOOL.get().getDefaultInstance(); }
}
