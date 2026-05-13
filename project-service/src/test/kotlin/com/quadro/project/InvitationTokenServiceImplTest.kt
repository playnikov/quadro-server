package com.quadro.project

import com.quadro.project.domain.services.InvitationTokenServiceImpl
import com.quadro.shared.data.config.JwtConfig
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InvitationTokenServiceImplTest {
    private lateinit var tokenService: InvitationTokenServiceImpl
    private lateinit var jwtConfig: JwtConfig

    @Before
    fun setUp() {
        jwtConfig = JwtConfig(
            secret = "test-secret-key-at-least-32-chars-long",
            issuer = "test-issuer",
            audience = "test-audience",
            accessExpiration = 3600,
            refreshExpiration = 86400,
            invitationExpiration = 7 * 24 * 60 * 60 * 1000L,
        )
        tokenService = InvitationTokenServiceImpl(jwtConfig)
    }

    @Test
    fun `generateToken - creates valid JWT with correct claims`() {
        val invitationId = UUID.randomUUID()
        val projectId = UUID.randomUUID()
        val expiresInDays = 3

        val token = tokenService.generateToken(invitationId, projectId, expiresInDays)

        assertNotNull(token)
        assertTrue(token.split(".").size == 3)

        val validation = tokenService.validateToken(token)
        assertTrue(validation.isValid)
        assertEquals(invitationId, validation.invitationId)
        assertEquals(projectId, validation.projectId)
        assertNotNull(validation.expiresAt)
    }

    @Test
    fun `generateToken - uses default expiration when expiresInDays is null`() {
        val invitationId = UUID.randomUUID()
        val projectId = UUID.randomUUID()

        val token = tokenService.generateToken(invitationId, projectId, null)

        val validation = tokenService.validateToken(token)
        assertTrue(validation.isValid)
        val expiresAt = validation.expiresAt!!
        val expectedExpiry = System.currentTimeMillis() + jwtConfig.invitationExpiration
        // Allow 5 seconds difference
        assertTrue(expiresAt in (expectedExpiry - 5000)..(expectedExpiry + 5000))
    }

    @Test
    fun `validateToken - returns valid result for correct token`() {
        val invitationId = UUID.randomUUID()
        val projectId = UUID.randomUUID()
        val token = tokenService.generateToken(invitationId, projectId, 1)

        val result = tokenService.validateToken(token)

        assertTrue(result.isValid)
        assertEquals(invitationId, result.invitationId)
        assertEquals(projectId, result.projectId)
        assertNull(result.error)
    }

    @Test
    fun `validateToken - returns invalid for expired token`() {
        // Generate token with 0 days expiration (already expired)
        val invitationId = UUID.randomUUID()
        val projectId = UUID.randomUUID()
        val token = tokenService.generateToken(invitationId, projectId, -1)

        val result = tokenService.validateToken(token)

        assertFalse(result.isValid)
        assertEquals("Invitation expired", result.error)
        assertNull(result.invitationId)
    }

    @Test
    fun `validateToken - returns invalid for malformed token`() {
        val result = tokenService.validateToken("invalid.token.string")

        assertFalse(result.isValid)
        assertEquals("Invalid invitation token", result.error)
    }
}