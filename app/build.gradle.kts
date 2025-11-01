import java.util.Properties


plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id("com.google.devtools.ksp")
    id ("kotlin-parcelize")
    id ("com.google.dagger.hilt.android")
}

android {

    val properties = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }

    val movieApiKey = properties.getProperty("movieApiKey")

    namespace = "com.example.movieflix"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.movieflix"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


    }

    buildTypes {
        release {
            buildConfigField("String", "MOVIE_API_KEY", movieApiKey)
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        debug {

            buildConfigField("String", "MOVIE_API_KEY", movieApiKey)
            applicationIdSuffix=".debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    kapt {
        correctErrorTypes=true
    }

    buildFeatures{
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    //Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")

    //Image Loading
    implementation ("com.github.bumptech.glide:glide:5.0.5")
    ksp ("com.github.bumptech.glide:compiler:5.0.5")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.9.4")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")


    // Airbnb lottie animation
    implementation("com.airbnb.android:lottie:6.6.10")

    // Intuit ssp & sdp
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation ("com.intuit.ssp:ssp-android:1.1.1")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // ScrollingPagerIndicator
    implementation ("ru.tinkoff.scrollingpagerindicator:scrollingpagerindicator:1.2.5")

    //Retrofit2 + okhttp
    implementation ("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation ("com.squareup.retrofit2:retrofit:3.0.0")
    implementation ("com.squareup.okhttp3:okhttp:5.2.1")
    implementation ("com.squareup.retrofit2:converter-scalars:3.0.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.2.1")


    //Dagger - Hilt
    implementation ("com.google.dagger:hilt-android:2.57.2")
    kapt ("com.google.dagger:hilt-compiler:2.57.2")

    //Shimmer
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    // VideoPlayer
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0")

    // Chrome Custom Tab
    implementation ("androidx.browser:browser:1.9.0")

    //Room Database
    implementation ("androidx.room:room-runtime:2.8.3")
    ksp ("androidx.room:room-compiler:2.8.3")
    implementation ("androidx.room:room-ktx:2.8.3")

    // WorkManager for scheduled notifications
    implementation ("androidx.work:work-runtime-ktx:2.10.0")

}