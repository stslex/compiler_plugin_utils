plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.library) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register(name = "type", type = Delete::class) {
    delete(rootProject.projectDir.resolve("build"))
}