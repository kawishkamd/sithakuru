plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "sithakuru.sinhala.keyboard"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "sithakuru.sinhala.keyboard"
        minSdk = 21
        targetSdk = 37
        versionCode = 20
        versionName = "2.0.0"
    }
    buildTypes {
        release {
            optimization { enable = true }
            proguardFiles("proguard-rules.pro")
        }
    }
    flavorDimensions += "app_id"
    productFlavors { create("sithakuru") }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { viewBinding = true }
}

dependencies {
    implementation(libs.androidx.core)
}
