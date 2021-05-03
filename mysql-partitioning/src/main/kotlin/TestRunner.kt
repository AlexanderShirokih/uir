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

fun runTests(): List<TestResult> {
    transaction {
        Parameters.deleteAll()
        ParametersWithPartition.deleteAll()
    }

    var total = 0

    return listOf(100, 400, 500, 1000).flatMap { insert ->
        insertItems(insert)
        total += insert
        executeQueries(total)
    }
}

fun executeQueries(total: Int) = sequence {
    yield(executeQuery(total, Parameters))
    yield(executeQuery(total, Parameters, id = 100))
    yield(executeQuery(total, ParametersWithPartition))
    yield(executeQuery(total, ParametersWithPartition, id = 100))
}

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

fun insertItems(count: Int) {
    val data = generateData(count).asIterable()

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