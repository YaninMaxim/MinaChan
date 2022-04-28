plugins {
    id("java")
}

group = "ru.yadev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.codehaus.groovy:groovy-all:3.0.10")
    implementation("commons-io:commons-io:2.11.0")
    implementation("io.github.privettoli:vertx-json-value-mapper:0.0.1")
    implementation("org.json:json:20220320")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}