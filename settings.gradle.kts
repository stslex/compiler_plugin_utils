pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        flatDir {
            dirs("../compiler-plugin-lib/build/libs")
        }
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}


rootProject.name = "CompilerPlugin"

include(":compiler-plugin")
include(":app")