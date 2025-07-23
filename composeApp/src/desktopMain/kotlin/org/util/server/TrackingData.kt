package org.util

import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking



object TrackingData{
    private val shipments: MutableMap<String, Shipment> = mutableMapOf()

    fun findShipment(id: String): Shipment? = shipments[id]
    fun addShipment(shipment: Shipment) = shipments.put(shipment.id, shipment)


    
}