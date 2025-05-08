plugins {
    kotlin("multiplatform")
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
    // js("web",IR) IR 编译器通过 gradle.properties 中指定
    js("web") {
        binaries.executable()
        browser {
            commonWebpackConfig {
                outputFileName = "index.js"
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.rsocket)
                implementation(project(":api"))
                implementation(project(":kit"))
                implementation(libs.ktor.client.cio)
                implementation(libs.rsocket.transport.ktor.tcp)
                implementation(libs.rsocket.transport.ktor.websocket.client)
                implementation(libs.kotlinxCoroutines)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.html.builder.jvm)
                implementation(libs.kotlinx.html.jvm)
                implementation(libs.kotlinx.coroutines.core.jvm)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlinxCoroutines)
            }
        }
        val webMain by getting {
            dependencies {
                implementation(kotlinWrappers.react)
                implementation(kotlinWrappers.reactDom)
                implementation(kotlinWrappers.reactRouter)
                implementation(kotlinWrappers.reactCore)
                implementation(kotlinWrappers.reactLegacy)

                implementation(kotlinWrappers.emotion.css)
                implementation(kotlinWrappers.emotion.react)
                implementation(kotlinWrappers.emotion.styled)

                implementation(kotlinWrappers.mui.material)
                implementation(kotlinWrappers.mui.iconsMaterial)
                implementation(kotlinWrappers.muix.datePickers)
                implementation(libs.kotlin.redux)
                implementation(libs.kotlin.react.redux)
                implementation(libs.koin.core.js)

                implementation(npm("date-fns", "2.30.0"))
                implementation(npm("@date-io/date-fns", "2.17.0"))

//                // React，React DOM + 包装器
//                implementation(project.dependencies.enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.430"))
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
//
//                // Kotlin React 情感（CSS）
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")

                implementation(npm("react-player", "2.12.0"))
                implementation(npm("react-share", "4.4.1"))

            }
        }
        val webTest by getting
    }
}

tasks.named<Copy>("jvmProcessResources") {
    val webBrowserDistribution = tasks.named("webBrowserDistribution")
    from(webBrowserDistribution)
}
