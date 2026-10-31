import java.io.StringReader
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// --- Seguridad: la API Key se lee desde local.properties (NO se sube a GitHub) ---
val localProps = Properties()
providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText.orNull?.let { localProps.load(StringReader(it)) }
val mapsApiKey: String = localProps.getProperty("MAPS_API_KEY") ?: ""

android {
    namespace = "com.example.hansgpsmapas"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.hansgpsmapas"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // Inyecta la clave en el AndroidManifest (${MAPS_API_KEY})
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Seguridad: activa R8 (reduce, optimiza y ofusca el código)
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Google Maps + ubicación (rúbrica: play-services-maps y play-services-location)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
