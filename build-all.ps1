$versions = @("2021.3", "2022.3.3", "2023.3.2", "2025.1", "2026.1")

foreach ($version in $versions) {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Building bundle for IDEA $version" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    
    .\gradlew buildBundle "-PplatformVersion=$version" --no-configuration-cache --console=plain
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build failed for version $version" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "All builds completed successfully!" -ForegroundColor Green
Write-Host "Bundles are located in build/bundle/" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
