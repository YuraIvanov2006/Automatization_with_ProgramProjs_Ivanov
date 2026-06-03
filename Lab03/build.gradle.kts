import org.gradle.api.tasks.bundling.Zip

plugins {
    java
    id("ua.edu.ukma.ivanov-plugin")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
    implementation("org.glassfish.jersey.containers:jersey-container-servlet:4.0.0-M2")
}

tasks.register<Zip>("createSubmissionArchive") {
    from(layout.projectDirectory) {
        exclude("build/", ".gradle/", "buildSrc/build/", "buildSrc/.gradle/")
    }
    archiveFileName.set("Ivanov_Lab03.zip")
    destinationDirectory.set(layout.buildDirectory.dir("archives"))
}

tasks.named("createSubmissionArchive") {
    dependsOn("generateDocs")
}