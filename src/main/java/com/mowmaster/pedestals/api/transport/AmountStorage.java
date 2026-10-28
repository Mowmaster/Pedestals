package com.mowmaster.pedestals.api.transport;

import com.mowmaster.pedestals.api.filter.FilterDirection;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

// A storage that only holds an amount (energy, experience, or another mod's amount-only type), with transaction
// support and the pedestal's filter applied. Subclasses give the capacity and what happens on removal.
public abstract class AmountStorage implements PedestalStorage<AmountResource>
{
    protected final PedestalStorageHost host;
    protected final PedestalTransportType<AmountResource> type;
    protected final AmountResource resource;
    protected int amount;

    // Transactions: before the first change inside a transaction this remembers the old amount, so a rollback
    // (transaction closed without commit) can put it back. Same pattern as NeoForge's SimpleEnergyHandler.
    private final SnapshotJournal<Integer> journal = new SnapshotJournal<>()
    {
        @Override
        protected Integer createSnapshot()
        {
            return amount;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot)
        {
            amount = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState)
        {
            if (amount != originalState) host.onStorageChanged(type);
        }
    };

    protected AmountStorage(PedestalStorageHost host, PedestalTransportType<AmountResource> type, AmountResource resource)
    {
        this.host = host;
        this.type = type;
        this.resource = resource;
    }

    // Before the filter's limit
    public abstract int getCapacity();

    public int getAmount()
    {
        return amount;
    }

    // The capacity the pedestal's filter allows
    protected int getAllowedCapacity()
    {
        return host.filterLimit(type, resource, getCapacity());
    }

    // ---- ResourceHandler: one index, holding this type's single resource ----

    @Override
    public int size()
    {
        return 1;
    }

    @Override
    public AmountResource getResource(int index)
    {
        return amount > 0 ? resource : AmountResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index)
    {
        return amount;
    }

    @Override
    public long getCapacityAsLong(int index, AmountResource resource)
    {
        return getAllowedCapacity();
    }

    @Override
    public boolean isValid(int index, AmountResource resource)
    {
        return this.resource.equals(resource) && host.filterAllows(type, resource, FilterDirection.INSERT);
    }

    @Override
    public int insert(int index, AmountResource resource, int amount, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!isValid(index, resource)) return 0;

        int inserted = Math.min(amount, Math.max(0, getAllowedCapacity() - this.amount));
        if (inserted > 0)
        {
            journal.updateSnapshots(transaction);
            this.amount += inserted;
        }
        return inserted;
    }

    @Override
    public int extract(int index, AmountResource resource, int amount, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!this.resource.equals(resource) || !host.filterAllows(type, resource, FilterDirection.EXTRACT)) return 0;

        int extracted = Math.min(amount, this.amount);
        if (extracted > 0)
        {
            journal.updateSnapshots(transaction);
            this.amount -= extracted;
        }
        return extracted;
    }

    // ---- Saving ----

    @Override
    public void serialize(ValueOutput output)
    {
        if (amount > 0) output.putInt("amount", amount);
    }

    @Override
    public void deserialize(ValueInput input)
    {
        amount = Math.max(0, input.getIntOr("amount", 0));
    }
}
