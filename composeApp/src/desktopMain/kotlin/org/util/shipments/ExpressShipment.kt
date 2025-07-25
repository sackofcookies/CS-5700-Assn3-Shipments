package org.util

class ExpressShipment(status: String, id: String, createdDate: Long, expectedDeliveryDateTimestamp: Long, currentLocation: String): Shipment(status, id, createdDate, expectedDeliveryDateTimestamp, currentLocation){
    override fun conditions(): String?{
        if (this.expectedDeliveryDateTimestamp - this.createdDate > 259200000){
            return "Shipment Was changed to have a Delivery date Greater than 1 day away"
        }
        return null
    }

    override val type = Shipment.Type.EXPRESS
}