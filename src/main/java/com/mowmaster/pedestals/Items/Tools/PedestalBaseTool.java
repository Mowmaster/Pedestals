package com.mowmaster.pedestals.Items.Tools;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseFilterableBlockEntity;
import com.mowmaster.mowlib.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.world.entity.player.Player;

public class PedestalBaseTool extends BaseTool {
    public PedestalBaseTool(Properties p_41383_) {
        super(p_41383_);
    }

    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {
        getBlockEntityDetailBase(pedestal,player);
    }
}
