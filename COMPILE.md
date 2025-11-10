# 🛠️ Guía de Compilación - Gym Companion

## Método 1: Android Studio (Recomendado)

### Requisitos Previos
- **Android Studio**: Hedgehog (2023.1.1) o superior
- **JDK**: 17 (incluido con Android Studio)
- **Android SDK**: 34 (se descarga automáticamente)

### Pasos

1. **Abrir el Proyecto**
   ```
   File → Open → Seleccionar: /home/llibre/acompanamiento_gym
   ```

2. **Esperar Sincronización Gradle**
   - Android Studio detectará los archivos `build.gradle.kts`
   - Descargará automáticamente:
     - Gradle Wrapper
     - Dependencias del proyecto
     - Android SDK necesario
   - Verás la barra de progreso en la parte inferior

3. **Resolver Errores (si aparecen)**
   - Si solicita instalar SDK: Click en "Install missing SDK"
   - Si solicita aceptar licencias: Click en "Accept" y "Finish"

4. **Compilar**
   - Opción A: `Build → Make Project` (Ctrl+F9)
   - Opción B: Click en el martillo 🔨 en la barra superior

5. **Ejecutar**
   - Conecta un dispositivo Android o crea un AVD (Android Virtual Device)
   - Click en el botón ▶️ Run
   - Selecciona el dispositivo

## Método 2: Línea de Comandos

⚠️ **Importante**: Primero debes abrir el proyecto en Android Studio al menos una vez para que descargue el Gradle Wrapper.

### Una vez que Android Studio haya configurado Gradle:

```bash
cd /home/llibre/acompanamiento_gym

# Compilar debug APK
./gradlew assembleDebug

# Instalar en dispositivo conectado
./gradlew installDebug

# Ejecutar tests
./gradlew test

# Limpiar proyecto
./gradlew clean
```

El APK compilado estará en: `app/build/outputs/apk/debug/app-debug.apk`

## Verificación de Estructura

Verifica que existan estos archivos clave:

```
✅ build.gradle.kts (root)
✅ settings.gradle.kts
✅ app/build.gradle.kts
✅ app/src/main/AndroidManifest.xml
✅ app/src/main/java/com/gymcompanion/app/GymCompanionApplication.kt
✅ app/src/main/java/com/gymcompanion/app/presentation/MainActivity.kt
```

## Solución de Problemas Comunes

### Error: "SDK location not found"
**Solución**: Android Studio lo resuelve automáticamente. Si usas terminal, crea `local.properties`:
```properties
sdk.dir=/home/TU_USUARIO/Android/Sdk
```

### Error: "Gradle sync failed"
**Solución**: 
1. File → Invalidate Caches → Invalidate and Restart
2. Elimina carpeta `.gradle` en el proyecto
3. Build → Clean Project → Rebuild Project

### Error: Dependencias no se descargan
**Solución**: Verifica conexión a internet. Gradle descarga dependencias desde Maven Central y Google Maven.

### Error: "Minimum supported Gradle version..."
**Solución**: Actualiza Android Studio a la última versión.

## Configuración del Dispositivo/Emulador

### Emulador (AVD)
1. Tools → Device Manager
2. Create Device
3. Selecciona: Pixel 6 (recomendado)
4. System Image: Android 13.0 (API 33) o superior
5. Finish

### Dispositivo Físico
1. Habilita "Opciones de desarrollador" en tu Android:
   - Configuración → Acerca del teléfono
   - Toca 7 veces sobre "Número de compilación"
2. Habilita "Depuración USB"
3. Conecta vía USB
4. Acepta la autorización en el dispositivo

## Configuraciones Gradle

El proyecto usa:
- **Gradle**: 8.2
- **Kotlin**: 1.9.20
- **Compose Compiler**: 1.5.4
- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 34 (Android 14)
- **compileSdk**: 34

## Dependencias Principales

Ya incluidas en `app/build.gradle.kts`:
- ✅ Jetpack Compose
- ✅ Room Database
- ✅ Hilt Dependency Injection
- ✅ Navigation Compose
- ✅ Vico Charts
- ✅ Coil (imágenes)
- ✅ Coroutines

## Primera Ejecución

Al ejecutar por primera vez:
1. La app creará la base de datos SQLite
2. Se poblarán 30 ejercicios predefinidos (automático)
3. Verás 5 pantallas: Inicio, Rutinas, Ejercicios, Progreso, Perfil
4. Las pantallas estarán vacías hasta que agregues datos

## Próximos Pasos Después de Compilar

1. ✅ Verifica que la app se ejecute sin crashes
2. ✅ Navega entre las 5 pantallas inferiores
3. ✅ Implementa el formulario de datos corporales
4. ✅ Completa el CRUD de rutinas
5. ✅ Desarrolla el sistema de seguimiento de sesiones

## Recursos Adicionales

- **Documentación de Compose**: https://developer.android.com/jetpack/compose
- **Room Database**: https://developer.android.com/training/data-storage/room
- **Hilt**: https://developer.android.com/training/dependency-injection/hilt-android

---

**¿Problemas?** Revisa los logs en Android Studio: View → Tool Windows → Logcat
