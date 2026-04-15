package com.quadro.shared.data.messaging

import com.quadro.shared.data.config.KafkaConfig
import com.quadro.shared.data.messaging.events.DomainEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.Properties
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class EventProducer(private val config: KafkaConfig) {
    @PublishedApi
    internal val logger = LoggerFactory.getLogger(javaClass)
    @PublishedApi
    internal val json = Json { ignoreUnknownKeys = true }

    @Volatile
    @PublishedApi
    internal var _producer: KafkaProducer<String, String>? = null
    @PublishedApi
    internal val producer: KafkaProducer<String, String>
        get() = _producer ?: synchronized(this) {
            _producer ?: createProducer().also { _producer = it }
        }

    private fun createProducer(): KafkaProducer<String, String> = try {
        KafkaProducer(Properties().apply {
            put("bootstrap.servers", config.bootstrapServers)
            put("key.serializer", StringSerializer::class.java.name)
            put("value.serializer", StringSerializer::class.java.name)
            put("acks", "all")
            put("retries", 3)
            put("enable.idempotence", true)
            put("compression.type", "snappy")
            put("request.timeout.ms", "5000")
            put("max.block.ms", "5000")
            put("delivery.timeout.ms", "10000")
        })
    } catch (e: Exception) {
        logger.error("Failed to create Kafka producer, will retry on next publish", e)
        throw e
    }

    suspend inline fun <reified T : DomainEvent> publish(topic: String, key: String, event: T): Unit = withContext(Dispatchers.IO) {
        val payload = json.encodeToString(event)
        val record = ProducerRecord(topic, key, payload)
        try {
            val prod = producer
            withTimeout(3000L) {
                suspendCancellableCoroutine { cont ->
                    prod.send(record) { _, exception ->
                        if (exception != null) cont.resumeWithException(exception)
                        else cont.resume(Unit)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to publish event ${event.eventId} to $topic [$key]", e)
            synchronized(this@EventProducer) {
                try { _producer?.close() } catch (_: Exception) {}
                _producer = null
            }
        }
    }

    fun close() {
        synchronized(this) {
            _producer?.close()
            _producer = null
        }
    }
}