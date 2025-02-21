plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.rkgroup.app"
    compileSdk = 34  // Direct value instead of version catalog

    defaultConfig {
        applicationId = "com.rkgroup.app"
        minSdk = 24    // Direct value instead of version catalog
        targetSdk = 34 // Direct value instead of version catalog
        versionCode = 1
        versionName = "1.0.0"
        multiDexEnabled = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            buildConfigField("String", "TELEGRAM_BOT_TOKEN", 
                "\"${System.getenv("TELEGRAM_BOT_TOKEN") ?: "default_debug_token"}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "TELEGRAM_BOT_TOKEN", 
                "\"${System.getenv("TELEGRAM_BOT_TOKEN") ?: ""}\"")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE*"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraint.layout)
    
    // Lifecycle Components
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.service)
    
    // WorkManager
    implementation(libs.work.runtime)
    
    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    
    // Hilt DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.coil)
    
    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    
    // Security
    implementation(libs.security.crypto)
    implementation(libs.biometric)
    
    // Debug Tools
    debugImplementation(libs.timber)
    debugImplementation(libs.leakcanary)

    // Desugaring
    coreLibraryDesugaring(libs.android.desugar)
}

kapt {
    correctErrorTypes = true
}
