package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 03.08.18.
 */
interface BuildingListOperationVisitor<T, R, D> : ListOperationVisitor<T, Unit, D> {

    fun build(): R

}
