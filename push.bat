@echo off
chcp 65001 > nul
set /p msg="Введите описание коммита на русском: "
git add .
git commit -m "%msg%"
git push origin main
echo === Все изменения успешно улетели на GitHub! ===
pause
