# 🛠️ Solución Definitiva: Error AAPT2 en Linux ARM64 (Orange Pi / Raspberry Pi)

Este documento detalla la solución **permanente** para el error de compilación relacionado con `aapt2` y `libdl.so.2` que ocurre al compilar proyectos Android en dispositivos Linux con arquitectura ARM64 (aarch64).

## 🚨 El Problema

Al compilar con `./gradlew assembleDebug`, la compilación falla con:

```text
Execution failed for task ':app:processDebugResources'.
> AAPT2 aapt2-8.2.0-10154469-linux Daemon #0: Daemon startup failed
  java.io.IOException: error while loading shared libraries: libdl.so.2: cannot open shared object file
```

### 🔍 Causa Raíz
Google **no distribuye** binarios de `aapt2` para Linux ARM64 en Maven. Gradle descarga automáticamente la versión `linux-x86_64`, que es incompatible con procesadores ARM64.

---

## ✅ Solución Definitiva (Recomendada)

Usar el paquete `aapt` del sistema operativo + la propiedad `aapt2FromMavenOverride` de Gradle. **Esta solución persiste entre limpiezas de caché.**

### Paso 1: Instalar aapt2 del sistema

```bash
sudo apt update && sudo apt install -y aapt
```

Verificar instalación:
```bash
aapt2 version
# Debe mostrar: Android Asset Packaging Tool (aapt) 2.19-debian (o similar)

# Confirmar soporte de --source-path (requerido por AGP 8.x)
aapt2 compile --help | grep source-path
```

### Paso 2: Localizar el binario

```bash
which aapt2
# Resultado típico: /usr/bin/aapt2

# El binario real está en:
readlink -f /usr/bin/aapt2
# Resultado: /usr/lib/android-sdk/build-tools/debian/aapt2
```

### Paso 3: Agregar propiedad en `gradle.properties`

Agregar esta línea al archivo `gradle.properties` del proyecto:

```properties
android.aapt2FromMavenOverride=/usr/lib/android-sdk/build-tools/debian/aapt2
```

> [!IMPORTANT]
> Usa la ruta completa del binario real (no el symlink). Puedes obtenerla con `readlink -f $(which aapt2)`.

### Paso 4: Limpiar caché de transforms (solo la primera vez)

```bash
# Detener daemon de Gradle
./gradlew --stop

# Eliminar transforms viejos con aapt2 corrupto
rm -rf ~/.gradle/caches/transforms-3

# Compilar
./gradlew clean assembleDebug
```

### Paso 5: Verificar

```bash
./gradlew assembleDebug
# Debe mostrar: BUILD SUCCESSFUL
# (La advertencia "experimental" es normal y no afecta la compilación)
```

---

## ⚠️ Notas Importantes

- **Persistente**: Esta solución NO se pierde al limpiar caché de Gradle, ya que la propiedad está en `gradle.properties` y el binario viene del sistema operativo.
- **Actualizaciones**: Si actualizas el paquete `aapt` del sistema (`sudo apt upgrade`), se actualiza automáticamente.
- **CI/CD**: Solo necesitas `apt install aapt` en el pipeline + la propiedad en `gradle.properties`.
- **Advertencia experimental**: Gradle muestra `WARNING: The option setting 'android.aapt2FromMavenOverride=...' is experimental` — esto es normal y no causa problemas.

---

## ❌ Solución Vieja (NO recomendada)

Anteriormente se reemplazaba manualmente el binario en `~/.gradle/caches/transforms-3/.../aapt2` con una versión ARM64 de la comunidad (android-11.0.0_r33). **Esta solución tiene problemas:**

1. Se pierde al limpiar caché de Gradle
2. La versión android-11 no soporta `--source-path` (requerido por AGP 8.x+)
3. Requiere repetir el proceso manualmente cada vez

---
*Documento actualizado — Gym Companion — Solución Definitiva ARM64*
