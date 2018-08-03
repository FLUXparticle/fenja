package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 03.08.18.
 */
interface BuildingListInitializationVisitor<T, R, D> : ListInitializationVisitor<T, Unit, D> {

    fun build(): R

}
