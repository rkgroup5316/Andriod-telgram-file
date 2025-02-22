pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = ("Andriod-telgram-file")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "app"
)
