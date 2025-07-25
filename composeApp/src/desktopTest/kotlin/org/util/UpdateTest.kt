package org.util

import kotlin.test.*

class UpdateTest {

    private fun testShipment(id: String): Shipment {
        // Use StandardShipment for testing since it has no special condition rules
        return StandardShipment("created", id, createdDate = 0L, expectedDeliveryDateTimestamp = 0L, currentLocation = "")
    }

    @Test
    fun testStatusUpdate() {
        val shipment = testShipment("s1")
        val update = StatusUpdate("shipped", 1234L)

        update.applyUpdate(shipment)

        assertEquals("shipped", shipment.status)
        assertEquals(1, shipment.updateHistory.size)
        val history = shipment.updateHistory.first()
        assertEquals("created", history.previousStatus)
        assertEquals("shipped", history.newStatus)
        assertEquals(1234L, history.timeStamp)
    }

    @Test
    fun testNoteUpdate() {
        val shipment = testShipment("s2")
        val update = NoteUpdate("Package scanned", 5678L)

        update.applyUpdate(shipment)

        assertEquals(listOf("Package scanned"), shipment.notes)
    }

    @Test
    fun testLocationUpdate() {
        val shipment = testShipment("s3")
        val update = LocationUpdate("Chicago", 4321L)

        update.applyUpdate(shipment)

        assertEquals("Chicago", shipment.currentLocation)
    }

    @Test
    fun testExpectedDeliveryUpdate() {
        val shipment = testShipment("s4")
        val update = ExpectedDeliveryUpdate("delayed", 9999L, 8888L)

        update.applyUpdate(shipment)

        assertEquals("delayed", shipment.status)
        assertEquals(9999L, shipment.expectedDeliveryDateTimestamp)
        assertEquals(1, shipment.updateHistory.size)
        val history = shipment.updateHistory.first()
        assertEquals("created", history.previousStatus)
        assertEquals("delayed", history.newStatus)
        assertEquals(8888L, history.timeStamp)
    }
}
