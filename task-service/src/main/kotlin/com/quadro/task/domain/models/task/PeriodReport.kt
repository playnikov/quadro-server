package com.quadro.task.domain.models.task

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class PeriodReport(
    val from: Instant,
    val to: Instant,
    val created: Long,
    val completed: Long,
    val statusBreakdown: Map<String, Long>,     // статус -> количество задач, перешедших в этот статус за период
    val dailyCreation: Map<String, Long>,       // дата (yyyy-MM-dd) -> количество созданных задач
    val dailyProgress: Map<String, Long>,     // дата (yyyy-MM-dd) -> количество начатых задач
    val dailyCompletion: Map<String, Long>,     // дата (yyyy-MM-dd) -> количество завершённых задач
    val averageCompletionDays: Double,          // среднее время выполнения задач (в днях) за всё время проекта
    val overdueCount: Long,                     // количество просроченных задач на конец периода
    val throughput: Double,
    val efficiency: Double,
    val wipAverage: Double
)

data class DurationPercentiles(
    val p50: Double,
    val p75: Double,
    val p90: Double,
    val p95: Double
)

@Serializable
enum class VelocityMetric {
    TASK_COUNT,
    STORY_POINTS,
    ESTIMATED_HOURS
}