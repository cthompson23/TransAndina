import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.0"
}

// Configuración de Supabase leída desde local.properties (no se sube al repositorio).
// Se lee con providers.fileContents para que Gradle registre el archivo como
// entrada del configuration cache y lo invalide cuando cambie.
val propiedadesLocales = Properties().apply {
    providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText.orNull
        ?.let { load(it.reader()) }
}

fun propiedadLocal(nombre: String): String =
    propiedadesLocales.getProperty(nombre)?.trim()?.takeIf { it.isNotEmpty() }
        ?: throw GradleException(
            "Falta la propiedad '$nombre' en local.properties (raíz del proyecto). " +
                "Copia las líneas de local.properties.example a tu local.properties " +
                "y pide los valores reales al equipo."
        )

val supabaseUrl = propiedadLocal("SUPABASE_URL")
val supabasePublishableKey = propiedadLocal("SUPABASE_PUBLISHABLE_KEY")

android {
    namespace = "com.transandina.flotilla"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.transandina.flotilla"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"$supabasePublishableKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.3"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    // Fotos de evidencia de los mantenimientos (bucket `mantenimientos`)
    implementation("io.github.jan-tennert.supabase:storage-kt")

    // Carga de imágenes desde las URLs firmadas del bucket.
    // 3.3.0 es la última que compila contra Kotlin 2.2; las más nuevas piden
    // una versión de Kotlin mayor a la del proyecto.
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    // Cliente HTTP requerido por supabase-kt
    implementation("io.ktor:ktor-client-android:3.0.0")

    // Serialización (para los data class @Serializable)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Navegación entre pantallas Compose
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // ViewModel + Compose (probablemente ya los tengas, revisa duplicados)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.material:material-icons-extended")
}