package org.util

class ExpressShipment(status: String, id: String, createdDate: Long, expectedDeliveryDateTimestamp: Long = 0, currentLocation: String = ""): Shipment(status, id, createdDate, expectedDeliveryDateTimestamp, currentLocation){
    override fun conditions(): String?{
        if (this.expectedDeliveryDateTimestamp - this.createdDate > 259200000){
            return "Shipment delayed past expected time"
        }
        return null
    }
}