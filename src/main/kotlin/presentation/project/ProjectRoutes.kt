package com.quadro.presentation.project

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.projectRoutes() {
    val controller: ProjectController by inject()

    route("/api/projects") {
        authenticate("auth-jwt") {
            post {
                controller.createProject(call)
            }

            get("/my") {
                controller.getUserProjects(call)
            }

            get("/{projectId}") {
                controller.getProject(call)
            }

            put("/{projectId}") {
                controller.updateProject(call)
            }

            delete("/{projectId}") {
                controller.deleteProject(call)
            }

            post("/{projectId}/archive") {
                controller.archiveProject(call)
            }

            post("/{projectId}/restore") {
                controller.restoreProject(call)
            }

            post("/{projectId}/teams") {
                controller.assignTeam(call)
            }

            get("/{projectId}/teams") {
                controller.getAssignedTeams(call)
            }

            put("/{projectId}/teams/{teamId}/role") {
                controller.updateTeamRole(call)
            }

            delete("/{projectId}/teams/{teamId}") {
                controller.unassignTeam(call)
            }

//            post("/{projectId}/teams/{teamId}/sync") {
//                controller.syncTeamMembers(call)
//            }

            get("/{projectId}/members") {
                controller.getProjectMembers(call)
            }

            post("/{projectId}/members") {
                controller.addMembers(call)
            }

            get("/{projectId}/members/{userId}") {
                controller.getProjectMember(call)
            }

            patch("/{projectId}/members/{userId}/role") {
                controller.updateMemberRole(call)
            }

            delete("/{projectId}/members/{userId}") {
                controller.removeMember(call)
            }

            post("/{projectId}/leave") {
                controller.leaveProject(call)
            }

            get("/{projectId}/stats") {
                controller.getProjectStats(call)
            }

            get("/{projectId}/permissions") {
                controller.getProjectPermissions(call)
            }
        }
    }

    route("/api/companies/{companyId}/projects") {
        authenticate("auth-jwt") {
            get {
                controller.getCompanyProjects(call)
            }

            get("/search") {
                controller.searchProjects(call)
            }

            get("/by-key/{key}") {
                controller.getProjectByKey(call)
            }
        }
    }

    route("/api/teams/{teamId}/projects") {
        authenticate("auth-jwt") {
            get {
                controller.getTeamProjects(call)
            }
        }
    }
}