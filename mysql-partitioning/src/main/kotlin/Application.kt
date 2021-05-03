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

// Таблица без партицирования
object Parameters : IntIdTable("params_no_part") {
    val code = varchar("code", length = 64)
    val value = varchar("value", length = 64)
}


// Таблица с партицированием
object ParametersWithPartition : IntIdTable("params_partitions") {
    val code = varchar("code", length = 64)
    val value = varchar("value", length = 64)
}


// Класс для передачи данных о параметре
data class Parameter(
    val code: String,
    val value: String,
)


fun main() {
    // Открываем соединение с базой данных
    Database.connect(DB.db)

    // Запускаем сервер
    embeddedServer(Netty, 8080) {
        // Маршрутизация запросов
        routing {
            static {
                resources("css")
                resources("js")
            }

            get("/") {
                // Запускаем тест и получаем его результаты
                val testResults = runTests()

                // Строим HTML ответ
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
                        buildResponse(testResults)
                    }
                }
            }
        }
    }.start(wait = true)
}

fun BODY.buildResponse(testResults: List<TestResult>) {
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

