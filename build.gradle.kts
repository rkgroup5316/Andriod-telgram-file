import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    // Core plugins
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    kotlin("android") version "1.9.20" apply false
    kotlin("kapt") version "1.9.20" apply false
    
    // Dependency injection
    id("com.google.dagger.hilt.android") version "2.48" apply false
    
    // Static code analysis
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
    
    // Dependency updates checker
    id("com.github.ben-manes.versions") version "0.47.0"
}

// Configuration for all projects (root + sub-projects)
allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Version catalog for dependency management
extra["compileSdkVersion"] = 34
extra["targetSdkVersion"] = 34
extra["minSdkVersion"] = 26  // Android 8.0 as required

subprojects {
    // Apply static code analysis to all subprojects
    apply(plugin = "io.gitlab.arturbosch.detekt")

    // Common configurations for all modules
    afterEvaluate {
        // Configure detekt for each subproject
        dependencies {
            "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.3")
        }
    }
}

// Task to check for dependency updates
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        // Reject non-stable versions for dependency updates
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { 
            candidate.version.uppercase().contains(it) 
        }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(candidate.version)
        !isStable
    }
    
    // Check for gradle updates
    gradleReleaseChannel = "current"
}

// Common build configuration
tasks {
    // Register a clean task
    register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }
}
