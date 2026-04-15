package com.quadro.auth.infrastructure.redis

import com.quadro.shared.data.config.RedisConfig
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.codec.StringCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisConnection(private val config: RedisConfig) {
    private val redisClient: RedisClient
    private val connection: StatefulRedisConnection<String, String>
    val coroutines: RedisCoroutinesCommands<String, String>

    init {
        val uri = RedisURI.Builder.redis(config.host, config.port)
            .apply { config.password?.let { withPassword(it) } }
            .withTimeout(Duration.ofSeconds(5))
            .build()

        redisClient = RedisClient.create(uri)
        connection = redisClient.connect(StringCodec.UTF8)
        coroutines = connection.coroutines()
    }

    suspend fun <T> withConnection(block: suspend RedisCoroutinesCommands<String, String>.() -> T): T {
        return withContext(Dispatchers.IO) {
            block(coroutines)
        }
    }

    fun close() {
        connection.close()
        redisClient.shutdown()
    }
}