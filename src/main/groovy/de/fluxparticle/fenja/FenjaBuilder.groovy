package de.fluxparticle.fenja

import javafx.beans.property.Property
import javafx.beans.value.ObservableValueBase
import nz.sodium.Transaction

class FenjaBuilder {

    static {
// LOG        println "Property.metaClass"
        Property.metaClass {
            leftShift = { ObservableValueBase value ->
                delegate.bind(value)
            }
        }
    }

    def storage = [:]

    def build(Closure definition) {
        runClosure definition
    }

    def propertyMissing(String name, value) {
// LOG        println "set $name = $value"
        if (storage.containsKey(name)) {
            storage[name].loop(value)
        } else {
            storage[name] = value
        }
    }

    def propertyMissing(String name) {
// LOG        println "get $name"
        def result = null;

        if (storage.containsKey(name)) {
            result = storage[name]
        } else if (name.startsWith("v")) {
            result = new ValueLoop<>();
            storage[name] = result
        } else if (name.startsWith("s")) {
            result = new EventStreamLoop<>();
            storage[name] = result
        }

        return result;
    }

    private runClosure(Closure runClosure) {
        runClosure.delegate = this
        runClosure.resolveStrategy = Closure.OWNER_FIRST

        Transaction.runVoid {
            try {
                runClosure()
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

}