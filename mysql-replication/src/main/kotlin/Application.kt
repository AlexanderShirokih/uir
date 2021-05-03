package ru.alexandershirokikh.uir

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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

private fun initDB() {
    // Создаем базу данных
    transaction(Database.connect(DB.raw)) {
        exec("DROP DATABASE IF EXISTS ${DB.dbName}")
        exec("CREATE DATABASE ${DB.dbName}")
        exec("GRANT ALL PRIVILEGES ON ${DB.dbName}.* TO ${DB.user}")
    }

    /// Подключаемся к мастер серверу и создаем таблицы
    transaction(Database.connect(DB.master)) {
        SchemaUtils.create(Parameters)
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

            post("/param") {
                val params = call.receiveParameters()
                val code = params["code"]
                val value = params["value"]

                if (!code.isNullOrBlank() && !value.isNullOrBlank())
                    transaction(Database.connect(DB.master)) {
                        Parameter.new {
                            this.code = code
                            this.value = value
                        }
                    }

                // Редирект на главную
                call.respondRedirect("/")
            }

            get("/") {
                // Строим HTML ответ
                call.respondHtmlTemplate(MainPageTemplate(fetchResults())) {}
            }
        }
    }.start(wait = true)
}

// Загружает всю таблицу из баз данных всех серверов
fun fetchResults(): Map<String, List<Parameter>> {
    return mapOf(
        "Master" to fetchFromServer(Database.connect(DB.master)),
        "Slave 1" to fetchFromServer(Database.connect(DB.slave1)),
        "Slave 2" to fetchFromServer(Database.connect(DB.slave2))
    )
}

fun fetchFromServer(database: Database) = transaction(database) { Parameter.all().toList() }
