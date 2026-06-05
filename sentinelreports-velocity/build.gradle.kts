plugins {
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":sentinelreports-common"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.2.0")

    runtimeOnly("org.xerial:sqlite-jdbc:3.47.1.0")
    runtimeOnly("com.mysql:mysql-connector-j:9.1.0")
}

tasks.shadowJar {
    archiveBaseName.set("SentinelReports-Velocity")
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.jar {
    enabled = false
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
