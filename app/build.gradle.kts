plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.appointmentlistapp"
    compileSdk = 36 // Recommended to keep in sync with targetSdk

    defaultConfig {
        applicationId = "com.example.appointmentlistapp"
        minSdk = 24
        targetSdk = 36 // Keeping your chosen target SDK version
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // Updated to a compatible version with the latest Compose BOM
        kotlinCompilerExtensionVersion = "1.5.15"
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

    // Core Lifecycle and Navigation
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.navigation:navigation-compose:2.9.4")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(libs.runtime)

    // CameraX dependencies (using the latest stable version)
    val camerax_version = "1.5.0"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")

    // ML Kit Barcode Scanning (updated to the latest stable version)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Compose Icons (updated to the latest stable version)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Other dependencies
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.runtime)
    implementation(libs.ui) // This dependency is duplicated with 'androidx.ui' and 'androidx.compose.ui'
    implementation(libs.transportation.consumer)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.play.services.fitness)

    // Test dependencies
    testImplementation("junit:junit:4.13.2")

    // Coroutines testing library
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // AndroidX Core testing for InstantTaskExecutorRule
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson converter (for JSON serialization/deserialization)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Kotlin Coroutine support for Retrofit
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")



    // Hilt ViewModel integration


}