---
description: Consultar la web para investigar ideas, validar enfoques técnicos o buscar mejores prácticas antes de implementar una funcionalidad.
---

# Web Research Skill

Este skill se usa para investigar y validar ideas técnicas antes de implementarlas.

## Cuándo usarlo

- Antes de implementar una nueva funcionalidad compleja
- Para confirmar si un enfoque técnico es correcto o hay mejores alternativas
- Para buscar mejores prácticas, patrones de diseño o librerías recomendadas
- Para verificar compatibilidad de versiones o APIs

## Pasos

### 1. Definir la pregunta de investigación
Antes de buscar, formular claramente:
- **¿Qué quiero saber?** (ej: "¿Cuál es la mejor forma de implementar offline-first sync en Android?")
- **¿Qué contexto tengo?** (ej: "Usamos Room + Kotlin + Compose")
- **¿Qué alternativas considero?** (ej: "WorkManager vs custom sync")

### 2. Buscar en la web
Usar la herramienta `search_web` con queries específicas. Ejemplos de buenos queries:
- `"Android Room offline first sync best practices 2024"`
- `"Jetpack Compose animation performance tips"`
- `"Kotlin coroutines vs RxJava comparison Android"`

Si el resultado no es suficiente, reformular el query con más contexto o términos diferentes.

### 3. Leer fuentes relevantes
Usar `read_url_content` para leer las páginas más relevantes de los resultados. Priorizar:
- Documentación oficial (developer.android.com, kotlinlang.org)
- Artículos técnicos de Medium, dev.to, o blogs reconocidos
- Respuestas aceptadas de Stack Overflow
- Repositorios de GitHub con muchas estrellas

### 4. Sintetizar hallazgos
Presentar al usuario un resumen estructurado con:
- **Opciones encontradas** (con pros y contras de cada una)
- **Recomendación** basada en el contexto del proyecto
- **Fuentes** (links a las páginas consultadas)
- **Ejemplo de código** si aplica

### 5. Documentar la decisión
Si el usuario aprueba un enfoque, documentarlo brevemente antes de implementar:
- Qué enfoque se eligió y por qué
- Qué alternativas se descartaron
- Links de referencia útiles para el futuro

## Ejemplo de uso

```
Usuario: "Quiero añadir notificaciones push a la app, ¿cuál es la mejor forma?"

1. Buscar: "Android push notifications best practices 2024 Kotlin"
2. Leer: Documentación de Firebase Cloud Messaging, alternativas como OneSignal
3. Sintetizar: FCM es gratuito y se integra bien con Android, OneSignal es más fácil pero tiene límites
4. Recomendar: FCM por ser nativo y sin costos adicionales
5. Documentar: Se eligió FCM, descartando OneSignal por dependencia externa
```
