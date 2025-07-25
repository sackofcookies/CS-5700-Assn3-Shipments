package org.util

import kotlin.test.*
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.io.path.createTempFile

class TrackingSimulatorTest {

    // Helper to clear internal state between tests
    @BeforeTest
    fun clearShipments() {
        val field = TrackingData::class.java.getDeclaredField("shipments")
        field.isAccessible = true
        val map = field.get(TrackingData) as MutableMap<*, *>
        map.clear()
    }

    @Test
    fun testAddAndFindShipment() {
        val shipment = StandardShipment("created", "abc123", 0L, 0L, "")
        val field = TrackingData::class.java.getDeclaredMethod("addShipment", Shipment::class.java)
        field.isAccessible = true
        field.invoke(TrackingData, shipment)

        val retrieved = TrackingData.findShipment("abc123")
        assertEquals(shipment, retrieved)
    }

    @Test
    fun testRunSimulationCreatesAndUpdatesShipments() {
        val tempFile = createTempFile().toFile()
        tempFile.writeText(
            """
            created,abc123,0,standard
            location,abc123,1000,New York
            noteadded,abc123,1001,Arrived at warehouse
            shipped,abc123,1002,1650000000
            """.trimIndent()
        )

        tempFile.forEachLine {
            TrackingData.processInput(it)
        }

        val shipment = TrackingData.findShipment("abc123")
        assertNotNull(shipment)
        assertEquals("shipped", shipment.status)
        assertEquals("New York", shipment.currentLocation)
        assertTrue(shipment.notes.contains("Arrived at warehouse"))
        assertEquals(1650000000L, shipment.expectedDeliveryDateTimestamp)

        tempFile.delete()
    }

    @Test
    fun testRunSimulationHandlesUnknownShipmentGracefully() {
        val tempFile = createTempFile().toFile()
        tempFile.writeText("location,doesnotexist,1000,Nowhere")
        tempFile.forEachLine { TrackingData.processInput(it) }
        assertNull(TrackingData.findShipment("doesnotexist"))
        tempFile.delete()
    }

    @Test
    fun testDeliveredStatusUpdate() {
        val shipment = runSimulatorWithLine(
            """
            created,shipment1,0,standard
            delivered,shipment1,1000
            """.trimIndent()
        )
        assertNotNull(shipment)
        assertEquals("delivered", shipment.status)
    }

    @Test
    fun testShippedWithInvalidExpected() {
        val shipment = runSimulatorWithLine(
            """
            created,shipment1,0,standard
            shipped,shipment1,1000,notanumber
            """.trimIndent()
        )
        assertNotNull(shipment)
        assertEquals("created", shipment.status)
        assertEquals(0L, shipment.expectedDeliveryDateTimestamp)
    }

    @Test
    fun testShippedWithInvalidTimestamp() {
        val shipment = runSimulatorWithLine(
            """
            created,shipment1,0,standard
            shipped,shipment1,notATime,3000
            """.trimIndent()
        )
        assertNotNull(shipment)
        assertEquals(0, shipment.expectedDeliveryDateTimestamp)
    }

    @Test
    fun testInvalidCreatedLine() {
        TrackingData.processInput("created") // missing fields
        assertNull(TrackingData.findShipment(""))
    }

    @Test
    fun testInvalidLocationLine() {
        TrackingData.processInput("location,abc123,1000") // missing location name
        assertNull(TrackingData.findShipment("abc123"))
    }

    @Test
    fun testInvalidNoteLine() {
        TrackingData.processInput("noteadded,abc123,1000") // missing note
        assertNull(TrackingData.findShipment("abc123"))
    }

    @Test
    fun testInvalidStatusLine() {
        TrackingData.processInput("delivered,abc123") // missing timestamp
        assertNull(TrackingData.findShipment("abc123"))
    }

    @Test
    fun testInvalidShippedLine() {
        TrackingData.processInput("shipped,abc123,1000") // missing expected delivery
        assertNull(TrackingData.findShipment("abc123"))
    }

    @Test
    fun testNonNumericTimestamp() {
        TrackingData.processInput("created,abc123,0,standard")
        TrackingData.processInput("location,abc123,notANumber,Somewhere")
        val shipment = TrackingData.findShipment("abc123")
        assertNotNull(shipment)
        assertEquals("", shipment.currentLocation)
    }

    @Test
    fun testInvalidExpectedDelivery() {
        TrackingData.processInput("created,abc123,0,standard")
        TrackingData.processInput("shipped,abc123,1000,notANumber")
        val shipment = TrackingData.findShipment("abc123")
        assertNotNull(shipment)
        assertEquals(0L, shipment.expectedDeliveryDateTimestamp)
    }

    @Test
    fun testUnknownCommandIsIgnored() {
        TrackingData.processInput("foobar,abc123,1000,whatever")
        assertNull(TrackingData.findShipment("abc123"))
    }

    @Test
    fun testDelayedStatusUpdate() {
        TrackingData.processInput("created,shipment1,0,standard")
        TrackingData.processInput("delayed,shipment1,2000,3000")
        val shipment = TrackingData.findShipment("shipment1")
        assertNotNull(shipment)
        assertEquals("delayed", shipment.status)
        assertEquals(3000L, shipment.expectedDeliveryDateTimestamp)
    }

    @Test
    fun testLocationUpdateWithoutShipment() {
        TrackingData.processInput("location,missingId,1234,City")
        assertNull(TrackingData.findShipment("missingId"))
    }

    @Test
    fun testLocationUpdateInvalidTimestamp() {
        TrackingData.processInput("created,shipment1,0,standard")
        TrackingData.processInput("location,shipment1,notATime,City")
        val shipment = TrackingData.findShipment("shipment1")
        assertNotNull(shipment)
        assertEquals("", shipment.currentLocation)
    }

    @Test
    fun testShippedButShipmentMissing() {
        TrackingData.processInput("shipped,notfound,1000,2000")
        assertNull(TrackingData.findShipment("notfound"))
    }

    @Test
    fun testBulkShipmentCreationViaProcessInput() {
        TrackingData.processInput("created,BULK001,1000,bulk")
        val shipment = TrackingData.findShipment("BULK001")
        assertNotNull(shipment)
        assertTrue(shipment is BulkShipment)
        assertEquals("created", shipment.status)
        assertEquals(1000L, shipment.createdDate)
        assertEquals(0L, shipment.expectedDeliveryDateTimestamp) // default
    }

    @Test
    fun testOvernightShipmentCreationViaProcessInput() {
        TrackingData.processInput("created,OVN002,1100,overnight")
        val shipment = TrackingData.findShipment("OVN002")
        assertNotNull(shipment)
        assertTrue(shipment is OvernightShipment)
    }

    @Test
    fun testStandardShipmentCreationViaProcessInput() {
        TrackingData.processInput("created,STD003,1200,standard")
        val shipment = TrackingData.findShipment("STD003")
        assertNotNull(shipment)
        assertTrue(shipment is StandardShipment)
    }

    @Test
    fun testExpressShipmentCreationViaProcessInput() {
        TrackingData.processInput("created,EXP004,1300,express")
        val shipment = TrackingData.findShipment("EXP004")
        assertNotNull(shipment)
        assertTrue(shipment is ExpressShipment)
    }
    @Test
    fun testCreatedWithInvalidTypeDoesNotCreateShipment() {
        val invalidType = "superfast"  // Not bulk, overnight, standard, or express
        val id = "invalid001"
        val createdTimestamp = 1000L

        TrackingData.processInput("created,$id,$createdTimestamp,$invalidType")

        val shipment = TrackingData.findShipment(id)
        assertNull(shipment, "Shipment should NOT be created with invalid type: $invalidType")
    }

    // Util
    private fun runSimulatorWithLine(content: String): Shipment? {
        val tmpFile = createTempFile().toFile()
        try {
            tmpFile.writeText(content)
            tmpFile.forEachLine { TrackingData.processInput(it) }
            return TrackingData.findShipment("shipment1")
        } finally {
            tmpFile.delete()
        }
    }
}
