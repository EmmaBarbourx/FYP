plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

apply(from = "download.gradle")

android {
    namespace = "com.example.injuryrecoveryapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.injuryrecoveryapplication"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Ensures .tflite files aren't compressed
    aaptOptions {
        noCompress("tflite")
    }
}



dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.ui.graphics.android)
    implementation(libs.preference)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")

    // CameraX
    implementation("androidx.camera:camera-core:1.2.2")
    implementation("androidx.camera:camera-camera2:1.2.2")
    implementation("androidx.camera:camera-lifecycle:1.2.2")
    implementation("androidx.camera:camera-view:1.2.2")

    implementation("com.google.guava:guava:31.1-android")

    // TFLite base API
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.3.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.5.0")





}
