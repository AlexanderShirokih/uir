package ru.alexandershirokikh.uir

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object DB {

    var db: DataSource = connect();

    private fun connect(): DataSource {
        val config = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = "jdbc:mysql://localhost:3308?useUnicode=true&serverTimezone=UTC\""
            username = "user"
            password = "password"
        }

        return HikariDataSource(config)

    }
}