package com.mowmaster.pedestals.Capability.Experience;

public interface IExperienceStorage
{
    /**
     * Adds experience to the storage. Returns quantity of experience that was accepted.
     *
     * @param maxReceive
     *            Maximum amount of experience to be inserted.
     * @param simulate
     *            If TRUE, the insertion will only be simulated.
     * @return Amount of experience that was (or would have been, if simulated) accepted by the storage.
     */
    int receiveExperience(int maxReceive, boolean simulate);

    /**
     * Removes experience from the storage. Returns quantity of experience that was removed.
     *
     * @param maxExtract
     *            Maximum amount of experience to be extracted.
     * @param simulate
     *            If TRUE, the extraction will only be simulated.
     * @return Amount of experience that was (or would have been, if simulated) extracted from the storage.
     */
    int extractExperience(int maxExtract, boolean simulate);

    /**
     * Returns the amount of experience currently stored.
     */
    int getExperienceStored();

    /**
     * Returns the maximum amount of experience that can be stored.
     */
    int getMaxExperienceStored();

    /**
     * Returns if this storage can have experience extracted.
     * If this is false, then any calls to extractExperience will return 0.
     */
    boolean canExtract();

    /**
     * Used to determine if this storage can receive experience.
     * If this is false, then any calls to receiveExperience will return 0.
     */
    boolean canReceive();
}
