package ru.alexandershirokikh.uir

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.li
import kotlinx.html.ul
import org.jetbrains.exposed.sql.Database

/// parameters (id, code, value)


//object Parameters (): Table() {
//
//}

fun main() {
    // Setup database connection
    Database.connect(DB.db)

    // Start the server
    embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondHtml {
                    body {
                        h1 { +"HTML" }
                        ul {
                            for (n in 1..10) {
                                li { +"$n" }
                            }
                        }
                    }
                }
            }
        }
    }
}