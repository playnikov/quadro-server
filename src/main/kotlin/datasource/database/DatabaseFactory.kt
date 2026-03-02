package com.quadro.datasource.database

import com.quadro.datasource.entities.CompaniesTable
import com.quadro.datasource.entities.CompanyInvitationsTable
import com.quadro.datasource.entities.CompanyMembersTable
import com.quadro.datasource.entities.TeamTable
import com.quadro.datasource.entities.UsersTable
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

object DatabaseFactory {
    private val config = ConfigFactory.load()
    private lateinit var dataSource: HikariDataSource

    private val hikariConfig = HikariConfig().apply {
        driverClassName = config.getString("database.driver")
        jdbcUrl = config.getString("database.jdbcURL")
        username = config.getString("database.username")
        password = config.getString("database.password")

        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        maximumPoolSize = config.getInt("database.connectionPool.maximumPoolSize")
        minimumIdle = config.getInt("database.connectionPool.minimumIdle")
        connectionTimeout = config.getDuration("database.connectionPool.connectionTimeout", TimeUnit.MILLISECONDS)
        idleTimeout = config.getDuration("database.connectionPool.idleTimeout", TimeUnit.MILLISECONDS)
        maxLifetime = config.getDuration("database.connectionPool.maxLifetime", TimeUnit.MILLISECONDS)

        validate()
    }

    fun init(): HikariDataSource {
        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(
                UsersTable,
                CompaniesTable,
                CompanyMembersTable,
                TeamTable,
                CompanyInvitationsTable
            )
        }

        return dataSource
    }

    fun getDataSource(): HikariDataSource {
        return dataSource
    }
}