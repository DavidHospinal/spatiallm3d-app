# Full build test with visualization components

Write-Host "Building SpatialLM3D with 3D Visualization..." -ForegroundColor Cyan

# Clean previous build
Write-Host "`nCleaning previous build..." -ForegroundColor Yellow
.\gradlew clean

# Compile Desktop target
Write-Host "`nCompiling Kotlin for JVM..." -ForegroundColor Yellow
.\gradlew :composeApp:compileKotlinJvm --console=plain

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild successful!" -ForegroundColor Green
    Write-Host "`nRunning Desktop application..." -ForegroundColor Cyan
    .\gradlew :composeApp:run
} else {
    Write-Host "`nBuild failed. Check errors above." -ForegroundColor Red
    exit 1
}
