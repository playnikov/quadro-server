package com.quadro.datasource.repositories.project

import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectStatus
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ProjectRepositoryImpl : ProjectRepository {
    override suspend fun create(project: Project): Project = newSuspendedTransaction{
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: UUID): Project? {
        TODO("Not yet implemented")
    }

    override suspend fun findByKey(
        companyId: UUID,
        key: String
    ): Project? {
        TODO("Not yet implemented")
    }

    override suspend fun findByName(
        companyId: UUID,
        name: String
    ): Project? {
        TODO("Not yet implemented")
    }

    override suspend fun update(project: Project): Project {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun findByCompany(
        companyId: UUID,
        limit: Int,
        offset: Int
    ): List<Project> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUser(
        userId: UUID,
        companyId: UUID?
    ): List<Project> {
        TODO("Not yet implemented")
    }

    override suspend fun findByTeam(teamId: UUID): List<Project> {
        TODO("Not yet implemented")
    }

    override suspend fun countByCompany(companyId: UUID): Long {
        TODO("Not yet implemented")
    }

    override suspend fun countByUser(userId: UUID, companyId: UUID?): Long {
        TODO("Not yet implemented")
    }

    override suspend fun existsByKey(companyId: UUID, key: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun existsByName(companyId: UUID, name: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updateStatus(
        id: UUID,
        status: ProjectStatus
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun search(
        companyId: UUID,
        query: String,
        limit: Int
    ): List<Project> {
        TODO("Not yet implemented")
    }
}