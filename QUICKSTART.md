# 🚀 Inicio Rápido - Gym Companion

## Para Comenzar Inmediatamente

### 1️⃣ Abre Android Studio
```bash
# Abre Android Studio y selecciona:
File → Open → /home/llibre/acompanamiento_gym
```

### 2️⃣ Espera la Sincronización
- Android Studio sincronizará Gradle automáticamente (2-5 minutos la primera vez)
- Descargará todas las dependencias necesarias

### 3️⃣ Ejecuta la App
- Click en el botón verde ▶️ (Run)
- Selecciona un emulador o dispositivo físico
- ¡Listo! La app se instalará y ejecutará

## 📁 Archivos Importantes

| Archivo | Descripción |
|---------|-------------|
| `README.md` | Documentación completa del proyecto |
| `COMPILE.md` | Guía detallada de compilación |
| `PROJECT_STATS.md` | Estadísticas y características |
| `setup.sh` | Script de preparación (ya ejecutado) |

## 🎯 Estructura del Proyecto

```
app/src/main/java/com/gymcompanion/app/
├── data/
│   ├── local/
│   │   ├── entity/      ← 7 entidades Room
│   │   ├── dao/         ← 5 DAOs
│   │   └── GymDatabase.kt
│   └── repository/      ← 30 ejercicios predefinidos
├── presentation/
│   ├── screens/         ← 5 pantallas Compose
│   ├── navigation/      ← NavHost
│   └── theme/           ← Material 3
└── di/                  ← Hilt modules
```

## ✨ Características Listas para Usar

- ✅ 5 pantallas con navegación inferior
- ✅ Base de datos Room configurada
- ✅ 30 ejercicios organizados por grupo muscular
- ✅ Sistema de métricas corporales completo
- ✅ Arquitectura Clean + MVVM
- ✅ Material Design 3 con tema personalizado

## 📱 Navegación de la App

1. **Inicio** - Dashboard con estadísticas
2. **Rutinas** - Gestión de rutinas de entrenamiento
3. **Ejercicios** - Biblioteca de 30 ejercicios
4. **Progreso** - Gráficos y evolución
5. **Perfil** - Datos corporales del usuario

## 🔧 Solución Rápida de Problemas

### Si Android Studio no sincroniza
```
File → Invalidate Caches → Invalidate and Restart
```

### Si falta el SDK
```
Tools → SDK Manager → Install Android 13.0 (API 33)
```

### Si hay errores de compilación
```
Build → Clean Project
Build → Rebuild Project
```

## 📚 Próximos Pasos de Desarrollo

Archivos que deberías implementar a continuación:

1. **Formulario de Datos Corporales**
   - `presentation/screens/profile/BodyMetricsForm.kt`
   - Capturar: peso, altura, edad, IMC, % grasa, medidas

2. **CRUD de Rutinas**
   - `presentation/screens/routines/CreateRoutineScreen.kt`
   - `presentation/screens/routines/RoutineDetailScreen.kt`

3. **Pantalla de Sesión de Entrenamiento**
   - `presentation/screens/workout/WorkoutSessionScreen.kt`
   - Con temporizadores y contadores

4. **ViewModels**
   - `presentation/screens/*/ViewModel.kt` para cada pantalla
   - Conectar con los DAOs

5. **Repositorios**
   - `data/repository/*Repository.kt`
   - Intermediarios entre DAOs y ViewModels

## 🎨 Personalización

### Cambiar colores del tema
```kotlin
// app/src/main/java/.../presentation/theme/Color.kt
val GymPrimary = Color(0xFF6200EE)  // Cambia esto
```

### Agregar más ejercicios
```kotlin
// app/src/main/java/.../data/repository/InitialDataRepository.kt
// Añade más ExerciseEntity al final de la lista
```

### Modificar strings
```xml
<!-- app/src/main/res/values/strings.xml -->
<string name="app_name">Tu Nombre</string>
```

## 🧪 Testing

```bash
# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests instrumentados
./gradlew connectedAndroidTest
```

## 📦 Generar APK

```bash
# APK Debug
./gradlew assembleDebug
# Salida: app/build/outputs/apk/debug/app-debug.apk

# APK Release (firmado)
./gradlew assembleRelease
```

## 💡 Tips Útiles

- 🔍 **Buscar en código**: Ctrl+Shift+F
- 🏗️ **Rebuil project**: Ctrl+Shift+F9
- ▶️ **Run app**: Shift+F10
- 🐛 **Debug app**: Shift+F9
- 📱 **Device Manager**: Ctrl+Shift+A → "Device Manager"

## 📖 Recursos de Aprendizaje

- **Compose**: https://developer.android.com/jetpack/compose/tutorial
- **Room**: https://developer.android.com/training/data-storage/room
- **Hilt**: https://developer.android.com/training/dependency-injection/hilt-android
- **Navigation**: https://developer.android.com/jetpack/compose/navigation

## 🆘 ¿Necesitas Ayuda?

1. Lee `COMPILE.md` para problemas de compilación
2. Revisa `README.md` para arquitectura detallada
3. Consulta `PROJECT_STATS.md` para estadísticas
4. Verifica logs en Android Studio: View → Tool Windows → Logcat

---

**¡Todo listo para empezar a desarrollar! 💪🏋️**

```
     🏋️ GYM COMPANION 🏋️
   Tu Asistente Personal de Fitness
           
     [▶️ EJECUTAR AHORA]
```
