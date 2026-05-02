# Week04-Lab01 - Spring Boot REST API

Proyecto Spring Boot con PostgreSQL, utilizando Docker Compose para la gestión de la base de datos.

## 🚀 Inicio Rápido

### Requisitos Previos
- Java 25+
- Maven 3.8+
- Docker y Docker Compose
- Git

### 1. Clonar el Repositorio
```bash
git clone <repository-url>
cd demosass
```

### 2. Configurar Variables de Entorno
```bash
cp .env.example .env
```

Edita `.env` con tus valores si es necesario (por defecto están configurados correctamente).

### 3. Iniciar la Base de Datos
```bash
docker-compose up -d
```

Verifica que esté corriendo:
```bash
docker-compose ps
```

### 4. Compilar e Iniciar la Aplicación
```bash
./mvnw clean install
./mvnw spring-boot:run
```

O en Windows:
```bash
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

## 📁 Estructura del Proyecto

```
demosass/
├── src/main/java/com/example/demosass/
│   ├── DemosassApplication.java              # Main class
│   ├── config/
│   │   └── MapperConfig.java                 # Configuración de mappers
│   ├── controller/
│   │   └── ProductController.java            # REST endpoints
│   ├── dto/
│   │   └── ProductDTO.java                   # Data Transfer Objects
│   ├── exception/
│   │   ├── ErrorResponse.java                # Respuesta de errores
│   │   ├── GlobalExceptionHandler.java       # Manejo global de excepciones
│   │   └── ResourceNotFoundException.java    # Excepción custom
│   ├── model/
│   │   └── Product.java                      # Entity de JPA
│   ├── repository/
│   │   └── ProductRepository.java            # Data access layer
│   └── service/
│       └── ProductService.java               # Lógica de negocio
├── src/main/resources/
│   └── application.properties                # Configuración de Spring Boot
├── src/test/java/
│   └── com/example/demosass/
│       ├── DemosassApplicationTests.java
│       ├── TestcontainersConfiguration.java
│       └── TestDemosassApplication.java
├── docker-compose.yml                        # Configuración de Docker
├── .env                                      # Variables de entorno (local)
├── .env.example                              # Plantilla de variables
├── pom.xml                                   # Dependencias Maven
└── README.md                                 # Este archivo
```

## 🔧 Configuración

### Variables de Entorno (.env)
```properties
DB_HOST=localhost        # Host de PostgreSQL
DB_PORT=5432            # Puerto de PostgreSQL
DB_NAME=week04-db       # Nombre de la base de datos
DB_USER=postgres        # Usuario de BD
DB_PASSWORD=postgres    # Contraseña de BD
```

### Propiedades de Spring Boot (application.properties)
```properties
spring.application.name=week04-lab01
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## 🧪 Testing

### Ejecutar Todos los Tests
```bash
./mvnw test
```

### Ejecutar Test Específico
```bash
./mvnw test -Dtest=NombreDeLaClaseTest
```

### Con Cobertura
```bash
./mvnw test jacoco:report
```

## 🐳 Docker Compose

### Iniciar
```bash
docker-compose up -d
```

### Ver Logs
```bash
docker-compose logs -f postgres
```

### Detener
```bash
docker-compose down
```

### Eliminar Datos
```bash
docker-compose down -v
```

### Acceder a PostgreSQL
```bash
docker-compose exec postgres psql -U postgres -d week04-db
```

## 📚 API Endpoints (Ejemplos)

Asume que existen endpoints de Product.

### GET todos los productos
```bash
curl http://localhost:8080/api/products
```

### GET producto por ID
```bash
curl http://localhost:8080/api/products/{id}
```

### POST crear producto
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Producto", "price": 29.99}'
```

### PUT actualizar producto
```bash
curl -X PUT http://localhost:8080/api/products/{id} \
  -H "Content-Type: application/json" \
  -d '{"name": "Producto Actualizado", "price": 39.99}'
```

### DELETE producto
```bash
curl -X DELETE http://localhost:8080/api/products/{id}
```

## 🛠️ Herramientas y Tecnologías

- **Spring Boot 4.0.5**: Framework web
- **Spring Data JPA**: ORM y persistencia
- **PostgreSQL 16**: Base de datos
- **Docker**: Containerización
- **Maven**: Gestión de dependencias
- **Lombok**: Reducción de boilerplate
- **Testcontainers**: Testing con contenedores

## 📋 Dependencias Principales

```xml
<!-- Spring Boot -->
<groupId>org.springframework.boot</groupId>

<!-- Data JPA -->
<artifactId>spring-boot-starter-data-jpa</artifactId>

<!-- Web MVC -->
<artifactId>spring-boot-starter-webmvc</artifactId>

<!-- Validation -->
<artifactId>spring-boot-starter-validation</artifactId>

<!-- PostgreSQL -->
<groupId>org.postgresql</groupId>
<artifactId>postgresql</artifactId>

<!-- Lombok -->
<groupId>org.projectlombok</groupId>
<artifactId>lombok</artifactId>

<!-- Testcontainers -->
<groupId>org.testcontainers</groupId>
```

## 🐛 Troubleshooting

### Error: "Connection refused"
```bash
# Verifica que Docker está corriendo
docker-compose ps

# Revisa los logs
docker-compose logs postgres

# Reinicia
docker-compose restart
```

### Error: "Port 5432 in use"
Cambia el puerto en `.env`:
```properties
DB_PORT=5433
```

### Error: Base de datos no existe
Limpia y reinicia:
```bash
docker-compose down -v
docker-compose up -d
```

### Limpiar caché Maven
```bash
./mvnw clean
```

## 📖 Documentación Adicional

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [CONFIGURATION.md](./CONFIGURATION.md) - Configuración detallada

## 👨‍💻 Contribuir

1. Crea una rama: `git checkout -b feature/mi-feature`
2. Comitea tus cambios: `git commit -am 'Add my feature'`
3. Push a la rama: `git push origin feature/mi-feature`
4. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la licencia MIT.

## 📞 Soporte

Para reportar bugs o sugerencias, abre un issue en el repositorio.

---

**Creado**: Abril 2026  
**Versión**: 1.0.0-SNAPSHOT

