package com.quadro.auth.infrastructure.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import java.util.UUID

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class SessionCache(private val redis: RedisConnection) {

    suspend fun createSession(userId: UUID, refreshToken: String) = redis.withConnection {
        setex("session:$userId:$refreshToken", 604800, "active")
        sadd("user_sessions:$userId", refreshToken)
    }

    suspend fun sessionExists(userId: UUID, refreshToken: String): Boolean = redis.withConnection {
        exists("session:$userId:$refreshToken") == 1L
    }

    suspend fun deleteSession(userId: UUID, refreshToken: String) = redis.withConnection {
        del("session:$userId:$refreshToken")
        srem("user_sessions:$userId", refreshToken)
    }

    suspend fun deleteAllUserSessions(userId: UUID) = redis.withConnection {
        val tokensResult = smembers("user_sessions:$userId")

        val tokens = if (tokensResult is Set<*>) {
            tokensResult.filterIsInstance<String>()
        } else {
            emptyList()
        }

        if (tokens.isNotEmpty()) {
            val keys = tokens.map { "session:$userId:$it" }
            del(*keys.toTypedArray())
            del("user_sessions:$userId")
        }
    }
}