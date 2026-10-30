# CantaPE 1.5

**[Descargar CantaPE.apk](https://github.com/alxgr08/CantaPE/releases/latest/download/CantaPE.apk)** (Android 8.0 o superior)

Aplicación Android independiente que anuncia por voz el importe detectado en una notificación de pago recibido de Yape. No pertenece a Yape ni al BCP, no modifica Yape y no se conecta a una cuenta bancaria.

## Instalar y usar

1. Para pruebas de desarrollo, instala `CantaPE.apk` desde el enlace de descarga de arriba en un teléfono Android 8.0 o posterior. Google Play Protect puede bloquear la instalación directa de un APK con acceso a notificaciones. Para las opciones de distribución sin Google Play, consulta `release/DISTRIBUCION_SIN_GOOGLE_PLAY.md`.
2. Abre **CantaPE** y pulsa **Vincular avisos de Yape**.
3. Lee la explicación del permiso y, si estás de acuerdo, activa el acceso a notificaciones para **CantaPE** en los ajustes de Android.
4. Elige qué quieres que cante la app (solo aviso, con cantidad o completo con nombre) y cámbialo cuando quieras. Configura el nombre de bienvenida, el tipo de voz y el volumen. Usa **Probar voz** para escuchar el resultado.
5. Abre **Ver todos los Yapes detectados**: el calendario muestra la semana actual (desplegable a mes completo) con nombre, monto y total.

El APK de `release/` se firma con `cantape-release.jks`, configurado en `keystore.properties` (ambos fuera de Git; haz copia de seguridad). Compila con `gradlew.bat assembleRelease`.

## Privacidad y límites

- El permiso de escucha de Android puede exponer notificaciones de todas las apps. El código descarta inmediatamente cualquier aviso cuyo paquete no sea `com.bcp.innovacxion.yapeapp`.
- Solo se examinan el título y el texto visible del aviso. No se piden permisos de Internet, contactos, SMS, almacenamiento ni accesibilidad.
- El historial local guarda el importe, la fecha, el nombre del remitente tal como lo muestra el aviso (solo letras, nunca números de teléfono) y un identificador de notificación convertido en hash para evitar duplicados. No almacena el texto completo del aviso.
- Mientras los anuncios están activos, `KeepAliveService` mantiene el proceso en primer plano con un aviso fijo para que el sistema no lo congele con la pantalla apagada. `BootReceiver` lo reinicia tras reiniciar el teléfono o actualizar la app. El historial permanece en este teléfono; se borra al desinstalar la app.
- El nombre del titular no está disponible mediante los avisos. Se introduce manualmente; del nombre completo solo se guarda el primer nombre y el primer apellido para el saludo.
- Las opciones Hombre y Mujer usan tonos grave y agudo de la voz española instalada. Android no garantiza voces de ambos géneros. El control de volumen ajusta el volumen multimedia del teléfono cuando se habla y puede verse limitado por el dispositivo o sus modos de silencio.
- Al pausar la app, no se anuncian ni se registran nuevos avisos. Al reactivarla, continúa con los siguientes avisos sin borrar el historial.
- Se exige una expresión de recepción y un importe inequívoco con `S/`, `PEN` o `soles`. Si el título y el cuerpo repiten el mismo importe se admite; si aparecen importes distintos se ignora el aviso. Si Yape cambia el formato, habrá que adaptar el analizador.
- El audio y el total reflejan avisos detectados; no consultan ni certifican los movimientos bancarios. Comprueba el pago en los movimientos de Yape.

## Compilar

Abre esta carpeta en Android Studio y ejecuta `assembleDebug`, o ejecuta `gradlew.bat assembleDebug` con Android SDK y JDK 17 o 21 instalados.

## Probar solo en tu teléfono

La preparación para Google Play no hace que un APK instalado desde Archivos se considere descargado de Google Play. El uso compartido interno de Google Play también requiere una cuenta de desarrollador con acceso a Play Console.

Para una prueba personal sin publicar la app, Android Studio o ADB pueden instalarla **una sola vez** por USB. Después puedes desconectar el cable y desactivar Depuración por USB; la app instalada continúa funcionando y escuchando avisos mientras mantengas su permiso de notificaciones. Una actualización posterior sí requerirá volver a instalar un APK.
