package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 02.08.18.
 */
interface ListOperationVisitor<T, R, D> : ListInitializationVisitor<T, R, D> {

    fun visitSetOperation(oldValue: T, newValue: T, data: D): R

    fun visitRemoveOperation(oldValue: T, data: D): R

    fun visitRetainOperation(count: Int, data: D): R

}
