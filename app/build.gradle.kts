import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Читаем файл local.properties. В нём лежат секреты, которые нельзя отправлять в Git.
// Блок стоит ПОСЛЕ plugins { }: до него в Gradle разрешены только import и несколько служебных блоков
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    // Файла может не быть (например, на чужом компьютере), поэтому проверяем
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.khti.fastikvpn"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.khti.fastikvpn"
        minSdk = 26
        targetSdk = 36
        versionCode = 10002
        versionName = "1.0.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Тестовая ссылка попадает в класс BuildConfig как строковая константа.
        // Кавычки и слэши экранируем, иначе они сломают сгенерированный код.
        val testLink = localProperties.getProperty("VLESS_TEST_LINK", "")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        buildConfigField("String", "VLESS_TEST_LINK", "\"$testLink\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    // Включаем Jetpack Compose в проекте
    buildFeatures {
        compose = true

        // Без этой строки класс BuildConfig не создаётся
        buildConfig = true
    }
}

// В Kotlin 2.2 старый блок kotlinOptions убран, используется compilerOptions
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // Базовые библиотеки
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)

    // Compose: BOM задаёт согласованные версии всех библиотек Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Hilt: библиотека + обработчик аннотаций (ksp) + интеграция с Compose
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Тесты
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Системная заставка: держит экран, пока не загрузится тема (убирает мигание)
    implementation(libs.androidx.core.splashscreen)

    // Ядро Xray, собранное под Android (положили файл в app/libs вручную)
    implementation(files("libs/libv2ray.aar"))

    // Настоящая библиотека JSON для юнит-тестов: в тестах на компьютере
    // версия из Android недоступна. Версию при необходимости подскажет студия
    testImplementation("org.json:json:20240303")
}