plugins {
    id("java-library")
    id("java")
    kotlin("jvm") version "1.6.21"
    application
}

group = "me.xhyrom.hychat"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly(files("libs/HyLib-0.1.0-api.jar"))
    compileOnly("me.clip:placeholderapi:2.11.2")

    implementation("net.kyori:adventure-text-serializer-plain:4.12.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}