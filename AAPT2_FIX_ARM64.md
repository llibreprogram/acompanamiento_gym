# 🛠️ Solución al Error AAPT2 en Linux ARM64 (Orange Pi / Raspberry Pi)

Este documento detalla la solución para el error de compilación relacionado con `aapt2` y `libdl.so.2` que ocurre al intentar compilar proyectos Android en dispositivos Linux con arquitectura ARM64 (aarch64), como Orange Pi, Raspberry Pi o Chromebooks.

## 🚨 El Problema

Al compilar el proyecto con `./gradlew assembleDebug`, la compilación falla con un error similar a este:

```text
Execution failed for task ':app:processDebugResources'.
> A failure occurred while executing com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask$TaskAction
   > Android resource linking failed
     AAPT2 aapt2-8.2.0-10154469-linux Daemon #0: Daemon startup failed
     java.io.IOException: ... error while loading shared libraries: libdl.so.2: cannot open shared object file: No such file or directory
```

O a veces:
```text
wrong ELF class: ELFCLASS64
```

### 🔍 Causa Raíz
Google **no distribuye oficialmente** binarios de `aapt2` (Android Asset Packaging Tool 2) para Linux ARM64 en su repositorio Maven. Gradle descarga automáticamente la versión para `linux-x86_64` (Intel/AMD 64-bit), que es incompatible con procesadores ARM64.

## ✅ La Solución

La solución consiste en reemplazar manualmente el binario `aapt2` incompatible (x86-64) que Gradle descargó en su caché, por una versión compilada por la comunidad para ARM64.

### Pasos para Aplicar el Fix

#### 1. Localizar el binario incompatible
Gradle guarda `aapt2` en su caché de transformaciones. Ejecuta el siguiente comando para encontrar dónde está:

```bash
find ~/.gradle/caches -name "aapt2"
```

Verás una o más rutas como esta:
`/home/usuario/.gradle/caches/transforms-3/.../transformed/aapt2-8.2.0-10154469-linux/aapt2`

#### 2. Descargar el binario compatible (ARM64)
Utilizamos una versión compilada por la comunidad (gracias al repositorio `JonForShort/android-tools`).

Descarga el binario `android-11.0.0_r33` para ARM64 (más compatible con versiones recientes):

```bash
wget https://raw.githubusercontent.com/JonForShort/android-tools/master/build/android-11.0.0_r33/aapt2/arm64-v8a/bin/aapt2 -O aapt2_arm64
```

#### 3. Reemplazar el archivo
Copia el archivo descargado sobre el archivo que encontró Gradle en el paso 1.

> **Nota:** Si tienes múltiples versiones en el caché, es recomendable reemplazarlas todas o al menos la que corresponde a la versión que usa tu proyecto (en este caso 8.2.0).

```bash
# Ejemplo (Asegúrate de usar TU ruta específica del paso 1)
cp aapt2_arm64 /home/usuario/.gradle/caches/transforms-3/.../transformed/aapt2-8.2.0-10154469-linux/aapt2
```

#### 4. Dar permisos de ejecución
Asegúrate de que el nuevo binario sea ejecutable:

```bash
chmod +x /home/usuario/.gradle/caches/transforms-3/.../transformed/aapt2-8.2.0-10154469-linux/aapt2
```

#### 5. Verificar
Intenta compilar de nuevo. El error de `libdl.so.2` debería desaparecer.

```bash
./gradlew assembleDebug
```

## ⚠️ Notas Importantes

- **Persistencia**: Si limpias el caché de Gradle (`./gradlew cleanBuildCache` o borras `~/.gradle/caches`), Gradle volverá a descargar la versión incorrecta (x86-64) y tendrás que repetir este proceso.
- **CI/CD**: Si configuras un pipeline de integración continua en ARM64, deberás incluir un paso de script que realice este reemplazo automáticamente antes de compilar.

---
*Documento generado para Gym Companion - Solución de Compatibilidad ARM64*
