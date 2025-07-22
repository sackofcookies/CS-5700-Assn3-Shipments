package org.util

import kotlin.test.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import java.io.File
import kotlin.io.path.createTempFile

class TrackingSimulatorTest {

    @Test
    fun testAddAndFindShipment() {
        val simulator = TrackingSimulator()
        val shipment = Shipment("created", "abc123")
        simulator.addShipment(shipment)
        val retrieved = simulator.findShipment("abc123")
        assertEquals(shipment, retrieved)
    }

    @Test
    fun testRunSimulationCreatesAndUpdatesShipments() {runBlocking {
        val tempFile = createTempFile().toFile()
        tempFile.writeText(
            """
            created,abc123
            location,abc123,1000,New York
            noteadded,abc123,1001,Arrived at warehouse
            shipped,abc123,1002,1650000000
            """.trimIndent()
        )

        val simulator = TrackingSimulator()

        val job = launch{
            simulator.runSimulation(tempFile, 0)
        }

        job.join()




        val shipment = simulator.findShipment("abc123")
        assertNotNull(shipment)
        assertEquals("shipped", shipment.status)
        assertEquals("New York", shipment.currentLocation)
        assertEquals(listOf("Arrived at warehouse"), shipment.notes)
        assertEquals(1650000000L, shipment.expectedDeliveryDateTimestamp)

        tempFile.delete()
    }}

    @Test
    fun testRunSimulationHandlesUnknownShipmentGracefully()  {runBlocking {
        val tempFile = createTempFile().toFile()
        tempFile.writeText(
            """
            location,doesnotexist,1000,Nowhere
            """.trimIndent()
        )

        val simulator = TrackingSimulator()
        // Should not throw exception even though shipment doesn't exist
        simulator.runSimulation(tempFile)
        val nonexistent = simulator.findShipment("doesnotexist")
        assertNull(nonexistent)
        tempFile.delete()
    }}

    @Test
    fun testRunSimulationWithInvalidLine() {
        runBlocking {
            val file = createTempFile().toFile()
            file.writeText("bad input")
            val sim = TrackingSimulator()
            sim.runSimulation(file)
            file.delete()
        }
    }

    private fun runSimulatorWithLine(line: String): Shipment? = runBlocking {
        val tmpFile = createTempFile().toFile()
        try {
            tmpFile.writeText(
                """
                created,shipment1
                $line
                """.trimIndent()
            )
            val simulator = TrackingSimulator()
            simulator.runSimulation(tmpFile)
            return@runBlocking simulator.findShipment("shipment1")
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    fun testDeliveredStatusUpdate() {
        val shipment = runSimulatorWithLine("delivered,shipment1,1000")
        assertNotNull(shipment)
        assertEquals("delivered", shipment.status)
        assertEquals(1, shipment.updateHistory.size)
        val update = shipment.updateHistory.first()
        assertEquals("created", update.previousStatus)
        assertEquals("delivered", update.newStatus)
        assertEquals(1000L, update.timeStamp)
    }
    @Test
    fun testInvalidCreatedLine() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText("created") // missing id
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        assertNull(simulator.findShipment("")) // nothing created
        file.delete()
    }}

    @Test
    fun testInvalidLocationLine() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText("location,abc123,1000") // missing location
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        file.delete()
    }}

    @Test
    fun testInvalidNoteLine() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText("noteadded,abc123,1000") // missing note
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        file.delete()
    }}

    @Test
    fun testInvalidStatusLine() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText("delivered,abc123") // missing timestamp
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        file.delete()
    }}

    @Test
    fun testInvalidShippedLine() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText("shipped,abc123,1000") // missing expected delivery timestamp
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        file.delete()
    }}

    @Test
    fun testNonNumericTimestamp() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText(
            """
            created,abc123
            location,abc123,notANumber,Somewhere
            """.trimIndent()
        )
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        val shipment = simulator.findShipment("abc123")
        assertNotNull(shipment)
        assertEquals("", shipment.currentLocation) // location update skipped
        file.delete()
    }}

    @Test
    fun testInvalidExpectedDelivery() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText(
            """
            created,abc123
            shipped,abc123,1000,notANumber
            """.trimIndent()
        )
        val simulator = TrackingSimulator()
        val job = launch(){
            simulator.runSimulation(file, 0)
        }
        job.join()
        val shipment = simulator.findShipment("abc123")
        assertNotNull(shipment)
        assertEquals(0, shipment.expectedDeliveryDateTimestamp) // update skipped
        file.delete()
    }}

    @Test
    fun testUnknownCommandIsIgnored() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText("foobar,abc123,1000,whatever")
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        assertNull(simulator.findShipment("abc123")) // command ignored
        file.delete()
    }}

    @Test
    fun testDelayedStatusUpdate() {
        val shipment = runSimulatorWithLine("delayed,shipment1,2000,3000")
        assertNotNull(shipment)
        assertEquals("delayed", shipment.status)
        assertEquals(3000L, shipment.expectedDeliveryDateTimestamp)
    }

    @Test
    fun testLocationUpdateWithoutShipment() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText("location,missingId,1234,City")
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        assertNull(simulator.findShipment("missingId"))
        file.delete()
    }}
    @Test
    fun testLocationUpdateInvalidTimestamp() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText("location,shipment1,notATime,City")
        val simulator = TrackingSimulator()
        simulator.addShipment(Shipment("created", "shipment1"))
        simulator.runSimulation(file, 0)
        val shipment = simulator.findShipment("shipment1")
        assertNotNull(shipment)
        assertEquals("", shipment.currentLocation)
        file.delete()
    }}
    @Test
    fun testShippedButShipmentMissing() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText("shipped,notfound,1000,2000")
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        assertNull(simulator.findShipment("notfound"))
        file.delete()
    }}
    @Test
    fun testShippedWithInvalidExpected() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText(
            """
            created,shipment1
            shipped,shipment1,1000,notanumber
            """.trimIndent()
        )
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        val shipment = simulator.findShipment("shipment1")
        assertNotNull(shipment)
        assertEquals(0, shipment.expectedDeliveryDateTimestamp)
        assertEquals("created", shipment.status)
        file.delete()
    }}
    @Test
    fun testShippedWithInvalidTimestamp() {runBlocking {
        val file = createTempFile().toFile()
        file.writeText(
            """
            created,shipment1
            shipped,shipment1,notATime,3000
            """.trimIndent()
        )
        val simulator = TrackingSimulator()
        simulator.runSimulation(file, 0)
        val shipment = simulator.findShipment("shipment1")
        assertNotNull(shipment)
        assertEquals(0, shipment.expectedDeliveryDateTimestamp)
        file.delete()
    }}
}