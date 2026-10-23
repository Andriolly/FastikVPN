package com.khti.fastikvpn.data.source

import com.khti.fastikvpn.BuildConfig
import com.khti.fastikvpn.data.vless.VlessLinkParser
import com.khti.fastikvpn.domain.model.VlessConfig
import javax.inject.Inject

// Временный источник: достаёт тестовую ссылку, которую мы положили в local.properties,
// и разбирает её. Позже его заменят настоящие источники (хранилище, подписка),
// а остальной код про это даже не узнает.
class TestVlessSource @Inject constructor(
    // Hilt сам подставит парсер
    private val parser: VlessLinkParser
) {

    fun getConfig(): Result<VlessConfig> {
        val link = BuildConfig.VLESS_TEST_LINK

        // Если ссылку не задали, возвращаем понятную ошибку, а не пытаемся разбирать пустоту
        if (link.isBlank()) {
            return Result.failure(
                IllegalStateException("В local.properties не задана VLESS_TEST_LINK")
            )
        }
        return parser.parse(link)
    }
}