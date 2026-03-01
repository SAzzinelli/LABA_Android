plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.laba.firenze"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.laba.firenze"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        // ImgBB API key: add IMGBB_API_KEY to local.properties or gradle.properties
        val imgbbKey = project.findProperty("IMGBB_API_KEY") as? String ?: ""
        buildConfigField("String", "IMGBB_API_KEY", "\"$imgbbKey\"")
        // SuperSaas API key: add SUPERSAAS_API_KEY to local.properties or gradle.properties
        val superSaasKey = project.findProperty("SUPERSAAS_API_KEY") as? String ?: "tTu_7aaIHDWCQGuGz_4cQA"
        buildConfigField("String", "SUPERSAAS_API_KEY", "\"$superSaasKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug") // Usa la firma di debug
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
    
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            // KT-73255: applica annotazioni (es. @Inject) sia al parametro che al field
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    lint {
        abortOnError = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts += "assets/faq.json"
        }
    }
}

// Workaround: alcuni plugin/IDE si aspettano il task testClasses (tipico del plugin Java).
// Nei moduli Android non esiste; registriamo un no-op per evitare "task testClasses not found".
tasks.register("testClasses") {
    group = "verification"
    description = "Dummy task for plugin/IDE compatibility (Android app module has no Java test source set)."
}

// Workaround: check*Classpath fallisce con "Cannot fingerprint compileVersionMap" (AGP 8.7 + Gradle 8.9)
tasks.matching { it.name.startsWith("check") && it.name.endsWith("Classpath") }.configureEach {
    enabled = false
}

// Allineamento versioni Kotlin - usa versione compatibile
configurations.all {
    resolutionStrategy {
        // Kotlin stdlib allineato alla versione del plugin (2.2.10)
        force("org.jetbrains.kotlin:kotlin-stdlib:2.2.10")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.2.10")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.10")
        force("org.jetbrains.kotlin:kotlin-stdlib-common:2.2.10")
        // kotlinx-coroutines aggiornato per compatibilità con Kotlin 2.2.10
        force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        force("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-compiler:2.57.2")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    
    // Kotlin coroutines extensions for Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // DataStore & Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Date & Time
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Work Manager (for background tasks)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Security Crypto (for Keychain equivalent)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // ZXing for QR code generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Task per rimuovere file duplicati prima della build
tasks.register("cleanDuplicates") {
    doLast {
        val buildDir = file("build/intermediates/classes/debug/transformDebugClassesWithAsm/dirs")
        if (buildDir.exists()) {
            buildDir.walkTopDown().forEach { file ->
                if (file.isFile && file.name.matches(Regex(".* \\d+\\.class$"))) {
                    file.delete()
                    println("Rimosso file duplicato: ${file.name}")
                }
            }
        }
    }
}

// Esegui cleanDuplicates prima di ogni build
tasks.named("preBuild").configure {
    dependsOn("cleanDuplicates")
}
