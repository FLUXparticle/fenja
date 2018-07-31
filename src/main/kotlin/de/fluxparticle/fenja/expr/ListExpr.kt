package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.list.LoopList

/**
 * Created by sreinck on 31.07.18.
 */
class ListExpr<T> {

    val elements: LoopList<T> = LoopList()

}

//fun ListExpr<Double>.min() = MinExpr(elements)

//fun ListExpr<Double>.max() = MaxExpr(elements)
