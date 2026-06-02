package com.quadro.auth.plugins

import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.services.SeedService
import com.quadro.auth.domain.services.SeedServiceImpl
import io.ktor.server.application.Application
import kotlinx.coroutines.launch
import org.koin.ktor.ext.getKoin
import org.slf4j.LoggerFactory

fun Application.seedSuperAdminOnStart() {
    val logger = LoggerFactory.getLogger("SeedService")

    try {
        val seedService = getKoin().get<SeedService>()

        val username = System.getenv("SEED_USERNAME") ?: System.getProperty("seed.username")
        val email = System.getenv("SEED_EMAIL") ?: System.getProperty("seed.email")
        val password = System.getenv("SEED_PASSWORD") ?: System.getProperty("seed.password")

        if (username == null || email == null || password == null) {
            logger.warn("SEED_PASSWORD not set. Skipping seed process.")
            return
        }

        val firstName = System.getenv("SEED_FIRST_NAME") ?: System.getProperty("seed.firstName") ?: "Super"
        val lastName = System.getenv("SEED_LAST_NAME") ?: System.getProperty("seed.lastName") ?: "Admin"
        val middleName = System.getenv("SEED_MIDDLE_NAME") ?: System.getProperty("seed.middleName")

        val credentials = UserCreate(
            username = username,
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            isNeedChangePassword = true
        )

        launch {
            seedService.createSuperAdminIfNotExists(credentials)
        }
    } catch (e: Exception) {
        logger.error("Failed to seed Super Admin user", e)
    }
}