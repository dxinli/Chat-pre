plugins {
    id("buildSrc.convention.spring-conventions")
}

dependencies {
    implementation(libs.bundles.springReactiveEcosystem)
    implementation(project(":api"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.github.f4b6a3:ulid-creator:5.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}