pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    
    // Version catalog for dependency management
    versionCatalogs {
        create("libs") {
            version("compileSdk", "34")
            version("targetSdk", "34")
            version("minSdk", "26")
            
            // Kotlin and Core Dependencies
            version("kotlin", "1.9.20")
            version("core-ktx", "1.12.0")
            version("appcompat", "1.6.1")
            version("material", "1.11.0")
            
            // Android Architecture Components
            version("lifecycle", "2.7.0")
            version("work-manager", "2.9.0")
            
            // Dependency Injection
            version("hilt", "2.48")
            
            // Networking
            version("retrofit", "2.9.0")
            version("okhttp", "4.12.0")
            
            // Coroutines
            version("coroutines", "1.7.3")
            
            // Testing
            version("junit", "4.13.2")
            version("androidx-test", "1.5.0")
            version("espresso", "3.5.1")
            
            // Libraries
            library("core-ktx", "androidx.core", "core-ktx").versionRef("core-ktx")
            library("appcompat", "androidx.appcompat", "appcompat").versionRef("appcompat")
            library("material", "com.google.android.material", "material").versionRef("material")
            
            // Lifecycle
            library("lifecycle-viewmodel", "androidx.lifecycle", "lifecycle-viewmodel-ktx").versionRef("lifecycle")
            library("lifecycle-livedata", "androidx.lifecycle", "lifecycle-livedata-ktx").versionRef("lifecycle")
            library("lifecycle-runtime", "androidx.lifecycle", "lifecycle-runtime-ktx").versionRef("lifecycle")
            library("lifecycle-service", "androidx.lifecycle", "lifecycle-service").versionRef("lifecycle")
            
            // WorkManager
            library("work-runtime", "androidx.work", "work-runtime-ktx").versionRef("work-manager")
            
            // Hilt
            library("hilt-android", "com.google.dagger", "hilt-android").versionRef("hilt")
            library("hilt-compiler", "com.google.dagger", "hilt-android-compiler").versionRef("hilt")
            
            // Networking
            library("retrofit", "com.squareup.retrofit2", "retrofit").versionRef("retrofit")
            library("retrofit-gson", "com.squareup.retrofit2", "converter-gson").versionRef("retrofit")
            library("okhttp", "com.squareup.okhttp3", "okhttp").versionRef("okhttp")
            library("okhttp-logging", "com.squareup.okhttp3", "logging-interceptor").versionRef("okhttp")
            
            // Coroutines
            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("coroutines")
            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("coroutines")
            
            // Testing
            library("junit", "junit", "junit").versionRef("junit")
            library("androidx-test-junit", "androidx.test.ext", "junit").versionRef("androidx-test")
            library("androidx-test-core", "androidx.test", "core").versionRef("androidx-test")
            library("espresso", "androidx.test.espresso", "espresso-core").versionRef("espresso")
        }
    }
}

rootProject.name = "AndroidTelegramFile"

// Include app module
include(":app")
