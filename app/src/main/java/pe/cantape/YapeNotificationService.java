package pe.cantape;

import android.app.Notification;
import android.content.ComponentName;
import android.os.PowerManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class YapeNotificationService extends NotificationListenerService {
    private static final String YAPE_PACKAGE = "com.bcp.innovacxion.yapeapp";
    private SpeechEngine speech;
    private AppSettings settings;
    private PaymentStore payments;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        speech = new SpeechEngine(this);
        settings = new AppSettings(this);
        payments = new PaymentStore(this);
        wakeLock = getSystemService(PowerManager.class)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cantape:speak");
        wakeLock.setReferenceCounted(false);
    }

    @Override
    public void onListenerConnected() {
        KeepAliveService.start(this);
    }

    @Override
    public void onListenerDisconnected() {
        // Battery savers sometimes unbind the listener; ask Android to reconnect it.
        requestRebind(new ComponentName(this, YapeNotificationService.class));
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!YAPE_PACKAGE.equals(sbn.getPackageName())) return;
        if (!settings.isEnabled()) return;
        Notification notification = sbn.getNotification();
        if (notification == null) return;
        Bundle extras = notification.extras;
        if (extras == null) return;

        String title = asString(extras.getCharSequence(Notification.EXTRA_TITLE));
        String body = asString(extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
        if (body.isEmpty()) body = asString(extras.getCharSequence(Notification.EXTRA_TEXT));
        Long cents = YapeMessageParser.receivedCents(title + " " + body);
        if (cents == null) return;

        // The name is optional extra detail: a failure here must never silence the amount.
        String sender = "";
        try {
            sender = YapeMessageParser.senderName(body);
            if (sender.isEmpty()) sender = YapeMessageParser.senderName(title);
        } catch (RuntimeException | Error ignored) { }
        try {
            if (!payments.addIfNew(hashKey(sbn.getKey()), System.currentTimeMillis(), cents, sender)) return;
        } catch (RuntimeException historyFailed) {
            // Still announce the payment even if the local history could not be written.
        }
        // Keep the CPU awake long enough to finish speaking with the screen off.
        wakeLock.acquire(30_000);
        int mode = settings.announceMode();
        speech.speak(AmountSpeech.announcement(mode < 0 ? AmountSpeech.MODE_AMOUNT : mode, cents, sender));
    }

    private static String hashKey(String key) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) hex.append(String.format("%02x", value & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static String asString(CharSequence value) {
        return value == null ? "" : value.toString();
    }

    @Override
    public void onDestroy() {
        if (speech != null) speech.close();
        if (payments != null) payments.close();
        if (wakeLock != null) wakeLock.release();
        super.onDestroy();
    }
}
