import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
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
        }

        jvmMain.dependencies {
            api("org.springframework:spring-messaging:6.2.4")
        }
    }
}

//dependencies {
//    implementation(libs.rsocket.core)
//    implementation(libs.kotlinx.serialization.json)
//    implementation("org.springframework:spring-messaging:6.2.4")
//}