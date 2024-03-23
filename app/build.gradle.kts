plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.github.auag0.imnotadeveloper"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.auag0.imnotadeveloper"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/kotlin/**"
            excludes += "/kotlin-tooling-metadata.json"
        }
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
    //compileOnly("de.robv.android.xposed:api:82:sources")
}