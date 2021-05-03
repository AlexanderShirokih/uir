package ru.alexandershirokikh.uir

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

// Объект который хранит подключение к базе данных
object DB {

    var db: DataSource = connect(dbName = "partitioning", port = 3305)

    // Подключаемся к базе данных с указанными параметрами
    private fun connect(dbName: String, port: Int): DataSource {
        val config = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = "jdbc:mysql://localhost:$port/$dbName?useUnicode=true&serverTimezone=UTC"
            username = "lab_user"
            password = "kL74pmb#h96Y%2^"
        }

        return HikariDataSource(config)

    }
}