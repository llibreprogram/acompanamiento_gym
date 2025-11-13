#!/bin/bash
# Script para subir los cambios a GitHub

cd /home/llibre/acompanamiento_gym

echo "📝 Agregando archivos..."
git add .

echo "💾 Commiteando cambios..."
git commit -m "Fix: Improve GitHub Actions workflow and add build documentation

- Add android-actions/setup-android@v3 for proper SDK setup
- Add Gradle caching to speed up builds  
- Add --stacktrace and --no-daemon flags
- Add BUILD_STATUS.md with detailed compilation guide
- Document ARM64 vs x86-64 compatibility issues
- Set artifact retention to 30 days"

echo "🚀 Subiendo a GitHub..."
git push origin main

echo "✅ Cambios subidos exitosamente!"
echo ""
echo "Ver el build en: https://github.com/llibreprogram/acompanamiento_gym/actions"
