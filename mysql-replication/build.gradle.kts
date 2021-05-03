val logbackVersion: String by project
val ktorVersion: String by project
val kotlinVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.4.32"
}

group = "ru.alexandershirokikh.uir"
version = "0.0.1"

application {
    mainClassName = "ru.alexandershirokikh.uir.ApplicationKt"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.exposed:exposed:0.17.13")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("mysql:mysql-connector-java:8.0.23")
    implementation("com.zaxxer:HikariCP:4.0.3")
}

// Задача для инициализации базы данных
tasks.create("initDatabase") {
    doFirst {
        delete("./db:master/", "./db:slave1/", "./db:slave2/")

        initializeInsecure("db-master")
        initializeInsecure("db-slave1")
        initializeInsecure("db-slave2")
    }
}

fun initializeInsecure(configName: String) {
    println("Running mysqld init on: $configName")
    exec {
        workingDir(projectDir)
        commandLine("mysqld", "--defaults-file=$configName.cnf", "--initialize-insecure")
    }
}

// Задача для запуска mysqld сервера
tasks.create("runServer") {
    doFirst {
        startServer("db-master")
        startServer("db-slave1")
        startServer("db-slave2")
    }
}

fun startServer(configName: String) {
    println("Starting server with config: $configName in background mode")
    ProcessBuilder()
        .directory(projectDir)
        .command("mysqld", "--defaults-file=$configName.cnf")
        .start()
}

// Задача для вывода подсказок для запуска клиентов
tasks.create("printHints") {
    doFirst {
        printHints("db-master")
        printHints("db-slave1")
        printHints("db-slave2")
    }
}

fun printHints(configName: String) {
    // Вытягиваем порт из конфигурационного файла
    val port = file("$configName.cnf")
        .readLines()
        .firstOrNull { line -> line.startsWith("port") }
        ?: "port=3306"

    println("To start ${configName}, type the following command: ")
    println("\$ mysql --protocol=tcp --$port --user=root")
    println("mysql> source sql/init_$configName.sql")
    println()
}

/// Задача для создания схемы базы данных
tasks.create("initSQLScripts") {
    doFirst {
        exec {
            isIgnoreExitValue = true
            workingDir(projectDir)
            commandLine("mysql", "--protocol=tcp", "--port=3305", "--user=root", "<", "db_init.sql")
        }
    }
}
