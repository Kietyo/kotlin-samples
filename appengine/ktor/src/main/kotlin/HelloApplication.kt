package com.example.demo

/* ktlint-disable no-wildcard-imports */
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.html.*
import java.util.*
import kotlin.collections.LinkedHashSet

// Entry Point of the application as defined in resources/application.conf.
// @see https://ktor.io/servers/configuration.html#hocon-file
fun Application.main() {
    // This adds Date and Server headers to each response, and allows custom additional headers
//    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
//    install(CallLogging)
    install(WebSockets)

    routing {
        // Here we use a DSL for building HTML on the route "/"
        // @see https://github.com/Kotlin/kotlinx.html
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor on Google App Engine Standard" }
                }
                body {
                    p {
                        +"Hello there! This is Ktor running on Google Appengine Standard"
                    }
                }
            }
        }
        get("/demo") {
            call.respondHtml {
                head {
                    title { +"Ktor on Google App Engine Standard" }
                }
                body {
                    p {
                        +"It's another route!"
                    }
                }
            }
        }

        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                send("You are connected! There are ${connections.size} users here.")
                for(frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
                    connections.forEach {
                        it.session.send(textWithUsername)
                    }
                }
            } catch (e: Exception) {
//                println(e.localizedMessage)
            } finally {
                connections -= thisConnection
//                println("Removing $thisConnection")
                connections.forEach {
                    it.session.send("${thisConnection.name} has left the room")
                }
            }

        }
    }
}
