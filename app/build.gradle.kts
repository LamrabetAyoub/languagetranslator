plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.languagetranslator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.languagetranslator"
        minSdk = 30
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
}
val room_version = "2.5.2"

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Core Libraries
    implementation ("androidx.core:core:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")

    // Networking
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")

    // Lifecycle and ViewModel
    implementation ("androidx.lifecycle:lifecycle-runtime:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.6.1")

    // Room Database
    implementation ("androidx.room:room-runtime:$room_version")
    annotationProcessor ("androidx.room:room-compiler:$room_version")
    implementation ("androidx.room:room-rxjava2:$room_version")

    // ML Kit for Text Recognition
    implementation ("com.google.mlkit:text-recognition:16.0.0-beta1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
