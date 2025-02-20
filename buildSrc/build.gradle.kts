plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.2.0")  // Use direct version temporarily
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")  // Use direct version temporarily
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
