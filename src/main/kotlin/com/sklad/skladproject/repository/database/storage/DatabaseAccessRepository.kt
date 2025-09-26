package com.sklad.skladproject.repository.database.storage

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import javax.sql.DataSource

@Repository
class DatabaseAccessRepository(
    @Value("\${spring.datasource.username}") user: String,
    @Value("\${spring.datasource.password}") pass: String,
    @Value("\${spring.datasource.host}") host: String,
    @Value("\${spring.datasource.port}") port: String,
    @Value("\${spring.datasource.database-name}") databaseName: String,
) {
    private val dataSource: DataSource = createDataSource(user, pass, host, port, databaseName)
    private val logger = LoggerFactory.getLogger("DatabaseStorage")

    private fun createDataSource(
        user: String?,
        pass: String?,
        host: String?,
        port: String?,
        databaseName: String?
    ): DataSource {
        if (user == null || pass == null || host == null || port == null || databaseName == null) {
            throw IllegalArgumentException(
                "Error connecting to database. Following fields in application.properties must not be null: " +
                        "spring.datasource.username, " +
                        "spring.datasource.password, " +
                        "spring.datasource.host, " +
                        "spring.datasource.port and " +
                        "spring.datasource.database-name"
            )
        }

        val hikariConfig = HikariConfig().apply {
            username = user
            password = pass
            jdbcUrl = "jdbc:postgresql://${host}:${port}/${databaseName}"
            addDataSourceProperty("ssl.mode", "disable")
        }

        return HikariDataSource(hikariConfig)
    }

    fun getDataSource(): DataSource {
        return dataSource
    }
}