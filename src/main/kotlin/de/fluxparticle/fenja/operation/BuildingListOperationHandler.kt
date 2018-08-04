package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 03.08.18.
 */
interface BuildingListOperationHandler<T, R> : ListOperationHandler<T> {

    fun build(): R

}
