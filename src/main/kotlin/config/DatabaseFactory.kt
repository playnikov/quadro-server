package com.quadro.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    private val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/quadro_db"
    private val dbUser = System.getenv("DB_USER") ?: "root"
    private val dbPassword = System.getenv("DB_PASSWORD") ?: "root"


    fun init() {
        val dataSource = getHikariDataSource()
        Database.connect(dataSource)
    }

    private fun getHikariDataSource(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = dbUrl
        config.username = dbUser
        config.password = dbPassword
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    private fun runMigration(dataSource: HikariDataSource) {
        Flyway.configure()
            .dataSource(
                dataSource.jdbcUrl,
                dataSource.username,
                dataSource.password
            )
            .locations("classpath:/db/migration")
            .load()
            .migrate()
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}