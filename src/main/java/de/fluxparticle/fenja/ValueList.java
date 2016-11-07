package de.fluxparticle.fenja;

import com.ajjpj.afoundation.collection.immutable.AMapEntry;
import com.ajjpj.afoundation.collection.immutable.ARedBlackTreeMap;
import com.ajjpj.afoundation.collection.immutable.ASortedMap;
import javafx.collections.ObservableListBase;
import nz.sodium.Operational;
import nz.sodium.Stream;
import nz.sodium.Transaction;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class ValueList<T> extends ObservableListBase<T> {

    private static <T> ARedBlackTreeMap<Integer, T> empty() {
        return ARedBlackTreeMap.empty(Integer::compareTo);
    }

    @SafeVarargs
    private static <T> ARedBlackTreeMap<Integer, T> filled(T... values) {
        ARedBlackTreeMap<Integer, T> result = empty();

        for (T value : values) {
            Integer nextIndex = ListChange.nextIndex(result);
            result = result.updated(nextIndex, value);
        }

        return result;
    }

    private final Value<ASortedMap<Integer, T>> values;

    private final EventStream<ListChange<T>> sChanges;

    @SafeVarargs
    public ValueList(EventStream<ListChange<T>> sChanges, T... init) {
        this(sChanges, filled(init));
    }

    private ValueList(EventStream<ListChange<T>> sChanges, ASortedMap<Integer, T> init) {
        this.sChanges = sChanges;

        values = Transaction.run(() -> {
            ValueLoop<ASortedMap<Integer, T>> valueLoop = new ValueLoop<>();
            valueLoop.loop(sChanges.snapshot(valueLoop, ListChange::applyTo).hold(init));

            Operational.defer(sChanges.stream).listen(lc -> {
                beginChange();
                lc.accept((index, data) -> {
                    if (index == null) {
                        int size = size();
                        nextAdd(size - 1, size);
                    } else if (data == null) {
                        nextRemove(index, (T) null);
                    } else {
                        nextUpdate(index);
                    }
                });
                endChange();
            });

            return valueLoop;
        });
    }

    public ValueList<T> filter(Predicate<T> predicate) {
        EventStream<ListChange<T>> sChanges = new EventStream<>(Stream.filterOptional(this.sChanges.stream.map(change -> change.filter(predicate))));

        // -----

        ASortedMap<Integer, T> init = empty();

        for (AMapEntry<Integer, T> entry : values.cell.sample()) {
            if (predicate.test(entry.getValue())) {
                init = init.updated(entry.getKey(), entry.getValue());
            }
        }

        // -----

        return new ValueList<>(sChanges, init);
    }

    public Integer getSourceIndex(int index) {
        return getEntry(index).getKey();
    }

    @Override
    public T get(int index) {
        return getEntry(index).getValue();
    }

    @Override
    public int size() {
        return values.sample().size();
    }

    private AMapEntry<Integer, T> getEntry(int index) {
        Iterator<AMapEntry<Integer, T>> iterator = values.sample().iterator();

        int count = index;
        while (count >= 0 && iterator.hasNext()) {
            AMapEntry<Integer, T> cur = iterator.next();
            if (count == 0) {
                return cur;
            }
            count--;
        }

        throw new NoSuchElementException("index=" + index);
    }

}
