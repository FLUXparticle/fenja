package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.stream.TransactionProvider
import javafx.beans.value.ObservableValue

/**
 * Created by sreinck on 31.07.18.
 */
class InputExpr<T> internal constructor(
        name: String,
        transactionProvider: TransactionProvider,
        logger: FenjaSystemLogger
) : SourceExpr<T>(SourceDependency<T>(name, transactionProvider, logger)) {

    fun setValue(value: T) {
        dependency.executeUpdates(value)
    }

}

infix fun <T> InputExpr<T>.bind(observableValue: ObservableValue<T>) {
    setValue(observableValue.value)
    observableValue.addListener { _, _, newValue -> setValue(newValue) }
}
