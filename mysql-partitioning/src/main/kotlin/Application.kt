package ru.alexandershirokikh.uir

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database

object Parameters : IntIdTable("params_no_part") {
    val code = varchar("code", length = 64)
    val value = varchar("value", length = 64)
}

object ParametersWithPartition : IntIdTable("params_partitions") {
    val code = varchar("code", length = 64)
    val value = varchar("value", length = 64)
}

data class Parameter(
    val code: String,
    val value: String,
)


fun main() {
    // Init database connection
    Database.connect(DB.db)

    // Start the server
    embeddedServer(Netty, 8080) {
        routing {
            static {
                resources("css")
                resources("js")
            }

            get("/") {
                val testResults = runTests()

                call.respondHtml {
                    head {
                        title { +"Partition time records" }
                        styleLink("style.css")
                        script(
                            type = ScriptType.textJavaScript,
                            src = "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.2.0/chart.min.js"
                        ) {}
                        script(type = ScriptType.textJavaScript, src = "script.js") {}
                    }
                    body {
                        val labels = testResults
                            .map { it.resultsCount.toString() }
                            .distinct()
                            .joinToString(prefix = "[", postfix = "]")

                        val withId = testResults
                            .filter { it.query.contains("id=") }

                        val noPartition = withId
                            .filter { it.table.contains("_no_") }
                            .sortedBy { it.resultsCount }
                            .joinToString(prefix = "[", postfix = "]") { it.executionTime.toString() }

                        val withPartition = withId
                            .filterNot { it.table.contains("_no_") }
                            .sortedBy { it.resultsCount }
                            .joinToString(prefix = "[", postfix = "]") { it.executionTime.toString() }

                        onLoad = "buildChart($labels,[$noPartition,$withPartition])"
                        div {
                            h1 { +"Measurement results 📈" }
                            testResults
                                .groupBy { it.resultsCount }
                                .forEach { (results, group) ->
                                    h3 { +"Records: $results" }
                                    table {
                                        tr {
                                            th { +"SQL Query" }
                                            th { +"SQL table" }
                                            th { +"Time (ms)" }
                                        }
                                        for (result in group) {
                                            tr {
                                                td { +result.query }
                                                td {
                                                    if (result.table.contains("_no_"))
                                                        +"❌"
                                                    else
                                                        +"✅"
                                                    +result.table
                                                }
                                                td { +result.executionTime.toString() }
                                            }
                                        }
                                    }
                                }
                        }
                        div {
                            canvas {
                                id = "chartCanvas"
                                width = "400"
                                height = "400"
                            }
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}