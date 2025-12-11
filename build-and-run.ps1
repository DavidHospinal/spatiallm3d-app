# Build and run the Desktop application

Write-Host "Building SpatialLM3D Desktop application..." -ForegroundColor Cyan

# Clean build (optional)
# .\gradlew clean

# Compile Desktop target
Write-Host "`nCompiling Kotlin for JVM..." -ForegroundColor Yellow
.\gradlew :composeApp:compileKotlinJvm

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild successful!" -ForegroundColor Green

    # Run Desktop application
    Write-Host "`nRunning Desktop application..." -ForegroundColor Cyan
    .\gradlew :composeApp:run
} else {
    Write-Host "`nBuild failed. Please check the errors above." -ForegroundColor Red
    exit 1
}
