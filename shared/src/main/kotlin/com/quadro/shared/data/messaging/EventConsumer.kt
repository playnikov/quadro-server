package com.quadro.shared.data.messaging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val consumerMutex = Mutex()
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisorJob)
    @Volatile
    private var running = false
    private var pollingJob: Job? = null

    private val consumer = KafkaConsumer<String, String>(Properties().apply {
        put("bootstrap.servers", brokers)
        put("group.id", groupId)
        put("key.deserializer", StringDeserializer::class.java.name)
        put("value.deserializer", StringDeserializer::class.java.name)
        put("auto.offset.reset", "earliest")
        put("enable.auto.commit", false)
        put("max.poll.records", 100)
    })

    @Synchronized
    fun start(handler: suspend (topic: String, key: String, value: String) -> Unit) {
        if (pollingJob?.isActive == true) {
            logger.warn("Consumer already started, ignoring duplicate start() call")
            return
        }
        running = true
        scope.launch {
            consumerMutex.withLock {
                consumer.subscribe(topics)
            }
        }
        pollingJob = scope.launch {
            while (running) {
                try {
                    val records = consumerMutex.withLock {
                        consumer.poll(Duration.ofMillis(500))
                    }
                    for (record in records) {
                        try {
                            handler(record.topic(), record.key() ?: "", record.value())
                        } catch (e: Exception) {
                            logger.error("Error handling record from ${record.topic()}", e)
                        }
                    }
                    consumerMutex.withLock {
                        consumer.commitSync()
                    }
                } catch (e: Exception) {
                    logger.error("Consumer poll error", e)
                }
            }
        }
    }


    suspend fun stop() {
        if (pollingJob?.isActive != true) return
        running = false
        pollingJob?.cancelAndJoin()
        consumerMutex.withLock {
            consumer.close()
        }
        pollingJob = null
    }

    fun close() {
        supervisorJob.cancel()
    }
}