package org.util

fun ShipmentFactory(type: Shipment.Type,status:String, id: String, createdDate: Long, expectedDeliveryDateTimestamp: Long = 0, currentLocation: String= "") = when (type){
    Shipment.Type.BULK -> BulkShipment(status, id, createdDate, expectedDeliveryDateTimestamp, currentLocation)
    Shipment.Type.OVERNIGHT -> OvernightShipment(status, id, createdDate, expectedDeliveryDateTimestamp, currentLocation)
    Shipment.Type.STANDARD -> StandardShipment(status, id, createdDate, expectedDeliveryDateTimestamp, currentLocation)
    Shipment.Type.EXPRESS -> ExpressShipment(status, id, createdDate, expectedDeliveryDateTimestamp, currentLocation)
}