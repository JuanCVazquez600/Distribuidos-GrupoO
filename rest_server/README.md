# Donation Excel Service

Microservicio para gestión de donaciones y generación de reportes Excel organizados por categorías.

## 🚀 Características

- **API REST** para gestión completa de donaciones
- **Reportes Excel** con múltiples hojas organizadas por categorías
- **Base de datos MariaDB** para persistencia de datos
- **Documentación Swagger** integrada
- **Validaciones** de entrada y manejo de errores
- **Datos de ejemplo** precargados

## 📋 Categorías de Donaciones

- ROPA
- COMIDA
- MEDICAMENTOS
- JUGUETES
- ELECTRODOMÉSTICOS
- LIBROS
- OTROS

## 🛠️ Tecnologías

- **Spring Boot 2.5.4**
- **Apache POI 5.2.3** para generación de Excel
- **Spring Data JPA** para persistencia
- **MariaDB** como base de datos
- **Swagger/OpenAPI 3** para documentación
- **Maven** para gestión de dependencias

## ⚙️ Configuración

### Requisitos previos
- Java 11 o superior
- Maven 3.6+
- MariaDB 10.x

### Base de datos
1. Crear base de datos en MariaDB:
```sql
CREATE DATABASE donaciones_db;
```

2. Configurar credenciales en `application.properties`:
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/donaciones_db
spring.datasource.username=root
spring.datasource.password=tu_password
```

## 🚀 Ejecución

1. **Compilar el proyecto:**
```bash
mvn clean compile
```

2. **Ejecutar la aplicación:**
```bash
mvn spring-boot:run
```

3. **Acceder a la documentación:**
- Swagger UI: http://localhost:8082/swagger-ui.html
- API Docs: http://localhost:8082/api-docs

## 📊 Endpoints principales

### Reportes Excel
- `GET /api/donaciones/reporte/excel` - Descargar reporte Excel completo

### Gestión de donaciones
- `GET /api/donaciones` - Listar todas las donaciones
- `POST /api/donaciones` - Crear nueva donación
- `GET /api/donaciones/{id}` - Obtener donación por ID
- `PUT /api/donaciones/{id}` - Actualizar donación
- `DELETE /api/donaciones/{id}` - Eliminar donación
- `GET /api/donaciones/categoria/{categoria}` - Filtrar por categoría
- `GET /api/donaciones/buscar/donante?nombre={nombre}` - Buscar por donante
- `GET /api/donaciones/fechas?fechaInicio={inicio}&fechaFin={fin}` - Filtrar por fechas

## 📄 Estructura del Excel generado

El reporte Excel incluye:

1. **Hoja "Resumen General"**: Estadísticas por categoría
2. **Hojas por categoría**: Una hoja para cada tipo de donación con detalles completos
3. **Formato profesional**: Encabezados estilizados, bordes, y columnas auto-ajustadas

## 🧪 Datos de ejemplo

Al iniciar la aplicación, se cargan automáticamente datos de ejemplo para facilitar las pruebas.

## 🔧 Estructura del proyecto

```
src/main/java/com/ong/donationexcel/
├── DonationExcelServiceApplication.java
├── config/
│   └── SwaggerConfig.java
├── controller/
│   └── DonacionController.java
├── dto/
│   └── DonacionDTO.java
├── model/
│   ├── DonacionEntity.java
│   └── CategoriaEnum.java
├── repository/
│   └── DonacionRepository.java
└── service/
    ├── DonacionService.java
    └── ExcelReportService.java
```

## 📝 Ejemplo de uso

### Crear una donación
```json
POST /api/donaciones
{
  "nombreDonante": "Juan Pérez",
  "descripcion": "Ropa de invierno para niños",
  "categoria": "ROPA",
  "cantidad": 10,
  "ubicacion": "Centro de Acopio Norte",
  "observaciones": "Ropa en excelente estado"
}
```

### Descargar reporte Excel
```bash
curl -X GET "http://localhost:8082/api/donaciones/reporte/excel" \
     -H "accept: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" \
     --output "reporte_donaciones.xlsx"
```

## 🏗️ Arquitectura

Este microservicio está diseñado para ser completamente independiente y puede ejecutarse en el puerto 8082 sin conflictos con otros servicios del proyecto.

## 📞 Soporte

Para reportar issues o solicitar nuevas características, contactar al equipo de desarrollo.