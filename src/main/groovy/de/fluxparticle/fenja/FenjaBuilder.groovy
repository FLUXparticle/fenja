package de.fluxparticle.fenja

import javafx.beans.property.Property
import javafx.beans.value.ObservableValueBase
import nz.sodium.Transaction
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class FenjaBuilder {

    private static final Log LOG = LogFactory.getLog(FenjaBuilder.class)

    static {
        LOG.debug("Property.metaClass")
        Property.metaClass {
            leftShift = { ObservableValueBase value ->
                delegate.bind(value)
            }
        }
    }

    def storage = [:]

    def build(Closure definition) {
        definition.delegate = this
        definition.resolveStrategy = Closure.OWNER_FIRST

        Transaction.runVoid {
            try {
                definition()
            } catch (Exception e) {
                LOG.error("exception in build", e)
                throw e;
            }
        }
    }

    def propertyMissing(String name, value) {
        if (storage.containsKey(name)) {
            storage[name].loop(value)
        } else {
            storage[name] = value
            def className = value.class.simpleName.replace("Loop", "")
            if (LOG.isTraceEnabled()) {
                value.listen { t -> LOG.trace(className + "(" + name + "): " + t) }
            }
        }
    }

    def propertyMissing(String name) {
        def result = null;

        if (storage.containsKey(name)) {
            result = storage[name]
        } else if (name.startsWith("v")) {
            result = new ValueLoop<>();
            storage[name] = result
        } else if (name.startsWith("s")) {
            result = new EventStreamLoop<>()
            storage[name] = result
        }

        return result;
    }

}