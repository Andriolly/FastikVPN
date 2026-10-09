// Корневой файл сборки. Здесь только ОБЪЯВЛЯЮТСЯ плагины (apply false),
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false // компилятор Compose
    alias(libs.plugins.ksp) apply false            // обработчик аннотаций для Hilt
    alias(libs.plugins.hilt) apply false           // сам Hilt
}