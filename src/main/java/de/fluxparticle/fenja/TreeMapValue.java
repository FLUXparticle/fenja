package de.fluxparticle.fenja;

import com.ajjpj.afoundation.collection.immutable.AMapEntry;
import com.ajjpj.afoundation.collection.immutable.ARedBlackTreeMap;
import com.ajjpj.afoundation.collection.immutable.ASortedMap;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import nz.sodium.Operational;
import org.apache.commons.beanutils.BeanUtils;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;

/**
 * Created by sreinck on 16.06.16.
 */
public class TreeMapValue<K extends Comparable<K>, V> extends Value<ASortedMap<K, V>> {

    private ObservableValuesList observableValuesList;

    private ObservableEntriesList observableEntriesList;

    public static Integer nextIndex(ASortedMap<Integer, ?> map) {
        return map.last().map(entry -> entry.getKey() + 1).getOrElse(0);
    }

    private static <K extends Comparable<K>, V> ASortedMap<K, V> empty() {
        return ARedBlackTreeMap.empty(K::compareTo);
    }

    private static <K extends Comparable<K>, V> ASortedMap<K, V> copy(Map<K, V> orig) {
        ASortedMap<K, V> result = empty();

        for (Entry<K, V> entry : orig.entrySet()) {
            result = result.updated(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static <K extends Comparable<K>, V> ASortedMap<K, V> apply(Map<K, V> change, ASortedMap<K, V> map) {
        for (Entry<K, V> entry : change.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();

            if (value == null) {
                map = map.removed(key);
            } else if (value instanceof Map) {
                V oldValue = map.getRequired(key);
                V newValue = patchObject(oldValue, (Map) value);
                map = map.updated(key, newValue);
            } else {
                map = map.updated(key, value);
            }
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    private static <V> V patchObject(V oldValue, Map map) {
        try {
            Object cloneValue = BeanUtils.cloneBean(oldValue);
            BeanUtils.populate(cloneValue, (Map<String, ?>) map);
            return (V) cloneValue;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final EventStream<Map<K, V>> sChanges;

    public TreeMapValue(EventStream<Map<K, V>> sChanges, Map<K, V> init) {
        this(sChanges, copy(init));
    }

    private TreeMapValue(EventStream<Map<K, V>> sChanges, ASortedMap<K, V> init) {
        super(sChanges.stream.accum(init, TreeMapValue::apply));
        this.sChanges = sChanges;
    }

    public TreeMapValue<K, V> filterByValue(Predicate<V> predicate) {
        EventStream<Map<K, V>> sFilteredChanges = new EventStream<>(
                sChanges.stream.map(change -> filterChangeByValue(predicate, change))
        );

        // -----

        ASortedMap<K, V> init = empty();

        for (AMapEntry<K, V> entry : cell.sample()) {
            if (predicate.test(entry.getValue())) {
                init = init.updated(entry.getKey(), entry.getValue());
            }
        }

        // -----

        return new TreeMapValue<>(sFilteredChanges, init);
    }

    public ObservableList<V> valuesAsList() {
        if (observableValuesList == null) {
            observableValuesList = new ObservableValuesList();
        }
        return observableValuesList;
    }

    public ObservableList<Entry<K, V>> entriesAsList() {
        if (observableEntriesList == null) {
            observableEntriesList = new ObservableEntriesList();
        }
        return observableEntriesList;
    }

    private AMapEntry<K, V> getEntry(int index) {
        Iterator<AMapEntry<K, V>> iterator = cell.sample().iterator();

        int count = index;
        while (count >= 0 && iterator.hasNext()) {
            AMapEntry<K, V> cur = iterator.next();
            if (count == 0) {
                return cur;
            }
            count--;
        }

        throw new NoSuchElementException("index=" + index);
    }

    private static <K, V> int getIndex(ASortedMap<K, V> map, K key) {
        int index = 0;
        for (AMapEntry<K, V> entry : map.toE(key)) {
            index++;
        }

        return index;
    }

    private static <K extends Comparable<K>, V> Map<K, V> filterChangeByValue(Predicate<V> predicate, Map<K, V> change) {
        Map<K, V> result = new HashMap<>();

        for (Entry<K, V> entry : change.entrySet()) {
            V value = entry.getValue();
            if (value != null && !predicate.test(value)) {
                value = null;
            }
            result.put(entry.getKey(), value);
        }

        return result;
    }

    private abstract class MyObservableListBase<T> extends ObservableListBase<T> {

        {
            Operational.defer(sChanges.stream.map(changes -> {
                ASortedMap<K, V> oldMap = cell.sample();

                Collection<Entry<Integer, Integer>> changeList = new ArrayList<>();

                for (Entry<K, V> entry : changes.entrySet()) {
                    K key = entry.getKey();
                    boolean exists = oldMap.containsKey(key);
                    boolean delete = entry.getValue() == null;

                    if (delete) {
                        if (exists) {
                            int index = getIndex(oldMap, key);
                            changeList.add(new SimpleImmutableEntry<>(index, -1));
                            oldMap = oldMap.removed(key);
                        }
                    } else {
                        if (exists) {
                            int index = getIndex(oldMap, key);
                            changeList.add(new SimpleImmutableEntry<>(index, 0));
                        } else {
                            oldMap = oldMap.updated(key, null);
                            int index = getIndex(oldMap, key);
                            changeList.add(new SimpleImmutableEntry<>(index, +1));
                        }
                    }
                }

                return changeList;
            })).listen(changeList -> {
                beginChange();
                for (Entry<Integer, Integer> entry : changeList) {
                    int index = entry.getKey();
                    int op = entry.getValue();
                    if (op < 0) {
                        nextRemove(index, (T) null);
                    } else if (op > 0) {
                        nextAdd(index, index + 1);
                    } else {
                        nextUpdate(index);
                    }
                }
                endChange();
            });
        }

        @Override
        public int size() {
            return cell.sample().size();
        }

    }

    private class ObservableEntriesList extends MyObservableListBase<Entry<K, V>> {

        @Override
        public Entry<K, V> get(int index) {
            AMapEntry<K, V> aMapEntry = getEntry(index);
            return new SimpleImmutableEntry<>(aMapEntry.getKey(), aMapEntry.getValue());
        }

    }

    private class ObservableValuesList extends MyObservableListBase<V> {

        @Override
        public V get(int index) {
            return getEntry(index).getValue();
        }

    }

}
