package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 02.08.18.
 */
interface ListInitializationVisitor<T, R, D> {

    fun visitAddOperation(value: T, data: D): R

}
