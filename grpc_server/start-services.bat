@echo off
echo Iniciando servicios Kafka...

echo.
echo [1/4] Limpiando logs antiguos...
rmdir /s /q "C:\tmp\kafka-logs" 2>nul
rmdir /s /q "C:\tmp\zookeeper" 2>nul

echo.
echo [2/4] Iniciando Zookeeper...
start "Zookeeper" cmd /c "cd /d C:\kafka && bin\windows\zookeeper-server-start.bat config\zookeeper.properties"

echo.
echo [3/4] Esperando Zookeeper (10 segundos)...
timeout /t 10 /nobreak > nul

echo.
echo [4/4] Iniciando Kafka...
start "Kafka" cmd /c "cd /d C:\kafka && bin\windows\kafka-server-start.bat config\server.properties"

echo.
echo [✓] Servicios iniciados correctamente
echo [INFO] Zookeeper: puerto 2181
echo [INFO] Kafka: puerto 9092
echo.
echo Presiona cualquier tecla para continuar...
pause > nul