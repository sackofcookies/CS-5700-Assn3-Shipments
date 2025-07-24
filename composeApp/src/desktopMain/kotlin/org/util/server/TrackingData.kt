package org.util

import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking



object TrackingData{
    private val shipments: MutableMap<String, Shipment> = mutableMapOf()

    fun findShipment(id: String): Shipment? = shipments[id]
    private fun addShipment(shipment: Shipment) = shipments.put(shipment.id, shipment)
    private fun updateShipment(id: String, update: Update) {
        val shipment = this.findShipment(id)
        if (shipment != null){
            update.applyUpdate(shipment)
        }
    }

    fun processInput(input: String){
        val entries = input.split(",", limit = 4)
        
    }


    
}