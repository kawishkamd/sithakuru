import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

/*
 * Release signing.
 *
 * CI provides the key material through environment variables (see
 * docs/RELEASING.md); a local build can instead use an untracked
 * keystore.properties file in the repo root:
 *
 *   storeFile=app/release.jks
 *   storePassword=...
 *   keyAlias=...
 *   keyPassword=...
 *
 * Without either, release builds fall back to the debug key so that the
 * project still builds. Those APKs cannot be published and cannot update an
 * APK signed with the real release key.
 */
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun signingValue(property: String, environment: String): String? =
    keystoreProperties.getProperty(property) ?: System.getenv(environment)?.takeIf { it.isNotBlank() }

val releaseKeystorePath = signingValue("storeFile", "KEYSTORE_FILE")

val releaseKeystore: File? = releaseKeystorePath?.let { path ->
    file(path).takeIf { it.exists() } ?: rootProject.file(path).takeIf { it.exists() }
}

if (releaseKeystorePath != null && releaseKeystore == null) {
    logger.warn("Release keystore '$releaseKeystorePath' not found; falling back to the debug key.")
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
    signingConfigs {
        val keystore = releaseKeystore
        if (keystore != null) {
            create("release") {
                storeFile = keystore
                storePassword = signingValue("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            optimization { enable = true }
            proguardFiles("proguard-rules.pro")
            signingConfig = if (releaseKeystore != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
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
    testImplementation("junit:junit:4.13.2")
}
