package com.quadro.shared.kafka

import com.quadro.shared.events.DomainEvent
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.Properties

class EventProducer(brokers: String) {
    val logger = LoggerFactory.getLogger(javaClass)
    val json = Json { ignoreUnknownKeys = true }

    val producer = KafkaProducer<String, String>(Properties().apply {
        put("bootstrap.servers", brokers)
        put("key.serializer", StringSerializer::class.java.name)
        put("value.serializer", StringSerializer::class.java.name)
        put("acks", "all")
        put("retries", 3)
        put("enable.idempotence", true)
        put("compression.type", "snappy")
    })

    inline fun <reified T : DomainEvent> publish(topic: String, key: String, event: T) {
        try {
            val payload = json.encodeToString(event)
            val record = ProducerRecord(topic, key, payload)
            producer.send(record) { metadata, ex ->
                if (ex != null) {
                    logger.error("Failed to publish event to $topic", ex)
                } else {
                    logger.debug("Event published to ${metadata.topic()} partition=${metadata.partition()}")
                }
            }
        } catch (e: Exception) {
            logger.error("Error publishing event to $topic", e)
            throw e
        }
    }

    fun close() = producer.close()
}