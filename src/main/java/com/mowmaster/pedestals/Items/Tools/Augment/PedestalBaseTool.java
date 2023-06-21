package com.mowmaster.pedestals.Items.Tools.Augment;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseFilterableBlockEntity;
import com.mowmaster.mowlib.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.world.entity.player.Player;

public class PedestalBaseTool extends BaseTool {
    public PedestalBaseTool(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void getBlockEntityDetail(MowLibBaseFilterableBlockEntity baseFilterableBlockEntity, Player player) {
        super.getBlockEntityDetail(baseFilterableBlockEntity, player);
    }

    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {
        super.getBlockEntityDetail(pedestal, player);
    }


}
