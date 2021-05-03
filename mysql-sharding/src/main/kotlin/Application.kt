package ru.alexandershirokikh.uir

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.span
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

// Класс описывающий схему таблицы
object Parameters : IntIdTable("parameters") {
    val code = varchar("code", length = 64)
    val value = varchar("value", length = 64)
}

// Класс описывающий строку таблицы
class Parameter(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Parameter>(Parameters)

    var code by Parameters.code
    var value by Parameters.value
}

/// Подключаемся к серверам и создаем таблицы
private fun initDB() {
    initDB(1, Database.connect(DB.db1))
    initDB(5, Database.connect(DB.db2))
    initDB(9, Database.connect(DB.db3))
}

private fun initDB(offset: Int, db: Database) {
    transaction(db) {
        SchemaUtils.drop(Parameters)
        SchemaUtils.create(Parameters)
        generateData(offset, 4)
    }
}


fun main() {
    initDB()

    // Запускаем сервер
    embeddedServer(Netty, 8080) {
        // Маршрутизация запросов
        routing {
            static {
                resources("css")
            }

            get("/") {
                // Строим HTML ответ
                call.respondHtmlTemplate(MainPageTemplate(fetchResults())) {}
            }

            get("param/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondText(
                    "ID отсутствует или неверный",
                    status = HttpStatusCode.BadRequest
                )

                val parameter =
                    findParameterByID(id) ?: return@get call.respondText(
                        "Параметр с ID=$id не найден",
                        status = HttpStatusCode.NotFound
                    )

                call.respondHtmlTemplate(DetailsPageTemplate()) {
                    item {
                        span { +"ID:${parameter.id.value}" }
                        span { +"CODE=${parameter.code}" }
                        span { +"VALUE=${parameter.value}" }
                    }
                }
            }
        }
    }.start(wait = true)
}

// Загружает всю таблицу из баз данных всех серверов
fun fetchResults(): Map<String, List<Parameter>> {
    return mapOf(
        "Database 1" to fetchFromServer(Database.connect(DB.db1)),
        "Database 2" to fetchFromServer(Database.connect(DB.db2)),
        "Database 3" to fetchFromServer(Database.connect(DB.db3)),
        "Database (common)" to listOf(
            Database.connect(DB.db1),
            Database.connect(DB.db2),
            Database.connect(DB.db3)
        ).flatMap { fetchFromServer(it) }
    )
}

fun fetchFromServer(database: Database) = transaction(database) { Parameter.all().toList() }

// Генерирует случайны данные для базы данных
fun generateData(start: Int, count: Int) {
    for (i in start until start + count) {
        Parameter.new(i) {
            code = ('a'..'z').take(6).shuffled().joinToString("")
            value = ('A'..'Z').take(10).shuffled().joinToString("")
        }
    }
}

// Ищет элемент в базе данных. Возвращает null если элемент не найден
fun findParameterByID(id: Int): Parameter? {
    return transaction(Database.connect(findServer(id))) { Parameter.findById(id) }
}

// Определеят сервер по ID записи
fun findServer(id: Int) =
    listOf(DB.db1, DB.db2, DB.db3)
        .getOrNull(id / 4) ?: DB.db3