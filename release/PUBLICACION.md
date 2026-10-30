# Publicación para instalación normal

La instalación directa desde un administrador de archivos puede ser bloqueada por Google Play Protect porque la función principal usa el acceso sensible a notificaciones. Cambiar el nombre del APK, firmarlo como versión de producción o desactivar una alerta dentro de la app no elimina esa protección. La ruta de distribución normal es publicar la app mediante Google Play con una cuenta de desarrollador verificada.

## Estado preparado

- La app tiene nombre e icono propios, declara que es independiente de Yape y del BCP, y explica el alcance del permiso antes de abrir los ajustes.
- Compila con `targetSdk 36`; el análisis Android Lint y `bundleRelease` pasan.
- `release/CantaPE-1.5.apk` está firmado con `cantape-release.jks` (fuera de Git). Para Play, generar el AAB con `./gradlew bundleRelease` y activar Play App Signing.
- `PRIVACIDAD.html`, `FICHA_GOOGLE_PLAY.md`, `icon-512.png` y `feature-graphic.png` (con el logo de CantaPE) están preparados para la ficha.

## Pasos que requieren al titular

1. Crear/verificar una cuenta de Play Console y pagar la tarifa de registro vigente. La identidad y los datos de contacto deben ser del titular real.
2. Elegir un correo de soporte y publicarlo en la política de privacidad. Hospedar `PRIVACIDAD.html` en una URL pública HTTPS y añadir esa URL a Play Console.
3. Generar una clave de carga de producción y guardarla fuera del repositorio, con una copia de seguridad segura. Firmar el AAB con esa clave y activar Play App Signing al crear la app. La clave de depuración actual no debe usarse para publicar.
4. Completar la ficha, el formulario de seguridad de datos, la declaración de permisos y cualquier declaración de funciones financieras que Play Console solicite. Las respuestas deben describir la versión final de la app.
5. Tomar capturas reales en un teléfono; no usar maquetas como capturas de la interfaz.
6. Probar notificaciones reales de Yape en al menos un teléfono antes de invitar evaluadores. Una cuenta personal nueva puede necesitar una prueba cerrada con 12 evaluadores durante 14 días antes de solicitar producción.
7. Enviar la app a revisión. Google decide si aprueba el uso del acceso a notificaciones; ninguna modificación local puede garantizar la aprobación.

## Fuentes

- [Google: bloqueo de APK con acceso a notificaciones](https://developers.google.com/android/play-protect/warning-dev-guidance)
- [Google Play: API de destino vigente](https://support.google.com/googleplay/android-developer/answer/11926878)
- [Google Play: requisitos de prueba de cuentas personales](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Google Play: suplantación de identidad](https://support.google.com/googleplay/android-developer/answer/9888374)
- [Android: firma para publicar](https://developer.android.com/studio/publish/preparing)
