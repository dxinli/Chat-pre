import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
    id("org.jetbrains.kotlinx.rpc.plugin") version "0.5.1"
}

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
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
            api(libs.ktor.client.core)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.serialization.protobuf)
            implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
            implementation("io.ktor:ktor-serialization-kotlinx-protobuf:3.1.1")
            implementation("io.ktor:ktor-client-resources:3.1.1")
            implementation("io.ktor:ktor-client-cio:3.1.1")
            api("com.charleskorn.kaml:kaml:0.73.0")
        }
        jvmMain.dependencies {
            runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
            implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.3")
        }
    }
}