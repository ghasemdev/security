@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.androidx.benchmark)
  alias(libs.plugins.org.jetbrains.kotlin.android)
}

android {
  namespace = "com.parsuomash.benchmark"
  compileSdk = 34

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }

  defaultConfig {
    minSdk = 21
    targetSdk = 34

    testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
  }

  testBuildType = "release"
  buildTypes {
    debug {
      // Since isDebuggable can"t be modified by gradle for library modules,
      // it must be done in a manifest - see src/androidTest/AndroidManifest.xml
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "benchmark-proguard-rules.pro"
      )
    }
    release {
      isDefault = true
    }
  }
}

dependencies {
  androidTestImplementation(libs.runner)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.junit)
  androidTestImplementation(libs.benchmark.junit4)
  // Add your dependencies here. Note that you cannot benchmark code
  // in an app module this way - you will need to move any code you
  // want to benchmark to a library module:
  // https://developer.android.com/studio/projects/android-library#Convert

}