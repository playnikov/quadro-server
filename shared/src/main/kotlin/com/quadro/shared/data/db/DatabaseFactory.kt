package com.quadro.shared.data.db

import com.quadro.shared.data.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.concurrent.TimeUnit

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private lateinit var dataSource: HikariDataSource

    fun init(config: DatabaseConfig, locations: Array<String> = arrayOf("classpath:db/migration")) {
        logger.info("Initializing database connection to: ${config.url}")
        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = config.url
            username = config.user
            password = config.password
            schema = config.schema

            maximumPoolSize = config.maxPoolSize
            minimumIdle     = config.minIdle
            poolName        = "quadro-pool"
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            connectionTestQuery = "SELECT 1"
            validationTimeout = 5000
            leakDetectionThreshold = 10000

            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")

            connectionInitSql = "SET statement_timeout = '30s'"
            validate()
        }
        dataSource = HikariDataSource(hikariConfig)

        runMigrations(config.url, config.user, config.password, config.schema, locations)

        Database.connect(dataSource)

        logger.info("Database initialized successfully")
    }

    private fun runMigrations(
        jdbcUrl: String,
        user: String,
        password: String,
        schema: String,
        locations: Array<String>
    ) {
        try {
            val flyway = Flyway.configure()
                .dataSource(jdbcUrl, user, password)
                .locations(*locations)
                .table("flyway_schema_history")
                .schemas(schema)
                .validateOnMigrate(true)
                .load()
                .migrate()
            logger.info("Flyway migrations applied: $flyway")
        } catch (e: Exception) {
            logger.error("Failed to run Flyway migrations", e)
            throw e
        }
    }

    fun close() {
        if (::dataSource.isInitialized && !dataSource.isClosed) {
            dataSource.close()
            logger.info("Database connection closed")
        }
    }
}