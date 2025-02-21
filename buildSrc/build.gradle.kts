plugins {
    `kotlin-dsl`
}
repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.agp)  // Use version catalog reference
    compileOnly(libs.kgp)  // Use version catalog reference
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

kotlin {
    jvmToolchain(17)
}
