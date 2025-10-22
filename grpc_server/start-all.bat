@echo off
title Iniciador Completo - Sistema Distribuido ONG
color 0A

echo ===============================================
echo    SISTEMA DISTRIBUIDO ONG - INICIADOR
echo ===============================================
echo.

echo [PASO 1] Verificando servicios existentes...
echo.

REM Detener servicios previos si existen
echo Deteniendo servicios previos...
taskkill /f /im java.exe 2>nul
echo.

echo [PASO 2] Limpiando datos temporales...
echo.
rmdir /s /q "C:\kafka-logs" 2>nul
rmdir /s /q "C:\zookeeper" 2>nul
echo Datos temporales limpiados.
echo.

echo [PASO 3] Iniciando Zookeeper...
echo.
start "Zookeeper-ONG" cmd /c "title Zookeeper Server && cd /d C:\kafka && echo Iniciando Zookeeper... && bin\windows\zookeeper-server-start.bat config\zookeeper.properties"
echo Zookeeper iniciado en puerto 2181
echo.

echo [PASO 4] Esperando estabilización de Zookeeper...
echo.
timeout /t 15 /nobreak > nul

echo [PASO 5] Iniciando Kafka...
echo.
start "Kafka-ONG" cmd /c "title Kafka Server && cd /d C:\kafka && echo Iniciando Kafka... && bin\windows\kafka-server-start.bat config\server.properties"
echo Kafka iniciado en puerto 9092
echo.

echo [PASO 6] Esperando estabilización de Kafka...
echo.
timeout /t 20 /nobreak > nul

echo [PASO 7] Verificando conectividad...
echo.
netstat -an | findstr :9092 > nul
if %errorlevel%==0 (
    echo ✓ Kafka está escuchando en puerto 9092
) else (
    echo ✗ Kafka no responde - reintentar en unos segundos
)
echo.

echo [PASO 8] Iniciando aplicación Spring Boot...
echo.
echo IMPORTANTE: Mantén esta ventana abierta mientras trabajas
echo Para detener todo, usa stop-services.bat
echo.
echo ===============================================
echo    SERVICIOS LISTOS - INICIANDO APLICACION
echo ===============================================
echo.

REM Iniciar Spring Boot
mvn spring-boot:run

echo.
echo ===============================================
echo La aplicación se ha detenido.
echo Para reiniciar, ejecuta este script nuevamente.
echo ===============================================
pause