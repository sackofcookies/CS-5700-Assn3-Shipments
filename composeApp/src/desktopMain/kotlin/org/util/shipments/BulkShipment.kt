package org.util

class BulkShipment(status: String, id: String, createdDate: Long, expectedDeliveryDateTimestamp: Long, currentLocation: String): Shipment(status, id, createdDate, expectedDeliveryDateTimestamp, currentLocation){
    override fun conditions(): String?{
        if (this.expectedDeliveryDateTimestamp - this.createdDate < 259200000){
            return "Shipment sooner expected time"
        }
        return null
    }
}