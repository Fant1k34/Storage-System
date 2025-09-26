package com.sklad.skladproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value

@SpringBootApplication
class SkladProjectApplication(
    @Value("\${spring.datasource.username}") val username: String,
    @Value("\${spring.datasource.password}") val password: String,
    @Value("\${spring.datasource.host}") val host: String,
    @Value("\${spring.datasource.port}") val port: String,
    @Value("\${spring.datasource.database-name}") val databaseName: String,
) {
    init {
        val flyway = Flyway.configure()
            .dataSource("jdbc:postgresql://$host:$port/$databaseName", username, password)
            .locations("classpath:db/migration") // Directory for migration scripts
            .load()

        flyway.migrate()
    }
}

fun main(args: Array<String>) {
    runApplication<SkladProjectApplication>(*args)
}
