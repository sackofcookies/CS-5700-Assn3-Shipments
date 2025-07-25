package org.util

class LocationUpdate(val location: String, override val timeStamp: Long): Update{
    override fun applyUpdate(shipment: Shipment){
        shipment.currentLocation = location
    }
}