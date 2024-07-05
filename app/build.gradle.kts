@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.example.security"
  compileSdk = 34
  ndkVersion = "26.3.11579264"

  defaultConfig {
    applicationId = "com.example.security"
    minSdk = 21
    //noinspection OldTargetApi
    targetSdk = 33
    versionCode = 1
    versionName = "0.0.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    externalNativeBuild {
      cmake {
        cppFlags += "-std=c++17"
      }
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isDebuggable = false
      isShrinkResources = true
      signingConfig = signingConfigs.getByName("debug")
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
    create("profileable") {
      isDebuggable = false
      signingConfig = signingConfigs.getByName("debug")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
  buildFeatures {
    compose = true
  }
  composeCompiler {
    enableStrongSkippingMode = true
  }
  packaging {
    resources.excludes.add("META-INF/AL2.0")
    resources.excludes.add("META-INF/LGPL2.1")
  }
  kotlin {
    sourceSets.debug {
      kotlin.srcDir("build/generated/ksp/debug/kotlin")
    }
    sourceSets.release {
      kotlin.srcDir("build/generated/ksp/release/kotlin")
    }
  }
  externalNativeBuild {
    cmake {
      path = file("src/main/cpp/CMakeLists.txt")
      version = "3.22.1"
    }
  }
}

dependencies {
  implementation(libs.core.ktx)
  implementation(libs.appcompat)

  implementation(libs.trustkit)
  implementation(libs.okhttp)

  // Compose ---------------------------------------------------------------------------------------
  implementation(libs.bundles.compose)

  debugImplementation(libs.bundles.compose.debug)
  // UI Tests
  androidTestImplementation(libs.androidx.ui.test.junit4)

  // Unit Test -------------------------------------------------------------------------------------
  testImplementation(libs.junit)

  // Android Test ----------------------------------------------------------------------------------
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.espresso.core)

  // Leakcanary ------------------------------------------------------------------------------------
  debugImplementation(libs.leakcanary.android)
}
