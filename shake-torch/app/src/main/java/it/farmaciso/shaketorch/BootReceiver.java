package it.farmaciso.shaketorch;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("enabled", false)) {
            context.startForegroundService(new Intent(context, ShakeService.class));
        }
    }
}
