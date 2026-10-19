plugins {
    id("com.android.application")
    kotlin("plugin.parcelize")
    id("com.android.legacy-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("dev.zacsweers.moshix")
}

setupMainApk()
setupCoreLib()

kapt {
    correctErrorTypes = true
    useBuildCache = true
    mapDiagnosticLocations = true
    javacOptions {
        option("-Xmaxerrs", "1000")
    }
}

android {
    buildFeatures {
        dataBinding = true
        aidl = true
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    defaultConfig {
        buildConfigField("String", "APP_PACKAGE_NAME", "\"su.android\"")
        buildConfigField("int", "APP_VERSION_CODE", "${Config.versionCode}")
        buildConfigField("String", "APP_VERSION_NAME", "\"${Config.version}\"")
        buildConfigField("int", "STUB_VERSION", Config.stubVersion)
        proguardFile("proguard-rules.pro")
        proguardFile("proguard-rules-core.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}

dependencies {
    implementation(files("../lib/native.jar"))
    api(project(":shared"))
    coreLibraryDesugaring(libs.jdk.libs)

    api(libs.timber)
    api(libs.markwon.core)
    implementation(libs.bcpkix)
    implementation(libs.commons.compress)

    api(libs.libsu.core)
    api(libs.libsu.service)
    api(libs.libsu.nio)

    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.retrofit.scalars)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.dnsoverhttps)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation(libs.core.splashscreen)
    implementation(libs.core.ktx)
    implementation(libs.activity)
    implementation(libs.collection.ktx)
    implementation(libs.profileinstaller)
    implementation(libs.coordinatorlayout)

    compileOnly(libs.test.junit)
    compileOnly(libs.test.uiautomator)

    implementation(libs.indeterminate.checkbox)
    implementation(libs.rikka.layoutinflater)
    implementation(libs.rikka.insets)
    implementation(libs.rikka.recyclerview)

    implementation(libs.navigation.fragment.ktx)

    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.recyclerview)
    implementation(libs.transition)
    implementation(libs.fragment.ktx)
    implementation(libs.appcompat)

    // Make sure kapt runs with a proper kotlin-stdlib
    kapt(kotlin("stdlib"))
}

// The app is built purely on AndroidX/AppCompat: the Google Material library is
// neither used nor wanted, not even as a transitive dependency.
configurations.configureEach {
    exclude(group = "com.google.android.material", module = "material")
}

tasks.matching { it.name.contains("JniLibs") }.configureEach {
    enabled = false
}
