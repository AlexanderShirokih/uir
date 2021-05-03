package ru.alexandershirokikh.uir

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

// Объект который хранит подключение к базе данных
object DB {

    const val dbName = "replication_test"

    val db1: DataSource by lazy { createConnection(port = 3307) }
    val db2: DataSource by lazy { createConnection(port = 3308) }
    val db3: DataSource by lazy { createConnection(port = 3309) }

    // Подключаемся к базе данных с указанными параметрами
    private fun createConnection(port: Int): DataSource {
        val config = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = "jdbc:mysql://localhost:$port/parameters?useUnicode=true&serverTimezone=UTC"
            username = "db_user"
            password = "3<Nw=j\$[uBse,3{g"
        }

        return HikariDataSource(config)
    }
}