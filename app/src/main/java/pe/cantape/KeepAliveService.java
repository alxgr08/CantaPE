package pe.cantape;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

/**
 * Keeps the app process at foreground priority so battery savers do not freeze it
 * while the screen is off. The Yape listener runs in this same process.
 */
public final class KeepAliveService extends Service {
    private static final String CHANNEL = "keep_alive";
    private static final int NOTIFICATION_ID = 1;

    static void start(Context context) {
        if (!new AppSettings(context).isEnabled()) return;
        try {
            context.startForegroundService(new Intent(context, KeepAliveService.class));
        } catch (RuntimeException notAllowedNow) {
            // Android refuses background starts in some states; the next app open or boot retries.
        }
    }

    static void stop(Context context) {
        context.stopService(new Intent(context, KeepAliveService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL, "Servicio activo", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Indica que CantaPE está escuchando avisos de Yape.");
        channel.setShowBadge(false);
        manager.createNotificationChannel(channel);

        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_voice)
                .setContentTitle("CantaPE activo")
                .setContentText("Escuchando avisos de Yape")
                .setContentIntent(open)
                .setOngoing(true)
                .build();
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (RuntimeException notAllowedNow) {
            // Never crash the shared process: the Yape listener lives in it too.
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
