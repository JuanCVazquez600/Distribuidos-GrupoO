# **TP-Distribuidos Grupo O**

Este proyecto es un sistema distribuido para la gestión de donaciones solidarias entre organizaciones, utilizando microservicios, gRPC para comunicación RPC, Kafka para mensajería asíncrona, y un frontend en React. 
El sistema permite transferencias de donaciones, ofertas, solicitudes y eventos solidarios de manera desacoplada y escalable.

## Tecnologías Utilizadas
**Backend (grpc_server):** Java 17, Spring Boot 2.5.4, gRPC 1.56.0, Spring Kafka 2.7.13, MariaDB, Lombok.
**Cliente (grpc_client):** Python, gRPC (grpcio), Flask, Protobuf.
**Frontend (grpc_frontend):** React 19.2.0, Axios, React Scripts.
**Mensajería:** Apache Kafka 3.8.1 con Zookeeper.
**Otros:** Maven para build, Protobuf para serialización, Spring Security y Mail.

## Prerrequisitos
Java 17: Para el servidor gRPC.
Python 3.x: Para el cliente.
Node.js y npm: Para el frontend.
MariaDB: Base de datos relacional.
Apache Kafka: Incluido en el directorio kafka/.
Maven: Para el build del servidor (viene con mvnw.cmd).
Asegurate de tener configurada una base de datos MariaDB en localhost:3306 con usuario root y contraseña root, o ajustá el application.properties en grpc_server/src/main/resources/.

# Instalación
1 - Clona o descarga el proyecto en tu directorio local.
2 - Configura la base de datos:
    Crea una base de datos llamada tp-distribuidos en MariaDB.
    Spring Boot creará las tablas automáticamente con spring.jpa.hibernate.ddl-auto=update.

3 - Instala dependencias:
    Para el cliente Python: cd grpc_client && pip install -r requirements.txt
    Para el frontend: cd grpc_frontend && npm install
    Genera protobuf para gRPC (si es necesario):

En grpc_server, Maven lo hace automáticamente con el plugin protobuf-maven-plugin.

# Ejecución
Ejecuta los componentes en el siguiente orden para levantar el sistema completo:

## Inicia Zookeeper (necesario para Kafka):
```
cd kafka
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

## Inicia Kafka:
```
cd kafka
bin\windows\kafka-server-start.bat config\server.properties
```

## Inicia el Servidor gRPC/Spring Boot:
```
cd grpc_server
.\mvnw.cmd spring-boot:run
```
El servidor estará disponible en http://localhost:8080 (para REST) y localhost:9090 (para gRPC).

## Inicia el Cliente REST/Bridge:
```
python grpc_client/rest_bridge.py
```
Puente entre REST y gRPC, corriendo en Flask.

## Inicia el Frontend React:
```
cd grpc_frontend
npm start
```
Accede en http://localhost:3000.

# Uso
Transferencias de Donaciones: Usa el endpoint REST /transfers/send/{recipientOrgId} en el servidor para enviar transferencias. Kafka maneja la mensajería asíncrona entre organizaciones.
Mensajería: Los tópicos en Kafka incluyen transferencia-donaciones-{orgId}, ofertas-donaciones, etc., con consumidores específicos por grupo.
Frontend: Interfaz para interactuar con el sistema (detalles en grpc_frontend/src/).
Cliente: Expone endpoints REST que llaman a gRPC (ver grpc_client/rest_bridge.py).
Ejemplo de transferencia vía POST a http://localhost:8080/transfers/send/org-200:
