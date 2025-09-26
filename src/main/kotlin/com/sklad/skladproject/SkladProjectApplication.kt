package com.sklad.skladproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.flywaydb.core.Flyway

@SpringBootApplication
class SkladProjectApplication

fun main(args: Array<String>) {
    val flyway = Flyway.configure()
        .dataSource("jdbc:postgresql://localhost:5432/sklad", "postgres", "postgres")
        .locations("classpath:db/migration") // Directory for migration scripts
        .load()

    flyway.migrate()

    runApplication<SkladProjectApplication>(*args)
}
