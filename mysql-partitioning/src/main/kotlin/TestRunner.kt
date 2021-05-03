package ru.alexandershirokikh.uir

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.system.measureNanoTime

data class TestResult(
    val resultsCount: Int,
    val executionTime: Float,
    val table: String,
    val query: String,
)

// Функция для запуска тестов
fun runTests(): List<TestResult> {
    // Удаляем все предыдущие записи
    transaction {
        Parameters.deleteAll()
        ParametersWithPartition.deleteAll()
    }

    var total = 0

    return listOf(100, 400, 500, 1000).flatMap { insert ->
        // Вставляем новые записи
        insertItems(insert)
        total += insert
        // Выполняем тестовые запросы
        executeQueries(total)
    }
}

// Выполняет серию тестовых запросов
fun executeQueries(total: Int) = sequence {
    yield(executeQuery(total, Parameters))
    yield(executeQuery(total, Parameters, id = 100))
    yield(executeQuery(total, ParametersWithPartition))
    yield(executeQuery(total, ParametersWithPartition, id = 100))
}

// Выполняет запрос и возвращает результат замеров
fun executeQuery(total: Int, entity: Table, id: Int? = null): TestResult {
    val whereClause = id?.run { "WHERE id=$this" } ?: ""
    val query = "SELECT SQL_NO_CACHE * FROM ${entity.tableName} $whereClause LIMIT 1"

    return TestResult(
        executionTime = measureNanoTime { transaction { exec(query) } } / 1000000f,
        resultsCount = total,
        table = entity.tableName,
        query = query,
    )
}

/// Заполняет таблицу данными
fun insertItems(count: Int) {
    // Генерируем случайные данные
    val data = generateData(count).asIterable()

    // Заполняем таблицу сгенерированными данными
    transaction {
        Parameters.batchInsert(data) { (code, value) ->
            this[Parameters.code] = code
            this[Parameters.value] = value
        }

        ParametersWithPartition.batchInsert(data) { (code, value) ->
            this[ParametersWithPartition.code] = code
            this[ParametersWithPartition.value] = value
        }
    }
}

// Генерирует последовательность случайных данных
fun generateData(count: Int) = sequence {
    repeat(count) {
        yield(
            Parameter(
                code = ('a'..'z').take(6).shuffled().joinToString(""),
                value = ('A'..'Z').take(10).shuffled().joinToString(""),
            )
        )
    }
}