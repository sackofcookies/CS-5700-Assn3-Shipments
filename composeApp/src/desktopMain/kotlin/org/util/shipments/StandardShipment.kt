package org.util

class StandardShipment(status: String, id: String, createdDate: Long, expectedDeliveryDateTimestamp: Long, currentLocation: String): Shipment(status, id, createdDate, expectedDeliveryDateTimestamp, currentLocation){
    override fun conditions(): String?{
        return null
    }

    override val type = Shipment.Type.STANDARD
}