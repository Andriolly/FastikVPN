package com.khti.fastikvpn.vpn

import com.khti.fastikvpn.core.model.ConnectionState
import org.junit.Assert.assertEquals
import org.junit.Test

// Проверяем табло состояния: начальное значение и обновление.
class VpnStatusHolderTest {

    @Test
    fun `в начале VPN выключен`() {
        assertEquals(ConnectionState.DISCONNECTED, VpnStatusHolder().state.value)
    }

    @Test
    fun `обновление меняет состояние`() {
        val holder = VpnStatusHolder()
        holder.update(ConnectionState.CONNECTED)
        assertEquals(ConnectionState.CONNECTED, holder.state.value)
    }
}