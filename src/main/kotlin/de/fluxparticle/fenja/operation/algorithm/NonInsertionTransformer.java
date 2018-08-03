package de.fluxparticle.fenja.operation.algorithm;

import de.fluxparticle.fenja.operation.BuildingListOperationVisitor;
import de.fluxparticle.fenja.operation.ListOperation;
import de.fluxparticle.fenja.operation.algorithm.PositionTracker.RelativePosition;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.sequences.Sequence;

import java.util.Iterator;

/**
 * A utility class for transforming insertion-free operations.
 *
 * @author Alexandre Mah
 */
final class NonInsertionTransformer<T> {

    /**
     * A cache for the effect of a component of a document mutation that affects a
     * range of the document.
     */
    private static abstract class RangeCache<T> {

        abstract void resolveRetainOperation(int retain);

        void resolveSetOperation(T oldValue, T newValue) {
            throw new UnsupportedOperationException();
        }

        void resolveRemoveOperation(T oldValue) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * A resolver for mutation components which affects ranges.
     */
    private interface RangeResolver<T> {

        /**
         * Resolves a mutation component with a cached mutation component from a
         * different document mutation.
         *  @param size The size of the range affected by the range modifications to
         *        resolve.
         * @param cache The cached range.
         */
        void resolve(int size, RangeCache<T> cache);

    }

    /**
     * A resolver for "deleteCharacters" mutation components.
     */
    private static final class DeleteCharactersResolver<T> implements RangeResolver<T> {

        private final T oldValue;

        DeleteCharactersResolver(T oldValue) {
            this.oldValue = oldValue;
        }

        @Override
        public void resolve(int size, RangeCache<T> range) {
            range.resolveRemoveOperation(oldValue);
        }

    }

    /**
     * A resolver for "replaceAttributes" mutation components.
     */
    private static final class ReplaceAttributesResolver<T> implements RangeResolver<T> {

        private final T oldValue;
        private final T newValue;

        ReplaceAttributesResolver(T oldValue, T newValue) {
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public void resolve(int size, RangeCache<T> range) {
            range.resolveSetOperation(oldValue, newValue);
        }

    }

    /**
     * A resolver for "retain" mutation components.
     */
    private final RangeResolver<T> retainResolver = new RangeResolver<T>() {

        @Override
        public void resolve(int size, RangeCache<T> range) {
            range.resolveRetainOperation(size);
        }

    };

    /**
     * A target of a document mutation which can be used to transform document
     * mutations by making use primarily of information from one mutation with the
     * help of auxiliary information from a second mutation. These targets should
     * be used in pairs.
     */
    private final class Target implements BuildingListOperationVisitor<T, Sequence<ListOperation<T>>, Void> {

        private final class DeleteCharactersCache extends RangeCache<T> {

            private T oldValue;

            DeleteCharactersCache(T oldValue) {
                this.oldValue = oldValue;
            }

            @Override
            void resolveRetainOperation(int itemCount) {
                doDeleteCharacters(oldValue);
                oldValue = null;
            }

            @Override
            void resolveRemoveOperation(T oldValue) {
                this.oldValue = null;
            }

        }

        private final class ReplaceAttributesCache extends RangeCache<T> {

            private final T oldValue;
            private final T newValue;

            ReplaceAttributesCache(T oldValue, T newValue) {
                this.oldValue = oldValue;
                this.newValue = newValue;
            }

            @Override
            void resolveRetainOperation(int itemCount) {
                targetDocument.visitSetOperation(oldValue, newValue, null);
                otherTarget.targetDocument.visitRetainOperation(1, null);
            }

            @Override
            void resolveSetOperation(T oldValue, T newValue) {
                targetDocument.visitSetOperation(newValue, this.newValue, null);
                otherTarget.targetDocument.visitRetainOperation(1, null);
            }

        }

        private final RangeCache<T> retainCache = new RangeCache<T>() {

            @Override
            void resolveRetainOperation(int itemCount) {
                targetDocument.visitRetainOperation(itemCount, null);
                otherTarget.targetDocument.visitRetainOperation(itemCount, null);
            }

            @Override
            void resolveRemoveOperation(T oldValue) {
                otherTarget.doDeleteCharacters(oldValue);
            }

            @Override
            void resolveSetOperation(T oldValue, T newValue) {
                targetDocument.visitRetainOperation(1, null);
                otherTarget.targetDocument.visitSetOperation(oldValue, newValue, null);
            }

        };

        /**
         * The target to which to write the transformed mutation.
         */
        private final ListOperationSequenceBuilder<T> targetDocument;

        /**
         * The position of the processing cursor associated with this target
         * relative to the position of the processing cursor associated to the
         * opposing target. All positional calculations are based on cursor
         * positions in the original document on which the two original operations
         * apply.
         */
        private final RelativePosition relativePosition;

        /**
         * The target that is used opposite this target in the transformation.
         */
        private Target otherTarget;

        /**
         * A cache for the effect of mutation components which affect ranges.
         */
        private RangeCache<T> rangeCache = retainCache;

        Target(ListOperationSequenceBuilder<T> targetDocument, RelativePosition relativePosition) {
            this.targetDocument = targetDocument;
            this.relativePosition = relativePosition;
        }

        void setOtherTarget(Target otherTarget) {
            this.otherTarget = otherTarget;
        }

        @Override
        public Sequence<ListOperation<T>> build() {
            return targetDocument.build();
        }

        @Override
        public Unit visitRetainOperation(int count, Void data) {
            resolveRange(count, retainResolver);
            rangeCache = retainCache;
            return Unit.INSTANCE;
        }

        @Override
        public Unit visitAddOperation(T value, Void data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Unit visitRemoveOperation(T oldValue, Void data) {
            int resolutionSize = resolveRange(1, new DeleteCharactersResolver<>(oldValue));
            if (resolutionSize >= 0) {
                rangeCache = new DeleteCharactersCache(null);
            }
            return Unit.INSTANCE;
        }

        @Override
        public Unit visitSetOperation(T oldValue, T newValue, Void data) {
            if (resolveRange(1, new ReplaceAttributesResolver<>(oldValue, newValue)) == 0) {
                rangeCache = new ReplaceAttributesCache(oldValue, newValue);
            }
            return Unit.INSTANCE;
        }

        /**
         * Resolves the transformation of a range.
         *
         * @param size the requested size to resolve
         * @param resolver the resolver to use
         * @return the portion of the requested size that was resolved, or -1 to
         *         indicate that the entire range was resolved
         */
        private int resolveRange(int size, RangeResolver<T> resolver) {
            int oldPosition = relativePosition.get();
            relativePosition.increase(size);
            if (relativePosition.get() > 0) {
                if (oldPosition < 0) {
                    resolver.resolve(-oldPosition, otherTarget.rangeCache);
                }
                return -oldPosition;
            } else {
                resolver.resolve(size, otherTarget.rangeCache);
                return -1;
            }
        }

        private void doDeleteCharacters(T oldValue) {
            targetDocument.visitRemoveOperation(oldValue, null);
        }

    }

    private final ListOperationSequenceBuilder<T> clientOperation = new ListOperationSequenceBuilder<>();
    
    private final ListOperationSequenceBuilder<T> serverOperation = new ListOperationSequenceBuilder<>();
    
    /**
     * Transforms a pair of insertion-free operations.
     *
     * @param clientOp the operation from the client
     * @param serverOp the operation from the server
     * @return the transformed pair of operations
     */
    Pair<Sequence<ListOperation<T>>, Sequence<ListOperation<T>>> transformOperations(Sequence<ListOperation<T>> clientOp, Sequence<ListOperation<T>> serverOp) {
        PositionTracker positionTracker = new PositionTracker();

        RelativePosition clientPosition = positionTracker.getPositivePosition();
        RelativePosition serverPosition = positionTracker.getNegativePosition();

        // The target responsible for processing components of the client operation.
        Target clientTarget = new Target(clientOperation, clientPosition);

        // The target responsible for processing components of the server operation.
        Target serverTarget = new Target(serverOperation, serverPosition);

        clientTarget.setOtherTarget(serverTarget);
        serverTarget.setOtherTarget(clientTarget);

        // Incrementally apply the two operations in a linearly-ordered interleaving
        // fashion.
        Iterator<ListOperation<T>> clientIt = clientOp.iterator();
        Iterator<ListOperation<T>> serverIt = serverOp.iterator();
        while (clientIt.hasNext()) {
            clientIt.next().accept(clientTarget, null);
            while (clientPosition.get() > 0) {
                serverIt.next().accept(serverTarget, null);
            }
        }
        while (serverIt.hasNext()) {
            serverIt.next().accept(serverTarget, null);
        }
        return new Pair<>(clientTarget.build(), serverTarget.build());
    }

}
