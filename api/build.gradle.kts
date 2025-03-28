import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js {
        nodejs()
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.rsocket.core)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.serialization.protobuf)
            implementation("com.squareup.okio:okio:3.10.2")
            api(libs.kotlinx.serialization.properties)
        }

        jvmMain.dependencies {
            api("org.springframework:spring-messaging:6.2.3")
        }
    }
}