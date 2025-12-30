# 나머지 폴더 정리 스크립트

Write-Host "=== 나머지 모듈 정리 시작 ===" -ForegroundColor Green

# 1. 도메인/스토리지를 libs로 이동
Write-Host "`n[1/3] 도메인/스토리지를 libs로 이동..." -ForegroundColor Cyan
if (Test-Path "chat-domain") {
    Move-Item -Path "chat-domain" -Destination "libs\chat-domain" -Force
    Write-Host "  ✓ chat-domain → libs/chat-domain" -ForegroundColor Green
}
if (Test-Path "chat-storage") {
    Move-Item -Path "chat-storage" -Destination "libs\chat-storage" -Force
    Write-Host "  ✓ chat-storage → libs/chat-storage" -ForegroundColor Green
}

# 2. common-util을 common으로 이동
Write-Host "`n[2/3] common-util을 common으로 이동..." -ForegroundColor Cyan
if (Test-Path "common-util") {
    Move-Item -Path "common-util" -Destination "common\util" -Force
    Write-Host "  ✓ common-util → common/util" -ForegroundColor Green
}

# 3. modules 내용을 common으로 이동
Write-Host "`n[3/3] modules를 common으로 통합..." -ForegroundColor Cyan
if (Test-Path "modules") {
    Get-ChildItem -Path "modules" | ForEach-Object {
        $targetPath = "common\$($_.Name)"
        Move-Item -Path $_.FullName -Destination $targetPath -Force
        Write-Host "  ✓ modules/$($_.Name) → common/$($_.Name)" -ForegroundColor Green
    }
    Remove-Item -Path "modules" -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ modules 폴더 제거" -ForegroundColor Green
}

# 4. 빈 chat 폴더 제거
if (Test-Path "chat") {
    $chatItems = Get-ChildItem -Path "chat" -Force
    if ($chatItems.Count -eq 0) {
        Remove-Item -Path "chat" -Force
        Write-Host "  ✓ 빈 chat 폴더 제거" -ForegroundColor Green
    }
}

Write-Host "`n=== 정리 완료! ===" -ForegroundColor Green
Write-Host "`n최종 구조:" -ForegroundColor Yellow
Write-Host "  📂 apps/" -ForegroundColor White
Write-Host "    ├─ auth-server/" -ForegroundColor Gray
Write-Host "    └─ chat/" -ForegroundColor Gray
Write-Host "        ├─ message-server/" -ForegroundColor Gray
Write-Host "        ├─ system-server/" -ForegroundColor Gray
Write-Host "        └─ websocket-server/" -ForegroundColor Gray
Write-Host "  📂 libs/" -ForegroundColor White
Write-Host "    ├─ chat-domain/" -ForegroundColor Gray
Write-Host "    └─ chat-storage/" -ForegroundColor Gray
Write-Host "  📂 common/" -ForegroundColor White
Write-Host "    ├─ util/" -ForegroundColor Gray
Write-Host "    ├─ logging/" -ForegroundColor Gray
Write-Host "    └─ auth-security/" -ForegroundColor Gray
