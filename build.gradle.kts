import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    kotlin("android") apply false
    alias(libs.plugins.compose-compiler) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.versions)
    cleanup
    base
}


subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    afterEvaluate {
        dependencies {
            "detektPlugins"(libs.detekt.formatting)
        }
    }
}

tasks {
    withType<DependencyUpdatesTask>().configureEach {
        rejectVersionIf {
            candidate.version.isStableVersion().not()
        }
    }
}
