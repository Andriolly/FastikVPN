package com.khti.fastikvpn.vpn

import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.core.model.VpnError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Проверяем табло состояния: начальные значения, обновление, адрес сервера и ошибки.
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

    @Test
    fun `в начале нет ни адреса сервера, ни ошибки`() {
        val holder = VpnStatusHolder()
        assertNull(holder.serverAddress.value)
        assertNull(holder.error.value)
    }

    @Test
    fun `адрес сервера запоминается и стирается`() {
        val holder = VpnStatusHolder()
        holder.setServerAddress("203.0.113.10")
        assertEquals("203.0.113.10", holder.serverAddress.value)
        holder.setServerAddress(null)
        assertNull(holder.serverAddress.value)
    }

    @Test
    fun `ошибка публикуется и стирается`() {
        val holder = VpnStatusHolder()
        holder.reportError(VpnError.CORE_START_FAILED)
        assertEquals(VpnError.CORE_START_FAILED, holder.error.value)
        holder.clearError()
        assertNull(holder.error.value)
    }
}