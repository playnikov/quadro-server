package com.quadro.shared.utils

import java.time.Instant as JavaInstant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.time.Instant as KotlinInstant

fun KotlinInstant.toOffsetDateTime(): OffsetDateTime =
    OffsetDateTime.ofInstant(JavaInstant.ofEpochMilli(toEpochMilliseconds()), ZoneOffset.UTC)

fun OffsetDateTime.toKotlinInstant(): KotlinInstant =
    KotlinInstant.fromEpochMilliseconds(toInstant().toEpochMilli())