package com.khti.fastikvpn

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

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
class FastikVpnApplication : Application()