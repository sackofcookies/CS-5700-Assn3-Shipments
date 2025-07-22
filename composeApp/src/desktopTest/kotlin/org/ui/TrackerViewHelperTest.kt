package org.ui

import kotlin.test.*
import org.util.Shipment
import org.util.StatusChange

class TrackerViewHelperTest {

    private fun createShipment(): Shipment {
        val shipment = Shipment("created", "testId", expectedDeliveryDateTimestamp = 1234567890L, currentLocation = "Initial Location")
        shipment.addNote("Initial note")
        shipment.addStatus("shipped", 1111111111L)
        return shipment
    }

    @Test
    fun testInitializationCopiesShipmentData() {
        val shipment = createShipment()
        val helper = TrackerViewHelper(shipment)

        // Initial notes copied
        assertEquals(1, helper.shipmentNotes.size)
        assertEquals("Initial note", helper.shipmentNotes[0])

        // Initial update history copied
        assertEquals(1, helper.shipmentUpdateHistory.size)
        val change = helper.shipmentUpdateHistory[0]
        assertEquals("created", change.previousStatus)
        assertEquals("shipped", change.newStatus)

        // Initial state values
        assertEquals(1234567890L, helper.expectedShipmentDeliveryDate.value)
        assertEquals("shipped", helper.shipmentStatus.value)
        assertEquals("Initial Location", helper.shipmentLocation.value)
    }

    @Test
    fun testUpdateReflectsShipmentChanges() {
        val shipment = createShipment()
        val helper = TrackerViewHelper(shipment)

        // Change shipment data
        shipment.addNote("Second note")
        shipment.addStatus("delivered", 2222222222L)
        shipment.currentLocation = "New Location"
        shipment.expectedDeliveryDateTimestamp = 9876543210L
        shipment.status = "delivered"

        // Call update
        helper.update(shipment)

        // Check that notes updated
        assertEquals(2, helper.shipmentNotes.size)
        assertEquals("Initial note", helper.shipmentNotes[0])
        assertEquals("Second note", helper.shipmentNotes[1])

        // Check update history updated
        assertEquals(2, helper.shipmentUpdateHistory.size)
        val lastChange = helper.shipmentUpdateHistory.last()
        assertEquals("shipped", lastChange.previousStatus)
        assertEquals("delivered", lastChange.newStatus)

        // Check mutableState values updated
        assertEquals("delivered", helper.shipmentStatus.value)
        assertEquals("New Location", helper.shipmentLocation.value)
        assertEquals(9876543210L, helper.expectedShipmentDeliveryDate.value)
    }
}