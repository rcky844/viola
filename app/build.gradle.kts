// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp")
}

fun getGitInfo(info: String): String {
    return try {
        val output = providers.exec {
            commandLine = when (info) {
                "revision" -> "git describe --match=NeVeRmAtCh --always --dirty"
                "branch" -> "git rev-parse --abbrev-ref HEAD"
                else -> "UNKNOWN"
            }.split(" ")
        }
        output.standardOutput.asText.get().trim()
    } catch (e: Exception) {
        // Unlike before, allow the build to continue without Git
        logger.warn("Unable to get Git information: ${e.message}." +
                "Did you install and place Git in a PATH on your system?")
        "UNKNOWN"
    }
}

android {
    namespace = "tipz.viola"
    compileSdk = 36

    defaultConfig {
        applicationId = "tipz.viola"
        minSdk = 14
        targetSdk = 36
        versionCode = 1
        versionName = "8.1"

        /* Extra build info */
        buildConfigField("String", "PRODUCT_LICENSE_DOCUMENT",
            "\"Apache License, Version 2.0\"")
        buildConfigField("String", "VERSION_CODENAME", "\"Francesco\"")
        buildConfigField("String", "VERSION_COPYRIGHT_YEAR", "\"2020-2025\"")
        buildConfigField("String", "VERSION_BUILD_EXTRA", "\"\"")
        buildConfigField("String", "VERSION_BUILD_ID", "\"${versionCode}\"")
        buildConfigField("int", "VERSION_BUILD_REVISION", "0")
        buildConfigField("String", "VERSION_BUILD_GIT_REVISION",
            "\"${getGitInfo("revision")}\"")
        buildConfigField("String", "VERSION_BUILD_BRANCH",
            "\"${getGitInfo("branch")}\"")
        buildConfigField("long", "VERSION_BUILD_TIMESTAMP",
            "${System.currentTimeMillis()}")

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        multiDexEnabled = true
        resourceConfigurations += listOf("ar", "bn", "de", "en-rGB", "es-rES", "fr", "hi",
            "it", "pt-rBR", "ru", "tr", "vi", "zh-rCN", "zh-rTW")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        // Stable release
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "VERSION_BUILD_TYPE", "\"stable\"")
        }

        // Next release
        create("next") {
            initWith(getByName("release"))
            buildConfigField("String", "VERSION_BUILD_TYPE", "\"next\"")
        }

        // Debug internal
        getByName("debug") {
            applicationIdSuffix = ".debug"

            buildConfigField("String", "VERSION_BUILD_TYPE", "\"debug\"")
            buildConfigField("String", "VERSION_BUILD_ID", "null")
        }
    }

    productFlavors {
        create("modern") {
            dimension = "apilevel"
            minSdk = 21
        }
        create("legacy") {
            dimension = "apilevel"
            minSdk = 14
            maxSdk = 20
            buildConfigField("String", "VERSION_BUILD_BRANCH",
                "\"${getGitInfo("branch")}_legacy\"")
            vectorDrawables.useSupportLibrary = true
        }
    }
    flavorDimensions.add("apilevel")

    sourceSets {
        // Next release
        named("next") {
            res.srcDir("src/next/res")
        }

        // Debug internal
        named("debug") {
            res.srcDir("src/debug/res")
        }
    }

    // Disables dependency metadata when building APKs and Android App Bundles.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    // Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // AndroidX
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.preference:preference-ktx:1.2.1") {
        exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel")
        exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel-ktx")
    }
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.webkit:webkit:1.14.0")
    annotationProcessor("androidx.annotation:annotation:1.9.1")

    // AndroidX Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Others
    implementation("cat.ereza:customactivityoncrash:2.3.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // For compatibility
    // Modern dependencies
    val modernImplementation by configurations
    modernImplementation("androidx.core:core-ktx:1.16.0")
    modernImplementation("androidx.appcompat:appcompat:1.7.1")
    modernImplementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
    modernImplementation("com.google.android.material:material:1.12.0")

    // Legacy dependencies
    // These will remain on their currently defined versions indefinitely,
    // since these are the last known versions to support API 14+
    val legacyImplementation by configurations
    legacyImplementation("androidx.core:core-ktx:1.12.0")
    legacyImplementation("androidx.appcompat:appcompat:1.6.1")
    legacyImplementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    legacyImplementation("com.google.android.material:material:1.11.0")
}
