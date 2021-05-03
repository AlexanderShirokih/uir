package ru.alexandershirokikh.uir

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

// Объект который хранит подключение к базе данных
object DB {

    const val dbName = "replication_test"
    const val user = "repl_master"

    val raw: DataSource by lazy { rawConnection() }
    val master: DataSource by lazy { createConnection(port = 3305) }
    val slave1: DataSource by lazy { createConnection(port = 3308) }
    val slave2: DataSource by lazy { createConnection(port = 3309) }

    // Подключается к серверу без открытия базы данных
    private fun rawConnection(): DataSource {
        val config = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = "jdbc:mysql://localhost:3305?useUnicode=true&serverTimezone=UTC"
            username = "root"
            password = ""
        }
        return HikariDataSource(config)
    }

    // Подключаемся к базе данных с указанными параметрами
    private fun createConnection(port: Int): DataSource {
        val config = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = "jdbc:mysql://localhost:$port/$dbName?useUnicode=true&serverTimezone=UTC"
            username = user
            password = "repl_master_pass"
        }

        return HikariDataSource(config)
    }
}