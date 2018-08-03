package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.ListOperation

/**
 * Created by sreinck on 03.08.18.
 */
class Transformer private constructor() {

    companion object {

        fun <T> transform(clientOp: Sequence<ListOperation<T>>, serverOp: Sequence<ListOperation<T>>): Pair<Sequence<ListOperation<T>>, Sequence<ListOperation<T>>> {
                // The transform process consists of decomposing the client and server
                // operations into two constituent operations each and performing four
                // transforms structured as in the following diagram:
                //     ci0     cn0
                // si0     si1     si2
                //     ci1     cn1
                // sn0     sn1     sn2
                //     ci2     cn2
                //
                val (ci0, cn0) = Decomposer.decompose(clientOp)
                val (si0, sn0) = Decomposer.decompose(serverOp)

                val (ci1, si1) = InsertionTransformer.transformOperations(ci0, si0)
                val (ci2, sn1) = InsertionNonInsertionTransformer<T>().transformOperations(ci1, sn0)
                val (si2, cn1) = InsertionNonInsertionTransformer<T>().transformOperations(si1, cn0)
                val (cn2, sn2) = NonInsertionTransformer<T>().transformOperations(cn1, sn1)

                return Pair(Composer.compose(ci2, cn2), Composer.compose(si2, sn2))
        }

    }

}
