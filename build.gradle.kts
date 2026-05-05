plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.flyway) apply false
    alias(libs.plugins.kover) apply false
}

allprojects {
    group = "com.quadro"
    version = "0.0.1"
}