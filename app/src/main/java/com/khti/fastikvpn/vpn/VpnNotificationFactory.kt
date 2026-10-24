package com.khti.fastikvpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.khti.fastikvpn.MainActivity
import com.khti.fastikvpn.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// Создаёт постоянное уведомление, без которого Android не разрешит сервису работать в фоне.
// Вынесено в отдельный класс, чтобы сервис не превращался в свалку из разных задач.
class VpnNotificationFactory @Inject constructor(
    // Hilt передаёт контекст всего приложения. Он живёт столько же, сколько процесс,
    // поэтому его безопасно хранить (контекст экрана хранить нельзя: будет утечка памяти)
    @ApplicationContext private val context: Context
) {

    // Собирает готовое уведомление
    fun build(): Notification {
        // Канал нужно создать до показа уведомления, иначе оно не появится
        createChannel()

        // Нажатие на само уведомление открывает приложение.
        // PendingIntent - заготовка действия, которую система выполнит от нашего имени.
        // FLAG_IMMUTABLE: чужие программы не смогут подменить заготовку (требование новых Android)
        val openAppIntent = PendingIntent.getActivity(
            context,
            REQUEST_OPEN,
            Intent(context, MainActivity::class.java).apply {
                // Если приложение уже открыто, не создаём второе окно, а возвращаемся к нему
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Кнопка "Отключить" посылает сервису команду остановки
        val stopIntent = PendingIntent.getService(
            context,
            REQUEST_STOP,
            FastikVpnService.stopIntent(context),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn_key)
            .setContentTitle(context.getString(R.string.vpn_notification_title))
            .setContentText(context.getString(R.string.vpn_notification_text))
            // Постоянное уведомление: его нельзя смахнуть, пока VPN включён
            .setOngoing(true)
            .setContentIntent(openAppIntent)
            .addAction(0, context.getString(R.string.vpn_notification_stop), stopIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    // Канал уведомлений - категория, настройки которой пользователь может менять.
    // IMPORTANCE_LOW: без звука и без всплывающей плашки, просто значок и строка в шторке
    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.vpn_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        // Повторное создание безопасно: если канал уже есть, ничего не произойдёт
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "fastikvpn_status"
        const val REQUEST_OPEN = 1
        const val REQUEST_STOP = 2
    }
}