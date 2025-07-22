package org.util

class NoteUpdate(val note: String, override val timeStamp: Long): Update{

    override fun applyUpdate(shipment: Shipment){
        shipment.addNote(note)
    }
}