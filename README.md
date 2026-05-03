# 🔧 ServiLink — Backend API

**CS 2031 Desarrollo Basado en Plataformas — UTEC 2026-1**

Marketplace de servicios domésticos estilo "swipe". Conecta clientes con profesionales verificados (electricistas, plomeros, limpieza, jardinería) usando geolocalización con **OpenStreetMap + Leaflet** (open source, sin API key).

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

### 1. Levantar la base de datos
```bash
docker-compose up -d
```

### 2. Correr la app
```bash
./mvnw spring-boot:run
```

La app corre en `http://localhost:8081` y carga datos de prueba automáticamente.

### 3. Correr tests
```bash
./mvnw test
```

---

## 📡 Endpoints REST

### Auth (público)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/register` | Registrar usuario (CLIENT o PROFESSIONAL) |
| POST | `/api/auth/login` | Login → retorna JWT |

### Profesionales
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/professionals/nearby?lat=X&lon=Y&radius=10` | No | Buscar por cercanía (Haversine) |
| GET | `/api/professionals/search?lat=X&lon=Y&categoryId=1` | No | Buscar con filtros |
| GET | `/api/professionals/{id}` | No | Ver perfil |
| POST | `/api/professionals/profile` | PROFESSIONAL | Crear perfil |
| PUT | `/api/professionals/profile` | PROFESSIONAL | Actualizar perfil |
| GET | `/api/professionals/me` | PROFESSIONAL | Mi perfil |

### Mapa (Leaflet / OpenStreetMap) — todos públicos
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/map/professionals?lat=X&lon=Y&radius=10` | Pines del mapa |
| GET | `/api/map/geocode?address=Miraflores` | Dirección → coordenadas |
| GET | `/api/map/reverse-geocode?lat=X&lon=Y` | Coordenadas → dirección |
| GET | `/api/map/distance?lat1=X&lon1=Y&lat2=X&lon2=Y` | Distancia Haversine |

### Categorías (público)
| Método | Endpoint |
|---|---|
| GET | `/api/categories` |
| GET | `/api/categories/{id}/services` |
| POST | `/api/categories` |

### Reservas (requiere JWT)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/bookings` | Crear reserva |
| GET | `/api/bookings/my` | Mis reservas (cliente) |
| GET | `/api/bookings/{id}` | Ver reserva |
| PATCH | `/api/bookings/{id}/status` | Cambiar estado |

### Pagos y Reseñas
| Método | Endpoint |
|---|---|
| POST | `/api/payments` | Procesar pago |
| GET | `/api/payments/booking/{bookingId}` | Ver pago |
| POST | `/api/reviews` | Crear reseña (auto-actualiza rating) |
| GET | `/api/reviews/professional/{id}` | Reseñas de un profesional |

---

## 🏗️ Arquitectura

```
src/main/java/com/example/demosass/
├── config/          # SecurityConfig, AppConfig, DataInitializer
├── controller/      # REST controllers (Auth, Professional, Map, Booking, Payment, Review, Category)
├── domain/
│   ├── enums/       # Role, BookingStatus, PaymentStatus, PaymentMethod, DayOfWeek
│   ├── model/       # 8 entidades JPA
│   └── repository/  # Spring Data JPA (con queries Haversine)
├── dto/
│   ├── request/     # Request DTOs con validación
│   └── response/    # Response DTOs
├── exception/       # GlobalExceptionHandler, excepciones custom
├── security/        # JwtUtil, JwtFilter, UserDetailsServiceImpl
└── service/         # Lógica de negocio (GeoService, AuthService, etc.)
```

## 🗺️ Integración OpenStreetMap

Se usa **Nominatim** (API gratuita de OSM) — sin API key, sin costo:
- `GeoService.geocodeAddress()` → dirección a coordenadas
- `GeoService.reverseGeocode()` → coordenadas a dirección  
- `GeoService.calculateDistance()` → fórmula Haversine
- Query Haversine en `ProfessionalRepository` para búsqueda por radio en DB

El frontend conecta con **Leaflet.js** usando los endpoints `/api/map/*`.

## 🗄️ Entidades (8)

`User` → `Professional` (1:1) → `Service` (N:M) → `Category`

`User` → `Booking` (1:N) ← `Professional`

`Booking` → `Payment` (1:1), `Booking` → `Review` (1:1)

`Professional` → `Availability` (1:N)
