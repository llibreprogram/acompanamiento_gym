#!/bin/bash
# Script de preparación del proyecto Android

echo "🏋️ Preparando proyecto Gym Companion..."

# Crear directorios necesarios para recursos
echo "📁 Creando directorios de recursos..."
mkdir -p app/src/main/res/mipmap-mdpi
mkdir -p app/src/main/res/mipmap-hdpi
mkdir -p app/src/main/res/mipmap-xhdpi
mkdir -p app/src/main/res/mipmap-xxhdpi
mkdir -p app/src/main/res/mipmap-xxxhdpi
mkdir -p app/src/main/res/drawable

# Crear directorio para ilustraciones de ejercicios
mkdir -p app/src/main/res/drawable/exercises

echo "✅ Directorios creados"

# Instrucciones para el usuario
echo ""
echo "📋 Próximos pasos:"
echo "1. Abre el proyecto en Android Studio"
echo "2. Espera a que Gradle se sincronice automáticamente"
echo "3. Android Studio descargará el Gradle Wrapper automáticamente"
echo "4. Compila el proyecto con: Build > Make Project"
echo ""
echo "⚡ Para compilar desde terminal (después de abrir en Android Studio):"
echo "   ./gradlew assembleDebug"
echo ""
echo "🎨 Nota: Los íconos de launcher e ilustraciones de ejercicios"
echo "   son placeholders que deben reemplazarse con assets reales."
