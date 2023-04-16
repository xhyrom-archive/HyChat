plugins {
    id("java-library")
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.6.21"
    application
}

group = "me.xhyrom.hychat"
version = "1.1.2"
description = "A powerful and lightweight chat plugin for minecraft servers."

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.jopga.me/releases")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("me.xhyrom.hylib:hylib-bukkit:1.2.3")
    compileOnly("me.clip:placeholderapi:2.11.2")

    implementation("net.kyori:adventure-text-serializer-plain:4.12.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.12.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.10.0")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(sourceSets.main.get().resources.srcDirs) {
        filter(
            org.apache.tools.ant.filters.ReplaceTokens::class, "tokens" to mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
            )
        )
    }
}

tasks {
    shadowJar {
        exclude("kotlin/**")
        archiveFileName.set("HyChat-${project.version}-all.jar")
    }
    named("build") {
        dependsOn(shadowJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

application {
    mainClass.set("me.xhyrom.hychat.HyChat")
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifact(tasks["shadowJar"])

        repositories.maven {
            url = uri("https://repo.jopga.me/releases")

            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }

        groupId = rootProject.group as String
        artifactId = project.name
        version = rootProject.version as String

        pom {
            name.set("HyChat")
        }
    }
}