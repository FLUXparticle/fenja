package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 03.08.18.
 */
interface BuildingListInitializationHandler<T, R> : ListInitializationHandler<T> {

    fun build(): R

}
