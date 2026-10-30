package pe.cantape;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Restarts the keep-alive service after a reboot or an app update, without opening the app. */
public final class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            KeepAliveService.start(context);
        }
    }
}
