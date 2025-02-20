plugins {
    // Core plugins - Use version catalog instead of hardcoded versions
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    
    // Dependency injection
    alias(libs.plugins.hilt) apply false
    
    // Static code analysis
    alias(libs.plugins.detekt)
    
    // Dependency updates checker
    alias(libs.plugins.versions)
}

// Remove extra version definitions since they're in version catalog
allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    afterEvaluate {
        dependencies {
            "detektPlugins"(libs.detekt.formatting)
        }
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { 
            candidate.version.uppercase().contains(it) 
        }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(candidate.version)
        !isStable
    }
    gradleReleaseChannel = "current"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
