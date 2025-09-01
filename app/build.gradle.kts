plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.appointmentlistapp"
    compileSdk = 36 // Keeping your chosen compile SDK version

    defaultConfig {
        applicationId = "com.example.appointmentlistapp"
        minSdk = 24
        targetSdk = 36 // Keeping your chosen target SDK version
        versionCode = 1
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Keeping your chosen Java source compatibility
        targetCompatibility = JavaVersion.VERSION_11 // Keeping your chosen Java target compatibility
    }
    kotlinOptions {
        jvmTarget = "11" // Keeping your chosen JVM target
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // Ensure this version is compatible with your AGP and Kotlin version
        kotlinCompilerExtensionVersion = "1.5.1" // Keeping your Compose compiler extension version
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Lifecycle ViewModel Compose (removed duplicate, kept the latest stable)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // CameraX dependencies (using a consistent version variable for clarity and easy updates)
    val camerax_version = "1.3.3" // This is the current latest stable version for CameraX
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}") // For CameraX extensions (optional but recommended)

    // ML Kit Barcode Scanninimplementation("androidx.navigation:navigation-compose:2.9.0-alpha01")
// Use the latest versiong
    implementation("com.google.mlkit:barcode-scanning:17.2.0") // This is the current latest stable version for ML Kit Barcode Scanning
    implementation("androidx.compose.material:material-icons-extended:1.7.0-beta01") // Use the latest stable or beta if targeting newer Compose BOM
    implementation("io.coil-kt:coil-compose:2.6.0")


    implementation("androidx.navigation:navigation-compose:2.9.0-alpha01")
// Use the latest version


}