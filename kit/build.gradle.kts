import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
}

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    jvm()
    js(IR) {
        nodejs()
        browser {
            binaries.executable()
        }
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
            implementation(libs.rsocket.core)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
            implementation(libs.kotlinxCoroutines)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.resources)
            implementation(libs.kaml)
            implementation(libs.okio)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinpoet)
            implementation(libs.symbol.processing.api)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.ktor.client.content.negotiation.js)
            implementation(libs.ktor.serialization.kotlinx.json.js)
            implementation(libs.ktor.client.resources.js)
        }
    }
}