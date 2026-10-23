package com.khti.fastikvpn

import android.app.Application
import android.util.Log
import com.khti.fastikvpn.data.source.TestVlessSource
import com.khti.fastikvpn.vpn.XrayVersionProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Класс приложения. Android создаёт его ОДИН раз при запуске процесса,
 * раньше любой Activity.
 *
 * Аннотация @HiltAndroidApp запускает Hilt: он генерирует «главный контейнер»
 * зависимостей, который живёт столько же, сколько приложение.
 * В нём будут храниться синглтоны (репозитории, VpnManager и т.д.).
 * Без этой аннотации Hilt не работает вообще.
 */
@HiltAndroidApp
class FastikVpnApplication : Application() {

    // Поля, которые Hilt заполнит сам. Заполнение происходит внутри super.onCreate(),
    // поэтому пользоваться ими можно только после этого вызова
    @Inject lateinit var xrayVersionProvider: XrayVersionProvider
    @Inject lateinit var testVlessSource: TestVlessSource

    override fun onCreate() {
        super.onCreate()

        // Проверку делаем только в отладочной сборке
        if (BuildConfig.DEBUG) {
            logCoreSmokeTest()
        }
    }

    // "Дымовой тест": если строки появились в логе, библиотека подключена, а парсер работает.
    // Саму ссылку и секреты в лог не пишем, toString модели их скрывает.
    private fun logCoreSmokeTest() {
        Log.i(TAG, "Версия Xray: ${xrayVersionProvider.getVersion()}")

        testVlessSource.getConfig()
            .onSuccess { Log.i(TAG, "Сервер разобран: $it") }
            .onFailure { Log.w(TAG, "Не удалось разобрать ссылку: ${it.message}") }
    }

    private companion object {
        const val TAG = "FastikVPN"
    }
}