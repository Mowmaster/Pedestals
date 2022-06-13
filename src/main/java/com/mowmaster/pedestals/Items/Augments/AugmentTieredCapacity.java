package com.mowmaster.pedestals.Items.Augments;

public class AugmentTieredCapacity extends AugmentBase{

    private int itemAmount;
    private int fluidAmount;
    private int energyAmount;
    private int experienceAmount;
    private int allowedToInsert;
    public AugmentTieredCapacity(Properties p_41383_, int amountItems, int amountFluids, int amountEnergy, int amountXPLevels, int allowedInsert) {
        super(p_41383_);
        this.itemAmount = amountItems;
        this.fluidAmount = amountFluids;
        this.energyAmount = amountEnergy;
        this.experienceAmount = amountXPLevels;
        this.allowedToInsert = allowedInsert;
    }

    public int getAdditionalItemTransferRatePerItem()
    {
        return itemAmount;
    }
    public int getAdditionalFluidTransferRatePerItem()
    {
        return fluidAmount;
    }
    public int getAdditionalEnergyTransferRatePerItem()
    {
        return energyAmount;
    }
    public int getAdditionalXpTransferRatePerItem()
    {
        return experienceAmount;
    }

    public int getAllowedInsertAmount()
    {
        return allowedToInsert;
    }


}
