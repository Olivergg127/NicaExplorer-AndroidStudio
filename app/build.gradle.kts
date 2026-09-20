import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    val signingPropertiesFile = rootProject.file("keystore.properties")
    val signingProperties = Properties()
    if (signingPropertiesFile.exists()) {
        signingProperties.load(FileInputStream(signingPropertiesFile))
    }

    // Configuración del backend (no versionada): local.properties -> nica.apiBaseUrl / nica.apiKey
    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }
    val nicaApiBaseUrl = localProperties.getProperty("nica.apiBaseUrl") ?: "https://nicaexplorer-backend.onrender.com"
    val nicaApiKey = localProperties.getProperty("nica.apiKey") ?: ""

    namespace = "com.lospuntoycoma.nicaexplorer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lospuntoycoma.nicaexplorer"
        minSdk = 30
        targetSdk = 34
        versionCode = 3
        versionName = "1.1.1"

        // Backend NicaExplorer (CI4 + Firestore). Se configura en local.properties.
        buildConfigField("String", "API_BASE_URL", "\"$nicaApiBaseUrl\"")
        buildConfigField("String", "API_KEY", "\"$nicaApiKey\"")


        vectorDrawables {
            useSupportLibrary = true
        }

        // Unity solo genera binarios arm64-v8a. Sin este filtro, los AAR de ARCore
        // meten librerías x86/x86_64 al APK y el instalador elige esa ABI en
        // emuladores, dejando fuera libgame.so (UnsatisfiedLinkError al abrir RA).
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        create("release") {
            if (!signingPropertiesFile.exists()) {
                throw GradleException("Falta keystore.properties para firmar la build release")
            }
            storeFile = file(signingProperties.getProperty("storeFile"))
            storePassword = signingProperties.getProperty("storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.08.00")
    implementation(composeBom)

    implementation(project(":unityLibrary"))
    implementation(files("${rootProject.projectDir}/unityLibrary/libs/unity-classes.jar"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Backend OpenGL ES para mayor compatibilidad con dispositivos Android.
    // La variante android-sdk usa Vulkan por defecto y provoca un crash nativo
    // en el dispositivo de prueba al renderizar el MapView.
    implementation("org.maplibre.gl:android-sdk-opengl:12.3.1")

    implementation(platform("com.google.firebase:firebase-bom:34.16.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-ai")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
