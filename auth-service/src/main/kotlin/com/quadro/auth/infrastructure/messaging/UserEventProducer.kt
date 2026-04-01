package com.quadro.auth.infrastructure.messaging

import com.quadro.auth.config.KafkaConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

class UserEventProducer(config: KafkaConfig) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val json = Json { ignoreUnknownKeys = true }
    private val producer = KafkaProducer<String, String>(
        mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to config.brokers,
            ProducerConfig.ACKS_CONFIG to "all"
        ),
        StringSerializer(),
        StringSerializer()
    )

    private suspend inline fun <reified T> send(topic: String, key: String, event: T) = withContext(Dispatchers.IO) {
        try {
            producer.send(ProducerRecord(topic, key, json.encodeToString(event))).get()
            logger.debug("Event sent to $topic")
        } catch (e: Exception) {
            logger.error("Failed to send event", e)
        }
    }

    fun close() = producer.close()
}