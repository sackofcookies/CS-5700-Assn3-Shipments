package org.util

import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking



class TrackingSimulator{
    private val shipments: MutableMap<String, Shipment> = mutableMapOf()

    fun findShipment(id: String): Shipment? = shipments[id]
    fun addShipment(shipment: Shipment) = shipments.put(shipment.id, shipment)


    suspend fun runSimulation(file: File, wait: Long = 1000) {
        val lines = file.readLines()
        for (line in lines) {
            val entries = line.split(",", limit = 4)
            when (entries[0]) {
                "created" -> {
                    if (entries.size < 2) continue
                    val id = entries[1]
                    addShipment(Shipment("created", id))
                }

                "location" -> {
                    if (entries.size < 4) continue
                    val id = entries[1]
                    val timestamp = entries[2].toLongOrNull()
                    val location = entries[3]
                    if (timestamp != null) {
                        findShipment(id)?.let {
                            LocationUpdate(location, timestamp).applyUpdate(it)
                        }
                    }
                }

                "noteadded" -> {
                    if (entries.size < 4) continue
                    val id = entries[1]
                    val timestamp = entries[2].toLongOrNull()
                    val note = entries[3]
                    if (timestamp != null) {
                        findShipment(id)?.let {
                            NoteUpdate(note, timestamp).applyUpdate(it)
                        }
                    }
                }

                "delivered", "lost", "canceled" -> {
                    if (entries.size < 3) continue
                    val id = entries[1]
                    val timestamp = entries[2].toLongOrNull()
                    if (timestamp != null) {
                        findShipment(id)?.let {
                            StatusUpdate(entries[0], timestamp).applyUpdate(it)
                        }
                    }
                }

                "delayed", "shipped" -> {
                    if (entries.size < 4) continue
                    val id = entries[1]
                    val timestamp = entries[2].toLongOrNull()
                    val expected = entries[3].toLongOrNull()
                    if (timestamp != null && expected != null) {
                        findShipment(id)?.let {
                            ExpectedDeliveryUpdate(entries[0], expected, timestamp).applyUpdate(it)
                        }
                    }
                }
            }
            delay(wait)
        }
    }
}