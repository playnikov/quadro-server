package com.quadro.shared.kafka

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties

class EventConsumer(
    brokers: String,
    groupId: String,
    private val topics: List<String>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO)
    @Volatile
    private var running = false

    private val consumer = KafkaConsumer<String, String>(Properties().apply {
        put("bootstrap.servers", brokers)
        put("group.id", groupId)
        put("key.deserializer", StringDeserializer::class.java.name)
        put("value.deserializer", StringDeserializer::class.java.name)
        put("auto.offset.reset", "earliest")
        put("enable.auto.commit", false)
        put("max.poll.records", 100)
    })

    fun start(handler: suspend (topic: String, key: String, value: String) -> Unit) {
        running = true
        consumer.subscribe(topics)
        scope.launch {
            while (running) {
                try {
                    val records = consumer.poll(Duration.ofMillis(500))
                    for (record in records) {
                        try {
                            handler(record.topic(), record.key() ?: "", record.value())
                        } catch (e: Exception) {
                            logger.error("Error handling record from ${record.topic()}", e)
                        }
                    }
                    consumer.commitSync()
                } catch (e: Exception) {
                    logger.error("Consumer poll error", e)
                }
            }
        }
    }

    fun stop() {
        running = false
        consumer.close()
    }
}