@echo off
REM Script para compilar y deployar en Android
REM Ejecutar desde PowerShell: .\compile-android.bat

set JAVA_HOME=C:\Program Files\Android\Android Studio2\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo Compilando e instalando APK en dispositivo Android...
echo JAVA_HOME=%JAVA_HOME%
echo.

gradlew.bat :composeApp:installDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo APK instalado exitosamente en dispositivo
) else (
    echo.
    echo Error al instalar APK
    exit /b %ERRORLEVEL%
)
