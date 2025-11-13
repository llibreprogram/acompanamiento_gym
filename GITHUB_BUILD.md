# 🚀 Compilación en GitHub Actions

## ✅ Workflow Activado

El proyecto está configurado para compilarse automáticamente en GitHub Actions con cada push.

## 📍 Ver el Progreso del Build

**URL directa:** https://github.com/llibreprogram/acompanamiento_gym/actions

### Pasos para ver el build:

1. **Abre GitHub Actions:**
   - Ve a: https://github.com/llibreprogram/acompanamiento_gym/actions
   - O en el repo: Click en la pestaña "Actions"

2. **Selecciona el workflow más reciente:**
   - Verás una lista de ejecuciones
   - Click en la más reciente (arriba)

3. **Observa el progreso:**
   - Verás los pasos ejecutándose:
     - ✓ Set up JDK 17
     - ✓ Setup Android SDK
     - ✓ Cache Gradle packages
     - ⏳ Build with Gradle (este tarda más, 3-5 minutos)
     - ✓ Upload APK

4. **Si el build es exitoso (✅):**
   - Aparecerá: "BUILD SUCCESSFUL"
   - Scroll hasta abajo de la página
   - Verás la sección "Artifacts"
   - Click en "app-debug" para descargar el APK

5. **Si el build falla (❌):**
   - Click en el paso que falló (en rojo)
   - Lee el error en los logs
   - Los errores comunes están documentados abajo

## 📥 Descargar el APK

Una vez que el build termine exitosamente:

1. En la página del workflow, scroll hasta **"Artifacts"**
2. Click en **"app-debug"** (se descargará un ZIP)
3. Descomprime el ZIP
4. Instala `app-debug.apk` en tu dispositivo Android

## ⏱️ Tiempo Estimado

- **Primera compilación:** 5-7 minutos (descarga dependencias)
- **Compilaciones subsecuentes:** 2-3 minutos (usa caché)

## 🔍 Estado Actual

**Último commit:** `c18de8d`  
**Mensaje:** "Improve GitHub Actions workflow with Android SDK setup"  
**Commits del workflow:** 4 mejoras realizadas

### Mejoras implementadas:

✅ android-actions/setup-android@v3 - Configura SDK automáticamente  
✅ Gradle caching - Builds más rápidos  
✅ --stacktrace flag - Mejor diagnóstico de errores  
✅ --no-daemon flag - Evita problemas de memoria  
✅ Artifact retention 30 días - APKs disponibles por un mes  

## 🐛 Errores Comunes y Soluciones

### Error: "SDK location not found"
**Solución:** Ya corregido con `android-actions/setup-android@v3`

### Error: "AAPT2 failed"
**Solución:** No ocurre en x86-64 de GitHub (solo en ARM64 local)

### Error: "Gradle build failed"
**Posibles causas:**
- Error de sintaxis en código Kotlin
- Dependencia faltante
- Error en recursos (strings.xml, etc.)

**Acción:** Revisar logs detallados del paso "Build with Gradle"

### Error: "Upload artifact failed"
**Causa:** El APK no se generó porque el build falló antes
**Acción:** Corregir el error de compilación primero

## 🔄 Forzar Nueva Compilación

Si quieres ejecutar el workflow manualmente:

1. Ve a: https://github.com/llibreprogram/acompanamiento_gym/actions
2. Click en "Android CI" en el panel izquierdo
3. Click en "Run workflow" (botón azul)
4. Selecciona la rama "main"
5. Click en "Run workflow"

## 📊 Badges de Estado

Puedes agregar badges al README.md:

```markdown
![Android CI](https://github.com/llibreprogram/acompanamiento_gym/workflows/Android%20CI/badge.svg)
```

Esto mostrará: ![Android CI](https://github.com/llibreprogram/acompanamiento_gym/workflows/Android%20CI/badge.svg)

## 📱 Próximos Pasos

Una vez que tengas el APK:

1. **Transferir a dispositivo:**
   ```bash
   adb install app-debug.apk
   ```

2. **O manualmente:**
   - Copia el APK al teléfono
   - Abre el archivo
   - Acepta instalar de fuentes desconocidas
   - Instala la app

3. **Probar todas las funcionalidades:**
   - [ ] Registro de usuario
   - [ ] Agregar métricas corporales
   - [ ] Navegar biblioteca de ejercicios
   - [ ] Crear rutinas
   - [ ] Iniciar sesión de entrenamiento
   - [ ] Ver progreso con gráficas

## 🎯 Objetivo

Obtener un APK funcional compilado en servidores x86-64 de GitHub, evitando el problema de incompatibilidad ARM64 vs x86-64 local.

---

**Última actualización:** 2025-11-12 23:30 UTC  
**Estado:** ⏳ Esperando que GitHub Actions compile el proyecto
