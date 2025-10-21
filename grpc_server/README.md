# 🚀 SISTEMA DISTRIBUIDO ONG - GUÍA DE USO

## ⚠️ PARA EVITAR PROBLEMAS CON KAFKA:

### 🔄 **Secuencia correcta de inicio:**

1. **SIEMPRE ejecutar en este orden:**
   ```batch
   # Opción A: Script automatizado (RECOMENDADO)
   start-all.bat
   
   # Opción B: Manual
   start-services.bat
   # Esperar 30 segundos
   mvn spring-boot:run
   ```

2. **Para detener correctamente:**
   ```batch
   # Ctrl+C en la consola de Spring Boot
   # Luego ejecutar:
   stop-services.bat
   ```

### 🛠️ **Scripts disponibles:**

- `start-all.bat` - **Inicia todo automáticamente** (RECOMENDADO)
- `start-services.bat` - Solo inicia Kafka y Zookeeper
- `stop-services.bat` - Detiene todo correctamente

### 🚨 **Si aparece el error de conexión:**

```
Connection to node -1 could not be established
```

**Solución rápida:**
```batch
stop-services.bat
# Esperar 10 segundos
start-all.bat
```

### ✅ **Verificar que todo funciona:**

1. **Kafka funcionando:**
   ```
   netstat -an | findstr :9092
   ```
   Debe mostrar: `TCP    0.0.0.0:9092           0.0.0.0:0              LISTENING`

2. **Aplicación funcionando:**
   - Puerto 8080 (REST API): http://localhost:8080
   - Puerto 9090 (gRPC): Conectado
   - Logs sin errores de Kafka

### 🎯 **Endpoints principales:**

- **Adhesiones:** `POST /adhesions/join-direct`
- **Ofertas:** `POST /offers/publish`, `GET /offers/list`
- **Solicitudes:** `POST /requests/publish`, `GET /requests/list`
- **Transferencias:** `POST /transfers/send/{recipientOrgId}`
- **Eventos:** `POST /events/publish`, `GET /events/list`
- **Inventario:** `GET /inventario/list`
- **Bajas:** `GET /event-cancellations` (con filtros opcionales)

### 💡 **Consejos:**

1. **Nunca cerrar** las consolas de Kafka/Zookeeper bruscamente
2. **Usar siempre** `stop-services.bat` antes de apagar
3. **Si hay dudas**, usar `start-all.bat` que hace todo automáticamente
4. **Mantener** las ventanas de Kafka abiertas mientras trabajas

---
**Sistema optimizado y limpio - Versión Octubre 2025** ✨