package de.fluxparticle.fenja.stream

/**
 * Created by sreinck on 04.08.18.
 */
/*
class EventStreamTest {

    private val logger = DelegateFenjaSystemLogger(PrintFenjaSystemLogger(System.out))

    private val system = FenjaSystem(logger)

    private val input by system.InputEventStreamDelegate<Int>()

    private var output: FenjaSystem.UpdateEventStream<String> by system.UpdateEventStreamDelegate()

    @Test
    fun simple1() {
        output = input map { it.toString() }

        system.finish()

        val mockLogger: FenjaSystemLogger = mock()
        val teeLogger = TeeFenjaSystemLogger(mockLogger, PrintFenjaSystemLogger(System.out))
        logger.setDelegate(teeLogger)

        input.sendValue(42)

        verify(mockLogger).updateSource(any())
        verify(mockLogger).executeUpdate(any())
    }

}
*/
