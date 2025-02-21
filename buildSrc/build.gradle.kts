plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

// Remove direct dependency declarations and use compileOnly
dependencies {
    compileOnly("com.android.tools.build:gradle:8.2.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
}
