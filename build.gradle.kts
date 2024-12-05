import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI


plugins {
    id("java-library")
    id("io.github.goooler.shadow") version "8.1.8"
    id("maven-publish")
}

group = "eu.koboo"
version = "1.2.1"

repositories {
    mavenCentral()
}

dependencies {
    api("org.mongodb:mongodb-driver-sync:5.2.0")
    testImplementation("org.mongodb:mongodb-driver-sync:5.2.0")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testImplementation("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")

    testImplementation("org.slf4j:slf4j-jdk14:2.0.16")
}

tasks.withType<Test> {
    useJUnitPlatform() // Make all tests use JUnit 5
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}


tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

publishing {
    repositories {
        maven {
            name = "Reposilite"
            url = URI(System.getenv("REPOSILITE_URL"))
            credentials {
                username = System.getenv("REPOSILITE_USER")
                password = System.getenv("REPOSILITE_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            groupId = "$group"
            version = "$version"
            artifact(tasks.named<ShadowJar>("shadowJar").get())
            artifact(tasks.named<Jar>("sourcesJar").get())
        }
    }
}


tasks.named("publishGprPublicationToReposiliteRepository") {
    dependsOn(tasks.named("jar"))
}
