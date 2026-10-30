package pe.cantape;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

public final class PrivacyActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        TextView text = new TextView(this);
        int spacing = Math.round(20 * getResources().getDisplayMetrics().density);
        text.setPadding(spacing, spacing, spacing, spacing);
        text.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(spacing, spacing + insets.getSystemWindowInsetTop(),
                    spacing, spacing + insets.getSystemWindowInsetBottom());
            return insets;
        });
        text.setTextSize(16);
        text.setText("Cómo se usan tus datos\n\n"
                + "Esta app es independiente de Yape y del BCP.\n\n"
                + "Permiso: Android concede acceso a todas las notificaciones. La app ignora inmediatamente las que no provienen de la app oficial de Yape.\n\n"
                + "Uso: analiza el título y texto del aviso para detectar un pago recibido y su importe. No lee contactos, SMS, cuentas bancarias ni contraseñas.\n\n"
                + "Almacenamiento: conserva localmente importe, fecha, el nombre del remitente tal como aparece en el aviso (sin número de teléfono) y un hash del identificador del aviso para evitar duplicados. No guarda el texto completo del aviso. Del nombre que introduces solo conserva el saludo abreviado.\n\n"
                + "Transferencia: la app no solicita acceso a Internet y no envía estos datos a terceros.\n\n"
                + "Segundo plano: mientras los anuncios estén activos, un servicio con un aviso fijo mantiene la app escuchando con la pantalla apagada y se reinicia al encender el teléfono.\n\n"
                + "Control: puedes pausar el registro y la voz dentro de la app o revocar el permiso en Ajustes de Android. Al desinstalar la app se elimina su historial local.\n\n"
                + "Importante: el anuncio y el total reflejan avisos, no verifican pagos con el banco. Consulta los movimientos oficiales de Yape.");
        scroll.addView(text);
        setContentView(scroll);
    }
}
