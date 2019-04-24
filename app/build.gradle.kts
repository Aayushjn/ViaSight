plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.aayush.viasight"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.30")
    implementation("androidx.appcompat:appcompat:1.1.0-alpha04")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    implementation("com.google.android.material:material:1.0.0")
    implementation("com.google.code.gson:gson:2.8.5")

    implementation("com.jakewharton.timber:timber:4.7.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.1")

    androidTestImplementation("androidx.test.ext:junit:1.1.0")
    androidTestImplementation("androidx.test:runner:1.2.0-alpha04")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0-alpha04")
}
