package org.ui

import org.util.ShipmnetObserver
import org.util.Shipment
import org.util.StatusChange
import org.ui.formatTimestamp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.ui.unit.dp

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}

class TrackerViewHelper(shipment: Shipment): ShipmnetObserver{
    val shipmentId: String = shipment.id
    val shipmentType: String = shipment.type.toString()
    val shipmentCreated: Long = shipment.createdDate
    val shipmentNotes = mutableStateListOf<String>()
    val shipmentUpdateHistory = mutableStateListOf<StatusChange>()
    var expectedShipmentDeliveryDate = mutableStateOf<Long>(shipment.expectedDeliveryDateTimestamp)
    var shipmentStatus = mutableStateOf<String>(shipment.status)
    var shipmentLocation = mutableStateOf<String>(shipment.currentLocation)
    init{
        shipment.notes.forEach {shipmentNotes.add(it)}
        shipment.updateHistory.forEach {shipmentUpdateHistory.add(it)}
        shipment.registerObserver(this)
    }

    override fun update(shipment: Shipment){
        shipmentNotes.clear()
        shipment.notes.forEach {shipmentNotes.add(it)}
        shipmentUpdateHistory.clear()
        shipment.updateHistory.forEach {shipmentUpdateHistory.add(it)}
        expectedShipmentDeliveryDate.value = shipment.expectedDeliveryDateTimestamp
        shipmentStatus.value = shipment.status
        shipmentLocation.value = shipment.currentLocation
    }

    @Composable
    public fun compose(){
        Column(modifier = Modifier.paddingFromBaseline(top = 50.dp)) {
            Text("Tracking " + shipmentType + " Shipment: " + shipmentId)
            Text("Ordered: " + formatTimestamp(shipmentCreated))
            Text("Status: " + shipmentStatus.value)
            Text("Location: " + shipmentLocation.value)
            Text("Expected Delivery Date: " + formatTimestamp(expectedShipmentDeliveryDate.value))
            Text("Status Updates:")
            shipmentUpdateHistory.forEach { Text("Shipment went from " + it.previousStatus + " to " + it.newStatus + " on " + formatTimestamp(it.timeStamp)) }
            Text("Notes:")
            shipmentNotes.forEach { Text(it) }
        }
    }
}