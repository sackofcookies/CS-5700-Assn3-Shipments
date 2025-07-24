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
import kotlinx.coroutines.runBlocking

suspend fun trackingServer() {
    embeddedServer(Netty, 8000) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            post("/input"){
                val data = call.receiveMultipart()
                data.forEachPart() {
                    if (it is PartData.FormItem){
                        TrackingData.processInput(it.value)
                    }
                }
                
            }

            get("/") {
            call.respondHtml(HttpStatusCode.OK) {
                head {
                    title { +"My Ktor Page" }
                }
                body {
                    h1 { +"Hello from Ktor!" }
                    p { +"This is a paragraph generated with Kotlin HTML DSL." }
                }
            }
        }
        }
    }.start(wait = true)
}