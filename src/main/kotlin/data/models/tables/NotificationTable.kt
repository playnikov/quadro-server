package com.quadro.data.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object NotificationTable : Table("notifications") {
    val id = long("id").autoIncrement()
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val title = varchar("title", 500)
    val message = text("message")

    override val primaryKey = PrimaryKey(id)
}