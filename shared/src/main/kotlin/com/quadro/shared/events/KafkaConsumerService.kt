package com.quadro.shared.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties
import kotlin.math.log

class KafkaConsumerService(
    private val bootstrapServers: String,
    private val groupId: String,
    private val topics: List<String>,
    private val onMessage: suspend (String) -> Unit
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val consumer: KafkaConsumer<String, String>
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    init {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        }
        consumer = KafkaConsumer(props)
        consumer.subscribe(topics)
    }

    fun start() {
        scope.launch {
            while (isActive) {
                try {
                    val records = consumer.poll(Duration.ofMillis(500))
                    records.forEach { record ->
                        launch { onMessage(record.value()) }
                    }
                } catch (e: Exception) {
                    logger.error("Kafka poll error: ${e.message}", e)
                }
            }
        }
    }

    fun stop() {
        scope.cancel()
        consumer.wakeup()
        consumer.close(Duration.ofSeconds(5))
    }
}