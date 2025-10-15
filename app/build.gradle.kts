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
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


    }

    buildTypes {
        release {
            buildConfigField("String", "MOVIE_API_KEY", "\"$movieApiKey\"")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        debug {

            buildConfigField("String", "MOVIE_API_KEY", "\"$movieApiKey\"")
            applicationIdSuffix=".debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    //Image Loading
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    ksp ("com.github.bumptech.glide:compiler:4.16.0")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.7")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")


    // Airbnb lottie animation
    implementation("com.airbnb.android:lottie:6.3.0")

    // Intuit ssp & sdp
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation ("com.intuit.ssp:ssp-android:1.1.1")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // ScrollingPagerIndicator
    implementation ("ru.tinkoff.scrollingpagerindicator:scrollingpagerindicator:1.2.4")

    //Retrofit2 + okhttp
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.okhttp3:okhttp:5.0.0-alpha.3")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.3")


    //Dagger - Hilt
    implementation ("com.google.dagger:hilt-android:2.54")
    kapt ("com.google.dagger:hilt-compiler:2.54")

    //Shimmer
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    // VideoPlayer
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0")

    // Chrome Custom Tab
    implementation ("androidx.browser:browser:1.8.0")

    //Room Database
    implementation ("androidx.room:room-runtime:2.7.2")
    ksp ("androidx.room:room-compiler:2.7.2")
    implementation ("androidx.room:room-ktx:2.7.2")

}