package org.util


class OvernightShipment(status: String, id: String, createdDate: Long, expectedDeliveryDateTimestamp: Long, currentLocation: String): Shipment(status, id, createdDate, expectedDeliveryDateTimestamp, currentLocation){
    override fun conditions(): String?{
        if (this.expectedDeliveryDateTimestamp - this.createdDate > 86400000){
            return "Shipment delayed past expected time"
        }
        return null
    }

    override val type = Shipment.Type.OVERNIGHT
}