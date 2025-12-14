@echo off
REM Script para compilar el proyecto KMP en Desktop/JVM
REM Ejecutar desde PowerShell: .\compile-desktop.bat

set JAVA_HOME=C:\Program Files\Android\Android Studio2\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo Compilando proyecto KMP para Desktop/JVM...
echo JAVA_HOME=%JAVA_HOME%
echo.

gradlew.bat :composeApp:compileKotlinJvm

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilacion exitosa
) else (
    echo.
    echo Error en compilacion
    exit /b %ERRORLEVEL%
)
