package org.util

class ExpectedDeliveryUpdate(status: String, val expectedArrival: Long, timeStamp: Long): StatusUpdate(status, timeStamp){
    
    override fun applyUpdate(shipment: Shipment){
        super.applyUpdate(shipment)
        shipment.expectedDeliveryDateTimestamp = expectedArrival
    }
}