package com.quadro.task.di

import com.quadro.shared.security.JwtValidator
import com.quadro.task.domain.repositories.UserRepository
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.domain.repositories.task.SprintRepository
import com.quadro.task.domain.repositories.task.TaskAttachmentRepository
import com.quadro.task.domain.repositories.task.TaskCommentRepository
import com.quadro.task.domain.repositories.task.TaskHistoryRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.services.SprintService
import com.quadro.task.domain.services.SprintServiceImpl
import com.quadro.task.domain.services.TaskAssignmentService
import com.quadro.task.domain.services.TaskAssignmentServiceImpl
import com.quadro.task.domain.services.TaskReportingService
import com.quadro.task.domain.services.TaskReportingServiceImpl
import com.quadro.task.domain.services.TaskService
import com.quadro.task.domain.services.TaskServiceImpl
import com.quadro.task.domain.services.TaskStatusService
import com.quadro.task.domain.services.TaskStatusServiceImpl
import com.quadro.task.infrastructure.database.repositories.UserRepositoryImpl
import com.quadro.task.infrastructure.database.repositories.project.ProjectMemberRepositoryImpl
import com.quadro.task.infrastructure.database.repositories.project.ProjectRepositoryImpl
import com.quadro.task.infrastructure.database.repositories.task.SprintRepositoryImpl
import com.quadro.task.infrastructure.database.repositories.task.TaskAttachmentRepositoryImpl
import com.quadro.task.infrastructure.database.repositories.task.TaskCommentRepositoryImpl
import com.quadro.task.infrastructure.database.repositories.task.TaskHistoryRepositoryImpl
import com.quadro.task.infrastructure.database.repositories.task.TaskRepositoryImpl
import com.quadro.task.presentation.controllers.SprintController
import com.quadro.task.presentation.controllers.TaskAssignmentController
import com.quadro.task.presentation.controllers.TaskController
import com.quadro.task.presentation.controllers.TaskReportingController
import com.quadro.task.presentation.controllers.TaskStatusController
import com.quadro.task.presentation.routes.SprintRoutes
import com.quadro.task.presentation.routes.TaskAssignmentRoutes
import com.quadro.task.presentation.routes.TaskReportingRoutes
import com.quadro.task.presentation.routes.TaskRoutes
import com.quadro.task.presentation.routes.TaskStatusRoutes
import org.koin.dsl.module

val taskModule = module {
    single { JwtValidator(get()) }

    // Repositories
    single<UserRepository> { UserRepositoryImpl() }
    single<ProjectRepository> { ProjectRepositoryImpl() }
    single<ProjectMemberRepository> { ProjectMemberRepositoryImpl() }

    single<TaskRepository> { TaskRepositoryImpl() }
    single<TaskHistoryRepository> { TaskHistoryRepositoryImpl() }
    single<TaskCommentRepository> { TaskCommentRepositoryImpl() }
    single<TaskAttachmentRepository> { TaskAttachmentRepositoryImpl() }
    single<SprintRepository> { SprintRepositoryImpl() }

    // Services
    single<TaskService> { TaskServiceImpl(get(), get(), get()) }
    single<TaskStatusService> { TaskStatusServiceImpl(get()) }
    single<TaskReportingService> { TaskReportingServiceImpl(get(), get(), get(), get()) }
    single<TaskAssignmentService> { TaskAssignmentServiceImpl(get(), get(), get(), get(), get()) }
    single<SprintService> { SprintServiceImpl(get()) }

    // Controllers
    factory { TaskController(get()) }
    factory { TaskStatusController(get()) }
    factory { TaskReportingController(get()) }
    factory { TaskAssignmentController(get()) }
    factory { SprintController(get()) }

    // Routes
    factory { TaskRoutes(get()) }
    factory { TaskStatusRoutes(get()) }
    factory { TaskReportingRoutes(get()) }
    factory { TaskAssignmentRoutes(get()) }
    factory { SprintRoutes(get()) }
}