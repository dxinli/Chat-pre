import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.ksp)
}

repositories {
    mavenCentral()
}

dependencies {
    add("kspCommonMainMetadata", project(":kit"))
    add("kspJvm", project(":kit"))
//    add("kspJvmTest", project(":kit"))
//    add("kspJs", project(":kit"))
//    add("kspJsTest", project(":kit"))
//    add("kspAndroidNativeX64", project(":kit"))
//    add("kspAndroidNativeX64Test", project(":kit"))
//    add("kspAndroidNativeArm64", project(":kit"))
//    add("kspAndroidNativeArm64Test", project(":kit"))
//    add("kspLinuxX64", project(":kit"))
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
            implementation(project(":kit"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
            implementation(libs.rsocket.core)
        }

        jvmMain.dependencies {
            implementation("org.springframework:spring-messaging:6.2.3")
        }
    }
}