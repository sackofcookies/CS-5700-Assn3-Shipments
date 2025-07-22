package org.util

import kotlin.test.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShipmentTest {

    class TestObserver : ShipmnetObserver {
        var updates = 0
        override fun update(shipment: Shipment) {
            updates++
        }
    }

    @Test
    fun testDefaultValues() {
        val shipment = Shipment("created", "123")
        assertEquals("created", shipment.status)
        assertEquals("123", shipment.id)
        assertEquals(0L, shipment.expectedDeliveryDateTimestamp)
        assertEquals("", shipment.currentLocation)
        assertTrue(shipment.notes.isEmpty())
        assertTrue(shipment.updateHistory.isEmpty())
    }

    @Test
    fun testSettersTriggerObservers() {
        val shipment = Shipment("created", "123")
        val observer = TestObserver()
        shipment.registerObserver(observer)

        shipment.currentLocation = "NYC"
        shipment.status = "in-transit"
        shipment.expectedDeliveryDateTimestamp = 123456789L

        assertEquals(3, observer.updates)
    }

    @Test
    fun testAddNote() {
        val shipment = Shipment("created", "123")
        shipment.addNote("Package scanned")
        assertEquals(1, shipment.notes.size)
        assertEquals("Package scanned", shipment.notes.first())
    }

    @Test
    fun testAddNoteTriggersObserver() {
        val shipment = Shipment("created", "123")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.addNote("Note A")
        assertEquals(1, observer.updates)
    }

    @Test
    fun testAddStatusUpdatesHistory() {
        val shipment = Shipment("created", "123")
        shipment.addStatus("shipped", 999L)
        assertEquals("shipped", shipment.status)
        assertEquals(1, shipment.updateHistory.size)
        val change = shipment.updateHistory.first()
        assertEquals("created", change.previousStatus)
        assertEquals("shipped", change.newStatus)
        assertEquals(999L, change.timeStamp)
    }

    @Test
    fun testAddStatusTriggersObserverTwice() {
        val shipment = Shipment("created", "123")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.addStatus("shipped", 888L)
        assertEquals(2, observer.updates) // one for addStatus(), one for status property setter
    }

    @Test
    fun testObserversCanBeRemoved() {
        val shipment = Shipment("created", "123")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.unregisterObserver(observer)

        shipment.status = "delivered"
        assertEquals(0, observer.updates)
    }

    @Test
    fun testNotesListIsImmutableCopy() {
        val shipment = Shipment("created", "123")
        shipment.addNote("a")
        val notes = shipment.notes
        notes.add("b") // should not affect internal list
        assertEquals(1, shipment.notes.size)
    }

    @Test
    fun testUpdateHistoryListIsImmutableCopy() {
        val shipment = Shipment("created", "123")
        shipment.addStatus("arrived", 100L)
        val history = shipment.updateHistory
        history.add(StatusChange("x", "y", 1L)) // should not affect internal list
        assertEquals(1, shipment.updateHistory.size)
    }

    @Test
    fun testMultipleObserversAllNotified() {
        val shipment = Shipment("created", "123")
        val observer1 = TestObserver()
        val observer2 = TestObserver()
        shipment.registerObserver(observer1)
        shipment.registerObserver(observer2)

        shipment.status = "in-transit"
        assertEquals(1, observer1.updates)
        assertEquals(1, observer2.updates)
    }

    @Test
    fun testEmptyNoteAllowed() {
        val shipment = Shipment("created", "123")
        shipment.addNote("")
        assertEquals("", shipment.notes.first())
    }

    @Test
    fun testSameStatusStillAddsToHistory() {
        val shipment = Shipment("created", "123")
        shipment.addStatus("created", 1234L)
        assertEquals(1, shipment.updateHistory.size)
        assertEquals("created", shipment.updateHistory.first().previousStatus)
    }
}