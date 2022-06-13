package com.mowmaster.pedestals.Items.Augments;

public class AugmentTieredRange extends AugmentBase{

    private int rangeAmount;
    private int allowedToInsert;
    public AugmentTieredRange(Properties p_41383_, int amount, int allowedInsert) {
        super(p_41383_);
        this.rangeAmount = amount;
        this.allowedToInsert = allowedInsert;
    }

    public int getRangeIncreasePerItem()
    {
        return rangeAmount;
    }

    public int getAllowedInsertAmount()
    {
        return allowedToInsert;
    }


}
