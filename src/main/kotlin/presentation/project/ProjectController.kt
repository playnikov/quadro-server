package com.quadro.presentation.project

import com.quadro.domain.models.project.AddProjectMembers
import com.quadro.domain.models.project.AssignTeam
import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectCreate
import com.quadro.domain.models.project.ProjectMember
import com.quadro.domain.models.project.ProjectPermissions
import com.quadro.domain.models.project.ProjectPriority
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectStatus
import com.quadro.domain.models.project.ProjectTeamAssignment
import com.quadro.domain.models.project.ProjectType
import com.quadro.domain.models.project.ProjectUpdate
import com.quadro.domain.models.project.ProjectVisibility
import com.quadro.domain.models.project.UpdateTeamRole
import com.quadro.domain.services.company.CompanyService
import com.quadro.domain.services.project.ProjectService
import com.quadro.domain.services.team.TeamService
import com.quadro.domain.services.user.UserService
import com.quadro.plugins.getUserId
import com.quadro.presentation.company.models.UpdateMemberRoleRequest
import com.quadro.presentation.project.models.AddProjectMembersRequest
import com.quadro.presentation.project.models.CreateProjectRequest
import com.quadro.presentation.project.models.ProjectListResponse
import com.quadro.presentation.project.models.ProjectMemberInfoResponse
import com.quadro.presentation.project.models.ProjectMemberResponse
import com.quadro.presentation.project.models.ProjectPermissionsResponse
import com.quadro.presentation.project.models.ProjectResponse
import com.quadro.presentation.project.models.ProjectStatsResponse
import com.quadro.presentation.project.models.ProjectTeamAssignmentRequest
import com.quadro.presentation.project.models.ProjectTeamInfoResponse
import com.quadro.presentation.project.models.ProjectTeamResponse
import com.quadro.presentation.project.models.UpdateProjectRequest
import com.quadro.presentation.project.models.UpdateTeamRoleRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import java.util.UUID

class ProjectController(
    private val projectService: ProjectService,
    private val companyService: CompanyService,
    private val teamService: TeamService,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun createProject(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        try {
            val request = call.receive<CreateProjectRequest>()
            logger.info("[$requestId] Creating project: ${request.name}")

            val projectCreate = ProjectCreate(
                companyId = UUID.fromString(request.companyId),
                type = try {
                    ProjectType.valueOf(request.type.uppercase())
                } catch (e: Exception) {
                    ProjectType.TEAM_MANAGED
                },
                name = request.name,
                key = request.key,
                description = request.description,
                priority = try {
                    ProjectPriority.valueOf(request.priority.uppercase())
                } catch (e: Exception) {
                    ProjectPriority.MEDIUM
                },
                visibility = try {
                    ProjectVisibility.valueOf(request.visibility.uppercase())
                } catch (e: Exception) {
                    ProjectVisibility.RESTRICTED
                },
                startDate = request.startDate,
                endDate = request.endDate,
                leadId = UUID.fromString(request.leadId),
                initialTeams = request.initialTeams?.map { assignment ->
                    ProjectTeamAssignment(
                        teamId = UUID.fromString(assignment.teamId),
                        role = try {
                            ProjectRole.valueOf(assignment.role.uppercase())
                        } catch (e: Exception) {
                            ProjectRole.MEMBER
                        },
                        isLeadTeam = assignment.isLeadTeam
                    )
                }
            )

            val result = projectService.createProject(userId, projectCreate)

            result.fold(
                onSuccess = { project ->
                    logger.info("[$requestId] Project created successfully: ${project.id}")

                    val response = buildProjectResponse(project, userId)
                    call.respond(HttpStatusCode.Created, response)
                },
                onFailure = { error ->
                    logger.warn("[$requestId] Project creation failed: ${error.message}")
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Project creation error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun getUserProjects(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val companyId = call.request.queryParameters["companyId"]?.let { UUID.fromString(it) }

        val result = projectService.getUserProjects(userId, companyId)

        result.fold(
            onSuccess = { projects ->
                val responses = projects.map { project ->
                    buildProjectResponse(project, userId)
                }
                call.respond(HttpStatusCode.OK, responses)
            },
            onFailure = { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
            }
        )
    }

    suspend fun getProject(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val result = projectService.getProject(projectId, userId)

        result.fold(
            onSuccess = { project ->
                logger.debug("[{}] Project retrieved: {}", requestId, project.id)
                val response = buildProjectResponse(project, userId)
                call.respond(HttpStatusCode.OK, response)
            },
            onFailure = { error ->
                when (error.message) {
                    "Project not found" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                    "Access denied" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun updateProject(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        try {
            val request = call.receive<UpdateProjectRequest>()

            val projectUpdate = ProjectUpdate(
                name = request.name,
                description = request.description,
                status = request.status?.let { ProjectStatus.valueOf(it.uppercase()) },
                priority = request.priority?.let { ProjectPriority.valueOf(it.uppercase()) },
                visibility = request.visibility?.let { ProjectVisibility.valueOf(it.uppercase()) },
                leadId = UUID.fromString(request.leadId),
                startDate = request.startDate,
                endDate = request.endDate
            )

            val result = projectService.updateProject(projectId, userId, projectUpdate)
            result.fold(
                onSuccess = { project ->
                    logger.info("[$requestId] Project updated: ${project.id}")
                    val response = buildProjectResponse(project, userId)
                    call.respond(HttpStatusCode.OK, response)
                },
                onFailure = { error ->
                    when (error.message) {
                        "Project not found" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                        "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                        else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Project update error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun deleteProject(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val result = projectService.deleteProject(projectId, userId)

        result.fold(
            onSuccess = {
                logger.info("[$requestId] Project deleted: $projectId")
                call.respond(HttpStatusCode.OK, mapOf("message" to "Project deleted successfully"))
            },
            onFailure = { error ->
                when (error.message) {
                    "Project not found" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                    "Only project owner can delete the project" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun archiveProject(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val result = projectService.archiveProject(projectId, userId)

        result.fold(
            onSuccess = {
                logger.info("[$requestId] Project archived: $projectId")
                call.respond(HttpStatusCode.OK, mapOf("message" to "Project archived successfully"))
            },
            onFailure = { error ->
                when (error.message) {
                    "Project not found" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                    "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun restoreProject(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val result = projectService.restoreProject(projectId, userId)

        result.fold(
            onSuccess = {
                logger.info("[$requestId] Project restored: $projectId")
                call.respond(HttpStatusCode.OK, mapOf("message" to "Project restored successfully"))
            },
            onFailure = { error ->
                when (error.message) {
                    "Project not found" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                    "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun getCompanyProjects(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val companyId = call.parameters["companyId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid company ID"))

        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20

        val result = projectService.getCompanyProjects(companyId, userId, page, size)

        result.fold(
            onSuccess = { projects ->
                val responses = projects.map { project ->
                    buildProjectResponse(project, userId)
                }
                call.respond(HttpStatusCode.OK, ProjectListResponse(
                    projects = responses,
                    total = projects.size.toLong(),
                    page = page,
                    size = size,
                    totalPages = (projects.size + size - 1) / size
                ))
            },
            onFailure = { error ->
                when (error.message) {
                    "User is not a member of this company" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun getTeamProjects(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid team ID"))

        val result = projectService.getTeamProjects(teamId, userId)

        result.fold(
            onSuccess = { projects ->
                val responses = projects.map { project ->
                    buildProjectResponse(project, userId)
                }
                call.respond(HttpStatusCode.OK, responses)
            },
            onFailure = { error ->
                when (error.message) {
                    "User is not a member of this team" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun searchProjects(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val companyId = call.parameters["companyId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid company ID"))

        val query = call.request.queryParameters["q"] ?: ""
        if (query.length < 2) {
            return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Search query must be at least 2 characters"))
        }

        val result = projectService.searchProjects(companyId, userId, query)

        result.fold(
            onSuccess = { projects ->
                val responses = projects.map { project ->
                    buildProjectResponse(project, userId)
                }
                call.respond(HttpStatusCode.OK, responses)
            },
            onFailure = { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
            }
        )
    }

    suspend fun getProjectByKey(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val companyId = call.parameters["companyId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid company ID"))

        val key = call.parameters["key"] ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project key"))

        val result = projectService.getProjectByKey(companyId, key, userId)

        result.fold(
            onSuccess = { project ->
                logger.debug("[$requestId] Project retrieved by key: $key")
                val response = buildProjectResponse(project, userId)
                call.respond(HttpStatusCode.OK, response)
            },
            onFailure = { error ->
                when (error.message) {
                    "Project not found" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                    "Access denied" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun assignTeam(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        try {
            val request = call.receive<ProjectTeamAssignmentRequest>()

            val assignRequest = AssignTeam(
                teamId = UUID.fromString(request.teamId),
                role = try {
                    ProjectRole.valueOf(request.role.uppercase())
                } catch (e: Exception) {
                    ProjectRole.MEMBER
                },
                isLeadTeam = request.isLeadTeam
            )

            val result = projectService.assignTeam(projectId, userId, assignRequest)

            result.fold(
                onSuccess = { projectTeam ->
                    logger.info("[$requestId] Team assigned to project: $projectId")

                    val teamResult = teamService.getTeam(projectTeam.teamId, userId).getOrNull()
                    val project = projectService.getProject(projectId, userId).getOrNull()

                    val response = ProjectTeamResponse(
                        projectId = projectId.toString(),
                        projectName = project?.name ?: "",
                        teamId = projectTeam.teamId.toString(),
                        teamName = teamResult?.name ?: "",
                        role = projectTeam.role.name,
                        isLeadTeam = projectTeam.isLeadTeam,
                        memberCount = teamResult?.currentMembers ?: 0,
                        assignedAt = projectTeam.assignedAt.toString()
                    )

                    call.respond(HttpStatusCode.Created, response)
                },
                onFailure = { error ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Assign team error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun getAssignedTeams(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val result = projectService.getAssignedTeams(projectId, userId)

        result.fold(
            onSuccess = { projectTeams ->
                val responses = projectTeams.map { pt ->
                    val team = teamService.getTeam(pt.teamId, userId).getOrNull()
                    val project = projectService.getProject(projectId, userId).getOrNull()

                    ProjectTeamResponse(
                        projectId = projectId.toString(),
                        projectName = project?.name ?: "",
                        teamId = pt.teamId.toString(),
                        teamName = team?.name ?: "",
                        role = pt.role.name,
                        isLeadTeam = pt.isLeadTeam,
                        memberCount = team?.currentMembers ?: 0,
                        assignedAt = pt.assignedAt.toString()
                    )
                }
                call.respond(HttpStatusCode.OK, responses)
            },
            onFailure = { error ->
                when (error.message) {
                    "Access denied" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun updateTeamRole(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid team ID"))

        try {
            val request = call.receive<UpdateTeamRoleRequest>()

            val updateRequest = UpdateTeamRole(
                role = try {
                    ProjectRole.valueOf(request.role.uppercase())
                } catch (e: Exception) {
                    ProjectRole.MEMBER
                },
                isLeadTeam = request.isLeadTeam
            )

            val result = projectService.updateTeamRole(projectId, userId, teamId, updateRequest)

            result.fold(
                onSuccess = {
                    logger.info("[$requestId] Team role updated in project: $projectId")
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Team role updated successfully"))
                },
                onFailure = { error ->
                    when (error.message) {
                        "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                        else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Update team role error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun unassignTeam(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid team ID"))

        val result = projectService.unassignTeam(projectId, userId, teamId)

        result.fold(
            onSuccess = {
                logger.info("[$requestId] Team unassigned from project: $projectId")
                call.respond(HttpStatusCode.OK, mapOf("message" to "Team unassigned successfully"))
            },
            onFailure = { error ->
                when (error.message) {
                    "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

//    suspend fun syncTeamMembers(call: ApplicationCall) {
//        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
//        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)
//
//        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
//            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))
//
//        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
//            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid team ID"))
//
//        val result = projectService.syncTeamMembers(projectId, userId, teamId)
//
//        result.fold(
//            onSuccess = { (added, removed) ->
//                logger.info("[$requestId] Synced team members: +$added, -$removed")
//                call.respond(HttpStatusCode.OK, mapOf(
//                    "added" to added,
//                    "removed" to removed,
//                    "message" to "Team members synchronized successfully"
//                ))
//            },
//            onFailure = { error ->
//                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
//            }
//        )
//    }

    suspend fun addMembers(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        try {
            val request = call.receive<AddProjectMembersRequest>()

            val addRequest = AddProjectMembers(
                userIds = request.userIds.map { UUID.fromString(it) },
                role = try {
                    ProjectRole.valueOf(request.role.uppercase())
                } catch (e: Exception) {
                    ProjectRole.MEMBER
                }
            )

            val result = projectService.addMembers(projectId, userId, addRequest)

            result.fold(
                onSuccess = { members ->
                    logger.info("[$requestId] Added ${members.size} members to project: $projectId")

                    val responses = members.map { member ->
                        buildProjectMemberResponse(member, userId)
                    }
                    call.respond(HttpStatusCode.Created, responses)
                },
                onFailure = { error ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Add members error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun getProjectMembers(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20

        val result = projectService.getProjectMembers(projectId, userId, page, size)

        result.fold(
            onSuccess = { members ->
                val responses = members.map { member ->
                    buildProjectMemberResponse(member, userId)
                }
                call.respond(HttpStatusCode.OK, responses)
            },
            onFailure = { error ->
                when (error.message) {
                    "Access denied" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun getProjectMember(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val targetUserId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))

        val result = projectService.getProjectMember(projectId, userId, targetUserId)

        result.fold(
            onSuccess = { member ->
                val response = buildProjectMemberResponse(member, userId)
                call.respond(HttpStatusCode.OK, response)
            },
            onFailure = { error ->
                when (error.message) {
                    "Access denied" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    "User is not a member of this project" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun updateMemberRole(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val targetUserId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))

        try {
            val request = call.receive<UpdateMemberRoleRequest>()

            val role = try {
                ProjectRole.valueOf(request.role.uppercase())
            } catch (e: Exception) {
                return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role"))
            }

            val result = projectService.updateMemberRole(projectId, userId, targetUserId, role)

            result.fold(
                onSuccess = {
                    logger.info("[$requestId] Member role updated in project: $projectId")
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Role updated successfully"))
                },
                onFailure = { error ->
                    when (error.message) {
                        "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                        else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Update member role error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun removeMember(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val targetUserId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))

        val result = projectService.removeMember(projectId, userId, targetUserId)

        result.fold(
            onSuccess = {
                logger.info("[$requestId] Member removed from project: $projectId")
                call.respond(HttpStatusCode.OK, mapOf("message" to "Member removed successfully"))
            },
            onFailure = { error ->
                when (error.message) {
                    "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun leaveProject(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val result = projectService.leaveProject(projectId, userId)

        result.fold(
            onSuccess = {
                logger.info("[$requestId] User left project: $projectId")
                call.respond(HttpStatusCode.OK, mapOf("message" to "Left project successfully"))
            },
            onFailure = { error ->
                when (error.message) {
                    "User is not a member of this project" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                    "Owner cannot leave the project" -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                    "Member from team cannot leave individually" -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun getProjectStats(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val result = projectService.getProjectStats(projectId, userId)

        result.fold(
            onSuccess = { stats ->
                val response = ProjectStatsResponse.fromStats(stats)
                call.respond(HttpStatusCode.OK, response)
            },
            onFailure = { error ->
                when (error.message) {
                    "Access denied" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun getProjectPermissions(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid project ID"))

        val result = projectService.getProjectPermissions(projectId, userId)

        result.fold(
            onSuccess = { permissions ->
                val response = ProjectPermissionsResponse.fromPermissions(permissions)
                call.respond(HttpStatusCode.OK, response)
            },
            onFailure = { error ->
                when (error.message) {
                    "Access denied" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    private suspend fun buildProjectResponse(project: Project, userId: UUID): ProjectResponse {
        val company = companyService.getCompany(project.companyId, userId).getOrNull()
        val lead = userService.getUser(project.leadId).getOrNull()
        val owner = userService.getUser(project.ownerId).getOrNull()

        val teams = projectService.getAssignedTeams(project.id, userId).getOrNull() ?: emptyList()
        val teamInfos = teams.map { pt ->
            val team = teamService.getTeam(pt.teamId, userId).getOrNull()
            ProjectTeamInfoResponse(
                teamId = pt.teamId.toString(),
                teamName = team?.name ?: "",
                role = pt.role.name,
                isLeadTeam = pt.isLeadTeam,
                memberCount = team?.currentMembers ?: 0
            )
        }

        val members = projectService.getProjectMembers(project.id, userId, 1, 100).getOrNull() ?: emptyList()
        val memberInfos = members.map { pm ->
            val user = userService.getUser(pm.userId).getOrNull()
            val sourceTeam = pm.sourceTeamId?.let { teamService.getTeam(it, userId).getOrNull() }
            ProjectMemberInfoResponse(
                userId = pm.userId.toString(),
                userEmail = user?.email ?: "",
                userName = user?.username ?: "",
                userAvatar = user?.avatar,
                role = pm.role.name,
                fromTeam = sourceTeam?.name
            )
        }

        val isMember = members.any { it.userId == userId }
        val userMember = members.find { it.userId == userId }
        val permissions = projectService.getProjectPermissions(project.id, userId).getOrNull()
            ?: ProjectPermissions.fromRole(null)

        return ProjectResponse.fromProject(
            project = project,
            companyName = company?.name ?: "",
            leadName = lead?.username ?: "",
            leadEmail = lead?.email ?: "",
            ownerName = owner?.username ?: "",
            teams = teamInfos,
            members = memberInfos,
            isMember = isMember,
            userRole = userMember?.role,
            permissions = permissions
        )
    }

    private suspend fun buildProjectMemberResponse(member: ProjectMember, userId: UUID): ProjectMemberResponse {
        val user = userService.getUser(member.userId).getOrNull()
        val inviter = userService.getUser(member.invitedBy).getOrNull()
        val sourceTeam = member.sourceTeamId?.let { teamService.getTeam(it, userId).getOrNull() }
        val permissions = projectService.getProjectPermissions(member.projectId, userId).getOrNull()

        val canEdit = permissions?.canManageMembers == true && member.sourceTeamId == null
        val canRemove = permissions?.canManageMembers == true && member.sourceTeamId == null && member.role != ProjectRole.OWNER

        return ProjectMemberResponse(
            id = member.id.toString(),
            projectId = member.projectId.toString(),
            userId = member.userId.toString(),
            userEmail = user?.email ?: "",
            userName = user?.username ?: "",
            userAvatar = user?.avatar,
            role = member.role.name,
            joinedAt = member.joinedAt.toString(),
            invitedBy = member.invitedBy.toString(),
            invitedByEmail = inviter?.email ?: "",
            sourceTeamId = member.sourceTeamId?.toString(),
            sourceTeamName = sourceTeam?.name,
            canEdit = canEdit,
            canRemove = canRemove
        )
    }
}