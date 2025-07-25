package org.util

import io.ktor.server.application.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import kotlinx.html.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

suspend fun trackingServer() {
    embeddedServer(Netty, 8000) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            post("/input"){
                val data = call.receiveParameters()
                TrackingData.processInput(data["update"].toString())
                call.respondRedirect("/")
            }

            get("/") {
                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        title { +"Shipment Tracking" }
                    }
                    body {
                        form(action="/input", method = FormMethod.post) {
                            textInput(name="update")
                            submitInput() { value = "Update" }
                        }
                    }
                }
            }
        }
    }.start(wait = false)
}