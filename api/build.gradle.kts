plugins {
    id("buildSrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.rsocket.core)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation("org.springframework:spring-messaging:6.2.4")
}