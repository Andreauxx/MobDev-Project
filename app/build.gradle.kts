plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.quadrants.memorix"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.quadrants.memorix"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation("androidx.compose.material3:material3:1.2.0-alpha02") // Or latest stable version


    implementation("com.google.code.gson:gson:2.10.1") // ✅ Use the latest version
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.compose.foundation:foundation:1.5.0")

    //DEPENDENCY FOR CHATGPT
    implementation ("com.squareup.okhttp3:okhttp:4.9.3") // For API calls
    implementation ("com.google.code.gson:gson:2.8.8")   // For JSON parsing

    //PFPS DEPENDENCIESSS CLOUDINARY
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.1.0")


    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation ("androidx.compose.material:material-icons-extended:1.5.0")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-analytics")

    implementation ("com.google.firebase:firebase-auth:22.3.1")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    // implementation("com.google.accompanist:@acompanist-pager:0.12.0")
    implementation("androidx.paging:paging-compose:1.0.0-alpha20")
    implementation("androidx.compose.foundation:foundation:1.5.4") // Latest Compose Foundation
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.8.0")

    // ✅ Correct Accompanist dependencies
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.0.0")
    // ✅ Lottie for animations (Already correct)
    implementation("com.airbnb.android:lottie-compose:5.2.0")

    // ✅ Navigation Components (Already correct)
    implementation("androidx.navigation:navigation-compose:2.7.1")

    // ✅ Jetpack Compose Dependencies (Make sure these are properly linked)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.storage.ktx)

    // ✅ Testing Dependencies (Already correct)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}