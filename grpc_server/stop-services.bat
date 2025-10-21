@echo off
echo Deteniendo servicios Kafka...

echo.
echo [1/3] Deteniendo Kafka...
cd /d "C:\kafka"
bin\windows\kafka-server-stop.bat 2>nul

echo.
echo [2/3] Deteniendo Zookeeper...
bin\windows\zookeeper-server-stop.bat 2>nul

echo.
echo [3/3] Limpiando procesos Java residuales...
taskkill /f /im java.exe 2>nul

echo.
echo [✓] Servicios detenidos correctamente
echo.
pause