package org.util

interface Update {
    public val timeStamp: Long
    public fun applyUpdate(shipment: Shipment)
}