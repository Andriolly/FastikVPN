package com.khti.fastikvpn.vpn

// Разбирает строку со счётчиками, которую отдаёт ядро Xray.
// Формат: "имя_выхода,направление,число;имя_выхода,направление,число;"
// Пример: "proxy,uplink,1200;proxy,downlink,54000;"
//
// Важно: после каждого запроса ядро обнуляет счётчики, поэтому здесь приходят только
// приращения (дельты) с прошлого запроса. Складывать их в общий итог нужно самим.
object TrafficStatsParser {

    // Результат разбора: сколько байт ушло и пришло с прошлого запроса
    data class Delta(val uploadBytes: Long, val downloadBytes: Long)

    // outboundTag - имя выхода, трафик которого считаем. У нас основной выход называется proxy
    fun parse(raw: String, outboundTag: String = "proxy"): Delta {
        var upload = 0L
        var download = 0L

        // Режем строку на записи, пустые отбрасываем (после последней ";" остаётся пустая часть)
        raw.split(';')
            .filter { it.isNotBlank() }
            .forEach { entry ->
                // Каждая запись должна состоять ровно из трёх частей
                val parts = entry.split(',')
                if (parts.size != 3) return@forEach

                // Чужие выходы (например, direct) нас не интересуют
                if (parts[0] != outboundTag) return@forEach

                // Если число не разобралось, пропускаем запись, а не падаем
                val value = parts[2].toLongOrNull() ?: return@forEach

                when (parts[1]) {
                    "uplink" -> upload += value
                    "downlink" -> download += value
                }
            }

        return Delta(upload, download)
    }
}