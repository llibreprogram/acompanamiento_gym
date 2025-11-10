# 🛠️ Comandos Útiles - Gym Companion

## Navegación del Proyecto

```bash
# Ir al directorio del proyecto
cd /home/llibre/acompanamiento_gym

# Ver archivos principales
ls -la

# Ver estructura de carpetas
find . -type d -name "java" -o -name "kotlin"

# Contar archivos Kotlin
find . -name "*.kt" | wc -l

# Ver todos los archivos Kotlin
find . -name "*.kt" -type f
```

## Gradle (después de abrir en Android Studio)

```bash
# Compilar proyecto
./gradlew build

# Limpiar proyecto
./gradlew clean

# Compilar APK debug
./gradlew assembleDebug

# Instalar en dispositivo
./gradlew installDebug

# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests instrumentados
./gradlew connectedAndroidTest

# Ver tareas disponibles
./gradlew tasks

# Ver dependencias
./gradlew dependencies

# Generar APK release
./gradlew assembleRelease
```

## Android Studio

```bash
# Abrir proyecto desde terminal (si tienes alias configurado)
studio /home/llibre/acompanamiento_gym

# O manualmente:
# 1. Abre Android Studio
# 2. File → Open
# 3. Selecciona: /home/llibre/acompanamiento_gym
```

## Git

```bash
# Inicializar repositorio
git init

# Ver estado
git status

# Agregar todos los archivos
git add .

# Commit inicial
git commit -m "Initial commit: Android fitness app with Clean Architecture"

# Crear archivo .gitignore (ya existe)
cat .gitignore

# Ver cambios
git diff

# Ver historial
git log --oneline

# Crear rama para nueva funcionalidad
git checkout -b feature/body-metrics-form
```

## Verificación del Proyecto

```bash
# Ver configuración de Gradle
cat build.gradle.kts
cat app/build.gradle.kts
cat settings.gradle.kts

# Ver manifest
cat app/src/main/AndroidManifest.xml

# Ver aplicación principal
cat app/src/main/java/com/gymcompanion/app/GymCompanionApplication.kt

# Ver MainActivity
cat app/src/main/java/com/gymcompanion/app/presentation/MainActivity.kt

# Ver entidades
ls app/src/main/java/com/gymcompanion/app/data/local/entity/

# Ver DAOs
ls app/src/main/java/com/gymcompanion/app/data/local/dao/

# Ver pantallas
ls app/src/main/java/com/gymcompanion/app/presentation/screens/
```

## Búsqueda en Código

```bash
# Buscar texto en archivos Kotlin
grep -r "ExerciseEntity" app/src/main/java/ --include="*.kt"

# Buscar en todos los archivos
grep -r "TODO" . --include="*.kt"

# Buscar archivos por nombre
find . -name "*Screen.kt"

# Contar líneas de código Kotlin
find . -name "*.kt" -exec wc -l {} + | sort -n

# Ver imports de un archivo
grep "^import" app/src/main/java/com/gymcompanion/app/presentation/MainActivity.kt
```

## Análisis del Proyecto

```bash
# Estadísticas de archivos
echo "Kotlin files: $(find . -name '*.kt' | wc -l)"
echo "XML files: $(find . -name '*.xml' | wc -l)"
echo "Gradle files: $(find . -name '*.gradle.kts' | wc -l)"

# Tamaño del proyecto
du -sh /home/llibre/acompanamiento_gym

# Tamaño por carpeta
du -h --max-depth=1 /home/llibre/acompanamiento_gym | sort -h

# Ver estructura de paquetes
find app/src/main/java -type d | sed 's|/|.|g'
```

## Documentación

```bash
# Leer README
less README.md
# O con colores:
cat README.md

# Ver guía rápida
cat QUICKSTART.md

# Ver guía de compilación
cat COMPILE.md

# Ver estadísticas
cat PROJECT_STATS.md

# Ver resumen de éxito
cat SUCCESS.md
```

## Desarrollo Activo

```bash
# Modo watch (requiere entr)
# Ejecutar tests automáticamente al cambiar archivos
find app/src -name "*.kt" | entr -c ./gradlew test

# Abrir archivos clave en editor
code app/src/main/java/com/gymcompanion/app/data/local/entity/
code app/src/main/java/com/gymcompanion/app/presentation/screens/
```

## Debugging

```bash
# Ver logs en tiempo real (requiere dispositivo/emulador conectado)
adb logcat | grep "GymCompanion"

# Ver dispositivos conectados
adb devices

# Instalar APK manualmente
adb install app/build/outputs/apk/debug/app-debug.apk

# Desinstalar app
adb uninstall com.gymcompanion.app

# Limpiar datos de la app
adb shell pm clear com.gymcompanion.app

# Ver base de datos (requiere root o app debuggable)
adb shell
run-as com.gymcompanion.app
cd databases
ls -la
```

## Limpieza

```bash
# Limpiar archivos de build
./gradlew clean
rm -rf .gradle/
rm -rf app/build/

# Limpiar caché de Android Studio (manualmente en Android Studio)
# File → Invalidate Caches → Invalidate and Restart
```

## Generación de Reportes

```bash
# Reporte de tests
./gradlew test
# Ver en: app/build/reports/tests/testDebugUnitTest/index.html

# Reporte de cobertura
./gradlew jacocoTestReport
# Ver en: app/build/reports/jacoco/test/html/index.html

# Reporte de lint
./gradlew lint
# Ver en: app/build/reports/lint-results.html
```

## Exportar e Importar

```bash
# Crear backup del proyecto
tar -czf gym_companion_backup.tar.gz /home/llibre/acompanamiento_gym

# Extraer backup
tar -xzf gym_companion_backup.tar.gz

# Copiar solo código fuente
rsync -av --exclude='.gradle' --exclude='build' \
  /home/llibre/acompanamiento_gym /ruta/destino/
```

## Configuración de Android Studio (Terminal)

```bash
# Verificar Java version
java -version
# Debe ser Java 17

# Verificar JAVA_HOME
echo $JAVA_HOME

# Configurar JAVA_HOME si es necesario
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Verificar Android SDK
echo $ANDROID_SDK_ROOT
# O
echo $ANDROID_HOME

# Listar AVDs (emuladores)
emulator -list-avds

# Iniciar emulador específico
emulator -avd Pixel_6_API_33
```

## Atajos de Teclado Android Studio

```
Ctrl + Shift + A       → Find Action
Ctrl + N               → Go to Class
Ctrl + Shift + N       → Go to File
Ctrl + Alt + Shift + N → Go to Symbol
Ctrl + /               → Comment/Uncomment Line
Ctrl + D               → Duplicate Line
Ctrl + Y               → Delete Line
Ctrl + Space           → Code Completion
Alt + Enter            → Quick Fix
Shift + F10            → Run
Shift + F9             → Debug
Ctrl + F9              → Build
Ctrl + Shift + F9      → Rebuild
Alt + F12              → Open Terminal
Ctrl + E               → Recent Files
Ctrl + Shift + E       → Recent Edited Files
```

## Scripts Personalizados

```bash
# Ejecutar setup inicial (si no lo has hecho)
bash setup.sh

# Compilar y instalar rápido
./gradlew installDebug && adb shell am start -n com.gymcompanion.app/.presentation.MainActivity

# Limpiar y rebuild completo
./gradlew clean build
```

## Notas Importantes

⚠️ **IMPORTANTE**: 
- El Gradle Wrapper (./gradlew) se descarga automáticamente al abrir el proyecto en Android Studio
- Si usas terminal directamente, primero abre el proyecto en Android Studio
- Algunos comandos requieren dispositivo/emulador conectado

💡 **TIPS**:
- Usa Android Studio para desarrollo diario
- Terminal para builds automatizados
- Git para control de versiones
- Gradle para tareas de compilación

---

**¿Necesitas más comandos?** Consulta la documentación oficial:
- Gradle: https://docs.gradle.org/
- ADB: https://developer.android.com/studio/command-line/adb
- Git: https://git-scm.com/docs
