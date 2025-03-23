plugins {
    kotlin("multiplatform")
    application
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvmToolchain(21)
    jvm {
        withJava()
    }
    js("web") {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.rsocket.kotlin:rsocket-ktor-client:0.20.0")
                implementation(project(":api"))
                implementation("io.ktor:ktor-client-cio:3.1.1")
                implementation("io.rsocket.kotlin:rsocket-transport-ktor-tcp:0.20.0")
                implementation("io.rsocket.kotlin:rsocket-transport-ktor-websocket-client:0.20.0")
            }
        }
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:2.3.2")
                implementation("io.ktor:ktor-server-html-builder-jvm:2.3.2")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
            }
        }
        val jvmTest by getting
        val webMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.3.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-redux:4.1.2-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-redux:7.2.6-pre.346")
            }
        }
        val webTest by getting
    }
}

application {
    mainClass.set("iuo.zmua.application.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val webBrowserDistribution = tasks.named("webBrowserDistribution")
    from(webBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}