import java.security.MessageDigest

plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

group = "io.github.stslex"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(libs.jetbrains.kotlin.compiler.embeddable)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "io.github.stslex"
            artifactId = "compiler-plugin"
            version = "0.0.1"

            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])
            suppressPomMetadataWarningsFor("runtime")

            pom {
                name.set("My Kotlin Compiler Plugin")
                description.set("A custom Kotlin compiler plugin for experimentation")
                url.set("https://github.com/stslex/compiler_plugin_workshow")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://raw.githubusercontent.com/stslex/compiler_plugin_workshow/refs/heads/publish_maven/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("stslex")
                        name.set("Ilya")
                        email.set("ilya977.077@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/stslex/compiler_plugin_workshow.git")
                    developerConnection.set("scm:git:ssh://github.com:stslex/compiler_plugin_workshow.git")
                    url.set("https://github.com/stslex/compiler_plugin_workshow")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

val localRepoPath = File(
    "${System.getProperty("user.home")}/.m2/repository/io/github/stslex/compiler-plugin/$version"
)

tasks.named("publishToMavenLocal") {
    finalizedBy(generateChecksums)
    finalizedBy(packageArtifacts)
}

val generateChecksums = tasks.register("generateChecksums") {
    group = "publishing"
    description = "Generate MD5 и SHA1 for all artifacts in local repository"

    doLast {
        if (!localRepoPath.exists()) {
            error("❌ Local repository not found: $localRepoPath")
        }

        val artifacts = localRepoPath.listFiles { file ->
            file.isFile &&
                    file.name.endsWith(".md5").not() &&
                    file.name.endsWith(".sha1").not()
        }

        if (artifacts.isNullOrEmpty()) error("❌ No artifacts found in local repository: $localRepoPath")

        artifacts.forEach { file ->
            val md5File = File(file.parent, "${file.name}.md5")
            val sha1File = File(file.parent, "${file.name}.sha1")

            md5File.writeText(file.md5Hex())
            sha1File.writeText(file.sha1Hex())

            println("✅ Checksums are generated: ${md5File.name}, ${sha1File.name}")
        }
    }
}

val packageArtifacts = tasks.registering(Zip::class) {
    group = "publishing"
    description = "Create ZIP-archive with artifacts for Central Publisher Portal"

    val localRepo = file(localRepoPath)

    if (localRepo.exists().not()) error("Local repo not found: $localRepo")

    from(localRepo) {
        into("io/github/stslex/compiler-plugin/$version/")
        include("**/*.jar", "**/*.pom", "**/*.asc", "**/*.md5", "**/*.sha1")
        exclude("*.module")
        exclude("*.module.*")
    }

    archiveFileName.set("compiler-plugin-${version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
}

fun File.md5Hex(): String = inputStream().use { stream ->
    MessageDigest.getInstance("MD5")
        .digest(stream.readBytes())
        .joinToString("") { "%02x".format(it) }
}

fun File.sha1Hex(): String = inputStream().use { stream ->
    MessageDigest.getInstance("SHA-1")
        .digest(stream.readBytes())
        .joinToString("") { "%02x".format(it) }
}
