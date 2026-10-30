package pe.cantape;

import android.content.Context;
import android.content.SharedPreferences;

final class AppSettings {
    private static final String FILE = "cantape_settings";
    private final SharedPreferences preferences;

    AppSettings(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    boolean isEnabled() { return preferences.getBoolean("enabled", true); }
    void setEnabled(boolean enabled) { preferences.edit().putBoolean("enabled", enabled).apply(); }

    boolean isMaleVoice() { return preferences.getBoolean("male_voice", false); }
    void setMaleVoice(boolean male) { preferences.edit().putBoolean("male_voice", male).apply(); }

    int volumePercent() { return preferences.getInt("volume_percent", 85); }
    void setVolumePercent(int percent) {
        preferences.edit().putInt("volume_percent", Math.max(0, Math.min(100, percent))).apply();
    }

    /** Which announcement to sing (see AmountSpeech.MODE_*), or -1 until the user first chooses. */
    int announceMode() { return preferences.getInt("announce_mode", -1); }
    void setAnnounceMode(int mode) { preferences.edit().putInt("announce_mode", mode).apply(); }

    boolean batteryAsked() { return preferences.getBoolean("battery_asked", false); }
    void setBatteryAsked() { preferences.edit().putBoolean("battery_asked", true).apply(); }

    String displayName() { return preferences.getString("display_name", ""); }
    void setDisplayName(String name) { preferences.edit().putString("display_name", name).apply(); }
}
