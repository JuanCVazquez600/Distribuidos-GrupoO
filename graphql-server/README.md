# GraphQL Donations Server

Microservicio GraphQL para reportes de donaciones del sistema de gestión solidaria.

##  Prerequisitos

- **Node.js** >= 16.0.0
- **npm** >= 8.0.0
- **MySQL** >= 8.0
- Base de datos `tp-distribuidos` configurada y funcionando

##  Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone <repo-url>
cd Distribuidos-GrupoO/graphql-server
```

### 2. Instalar dependencias
```bash
npm install
```

### 3. Configurar variables de entorno
```bash
# Windows
copy .env.example .env

# Linux/Mac
cp .env.example .env
```

Editar `.env` con tus credenciales locales:
```env
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=tu_password_mysql
DB_NAME=tp-distribuidos
PORT=4000
```

### 4. Verificar base de datos
Asegúrate de que la base de datos `tp-distribuidos` existe y tiene las tablas necesarias:
- `usuario` (con roles PRESIDENTE/VOCAL)
- `inventario_de_donaciones`

### 5. Ejecutar el servidor
```bash
npm start
```

El servidor estará disponible en:
- **GraphQL API**: http://localhost:4000/graphql
- **GraphQL Playground**: http://localhost:4000/graphql

##  Uso

### Permisos
Solo usuarios con rol `PRESIDENTE` o `VOCAL` pueden acceder a los reportes.

### Queries disponibles

#### Estadísticas generales
```graphql
query {
  donationsStats(userId: 1) {
    total
    activas
    eliminadas
    totalCantidad
  }
}
```

#### Reporte completo con filtros
```graphql
query {
  donationsReport(userId: 1, filters: {
    categoria: ROPA,
    eliminado: false
  }) {
    stats {
      total
      activas
      eliminadas
    }
    donations {
      id
      categoria
      descripcion
      cantidad
      fechaAlta
      eliminado
    }
  }
}
```

### Headers necesarios
En GraphQL Playground o cliente, incluir:
```json
{
  "userid": "1"
}
```

##  Estructura del proyecto

```
graphql-server/
├── server.js          # Servidor principal con schema y resolvers
├── package.json       # Dependencias y scripts
├── .env.example       # Plantilla de variables de entorno
└── README.md         # Este archivo
```

##  Integración con Frontend

Este servidor se conecta automáticamente con el frontend React a través de Apollo Client. Asegúrate de que ambos estén ejecutándose:

- Frontend React: http://localhost:3000
- GraphQL Server: http://localhost:4000
- Backend SpringBoot: http://localhost:8080

##  Troubleshooting

### Error de conexión a base de datos
```
Error: ER_ACCESS_DENIED_ERROR
```
- Verificar credenciales en `.env`
- Confirmar que MySQL esté ejecutándose
- Verificar permisos del usuario de base de datos

### Puerto en uso
```
Error: listen EADDRINUSE :::4000
```
- Cambiar `PORT` en `.env` o detener proceso que usa puerto 4000

### Sin permisos para reportes
```
Error: Sin permisos para acceder a estadísticas
```
- Verificar que el usuario tenga rol `PRESIDENTE` o `VOCAL`
- Confirmar que el `userid` en headers sea correcto

##  Dependencias principales

- `apollo-server-express`: Servidor GraphQL
- `express`: Framework web
- `mysql2`: Driver MySQL
- `cors`: Manejo de CORS
- `graphql`: Librería GraphQL core