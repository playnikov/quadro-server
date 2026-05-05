package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.Task
import java.util.UUID

interface TaskAssignmentService {
    suspend fun assignTaskToUser(taskId: UUID, userId: UUID): Task
    suspend fun assignTaskToTeam(taskId: UUID, teamId: UUID): Task
    suspend fun unassignTask(taskId: UUID): Task
    suspend fun validateUserAssignment(taskId: UUID, userId: UUID): Boolean
    suspend fun validateTeamAssignment(taskId: UUID, teamId: UUID): Boolean
}