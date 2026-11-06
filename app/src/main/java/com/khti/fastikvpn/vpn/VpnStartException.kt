package com.khti.fastikvpn.vpn

import com.khti.fastikvpn.core.model.VpnError

// Своё исключение для ошибок запуска VPN.
// Обычные исключения говорят, ЧТО сломалось на низком уровне ("файл не найден"),
// но не говорят, на каком ЭТАПЕ запуска это произошло. Этот класс хранит код этапа (error)
// и исходную причину (cause). Приём называется "обернуть ошибку".
//
// Exception(cause?.message, cause): передаём родителю текст и причину,
// чтобы они были доступны через e.message и e.cause
class VpnStartException(
    val error: VpnError,
    cause: Throwable? = null
) : Exception(cause?.message, cause)