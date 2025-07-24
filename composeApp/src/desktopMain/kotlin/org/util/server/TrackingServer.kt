package org.util

import io.ktor.server.application.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File
import kotlinx.serialization.Serializable


@Serializable
private data class InputBody(val input: String)

fun trackingServer() {
    embeddedServer(Netty, 8000) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            post("/input"){
                val data = call.receiveMultipart()
                data.forEachPart {TrackingData.processInput(it)}
            }
        }
    }.start(wait = true)
}