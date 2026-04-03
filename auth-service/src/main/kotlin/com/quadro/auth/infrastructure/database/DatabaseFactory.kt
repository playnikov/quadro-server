package com.quadro.auth.infrastructure.database

import com.quadro.auth.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.concurrent.TimeUnit

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private lateinit var dataSource: HikariDataSource

    fun init(
        config: DatabaseConfig
    ): HikariDataSource {
        logger.info("Initializing database connection to: ${config.jdbcUrl}")

        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = config.jdbcUrl
            username = config.user
            password = config.password
            schema = config.schema

            maximumPoolSize = config.poolSize
            minimumIdle = 2
            connectionTimeout = TimeUnit.SECONDS.toMillis(30)
            idleTimeout = TimeUnit.MINUTES.toMillis(5)
            maxLifetime = TimeUnit.MINUTES.toMillis(30)

            connectionTestQuery = "SELECT 1"
            validationTimeout = 5000
            leakDetectionThreshold = 10000

            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")

            connectionInitSql = "SET statement_timeout = '30s'"
        }
        dataSource = HikariDataSource(hikariConfig)

        runMigrations(config.jdbcUrl, config.user, config.password, config.schema)

        Database.connect(dataSource)
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED

        logger.info("Database initialized successfully")
        return dataSource
    }

    private fun runMigrations(
        jdbcUrl: String,
        user: String,
        password: String,
        schema: String
    ) {
        try {
            val flyway = Flyway.configure()
                .dataSource(jdbcUrl, user, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .table("flyway_schema_history")
                .schemas(schema)
                .validateOnMigrate(true)
                .load()

            val migrationsApplied = flyway.migrate()
            logger.info("Flyway migrations applied: $migrationsApplied")
        } catch (e: Exception) {
            logger.error("Failed to run Flyway migrations", e)
            throw e
        }
    }

    fun getDataSource(): HikariDataSource = dataSource

    fun close() {
        if (::dataSource.isInitialized && !dataSource.isClosed) {
            dataSource.close()
            logger.info("Database connection closed")
        }
    }
}