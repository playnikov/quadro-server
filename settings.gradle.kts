rootProject.name = "quadro-server"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}


include(
    ":shared",
    ":api-gateway",
    ":auth-service",
    ":company-service",
    ":team-service",
    ":project-service",
    ":task-service"
)