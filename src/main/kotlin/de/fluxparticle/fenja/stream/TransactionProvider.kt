package de.fluxparticle.fenja.stream

/**
 * Created by sreinck on 05.08.18.
 */
class TransactionProvider {

    private var transaction: Long = 0

    fun newTransaction(): Long {
        return ++transaction
    }

}
