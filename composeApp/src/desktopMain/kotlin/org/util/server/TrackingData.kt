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
        if (entries.size >= 3){
            val id = entries[1]
            val timeStamp = entries[2].toLongOrNull()
            if (timeStamp != null && id != ""){
                if (entries.size >= 4){
                    val otherInfo = entries[3]
                    when (entries[0]){
                        "created" -> {
                            when (otherInfo){
                                "bulk" -> this.addShipment(ShipmentFactory(Shipment.Type.BULK, "created", id, timeStamp))
                                "express" -> this.addShipment(ShipmentFactory(Shipment.Type.EXPRESS, "created", id, timeStamp))
                                "overnight" -> this.addShipment(ShipmentFactory(Shipment.Type.OVERNIGHT, "created", id, timeStamp))
                                "standard" -> this.addShipment(ShipmentFactory(Shipment.Type.STANDARD, "created", id, timeStamp))
                                else -> {}
                            }
                        }
                        "shipped", "delayed" -> {
                            val expectedArrival = otherInfo.toLongOrNull()
                            if (expectedArrival != null){
                                this.updateShipment(id, ExpectedDeliveryUpdate(entries[0], expectedArrival, timeStamp))
                            }
                        }
                        "location" -> this.updateShipment(id, LocationUpdate(otherInfo, timeStamp))
                        "noteadded" -> this.updateShipment(id, NoteUpdate(otherInfo, timeStamp))
                    }
                }
                else {
                    this.updateShipment(id, StatusUpdate(entries[0], timeStamp))
                }
            }
            
        }
        
    }


    
}