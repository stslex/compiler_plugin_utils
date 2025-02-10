plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.stslex.compiler_app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    namespace = "com.stslex.compiler_app.app"
}

dependencies {
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.lifecycle.viewModel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.constraintlayout)
}
