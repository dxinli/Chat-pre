plugins {
    id("buildSrc.convention.spring-conventions")
}

dependencies {
    implementation(libs.bundles.springReactiveEcosystem)
    implementation(project(":api"))
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.github.f4b6a3:ulid-creator:5.2.3")
}