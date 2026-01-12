package com.quadro.domain.repositories

import com.quadro.data.models.UserModel

interface UserRepository {
    suspend fun createUser(user: UserModel): UserModel
    suspend fun updateUser(user: UserModel): UserModel
    suspend fun deleteUser(id: Long): Boolean
    suspend fun findUserById(id: Long): UserModel?
    suspend fun findUserByEmail(email: String): UserModel?
    suspend fun findAll(page: Int, pageSize: Int): List<UserModel>
    suspend fun existsByEmail(email: String): Boolean
}