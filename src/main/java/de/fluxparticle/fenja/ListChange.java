package de.fluxparticle.fenja;

import com.ajjpj.afoundation.collection.immutable.ASortedMap;

import java.util.Optional;
import java.util.function.Predicate;

public final class ListChange<T> {

    public static Integer nextIndex(ASortedMap<Integer, ?> map) {
        return map.last().map(entry -> entry.getKey() + 1).getOrElse(0);
    }

    private final Integer index;

    private final T data;

    public ListChange(Integer index, T data) {
        this.index = index;
        this.data = data;
    }

    public ASortedMap<Integer, T> applyTo(ASortedMap<Integer, T> map) {
        if (index == null) {
            Integer nextIndex = nextIndex(map);
            return map.updated(nextIndex, data);
        } else if (data == null) {
            return map.removed(index);
        } else {
            return map.updated(index, data);
        }
    }

    public Optional<ListChange<T>> filter(Predicate<T> p) {
        if (index == null) {
            return p.test(data) ? Optional.of(this) : Optional.empty();
        } else if (data == null) {
            return Optional.of(this);
        } else {
            return p.test(data) ? Optional.of(this) : Optional.of(new ListChange<>(index, null));
        }
    }

    public void accept(ListChangeListener<T> visitor) {
        visitor.visitUpdate(index, data);
    }

}
