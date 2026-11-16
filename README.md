# LYGANE Smart TV Simulator — Android nativo

Proyecto Android con Kotlin + Jetpack Compose, preparado como launcher para teléfonos Android y Android TV / Google TV.

## Funciones implementadas
- Actividad de inicio estándar (`CATEGORY_LAUNCHER`).
- Entrada para Android TV (`CATEGORY_LEANBACK_LAUNCHER`) y actividad HOME seleccionable.
- Consulta de aplicaciones publicadas como launcher móvil o Leanback TV.
- Iconos y nombres reales de las aplicaciones consultables.
- Apertura de aplicaciones instaladas.
- Búsqueda y pestaña de favoritos persistentes.
- Interfaz adaptable: tarjetas compactas en móvil y grandes en pantallas anchas.
- Enfoque visual para navegación con mando/teclado y selección por OK.
- Botón de refresco e información.

## Requisitos para compilar
- Android Studio reciente o Android SDK + Gradle.
- JDK 17 o superior.
- Android SDK Platform 35.

## Compilar APK desde Android Studio
1. Descomprime el ZIP.
2. En Android Studio selecciona **Open** y abre esta carpeta.
3. Espera a que termine la sincronización de Gradle.
4. Conecta un dispositivo Android o selecciona un emulador y pulsa Run.
5. Para generar el APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. El APK de depuración aparecerá en `app/build/outputs/apk/debug/app-debug.apk`.

## Compilar desde terminal Ubuntu
Desde esta carpeta, con Android SDK configurado en `ANDROID_HOME` y Gradle instalado:

```bash
gradle :app:assembleDebug
```

El APK quedará en `app/build/outputs/apk/debug/app-debug.apk`.

## Notas importantes
- El proyecto fuente no es todavía un APK compilado. La compilación requiere Android SDK y Gradle, que no están disponibles en el entorno donde se preparó este ZIP.
- Android limita la visibilidad de paquetes. La app consulta aplicaciones que publican actividades `LAUNCHER` o `LEANBACK_LAUNCHER`; algunas apps del fabricante o apps sin actividad pública no aparecerán.
- La navegación DPAD se apoya en el sistema de foco de Compose. Debe validarse con mandos y televisores reales para ajustar el foco inicial y el comportamiento específico de cada fabricante.
- La aplicación puede seleccionarse como HOME si el fabricante permite launchers de terceros. Algunos televisores restringen esta opción.
- Para publicar en Google Play, revisar las políticas actuales de target SDK y visibilidad de paquetes antes de distribuir.


## Generar APK automáticamente en la nube (sin Android físico)
El repositorio incluye `.github/workflows/build-apk.yml`. Sube esta carpeta a un repositorio GitHub y abre **Actions → Build Android APK → Run workflow**. Al finalizar, descarga el artefacto `LYGANE-SmartTV-debug-apk`; contiene `app-debug.apk`. También se ejecuta automáticamente al hacer push a `main` o `master`.
