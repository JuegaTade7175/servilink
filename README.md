# 🔧 ServiLink — Backend API

**CS 2031 Desarrollo Basado en Plataformas — UTEC 2026-1**

Marketplace de servicios domésticos estilo "swipe". Conecta clientes con profesionales verificados usando geolocalización con **OpenStreetMap + Leaflet** (open source, sin API key).

---

## 👥 Integrantes

| Nombre | Código |
|---|---|
| Tadeo Joaquín Cárdenas Soto | 202510004 |
| José Enrique Hilario Ruiz Lam | 202510050 |
| Sebastian Falvy Mendoza | 202510469 |
| Joel Rodrigo Eulogio Coquil | 202510112 |
| Miguel Adrian Espinoza Arnero | 202320031 |

---

## 🚀 Cómo correr el proyecto

### Pre-requisitos
- Java 21
- Docker Desktop corriendo

### 1. Variables de entorno
El archivo `.env` ya está configurado:
```
DB_HOST=localhost
DB_PORT=5438
DB_NAME=servilink-db
DB_USER=postgres
DB_PASSWORD=postgres
SERVER_PORT=8081
```

### 2. Levantar la base de datos
Spring Boot levanta Docker Compose automáticamente al arrancar.
O manualmente:
```bash
docker-compose up -d
```

### 3. Correr la app
```bash
./mvnw spring-boot:run
```

La app corre en `http://localhost:8081`. Al iniciar por primera vez carga datos de prueba automáticamente (3 profesionales en Lima con coordenadas reales).

### 4. Correr tests
```bash
./mvnw test
```

---

## 📡 Endpoints REST

### Auth (público)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/register` | Registro (CLIENT o PROFESSIONAL) |
| POST | `/api/auth/login` | Login → retorna JWT |

**Usuarios de prueba cargados automáticamente:**
| Email | Password | Rol |
|---|---|---|
| `carlos@servilink.pe` | `password123` | CLIENT |
| `juan.rios@servilink.pe` | `password123` | PROFESSIONAL |
| `maria.condori@servilink.pe` | `password123` | PROFESSIONAL |
| `luis.paredes@servilink.pe` | `password123` | PROFESSIONAL |

### Profesionales
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/professionals/nearby?lat=X&lon=Y&radius=10` | No | Buscar por cercanía (Haversine) |
| GET | `/api/professionals/search?lat=X&lon=Y&categoryId=1` | No | Buscar con filtros |
| GET | `/api/professionals/{id}` | No | Ver perfil |
| POST | `/api/professionals/profile` | PROFESSIONAL | Crear perfil profesional |
| PUT | `/api/professionals/profile` | PROFESSIONAL | Actualizar perfil |
| GET | `/api/professionals/me` | PROFESSIONAL | Mi perfil |

### Mapa — Leaflet / OpenStreetMap (público)
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/map/professionals?lat=X&lon=Y&radius=10` | Pines del mapa para Leaflet |
| GET | `/api/map/geocode?address=Miraflores` | Dirección → coordenadas (Nominatim) |
| GET | `/api/map/reverse-geocode?lat=X&lon=Y` | Coordenadas → dirección |
| GET | `/api/map/distance?lat1=X&lon1=Y&lat2=X&lon2=Y` | Distancia Haversine en km |

### Categorías (público)
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/categories` | Listar todas |
| GET | `/api/categories/{id}` | Ver una |
| GET | `/api/categories/{id}/services` | Servicios de la categoría |
| POST | `/api/categories` | Crear categoría |
| POST | `/api/categories/{id}/services` | Agregar servicio |

### Reservas (requiere JWT)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/bookings` | Crear reserva |
| GET | `/api/bookings/my` | Mis reservas (como cliente) |
| GET | `/api/bookings/professional?professionalId=X` | Reservas del profesional |
| GET | `/api/bookings/{id}` | Ver reserva |
| PATCH | `/api/bookings/{id}/status` | Cambiar estado |

### ✅ Confirmación de Citas (nueva — implementación interna)
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | `/api/confirmations/booking/{id}/generate` | Sí | Genera código de 6 dígitos |
| POST | `/api/confirmations/confirm` | PROFESSIONAL | Profesional confirma con el código |
| GET | `/api/confirmations/booking/{id}` | Sí | Ver estado de confirmación |
| DELETE | `/api/confirmations/booking/{id}` | Sí | Cancelar confirmación |

**Flujo de confirmación:**
```
1. Cliente crea reserva          → POST /api/bookings          (status: PENDING)
2. Sistema genera código         → POST /api/confirmations/booking/{id}/generate
3. Código aparece en dashboard   → GET  /api/confirmations/booking/{id}
4. Profesional confirma          → POST /api/confirmations/confirm  {"code":"123456"}
5. Reserva queda confirmada      → status: CONFIRMED ✓
```

> **Nota:** La confirmación por WhatsApp/Twilio está planificada para cuando alcance el tiempo (último sprint). Por ahora el código vive dentro de la app, que es lo más robusto para el MVP.

### Pagos (requiere JWT)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/payments` | Procesar pago (MVP simulado) |
| GET | `/api/payments/booking/{bookingId}` | Ver pago de una reserva |

**Body de pago:**
```json
{
  "bookingId": 1,
  "amount": 100.00,
  "method": "CARD"
}
```
Métodos disponibles: `CARD`, `YAPE`, `BANK_TRANSFER`

> **TODO:** Integrar MercadoPago SDK cuando corresponda. El `PaymentService` tiene el punto marcado.

### Reseñas (requiere JWT)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/reviews` | Crear reseña (solo reservas COMPLETED) |
| GET | `/api/reviews/professional/{id}` | Reseñas de un profesional |

Al crear una reseña, el `averageRating` del profesional se recalcula automáticamente.

---

## 🏗️ Arquitectura

```
src/main/java/com/example/demosass/
├── config/
│   ├── AppConfig.java           # RestTemplate + ModelMapper beans
│   ├── DataInitializer.java     # Seed de datos de prueba (Lima)
│   ├── SecurityConfig.java      # JWT + CORS + rutas públicas/privadas
│   └── StartupPortLogger.java   # Log del puerto al arrancar
├── controller/
│   ├── AuthController.java
│   ├── BookingController.java
│   ├── BookingConfirmationController.java  ← nuevo
│   ├── MapController.java
│   ├── PaymentController.java
│   ├── ProfessionalController.java
│   └── PaymentController.java   # también contiene Review y Category
├── domain/
│   ├── enums/                   # Role, BookingStatus, PaymentStatus, PaymentMethod, DayOfWeek
│   ├── model/                   # 9 entidades JPA
│   │   └── BookingConfirmation.java  ← nueva
│   └── repository/              # 9 repositorios Spring Data JPA
│       └── BookingConfirmationRepository.java  ← nuevo
├── dto/
│   ├── request/                 # Request DTOs con validación Jakarta
│   └── response/                # Response DTOs (records Java)
├── exception/                   # GlobalExceptionHandler, custom exceptions
├── security/                    # JwtUtil, JwtFilter, UserDetailsServiceImpl
└── service/
    ├── AuthService.java
    ├── BookingService.java
    ├── BookingConfirmationService.java  ← nuevo (con @Scheduled)
    ├── CategoryService.java
    ├── GeoService.java          # OpenStreetMap Nominatim + Haversine
    ├── PaymentService.java
    ├── ProfessionalService.java
    └── ReviewService.java
```

---

## 🗺️ Integración OpenStreetMap + Leaflet

Sin API key, sin costo — recomendado por el equipo docente como alternativa open source a Google Maps.

**Backend (GeoService):**
- `geocodeAddress(address)` → Nominatim convierte dirección a lat/lon
- `reverseGeocode(lat, lon)` → lat/lon a dirección legible
- `calculateDistance(...)` → fórmula Haversine
- Query JPQL con Haversine en `ProfessionalRepository` para búsqueda por radio en PostgreSQL

**Frontend (Leaflet):**
- `GET /api/map/professionals?lat=X&lon=Y&radius=10` → retorna `GeoPointResponse[]` listo para pines
- Tiles gratuitos: `https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png`

**Alternativas evaluadas (según sugerencia del equipo docente):**
- Leaflet.js — elegida ✅ (ligera, 42KB, mobile-friendly)
- OpenStreetMap Nominatim — elegida ✅ (sin API key)
- Google Maps — descartada (costo)

---

## 🗄️ Entidades (9)

```
User ──────────── Professional (1:1)
                      │
                      ├──── Service (N:M) ──── Category (N:1)
                      ├──── Availability (1:N)
                      └──── Booking (1:N)
                                │
User (client) ────────────────┘
                                │
                                ├──── Payment (1:1)
                                ├──── Review (1:1)
                                └──── BookingConfirmation (1:1) ← nueva
```

| Entidad | Descripción |
|---|---|
| `User` | Base: nombre, email, rol (CLIENT/PROFESSIONAL/ADMIN) |
| `Professional` | Extiende User: especialidad, lat/lon, tarifa, rating |
| `Service` | Tipo de trabajo: categoría, precio referencial |
| `Category` | Plomería, Electricidad, Limpieza, Jardinería |
| `Booking` | Reserva: cliente + profesional + servicio + fecha |
| `Payment` | Transacción: monto, método, estado, transactionId |
| `Review` | Calificación 1-5 + comentario, actualiza rating automáticamente |
| `Availability` | Horarios del profesional por día de semana |
| `BookingConfirmation` | Código 6 dígitos + estado + expiración (48h) ← nueva |

---

## ⚙️ Configuración (.env)

```properties
DB_HOST=localhost
DB_PORT=5438
DB_NAME=servilink-db
DB_USER=postgres
DB_PASSWORD=postgres
SERVER_PORT=8081
```

La app lee el `.env` automáticamente vía `dotenv-java` en el arranque.

---

## 🧪 Tests

```bash
./mvnw test
```

Incluye tests de integración con **Testcontainers** (PostgreSQL real en Docker):

| Test | Qué verifica |
|---|---|
| `contextLoads` | El contexto de Spring Boot levanta sin errores |
| `registerAndLoginFlow` | Registro → Login → JWT válido |
| `getCategoriesPublicEndpoint` | Endpoint público sin auth |
| `nearbySearchPublicEndpoint` | Búsqueda geográfica con Haversine |
| `mapGeoDistanceEndpoint` | Cálculo de distancia entre dos puntos |

---

## 🗓️ Roadmap

| Funcionalidad | Estado |
|---|---|
| Auth JWT (registro + login) | ✅ Completo |
| 9 entidades JPA + PostgreSQL | ✅ Completo |
| Búsqueda geográfica Haversine | ✅ Completo |
| Integración OpenStreetMap Nominatim | ✅ Completo |
| API REST completa (CRUD) | ✅ Completo |
| Confirmación de citas interna | ✅ Completo |
| Pagos MVP (simulado) | ✅ Completo |
| Reseñas + auto-rating | ✅ Completo |
| Disponibilidad del profesional | ✅ Completo |
| Integración MercadoPago real | 🔜 Próximo sprint |
| Notificaciones WhatsApp / Twilio | 🔜 Último sprint (si alcanza) |
| Frontend React + Leaflet | 🔜 En paralelo |

---

*ServiLink • CS 2031 DBP 2026-1 • UTEC*
