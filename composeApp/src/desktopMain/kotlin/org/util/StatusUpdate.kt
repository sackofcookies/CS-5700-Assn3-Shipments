package org.util

open class StatusUpdate(val status: String, override val timeStamp: Long): Update{
    override fun applyUpdate(shipment: Shipment){
        shipment.addStatus(status, timeStamp)
    }
}