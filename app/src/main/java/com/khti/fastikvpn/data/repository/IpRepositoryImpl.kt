package com.khti.fastikvpn.data.repository

import com.khti.fastikvpn.domain.repository.IpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

// Реализация: ходим в интернет на публичные сервисы, которые отвечают
// одной строкой с IP-адресом того, кто к ним обратился.
class IpRepositoryImpl @Inject constructor() : IpRepository {

    override suspend fun getPublicIp(): String? =
    // Запросы в сеть нельзя делать в главном потоке: экран бы замёрз,
        // а Android выдал бы ошибку. Поэтому переходим в фоновый поток для сети и файлов.
        withContext(Dispatchers.IO) {
            // Пробуем сервисы по очереди. Если первый не ответил, идём ко второму.
            for (address in SOURCES) {
                val ip = requestIp(address)
                if (ip != null) {
                    return@withContext ip
                }
            }
            // Ни один сервис не ответил
            null
        }

    // Один запрос к одному сервису. Возвращает адрес или null, если что-то пошло не так.
    private fun requestIp(address: String): String? {
        return try {
            val connection = URL(address).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            // Таймауты нужны, чтобы приложение не ждало ответа бесконечно
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS

            try {
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    // Читаем весь ответ как текст и убираем пробелы и перенос строки по краям
                    val text = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                    // Защита: принимаем ответ, только если он похож на IP-адрес
                    if (IP_REGEX.matches(text)) text else null
                } else {
                    null
                }
            } finally {
                // Соединение закрываем в любом случае
                connection.disconnect()
            }
        } catch (e: IOException) {
            // Нет интернета, сервис недоступен, вышло время ожидания и т.д.
            null
        }
    }

    private companion object {
        // Адреса сервисов. Если первый недоступен, используем второй
        val SOURCES = listOf(
            "https://api.ipify.org",
            "https://icanhazip.com"
        )

        const val TIMEOUT_MS = 5_000

        // Допустимые символы: цифры, буквы a-f, точки и двоеточия (для IPv4 и IPv6)
        val IP_REGEX = Regex("^[0-9a-fA-F:.]{3,45}$")
    }
}