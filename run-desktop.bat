@echo off
REM Script para ejecutar la aplicacion Desktop
REM Ejecutar desde PowerShell: .\run-desktop.bat

set JAVA_HOME=C:\Program Files\Android\Android Studio2\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo Ejecutando aplicacion Desktop...
echo JAVA_HOME=%JAVA_HOME%
echo.

gradlew.bat :composeApp:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error al ejecutar aplicacion
    exit /b %ERRORLEVEL%
)
