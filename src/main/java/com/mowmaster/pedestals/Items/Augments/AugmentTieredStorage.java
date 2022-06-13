package com.mowmaster.pedestals.Items.Augments;

public class AugmentTieredStorage extends AugmentBase{

    private int itemAmount;
    private int fluidAmount;
    private int energyAmount;
    private int experienceAmount;
    private int allowedToInsert;
    public AugmentTieredStorage(Properties p_41383_, int amountStacks, int amountFluids, int amountEnergy, int amountXPLevels, int allowedInsert) {
        super(p_41383_);
        this.itemAmount = amountStacks;
        this.fluidAmount = amountFluids;
        this.energyAmount = amountEnergy;
        this.experienceAmount = amountXPLevels;
        this.allowedToInsert = allowedInsert;
    }

    public int getAdditionalItemStoragePerItem()
    {
        return itemAmount;
    }
    public int getAdditionalFluidStoragePerItem()
    {
        return fluidAmount;
    }
    public int getAdditionalEnergyStoragePerItem()
    {
        return energyAmount;
    }
    public int getAdditionalXpStoragePerItem()
    {
        return experienceAmount;
    }

    public int getAllowedInsertAmount()
    {
        return allowedToInsert;
    }


}
