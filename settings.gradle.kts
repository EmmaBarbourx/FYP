pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*");
                includeGroupByRegex("com\\.google.*");
                includeGroupByRegex("androidx.*");
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // JitPack (already in your file)
        maven("https://jitpack.io")
    }
}

rootProject.name = "InjuryRecoveryApplication"
include(":app")
