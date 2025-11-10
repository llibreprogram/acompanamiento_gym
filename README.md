# 🏋️ Gym Companion - Asistente Personal de Fitness

**Gym Companion** es una aplicación Android nativa diseñada para acompañarte en tus sesiones de gimnasio, gestionar rutinas personalizadas y evolucionar tus entrenamientos basándose en tu progreso individual.

## 📋 Características Principales

### ✅ Implementadas en MVP
- ✅ **Arquitectura Clean + MVVM** con inyección de dependencias (Hilt)
- ✅ **Base de datos Room** con 7 entidades relacionales y auto-inicialización
- ✅ **Sistema completo de datos corporales**: peso, altura, edad, IMC, % grasa, medidas
- ✅ **Biblioteca de 30 ejercicios fundamentales** con instrucciones detalladas
- ✅ **Interfaz Jetpack Compose** con navegación y Material Design 3
- ✅ **5 pantallas principales completamente funcionales**:
  - 🏠 **Home**: Estadísticas semanales, rutinas del día, último entrenamiento
  - 💪 **Rutinas**: Lista de rutinas, iniciar entrenamientos, gestión CRUD
  - 📚 **Ejercicios**: Búsqueda, filtros, detalles completos con técnica
  - 📊 **Progreso**: Gráficos Vico (peso, IMC, volumen), estadísticas totales
  - � **Perfil**: Datos corporales, historial de métricas
- ✅ **Sistema de sesiones de entrenamiento**: Timer en vivo, registro de sets (peso/reps/RIR)
- ✅ **Repositorios completos**: 5 repositorios con interfaces y implementaciones
- ✅ **48 archivos Kotlin** con Clean Architecture

### 🚧 En Desarrollo
- 🔄 Pantalla de crear/editar rutinas con selector de ejercicios
- 🔄 Motor de recomendaciones basado en IA
- 🔄 Integración con wearables

## 🏗️ Arquitectura del Proyecto

```
app/
├── data/
│   ├── local/
│   │   ├── entity/          # Entidades Room (User, BodyMetrics, Exercise, etc.)
│   │   ├── dao/             # Data Access Objects
│   │   └── GymDatabase.kt   # Configuración de Room
│   └── repository/          # Repositorios y datos iniciales
├── domain/                  # Casos de uso (próximamente)
├── presentation/
│   ├── navigation/          # NavHost y rutas
│   ├── screens/             # Pantallas Compose
│   │   ├── home/
│   │   ├── routines/
│   │   ├── exercises/
│   │   ├── progress/
│   │   └── profile/
│   └── theme/               # Colores, tipografía, temas
└── di/                      # Módulos de Hilt
```

## 🗄️ Modelo de Datos

### Entidades Principales

1. **UserEntity**: Información del usuario (nombre, fecha de nacimiento, género)
2. **BodyMetricsEntity**: Métricas corporales con seguimiento histórico
   - Datos básicos: peso, altura, edad
   - Composición: IMC (calculado), % grasa corporal
   - Medidas: pecho, cintura, cadera, muslos, brazos, pantorrillas
3. **ExerciseEntity**: Biblioteca de ejercicios con instrucciones detalladas
4. **RoutineEntity**: Rutinas personalizadas del usuario
5. **RoutineExerciseEntity**: Relación N:M entre rutinas y ejercicios
6. **WorkoutSessionEntity**: Sesiones de entrenamiento completadas
7. **ExerciseSetEntity**: Series individuales realizadas

### Relaciones
- Usuario → Múltiples métricas corporales (1:N)
- Usuario → Múltiples rutinas (1:N)
- Rutina → Múltiples ejercicios (N:M)
- Usuario → Múltiples sesiones de entrenamiento (1:N)
- Sesión → Múltiples series de ejercicios (1:N)

## 📚 Biblioteca de Ejercicios

La app incluye **30 ejercicios fundamentales** distribuidos en:

- 🫀 **Pecho**: 5 ejercicios (press banca, flexiones, aperturas, etc.)
- 🔙 **Espalda**: 5 ejercicios (dominadas, remos, peso muerto, etc.)
- 🦵 **Piernas**: 6 ejercicios (sentadillas, peso muerto rumano, zancadas, etc.)
- 💪 **Hombros**: 4 ejercicios (press militar, elevaciones, face pulls)
- 💪 **Brazos**: 5 ejercicios (curls, extensiones, fondos)
- 🧘 **Core**: 5 ejercicios (planchas, crunches, elevaciones, etc.)

Cada ejercicio incluye:
- Descripción detallada de la técnica
- Músculos objetivo
- Nivel de dificultad (principiante, intermedio, avanzado)
- Instrucciones paso a paso
- Errores comunes a evitar
- Consejos de seguridad
- Variaciones por nivel

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Arquitectura**: Clean Architecture + MVVM
- **Base de datos**: Room
- **Inyección de dependencias**: Hilt/Dagger
- **Navegación**: Navigation Compose
- **Coroutines**: Para operaciones asíncronas
- **Gráficos**: Vico Charts (para visualización de progreso)
- **Imágenes**: Coil (para carga de ilustraciones)

## 📦 Dependencias Principales

```kotlin
// Compose
androidx.compose.ui
androidx.compose.material3
androidx.navigation:navigation-compose

// Room Database
androidx.room:room-runtime
androidx.room:room-ktx

// Hilt
com.google.dagger:hilt-android
androidx.hilt:hilt-navigation-compose

// Charts
com.patrykandpatrick.vico:compose

// Coil
io.coil-kt:coil-compose
```

## 🚀 Cómo Compilar y Ejecutar

### Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Pasos

1. **Clonar el repositorio**
   ```bash
   cd acompanamiento_gym
   ```

2. **Abrir en Android Studio**
   - File → Open → Seleccionar carpeta del proyecto

3. **Sincronizar Gradle**
   - Android Studio sincronizará automáticamente
   - O manualmente: File → Sync Project with Gradle Files

4. **Compilar el proyecto**
   ```bash
   ./gradlew build
   ```

5. **Ejecutar en emulador o dispositivo**
   - Conectar dispositivo Android con depuración USB habilitada
   - O crear un AVD (Android Virtual Device)
   - Click en el botón ▶️ Run

## 📱 Características Detalladas

### Sistema de Datos Corporales

El módulo de métricas corporales captura:

**Datos obligatorios:**
- Peso (kg) con seguimiento histórico
- Altura (cm)
- Edad (calculada dinámicamente desde fecha de nacimiento)
- Nivel de experiencia (principiante, intermedio, avanzado)

**Métricas de composición:**
- IMC (calculado automáticamente)
- Porcentaje de grasa corporal
- Medidas corporales detalladas

**Funcionalidades:**
- Validación de rangos razonables
- Visualización de tendencias históricas
- Interpretación automática de IMC según OMS

### Motor de Recomendaciones (Planificado)

Utilizará los datos corporales para:
- Calcular intensidad segura basada en edad y experiencia
- Ajustar volumen según composición corporal
- Personalizar ejercicios según proporciones
- Sugerir progresión de cargas apropiada
- Identificar desbalances musculares potenciales

## 🔐 Seguridad y Privacidad

- Almacenamiento local seguro con Room
- Sin transmisión de datos a servidores externos (por ahora)
- Cumplimiento de regulaciones de privacidad de datos de salud
- Disclaimers apropiados sobre consulta médica

## 🗺️ Roadmap

### Fase 1 - MVP (Completada) ✅
- [x] Arquitectura base Clean + MVVM
- [x] Base de datos Room con 7 entidades
- [x] Biblioteca de 30 ejercicios con datos completos
- [x] Navegación y UI con Material Design 3
- [x] Sistema de datos corporales funcional
- [x] CRUD de rutinas con repositorios
- [x] Sistema de sesiones de entrenamiento con timer
- [x] Pantalla de progreso con gráficos Vico
- [x] 5 pantallas principales completamente funcionales

### Fase 2 - Funcionalidad Core
- [ ] Seguimiento de sesiones en tiempo real
- [ ] Temporizadores y contadores
- [ ] Historial de entrenamientos
- [ ] Gráficos de progreso

### Fase 3 - IA y Personalización
- [ ] Recomendaciones basadas en reglas heurísticas
- [ ] Integración con API de IA (OpenAI/Gemini)
- [ ] Sugerencias de rutinas personalizadas

### Fase 4 - Características Avanzadas
- [ ] Integración con wearables
- [ ] Sincronización en la nube
- [ ] Sistema de logros y gamificación
- [ ] Calculadoras de fitness (1RM, calorías, macros)

## 🎨 Diseño UI/UX

- **Material Design 3** con tema personalizable
- **Navegación inferior** para acceso rápido a secciones principales
- **Cards y elevaciones** para jerarquía visual
- **Colores específicos por grupo muscular** para identificación rápida
- **Modo offline completo** (sin dependencia de red)

## 📄 Licencia

Este proyecto es de código abierto para propósitos educativos y de desarrollo personal.

## ⚠️ Disclaimer

Esta aplicación proporciona recomendaciones generales de fitness. **Consulte con un médico o profesional de la salud antes de iniciar cualquier programa de ejercicio**, especialmente si tiene condiciones de salud preexistentes, está embarazada, es mayor de 40 años o ha estado inactivo por un período prolongado.

## 👨‍💻 Desarrollo

**Estado actual**: MVP Completado ✅
**Versión**: 1.0.0
**Archivos Kotlin**: 48
**Última actualización**: 10 de Noviembre 2025

### 📊 Estadísticas del Proyecto
- **Total de archivos Kotlin**: 48
- **Entidades Room**: 7
- **DAOs**: 5
- **Repositorios**: 5 (interfaces + implementaciones)
- **ViewModels**: 5
- **Pantallas Compose**: 8
- **Ejercicios precargados**: 30
- **Líneas de código**: ~4,500+

---

**¿Tienes preguntas o sugerencias?** ¡Abre un issue o contribuye al proyecto! 💪
