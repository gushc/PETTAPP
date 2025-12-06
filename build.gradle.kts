// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false

    // 1. DECLARACIÓN DEL PLUGIN DE GOOGLE SERVICES
    // Esto hace que el plugin esté disponible para ser aplicado en los módulos (como 'app').
    id("com.google.gms.google-services") version "4.4.0" apply false // Usa la versión más reciente que necesites
}