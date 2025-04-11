import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.ksp)
    idea
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

idea {
    module {
        // Not using += due to https://github.com/gradle/gradle/issues/8749
        sourceDirs = sourceDirs + file("build/generated/ksp/jvm/jvmMain/kotlin") // or tasks["kspKotlin"].destination
        generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/jvm/jvmMain/kotlin") + file("build/generated/ksp/jvm/jvmTest/kotlin")
    }
}

// 该配置一定要在 kotlin 配置之后,需要配置kotlin target才有具体的配置
dependencies {
    add("kspCommonMainMetadata", project(":kit"))
    add("kspJvm", project(":kit"))
//    add("kspJs", project(":kit"))
    // 或者直接使用 ksp(project(":kit")) 会全量配置，多平台影响性能
}