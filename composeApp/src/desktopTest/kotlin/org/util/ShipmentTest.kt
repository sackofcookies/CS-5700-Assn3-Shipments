package org.util

import kotlin.test.*

class ShipmentTest {

    class TestObserver : ShipmnetObserver {
        var updates = 0
        override fun update(shipment: Shipment) {
            updates++
        }
    }


    @Test
    fun testDefaultValues() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        assertEquals("created", shipment.status)
        assertEquals("123", shipment.id)
        assertEquals(0L, shipment.expectedDeliveryDateTimestamp)
        assertEquals("", shipment.currentLocation)
        assertTrue(shipment.notes.isEmpty())
        assertTrue(shipment.updateHistory.isEmpty())
    }

    @Test
    fun testSettersTriggerObservers() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        val observer = TestObserver()
        shipment.registerObserver(observer)

        shipment.currentLocation = "NYC"
        shipment.status = "in-transit"
        shipment.expectedDeliveryDateTimestamp = 123456789L

        assertEquals(3, observer.updates)
    }

    @Test
    fun testAddNote() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        shipment.addNote("Package scanned")
        assertEquals(1, shipment.notes.size)
        assertEquals("Package scanned", shipment.notes.first())
    }

    @Test
    fun testAddNoteTriggersObserver() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.addNote("Note A")
        assertEquals(1, observer.updates)
    }

    @Test
    fun testAddStatusUpdatesHistory() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
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
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.addStatus("shipped", 888L)
        assertEquals(2, observer.updates) // status + notify
    }

    @Test
    fun testObserversCanBeRemoved() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.unregisterObserver(observer)

        shipment.status = "delivered"
        assertEquals(0, observer.updates)
    }

    @Test
    fun testNotesListIsImmutableCopy() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        shipment.addNote("a")
        val notes = shipment.notes
        notes.add("b")
        assertEquals(1, shipment.notes.size)
    }

    @Test
    fun testUpdateHistoryListIsImmutableCopy() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        shipment.addStatus("arrived", 100L)
        val history = shipment.updateHistory
        history.add(StatusChange("x", "y", 1L))
        assertEquals(1, shipment.updateHistory.size)
    }

    @Test
    fun testMultipleObserversAllNotified() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
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
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        shipment.addNote("")
        assertEquals("", shipment.notes.first())
    }

    @Test
    fun testSameStatusStillAddsToHistory() {
        val shipment = StandardShipment("created", "123", 0L, 0L, "")
        shipment.addStatus("created", 1234L)
        assertEquals(1, shipment.updateHistory.size)
        assertEquals("created", shipment.updateHistory.first().previousStatus)
    }

    @Test
    fun testBulkShipmentTriggersCondition() {
        val created = 0L
        val deliverySoon = created + 1000L
        val shipment = BulkShipment("created", "b1", created, deliverySoon, "")
        assertTrue(shipment.notes.contains("Shipment Was changed to have a Delivery date sooner than 3 days"))
    }

    @Test
    fun testExpressShipmentTriggersCondition() {
        val created = 0L
        val deliveryLate = created + 259200001L
        val shipment = ExpressShipment("created", "e1", created, deliveryLate, "")
        assertTrue(shipment.notes.contains("Shipment Was changed to have a Delivery date Greater than 1 day away"))
    }

    @Test
    fun testOvernightShipmentTriggersCondition() {
        val created = 0L
        val deliveryTooLate = created + 86400001L
        val shipment = OvernightShipment("created", "o1", created, deliveryTooLate, "")
        assertTrue(shipment.notes.contains("Shipment Was changed to have a Delivery date Greater than 3 days away"))
    }

    @Test
    fun testStandardShipmentDoesNotTriggerCondition() {
        val shipment = StandardShipment("created", "s1", 0L, 9999999999L, "")
        assertTrue(shipment.notes.isEmpty())
    }

    @Test
    fun testConditionTriggersOnlyOnce() {
        val created = 0L
        val deliverySoon = created + 1000L
        val shipment = BulkShipment("created", "bx", created, deliverySoon, "")
        val originalSize = shipment.notes.size

        shipment.currentLocation = "test"
        shipment.status = "test2"
        shipment.expectedDeliveryDateTimestamp = 999999999L

        assertEquals(originalSize, shipment.notes.size)
    }

    @Test
    fun testBulkShipmentCreation() {
        val shipment = ShipmentFactory(
            type = Shipment.Type.BULK,
            status = "pending",
            id = "B001",
            createdDate = 1000L,
            expectedDeliveryDateTimestamp = 2000L,
            currentLocation = "Warehouse A"
        )
        assertTrue(shipment is BulkShipment)
        assertEquals("pending", shipment.status)
        assertEquals("B001", shipment.id)
        assertEquals(1000L, shipment.createdDate)
        assertEquals(2000L, shipment.expectedDeliveryDateTimestamp)
        assertEquals("Warehouse A", shipment.currentLocation)
    }

    @Test
    fun testOvernightShipmentCreation() {
        val shipment = ShipmentFactory(
            type = Shipment.Type.OVERNIGHT,
            status = "shipped",
            id = "O002",
            createdDate = 1500L,
            expectedDeliveryDateTimestamp = 2500L,
            currentLocation = "Sorting Center"
        )
        assertTrue(shipment is OvernightShipment)
        assertEquals("shipped", shipment.status)
    }

    @Test
    fun testStandardShipmentCreation() {
        val shipment = ShipmentFactory(
            type = Shipment.Type.STANDARD,
            status = "created",
            id = "S003",
            createdDate = 1700L
        )
        assertTrue(shipment is StandardShipment)
        assertEquals("created", shipment.status)
        assertEquals("", shipment.currentLocation) // default
        assertEquals(0L, shipment.expectedDeliveryDateTimestamp) // default
    }

    @Test
    fun testExpressShipmentCreation() {
        val shipment = ShipmentFactory(
            type = Shipment.Type.EXPRESS,
            status = "in transit",
            id = "E004",
            createdDate = 1800L,
            expectedDeliveryDateTimestamp = 3000L,
            currentLocation = "Transit Hub"
        )
        assertTrue(shipment is ExpressShipment)
        assertEquals("in transit", shipment.status)
        assertEquals("E004", shipment.id)
    }
}
