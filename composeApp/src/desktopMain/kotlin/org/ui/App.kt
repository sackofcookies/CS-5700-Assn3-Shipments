package org.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.remember
import java.io.File
import androidx.compose.material.TextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.launch

import org.util.Shipment
import org.util.LocationUpdate
import org.util.ExpectedDeliveryUpdate
import org.util.NoteUpdate
import org.util.TrackingData
import org.util.trackingServer


@Composable
@Preview
fun App() {
    MaterialTheme {
        val tracked = remember{ mutableStateListOf<TrackerViewHelper>() }
        var searchContent by remember { mutableStateOf("") }

        LaunchedEffect(true){
            launch {trackingServer()}
        }

        Column() {
            Row() {
                TextField(searchContent, onValueChange = {searchContent = it})
                Button(onClick = {
                    val shipment = TrackingData.findShipment(searchContent)
                    if (shipment != null){
                        tracked += TrackerViewHelper(shipment)
                        searchContent = ""
                    }
                }) {
                    Text("Search")
                }
            }
            LazyColumn() { 
                items(tracked){
                    it.compose()
                }
            }
        }
    }
}