package pe.cantape;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.view.View;

public final class MainActivity extends Activity {
    private AppSettings settings;
    private SpeechEngine speech;
    private PaymentStore payments;
    private TextView welcome;
    private TextView status;
    private TextView total;
    private RadioGroup modes;
    private final int[] modeIds = new int[3];
    private static final String[] MODE_LABELS = {
            "Solo aviso de Yape (\"Yape confirmado\")",
            "Aviso con cantidad (confirma el Yape y el monto)",
            "Aviso completo (nombre de la persona y el monto)"};

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        settings = new AppSettings(this);
        speech = new SpeechEngine(this);
        payments = new PaymentStore(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int spacing = dp(20);
        layout.setPadding(spacing, spacing, spacing, spacing);
        layout.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(spacing, spacing + insets.getSystemWindowInsetTop(),
                    spacing, spacing + insets.getSystemWindowInsetBottom());
            return insets;
        });
        scroll.setBackgroundColor(Palette.BACKGROUND);
        scroll.addView(layout);
        setContentView(scroll);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo);
        logo.setAdjustViewBounds(true);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(-1, dp(170));
        logoParams.bottomMargin = dp(8);
        layout.addView(logo, logoParams);
        welcome = addText(layout, "", 20, true, 12);
        welcome.setGravity(Gravity.CENTER);
        addText(layout, "CantaPE es una app independiente que anuncia en voz alta tus avisos de Yape. No pertenece a Yape ni al BCP.", 14, false, 16);

        section(layout, "Conexión con Yape");
        status = addText(layout, "", 16, true, 8);
        addButton(layout, "Vincular avisos de Yape", this::showNotificationDisclosure);
        addText(layout, "La vinculación permite leer los avisos de la app oficial de Yape. No requiere contraseña ni acceso a tu cuenta.", 14, false, 16);
        addButton(layout, "Cómo se usan mis datos", () -> startActivity(new Intent(this, PrivacyActivity.class)));

        section(layout, "Perfil");
        addButton(layout, "Configurar nombre", this::editProfile);
        addText(layout, "Escribe tu nombre completo una vez. Solo se guardarán el primer nombre y el primer apellido para el saludo.", 14, false, 16);

        section(layout, "¿Qué quieres que cante la app?");
        modes = new RadioGroup(this);
        for (int i = 0; i < MODE_LABELS.length; i++) {
            RadioButton option = new RadioButton(this);
            option.setId(View.generateViewId());
            option.setText(MODE_LABELS[i]);
            modeIds[i] = option.getId();
            modes.addView(option);
        }
        selectMode(settings.announceMode() < 0 ? AmountSpeech.MODE_AMOUNT : settings.announceMode());
        modes.setOnCheckedChangeListener((group, id) -> {
            for (int i = 0; i < modeIds.length; i++) if (modeIds[i] == id) settings.setAnnounceMode(i);
        });
        layout.addView(modes, margin(6));
        addText(layout, "Puedes cambiar esta opción cuando quieras. El nombre se toma del aviso que muestra el teléfono.", 14, false, 16);

        section(layout, "Anuncios");
        Switch enabled = new Switch(this);
        enabled.setText("Anunciar y registrar nuevos Yapes");
        enabled.setTextSize(16);
        enabled.setChecked(settings.isEnabled());
        enabled.setOnCheckedChangeListener((button, checked) -> {
            settings.setEnabled(checked);
            if (checked) KeepAliveService.start(this);
            else KeepAliveService.stop(this);
        });
        layout.addView(enabled, margin(12));
        addText(layout, "Al desactivar, los nuevos avisos no se anuncian ni se agregan al historial. Los anteriores permanecen guardados.", 14, false, 12);

        addText(layout, "Tipo de voz", 16, true, 6);
        RadioGroup voices = new RadioGroup(this);
        voices.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton woman = new RadioButton(this);
        woman.setId(View.generateViewId());
        woman.setText("Mujer");
        voices.addView(woman);
        RadioButton man = new RadioButton(this);
        man.setId(View.generateViewId());
        man.setText("Hombre");
        voices.addView(man);
        voices.check(settings.isMaleVoice() ? man.getId() : woman.getId());
        voices.setOnCheckedChangeListener((group, id) -> settings.setMaleVoice(id == man.getId()));
        layout.addView(voices, margin(6));
        addText(layout, "Son perfiles de tono grave y agudo de la voz española disponible en tu teléfono. El timbre exacto depende del motor de voz instalado.", 14, false, 12);

        TextView volumeLabel = addText(layout, "", 16, true, 4);
        SeekBar volume = new SeekBar(this);
        volume.setMax(100);
        volume.setProgress(settings.volumePercent());
        volumeLabel.setText("Volumen de voz: " + volume.getProgress() + "%");
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                volumeLabel.setText("Volumen de voz: " + progress + "%");
                if (fromUser) settings.setVolumePercent(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        layout.addView(volume, margin(4));
        addText(layout, "Al hablar se ajusta el volumen multimedia del teléfono a este valor. El modo No molestar o el volumen físico pueden limitar el sonido.", 14, false, 8);
        addButton(layout, "Probar voz", () -> speech.speak(AmountSpeech.announcement(
                settings.announceMode() < 0 ? AmountSpeech.MODE_AMOUNT : settings.announceMode(), 250, "Juan P.")));

        section(layout, "Funcionar siempre");
        addText(layout, "La app sigue escuchando con la pantalla apagada y sin abrirla. Verás un aviso fijo \"CantaPE activo\" mientras esté encendida, y se activa sola al reiniciar el teléfono.", 14, false, 8);
        addButton(layout, "Permitir funcionar en segundo plano", this::openBatterySettings);
        addText(layout, "Pulsa el botón y acepta el aviso de Android para que CantaPE no se cierre. En Infinix, Tecno, Xiaomi u Oppo activa también \"Inicio automático\" si aparece en los ajustes.", 14, false, 16);

        section(layout, "Historial y ganancias");
        total = addText(layout, "", 22, true, 8);
        addButton(layout, "Ver todos los Yapes detectados", () -> startActivity(new Intent(this, HistoryActivity.class)));
        addText(layout, "El total suma avisos detectados, no movimientos verificados. Confirma siempre los pagos en Yape.", 14, false, 24);

        section(layout, "Acerca de CantaPE");
        addText(layout, "Versión " + versionName(), 15, true, 6);
        addText(layout, "Información importante (solo informativa):\n\n"
                + "• CantaPE es una aplicación independiente, creada como apoyo para quienes reciben pagos por Yape. No está afiliada, patrocinada ni respaldada por Yape, el BCP ni ninguna entidad financiera.\n"
                + "• \"Yape\" y sus logotipos son marcas de sus respectivos titulares. Se mencionan solo para indicar con qué app funciona CantaPE.\n"
                + "• No usamos información personal ni datos privados: no pedimos contraseñas, claves, número de cuenta ni de teléfono, y no accedemos a tu cuenta de Yape. Solo leemos el texto visible del aviso para anunciar el pago.\n"
                + "• Los datos del historial se guardan únicamente en tu teléfono. La app no envía información a Internet.\n"
                + "• Los avisos y totales son referenciales y no verifican pagos. Antes de entregar un producto o servicio, confirma siempre el pago en la app oficial de Yape.\n"
                + "• Si el motor de voz del teléfono falla, el volumen está bajo o el ahorro de batería cierra la app, un aviso podría no sonar. CantaPE no se responsabiliza por pagos no anunciados ni por decisiones tomadas solo con su voz.\n"
                + "• La app se ofrece \"tal cual\", sin garantías. Si cambia el formato de los avisos de Yape, algunas funciones podrían dejar de funcionar.\n"
                + "• Puedes revisar o revocar los permisos concedidos en Ajustes cuando quieras.", 13, false, 8);
    }

    private String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private void selectMode(int mode) { modes.check(modeIds[mode]); }

    private void askMode() {
        new AlertDialog.Builder(this)
                .setTitle("¿Qué quieres que cante la app?")
                .setCancelable(false)
                .setItems(MODE_LABELS, (dialog, which) -> {
                    settings.setAnnounceMode(which);
                    selectMode(which);
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = settings.displayName();
        welcome.setText(name.isEmpty() ? "Bienvenido" : "Bienvenido " + name);
        ComponentName component = new ComponentName(this, YapeNotificationService.class);
        boolean linked;
        if (Build.VERSION.SDK_INT >= 27) {
            linked = getSystemService(NotificationManager.class).isNotificationListenerAccessGranted(component);
        } else {
            String listeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
            linked = listeners != null && containsComponent(listeners, component.flattenToString());
        }
        status.setText(linked ? "Avisos vinculados" : "Avisos sin vincular");
        if (linked) {
            if (Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
            KeepAliveService.start(this);
            if (!batteryExempt() && !settings.batteryAsked()) {
                settings.setBatteryAsked();
                openBatterySettings();
            }
        }
        status.setTextColor(linked ? Palette.TEAL_DARK : Color.rgb(176, 72, 48));
        total.setText(Money.format(payments.totalCents()) + " · " + payments.count() + " Yapes detectados");
        if (settings.announceMode() < 0) askMode();
    }

    private void editProfile() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Coloca tu nombre");
        new AlertDialog.Builder(this)
                .setTitle("Tu nombre")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String shortName = ProfileName.shortName(input.getText().toString());
                    settings.setDisplayName(shortName);
                    welcome.setText(shortName.isEmpty() ? "Bienvenido" : "Bienvenido " + shortName);
                })
                .show();
    }

    private boolean batteryExempt() {
        return getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(getPackageName());
    }

    /** Shows Android's one-tap "allow background activity" dialog; falls back to app settings. */
    private void openBatterySettings() {
        if (!batteryExempt()) {
            try {
                startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.fromParts("package", getPackageName(), null)));
                return;
            } catch (RuntimeException unavailable) { }
        }
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null)));
    }

    private void openListenerSettings() {
        if (Build.VERSION.SDK_INT >= 30) {
            Intent detail = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                    .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                            new ComponentName(this, YapeNotificationService.class).flattenToString());
            if (detail.resolveActivity(getPackageManager()) != null) {
                startActivity(detail);
                return;
            }
        }
        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    private void showNotificationDisclosure() {
        new AlertDialog.Builder(this)
                .setTitle("Acceso a notificaciones")
                .setMessage("Android dará a CantaPE acceso a las notificaciones de todas las apps. La app descarta las que no son de Yape; de los avisos de Yape extrae el importe y guarda el monto, la fecha, el nombre de quien te yapeó (tal como aparece en el aviso, sin número de teléfono) y un resumen del identificador en este teléfono. Anuncia el monto en voz alta. No envía datos a Internet ni modifica Yape. Puedes revocar el acceso cuando quieras en Ajustes.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar y continuar", (dialog, which) -> openListenerSettings())
                .show();
    }

    private static boolean containsComponent(String listeners, String component) {
        for (String item : listeners.split(":")) if (component.equals(item)) return true;
        return false;
    }

    private void section(LinearLayout parent, String title) { addText(parent, title, 19, true, 8); }

    private TextView addText(LinearLayout parent, String words, int size, boolean bold, int bottomDp) {
        TextView text = new TextView(this);
        text.setText(words);
        text.setTextSize(size);
        text.setTextColor(Palette.SLATE);
        if (bold) text.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(text, margin(bottomDp));
        return text;
    }

    private void addButton(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Palette.TEAL_DARK));
        button.setOnClickListener(v -> action.run());
        parent.addView(button, margin(8));
    }

    private LinearLayout.LayoutParams margin(int bottomDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(bottomDp);
        return params;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override
    protected void onDestroy() {
        speech.close();
        payments.close();
        super.onDestroy();
    }
}
