# Quick build check without running

Write-Host "Checking build for SpatialLM3D..." -ForegroundColor Cyan

# Compile Desktop target (fastest for quick validation)
.\gradlew :composeApp:compileKotlinJvm --console=plain

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild check passed!" -ForegroundColor Green
} else {
    Write-Host "`nBuild check failed. Please fix the errors above." -ForegroundColor Red
    exit 1
}
