package de.fluxparticle.fenja.operation.algorithm;

import de.fluxparticle.fenja.operation.BuildingListOperationHandler;
import de.fluxparticle.fenja.operation.ListOperation;
import de.fluxparticle.fenja.operation.algorithm.PositionTracker.RelativePosition;
import kotlin.Pair;
import kotlin.sequences.Sequence;

import java.util.Iterator;

/**
 * A utility class for transforming an insertion operation with an
 * insertion-free operation.
 *
 * @author Alexandre Mah
 */
final class InsertionNonInsertionTransformer<T> {

    /**
     * A cache for the effect of a component of a document mutation that affects a
     * range of the document.
     */
    private static abstract class RangeCache {

        abstract void resolve(int retain);

    }

    /**
     * A target of a document mutation which can be used to transform document
     * mutations by making use primarily of information from one mutation with the
     * help of auxiliary information from a second mutation. These targets should
     * be used in pairs.
     */
    private static abstract class Target<T> implements BuildingListOperationHandler<T, Sequence<ListOperation<T>>> {

        /**
         * The target to which to write the transformed mutation.
         */
        final ListOperationSequenceBuilder<T> targetDocument;

        /**
         * The position of the processing cursor associated with this target
         * relative to the position of the processing cursor associated to the
         * opposing target. All positional calculations are based on cursor
         * positions in the original document on which the two original operations
         * apply.
         */
        final RelativePosition relativePosition;

        Target(ListOperationSequenceBuilder<T> targetDocument, RelativePosition relativePosition) {
            this.targetDocument = targetDocument;
            this.relativePosition = relativePosition;
        }

        @Override
        public Sequence<ListOperation<T>> build() {
            return targetDocument.build();
        }

    }

    private static final class InsertionTarget<T> extends Target<T> {

        NonInsertionTarget<T> otherTarget;

        InsertionTarget(RelativePosition relativePosition) {
            super(new ListOperationSequenceBuilder<>(), relativePosition);
        }

        void setOtherTarget(NonInsertionTarget<T> otherTarget) {
            this.otherTarget = otherTarget;
        }

        @Override
        public void retain(int count) {
            int oldPosition = relativePosition.get();
            relativePosition.increase(count);
            if (relativePosition.get() < 0) {
                otherTarget.rangeCache.resolve(count);
            } else if (oldPosition < 0) {
                otherTarget.rangeCache.resolve(-oldPosition);
            }
        }

        @Override
        public void add(T value) {
            targetDocument.add(value);
            otherTarget.targetDocument.retain(1);
        }

        @Override
        public void remove(T oldValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T oldValue, T newValue) {
            throw new UnsupportedOperationException();
        }

    }

    private static final class NonInsertionTarget<T> extends Target<T> {

        private final class RemoveOperationCache extends RangeCache {

            private T oldValue;

            RemoveOperationCache(T oldValue) {
                this.oldValue = oldValue;
            }

            @Override
            void resolve(int itemCount) {
                targetDocument.remove(oldValue);
                oldValue = null;
            }

        }

        private final class SetOperationCache extends RangeCache {

            private final T oldAttributes;
            private final T newAttributes;

            SetOperationCache(T oldAttributes, T newAttributes) {
                this.oldAttributes = oldAttributes;
                this.newAttributes = newAttributes;
            }

            @Override
            void resolve(int itemCount) {
                targetDocument.set(oldAttributes, newAttributes);
                otherTarget.targetDocument.retain(1);
            }

        }

        private final RangeCache retainCache = new RangeCache() {

            @Override
            void resolve(int itemCount) {
                targetDocument.retain(itemCount);
                otherTarget.targetDocument.retain(itemCount);
            }

        };

        /**
         * A cache for the effect of mutation components which affect ranges.
         */
        private RangeCache rangeCache = retainCache;

        private InsertionTarget otherTarget;

        NonInsertionTarget(RelativePosition relativePosition) {
            super(new ListOperationSequenceBuilder<>(), relativePosition);
        }

        void setOtherTarget(InsertionTarget otherTarget) {
            this.otherTarget = otherTarget;
        }

        @Override
        public void retain(int count) {
            resolveRange(count, retainCache);
            rangeCache = retainCache;
        }

        @Override
        public void add(T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(T oldValue) {
            RangeCache cache = new RemoveOperationCache(oldValue);
            if (resolveRange(1, cache) >= 0) {
                rangeCache = cache;
            }
        }

        @Override
        public void set(T oldValue, T newValue) {
            RangeCache cache = new SetOperationCache(oldValue, newValue);
            if (resolveRange(1, cache) == 0) {
                rangeCache = cache;
            }
        }

        /**
         * Resolves the transformation of a range.
         *
         * @param size the requested size to resolve
         * @param cache the cache to use
         * @return the portion of the requested size that was resolved, or -1 to
         *         indicate that the entire range was resolved
         */
        private int resolveRange(int size, RangeCache cache) {
            int oldPosition = relativePosition.get();
            relativePosition.increase(size);
            if (relativePosition.get() > 0) {
                if (oldPosition < 0) {
                    cache.resolve(-oldPosition);
                }
                return -oldPosition;
            } else {
                cache.resolve(size);
                return -1;
            }
        }

    }

    Pair<Sequence<ListOperation<T>>, Sequence<ListOperation<T>>> transformOperations(Sequence<ListOperation<T>> insertionOp, Sequence<ListOperation<T>> nonInsertionOp) {
        PositionTracker positionTracker = new PositionTracker();

        RelativePosition insertionPosition = positionTracker.getPositivePosition();
        RelativePosition nonInsertionPosition = positionTracker.getNegativePosition();

        // The target responsible for processing components of the insertion operation.
        InsertionTarget<T> insertionTarget = new InsertionTarget<>(insertionPosition);

        // The target responsible for processing components of the insertion-free operation.
        NonInsertionTarget<T> nonInsertionTarget = new NonInsertionTarget<>(nonInsertionPosition);

        insertionTarget.setOtherTarget(nonInsertionTarget);
        nonInsertionTarget.setOtherTarget(insertionTarget);

        Iterator<ListOperation<T>> insertionIt = insertionOp.iterator();
        Iterator<ListOperation<T>> nonInsertionIt = nonInsertionOp.iterator();
        while (insertionIt.hasNext()) {
            insertionIt.next().apply(insertionTarget);
            while (insertionPosition.get() > 0) {
                nonInsertionIt.next().apply(nonInsertionTarget);
            }
        }
        while (nonInsertionIt.hasNext()) {
            nonInsertionIt.next().apply(nonInsertionTarget);
        }
        return new Pair<>(insertionTarget.build(), nonInsertionTarget.build());
    }

}
