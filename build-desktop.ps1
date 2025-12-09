# Build script for Desktop-only compilation
# Use this when Android SDK is not available

Write-Host "Building KMP project (Desktop + Common + iOS only)..." -ForegroundColor Cyan

# Clean previous builds
./gradlew clean

# Build common code and Desktop
./gradlew :composeApp:jvmJar

# Run tests
./gradlew :composeApp:jvmTest

Write-Host "`nBuild completed! Run the app with:" -ForegroundColor Green
Write-Host "  ./gradlew :composeApp:run" -ForegroundColor Yellow
