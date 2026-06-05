dependencies {
    api("net.kyori:adventure-api:4.17.0")
    api("net.kyori:adventure-text-minimessage:4.17.0")
    api("com.zaxxer:HikariCP:6.3.0")
    api("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    api("org.slf4j:slf4j-api:2.0.16")

    runtimeOnly("org.xerial:sqlite-jdbc:3.47.1.0")
    runtimeOnly("com.mysql:mysql-connector-j:9.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.assertj:assertj-core:3.27.2")
}
