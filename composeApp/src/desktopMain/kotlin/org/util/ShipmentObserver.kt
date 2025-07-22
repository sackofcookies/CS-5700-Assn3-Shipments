package org.util

fun interface ShipmnetObserver {
    fun update(shipment: Shipment)
}