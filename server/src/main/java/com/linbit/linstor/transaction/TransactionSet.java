package com.linbit.linstor.transaction;

import com.linbit.NoOpSetDatabaseDriver;
import com.linbit.SetDatabaseDriver;

import javax.inject.Provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class TransactionSet<PARENT, VALUE extends TransactionObject>
    extends AbsTransactionObject implements Set<VALUE>
{
    private final PARENT parent;
    private final SetDatabaseDriver<PARENT, VALUE> dbDriver;
    private final Set<VALUE> backingSet;
    private final Set<VALUE> oldValues;

    public TransactionSet(
        PARENT parentRef,
        Set<VALUE> backingSetRef,
        SetDatabaseDriver<PARENT, VALUE> driver,
        Provider<TransactionMgr> transMgrProviderRef
    )
    {
        super(transMgrProviderRef);
        parent = parentRef;
        backingSet = backingSetRef;
        if (driver == null)
        {
            dbDriver = new NoOpSetDatabaseDriver<>();
        }
        else
        {
            dbDriver = driver;
        }

        oldValues = new HashSet<>();
    }

    @Override
    protected void postSetConnection(TransactionMgr transMgrRef)
    {
        // forward transaction manager to values
        backingSet.forEach(to -> to.setConnection(transMgrRef));
    }

    @Override
    public boolean isDirty()
    {
        return !oldValues.isEmpty();
    }

    @Override
    protected void commitImpl()
    {
        assert (TransactionMgr.isCalledFromTransactionMgr("commit"));
        oldValues.clear();
    }

    @Override
    protected void rollbackImpl()
    {
        assert (TransactionMgr.isCalledFromTransactionMgr("rollback"));
        backingSet.clear();
        backingSet.addAll(oldValues);
        oldValues.clear();
    }

    @Override
    public int size()
    {
        return backingSet.size();
    }

    @Override
    public boolean isEmpty()
    {
        return backingSet.isEmpty();
    }

    @Override
    public boolean contains(Object obj)
    {
        return backingSet.contains(obj);
    }

    @Override
    public Iterator<VALUE> iterator()
    {
        // workaround to prevent the call of iterator.remove()
        final Iterator<VALUE> it = backingSet.iterator();
        return new Iterator<VALUE>()
        {
            @Override
            public boolean hasNext()
            {
                return it.hasNext();
            }

            @Override
            public VALUE next()
            {
                return it.next();
            }
        };
    }

    @Override
    public Object[] toArray()
    {
        Object[] array = backingSet.toArray();
        return Arrays.copyOf(array, array.length);
    }

    @Override
    public <T> T[] toArray(T[] arr)
    {
        return backingSet.toArray(arr);
    }

    @Override
    public boolean add(VALUE element)
    {
        boolean ret = backingSet.add(element);
        dbDriver.insert(parent, element);
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object obj)
    {
        boolean ret = backingSet.remove(obj);
        if (ret) // also prevents class cast exception
        {
            dbDriver.remove(parent, (VALUE) obj);
        }
        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> coll)
    {
        Objects.requireNonNull(coll);
        return backingSet.containsAll(coll);
    }

    @Override
    public boolean addAll(Collection<? extends VALUE> coll)
    {
        Objects.requireNonNull(coll);

        boolean modified = false;
        for (VALUE val : coll)
        {
            modified |= add(val);
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> coll)
    {
        Objects.requireNonNull(coll);

        HashSet<VALUE> toRemove = new HashSet<>();
        // prevent concurrentModificationException
        for (VALUE val : backingSet)
        {
            if (!coll.contains(val))
            {
                toRemove.add(val);
            }
        }

        for (VALUE val : toRemove)
        {
            remove(val);
        }

        return !toRemove.isEmpty();
    }

    @Override
    public boolean removeAll(Collection<?> coll)
    {
        Objects.requireNonNull(coll);

        boolean modified = false;
        for (Object val : coll)
        {
            modified |= remove(val);
        }
        return modified;
    }

    @Override
    public void clear()
    {
        HashSet<VALUE> copy = new HashSet<>(backingSet);
        for (VALUE val : copy)
        {
            remove(val);
        }
    }
}
