package com.mowmaster.pedestals.Items.Augments;

public class AugmentTieredSpeed extends AugmentBase{

    private int ticksReduced;
    private int allowedToInsert;
    public AugmentTieredSpeed(Properties p_41383_, int amount, int allowedInsert) {
        super(p_41383_);
        this.ticksReduced = amount;
        this.allowedToInsert = allowedInsert;
    }

    public int getTicksReducedPerItem()
    {
        return ticksReduced;
    }

    public int getAllowedInsertAmount()
    {
        return allowedToInsert;
    }


}
