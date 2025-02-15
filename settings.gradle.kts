pluginManagement {
    includeBuild("compiler-plugin")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()

        // for getting plugin from local maven repository
        // mavenLocal()
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

include(":app")