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
    mainClassName = "io.ktor.server.netty.EngineMain"
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

tasks.create("initDatabase") {
    doFirst {
        delete("./db/")

        exec {
            workingDir(projectDir)
            commandLine("mysqld", "--defaults-file=db.cnf", "--initialize-insecure")
        }
    }
}

tasks.create("runServer") {
    doFirst {
        println("Starting server...")
        ProcessBuilder()
            .directory(projectDir)
            .command("mysqld", "--defaults-file=db.cnf")
            .start()
    }
}

tasks.create("initSQLScripts") {
    doFirst {
        exec {
            isIgnoreExitValue = true
            workingDir(projectDir)
            commandLine("mysql", "--protocol=tcp", "--port=3305", "--user=root", "<", "db_init.sql")
        }
    }
}