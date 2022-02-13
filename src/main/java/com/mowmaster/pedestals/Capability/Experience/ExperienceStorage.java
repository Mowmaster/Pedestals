package com.mowmaster.pedestals.Capability.Experience;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public class ExperienceStorage implements IExperienceStorage, INBTSerializable<Tag>
{
    protected int experience;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public ExperienceStorage(int capacity)
    {
        this(capacity, capacity, capacity, 0);
    }

    public ExperienceStorage(int capacity, int maxTransfer)
    {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    public ExperienceStorage(int capacity, int maxReceive, int maxExtract)
    {
        this(capacity, maxReceive, maxExtract, 0);
    }

    public ExperienceStorage(int capacity, int maxReceive, int maxExtract, int energy)
    {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.experience = Math.max(0 , Math.min(capacity, energy));
    }

    @Override
    public int receiveExperience(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int experienceReceived = Math.min(capacity - experience, Math.min(this.maxReceive, maxReceive));
        if (!simulate)
            experience += experienceReceived;
        return experienceReceived;
    }

    @Override
    public int extractExperience(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        int experienceExtracted = Math.min(experience, Math.min(this.maxExtract, maxExtract));
        if (!simulate)
            experience -= experienceExtracted;
        return experienceExtracted;
    }

    @Override
    public int getExperienceStored() {
        return experience;
    }

    @Override
    public int getMaxExperienceStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    @Override
    public Tag serializeNBT() {
        return IntTag.valueOf(this.getExperienceStored());
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (!(nbt instanceof IntTag intNbt))
            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
        this.experience = intNbt.getAsInt();
    }
}
