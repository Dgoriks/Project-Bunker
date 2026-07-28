@echo off
chcp 65001 > nul
echo === СИНХРОНИЗАЦИЯ МОДА С KIMI CHAT ===
echo.
echo Скачиваю свежие правки из GitHub...
git pull origin main
echo.
echo === Готово! Файлы на ПК обновлены ===
timeout /t 3
