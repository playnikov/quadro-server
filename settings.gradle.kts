rootProject.name = "ktor"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "quadro-server"

include(
    ":shared",
    ":api-gateway",
    ":auth-service",
    ":company-service",
    ":team-service",
    ":project-service",
    ":task-service",
    ":notification-service",
    ":activity-service",
)