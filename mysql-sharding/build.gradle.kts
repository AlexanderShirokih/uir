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
        for (db in listOf("db1", "db2", "db3")) {
            delete("./$db/")
            initializeInsecure(db)
        }
    }
}

fun initializeInsecure(configName: String) {
    println("Running mysqld init on: $configName")
    exec {
        workingDir(projectDir)
        commandLine(
            "mysqld",
            "--defaults-file=$configName.cnf",
            "--initialize-insecure",
            "--init-file=${projectDir.absolutePath}/db_init.sql"
        )
    }
}

// Задача для запуска mysqld сервера
tasks.create("runServer") {
    doFirst {
        for (db in listOf("db1", "db2", "db3")) {
            startServer(db)
        }
    }
}

fun startServer(configName: String) {
    println("Starting server with config: $configName in background mode")
    ProcessBuilder()
        .directory(projectDir)
        .command("mysqld", "--defaults-file=$configName.cnf")
        .start()
}