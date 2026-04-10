$FX_PATH = "c:\Users\Student\Desktop\openjfx-21.0.10_windows-x64_bin-sdk\javafx-sdk-21.0.10\lib"

Write-Host "Compiling Zenith Hotel Suite v2.0..." -ForegroundColor Cyan
if (!(Test-Path bin)) { New-Item -ItemType Directory -Path bin | Out-Null }

javac --module-path $FX_PATH --add-modules javafx.controls,javafx.fxml -d bin src/com/zenith/model/*.java src/com/zenith/*.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "Copying resources..." -ForegroundColor Cyan
    $DEST = "bin\com\zenith"
    if (!(Test-Path $DEST)) { New-Item -ItemType Directory -Path $DEST | Out-Null }
    Copy-Item "resources\*.fxml" $DEST -Force
    Copy-Item "resources\*.css" $DEST -Force
    Write-Host "Compilation successful. Launching..." -ForegroundColor Green
    java --module-path $FX_PATH --add-modules javafx.controls,javafx.fxml -cp bin com.zenith.ZenithHotelApp
} else {
    Write-Host "Compilation failed." -ForegroundColor Red
}
