plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.agp)  // Use version from catalog instead
    implementation(libs.kgp)  // Use version from catalog instead
}
