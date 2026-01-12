package com.quadro.data.models.extensions

import com.quadro.data.models.UserModel
import com.quadro.data.models.tables.UserTable
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow?.toUser(): UserModel? {
    return if (this == null) null
    else UserModel(
        id = this[UserTable.id],
        email = this[UserTable.email],
        passwordHash = this[UserTable.passwordHash],
        lastName = this[UserTable.lastName],
        firstName = this[UserTable.firstName],
        middleName = this[UserTable.middleName],
        role = this[UserTable.role],
        createdAt = this[UserTable.createdAt],
        updatedAt = this[UserTable.updatedAt]
    )
}