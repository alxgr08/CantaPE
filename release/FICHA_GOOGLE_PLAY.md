# Ficha preparada para Google Play

**Nombre:** CantaPE

**Descripción breve:** Escucha importes de avisos de Yape y consulta tu historial local.

**Descripción completa:**

CantaPE anuncia en español el importe que aparece en una notificación de dinero recibido de Yape. Por ejemplo, S/ 2.00 se anuncia como «2 soles» y S/ 2.50 como «2 soles y 50 céntimos».

Tú eliges qué canta la app: solo el aviso («Yape confirmado»), el aviso con la cantidad, o el aviso completo con el nombre que muestra la notificación y el monto. Puedes cambiarlo cuando quieras, ajustar el volumen y el tono, pausar los anuncios y ver tus yapes en un calendario semanal (desplegable a mes completo) con nombre y monto. El historial se guarda solamente en tu teléfono.

La app solicita acceso a notificaciones porque es necesario para reconocer los avisos de Yape. Android concede acceso a todos los avisos, pero esta app descarta los de otras aplicaciones. No requiere contraseña ni acceso a tu cuenta bancaria. No solicita permiso de Internet.

CantaPE es una aplicación independiente, sin afiliación con Yape ni con el BCP. El audio y el historial reflejan notificaciones; no certifican pagos. Comprueba cada abono en los movimientos oficiales de Yape.

## Pendiente del titular de la cuenta

- Crear y verificar la cuenta de desarrollador de Google Play.
- Indicar correo de soporte y publicar `PRIVACIDAD.html` en una URL pública HTTPS, sustituyendo el contacto de ejemplo.
- Custodiar `cantape-release.jks` y `keystore.properties` (copia de seguridad); firmar el AAB con esa clave.
- Completar las declaraciones de contenido, permisos, datos y servicios financieros que solicite Play Console según sus formularios vigentes.
- Añadir capturas tomadas en un teléfono real y los recursos gráficos de la ficha.
- En cuentas personales nuevas, completar la prueba cerrada requerida antes de solicitar producción.

El APK de debug sirve solo para desarrollo. A Google Play se sube el AAB de release.
