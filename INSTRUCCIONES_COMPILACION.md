# 🚀 Instrucciones de Compilación - Gym Companion

## ✅ Estado del Proyecto
**MVP COMPLETADO** - Todas las funcionalidades core implementadas y testeadas

## 📋 Requisitos Previos

### Software Necesario
- ☑️ **Android Studio**: Hedgehog (2023.1.1) o superior
- ☑️ **JDK**: 17 o superior
- ☑️ **Android SDK**: API 34 (Android 14)
- ☑️ **Gradle**: 8.2+ (incluido con el proyecto)
- ☑️ **Kotlin**: 1.9.20

### Configuración del Sistema
- **RAM mínima**: 8 GB (16 GB recomendado)
- **Espacio en disco**: 10 GB libres
- **Conexión a internet**: Para descargar dependencias la primera vez

## 🛠️ Pasos de Compilación

### 1. Verificar Instalación de Android Studio

```bash
# Verificar versión de Android Studio
# Debe ser Hedgehog (2023.1.1) o superior
```

### 2. Configurar Android SDK

En Android Studio:
1. Ir a **File → Settings → Appearance & Behavior → System Settings → Android SDK**
2. Verificar que estén instalados:
   - ✅ Android 14.0 (API 34) - SDK Platform
   - ✅ Android SDK Build-Tools 34.0.0
   - ✅ Android SDK Platform-Tools
   - ✅ Android Emulator (si vas a usar emulador)

### 3. Abrir el Proyecto

```bash
# Desde la terminal
cd /home/llibre/acompanamiento_gym
```

Luego en Android Studio:
- **File → Open** → Seleccionar la carpeta `acompanamiento_gym`

### 4. Sincronizar Dependencias

Android Studio sincronizará automáticamente. Si no:
1. Click en el ícono del elefante 🐘 en la barra superior
2. O **File → Sync Project with Gradle Files**
3. Esperar a que descargue todas las dependencias (~2-5 minutos primera vez)

### 5. Compilar el Proyecto

#### Opción A: Desde Android Studio (Recomendado)
1. **Build → Make Project** (o `Ctrl+F9`)
2. Verificar que compile sin errores en la pestaña "Build"

#### Opción B: Desde Terminal
```bash
# Compilación debug
./gradlew assembleDebug

# Compilación release
./gradlew assembleRelease

# Ejecutar tests
./gradlew test
```

### 6. Ejecutar la Aplicación

#### En Emulador (AVD)
1. **Tools → Device Manager**
2. Crear un nuevo dispositivo virtual:
   - **Device**: Pixel 6 o similar
   - **System Image**: Android 14.0 (API 34)
   - **RAM**: 2048 MB mínimo
3. Iniciar el emulador
4. Click en ▶️ **Run** (o `Shift+F10`)

#### En Dispositivo Físico
1. Habilitar **Opciones de Desarrollador** en el dispositivo:
   - Ajustes → Acerca del teléfono → Tocar "Número de compilación" 7 veces
2. Habilitar **Depuración USB**:
   - Ajustes → Sistema → Opciones de desarrollador → Depuración USB
3. Conectar el dispositivo por USB
4. Autorizar la depuración en el dispositivo
5. Seleccionar el dispositivo en Android Studio
6. Click en ▶️ **Run**

## 🔍 Verificación de la Compilación

### Checklist Post-Compilación
- [ ] Sin errores en la pestaña "Build"
- [ ] Sin warnings críticos
- [ ] APK generado en `app/build/outputs/apk/debug/`
- [ ] App se inicia correctamente
- [ ] Base de datos se inicializa con 30 ejercicios
- [ ] Navegación entre pantallas funciona
- [ ] No hay crashes al abrir cualquier pantalla

### Estructura de Archivos Generados
```
app/build/
├── outputs/
│   └── apk/
│       ├── debug/
│       │   └── app-debug.apk      # APK instalable
│       └── release/
│           └── app-release.apk    # APK firmado (requiere firma)
├── intermediates/                  # Archivos temporales
└── tmp/                           # Cache de compilación
```

## 🐛 Solución de Problemas Comunes

### Error: "SDK location not found"
**Solución**: Crear archivo `local.properties` en la raíz:
```properties
sdk.dir=/home/USUARIO/Android/Sdk
```

### Error: "Unsupported class file major version"
**Causa**: JDK incompatible
**Solución**: 
```bash
# Verificar versión de Java
java -version
# Debe ser 17 o superior

# Cambiar en Android Studio:
# File → Settings → Build, Execution, Deployment → Build Tools → Gradle
# Seleccionar JDK 17
```

### Error: "Failed to resolve dependencies"
**Solución**:
```bash
# Limpiar y reconstruir
./gradlew clean
./gradlew build --refresh-dependencies
```

### Error: "Manifest merger failed"
**Solución**: Verificar que no haya conflictos en `AndroidManifest.xml`

### Error: "libdl.so.2" o "AAPT2 Daemon startup failed" (Linux ARM64)
**Causa**: Incompatibilidad de arquitectura (x86-64 vs ARM64).
**Solución**: Ver documento detallado [AAPT2_FIX_ARM64.md](AAPT2_FIX_ARM64.md) para instrucciones de reemplazo manual del binario.

### App crashea al iniciar
**Verificar**:
1. Logs en Logcat (filtrar por "GymCompanion")
2. Permisos en el manifest
3. Versión de Android del dispositivo (mínimo API 26)

## 📱 Configuraciones de Build

### Build Types Disponibles

#### Debug (Por defecto)
- Optimización deshabilitada
- Logs completos
- Depuración habilitada
```bash
./gradlew assembleDebug
```

#### Release
- Código optimizado
- ProGuard habilitado
- Requiere firma
```bash
./gradlew assembleRelease
```

### Variantes de Build
```bash
# Listar todas las variantes
./gradlew tasks --all | grep assemble

# Compilar todas las variantes
./gradlew assemble
```

## 🧪 Ejecutar Tests

```bash
# Tests unitarios
./gradlew test

# Tests instrumentados (requiere dispositivo/emulador)
./gradlew connectedAndroidTest

# Reporte de coverage
./gradlew jacocoTestReport
```

## 📦 Generar APK Firmado

### Para Producción
1. **Build → Generate Signed Bundle / APK**
2. Seleccionar **APK**
3. Crear o seleccionar keystore
4. Ingresar contraseñas
5. Seleccionar **release** build type
6. Click en **Finish**

El APK firmado estará en `app/release/app-release.apk`

## 🔧 Configuración Avanzada

### Optimización de Build
En `gradle.properties`:
```properties
# Habilitar parallel build
org.gradle.parallel=true

# Aumentar heap de Gradle
org.gradle.jvmargs=-Xmx4096m

# Habilitar cache
org.gradle.caching=true
```

### Build Cache
```bash
# Limpiar cache si hay problemas
./gradlew cleanBuildCache
```

## 📊 Métricas de Compilación

### Tiempos Estimados (Hardware moderado)
- **Primera compilación**: 3-5 minutos
- **Compilación incremental**: 15-30 segundos
- **Clean build**: 1-2 minutos
- **Instalación en dispositivo**: 10-20 segundos

### Tamaño de Archivos
- **APK Debug**: ~15-20 MB
- **APK Release**: ~8-12 MB (con ProGuard)
- **Dependencias descargadas**: ~300-400 MB

## ✅ Checklist de Producción

Antes de liberar a producción:
- [ ] Cambiar `applicationId` si es necesario
- [ ] Actualizar `versionCode` y `versionName`
- [ ] Configurar ProGuard rules
- [ ] Firmar APK con keystore de producción
- [ ] Probar en múltiples dispositivos/versiones de Android
- [ ] Verificar que no haya logs sensibles
- [ ] Preparar assets de Play Store (iconos, screenshots)

## 🆘 Soporte

Si encuentras problemas:
1. Revisar logs en Logcat
2. Verificar archivo `build.gradle.kts`
3. Limpiar proyecto: `./gradlew clean`
4. Invalidar cache: **File → Invalidate Caches / Restart**

## 📚 Recursos Adicionales

- [Documentación oficial de Android Studio](https://developer.android.com/studio)
- [Guía de Gradle para Android](https://developer.android.com/studio/build)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)

---

**¡La aplicación está lista para compilar y ejecutar! 🎉**

*Última actualización: 10 de Noviembre 2025*
