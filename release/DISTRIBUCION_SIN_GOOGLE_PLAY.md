# Distribución sin Google Play

## Límite de instalación

La función automática necesita `NotificationListenerService`. Google Play Protect puede bloquear la instalación de APK que declaran acceso a notificaciones cuando se descargan y abren desde navegador, mensajería o administrador de archivos. Firmar el APK como producción, cambiar su nombre o registrar al desarrollador no quita este requisito funcional ni garantiza que Play Protect deje de bloquearlo. Tampoco sería correcto ocultar el permiso: sin él no puede escuchar los cobros de forma automática.

## Opciones reales

1. **Tienda alternativa:** publicar en una tienda que instale apps con su propio cliente. Los usuarios tendrían que instalar o usar esa tienda y autorizarla como fuente de instalación. La disponibilidad varía según el fabricante y el país. No hay garantía de instalación sin alertas en todos los teléfonos.
2. **F-Droid:** opción multiplataforma para software libre. Requiere publicar el código fuente con una licencia libre y pasar el proceso de inclusión de F-Droid. Los usuarios instalarían F-Droid primero. La app debe probarse en un teléfono con Google Play Protect antes de afirmar que evita el bloqueo observado.
3. **Android Developer Console:** permite registrar apps distribuidas fuera de Play. La cuenta limitada permite hasta 20 dispositivos autorizados; la distribución amplia exige verificación del titular. La verificación del desarrollador y el bloqueo de Play Protect son mecanismos distintos, por lo que esto solo no garantiza la instalación directa de un APK con acceso a notificaciones.
4. **ADB:** útil para probar en dispositivos propios, pero requiere depuración por USB y no es un método para clientes comunes.

## Decisión pendiente

Para distribuir sin Google Play a muchos usuarios, elegir una tienda alternativa y probar su flujo real de instalación en los modelos objetivo. Si se elige F-Droid, el titular debe autorizar expresamente publicar el código fuente bajo una licencia libre. También debe conservar una clave de firma de producción y aportar una dirección de contacto para la política de privacidad.

## Fuentes

- [Google: bloqueo de apps con acceso a notificaciones instaladas desde archivos o navegador](https://developers.google.com/android/play-protect/warning-dev-guidance)
- [Android: opciones de distribución alternativas](https://developer.android.com/distribute/marketing-tools/alternative-distribution)
- [Android: verificación de desarrolladores fuera de Google Play](https://developer.android.com/developer-verification/guides)
- [F-Droid: construcción desde código fuente](https://f-droid.org/docs/Building_Applications/)
