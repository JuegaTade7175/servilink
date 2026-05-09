# 🔧 ServiLink — Backend API

**CS 2031 Desarrollo Basado en Plataformas — UTEC 2026-1**

Marketplace de servicios domésticos. Conecta clientes con profesionales verificados usando geolocalización con **OpenStreetMap + Leaflet** (open source, sin API key).

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
Spring Boot levanta Docker Compose automáticamente al arrancar. O manualmente:
```bash
docker-compose up -d
```

### 3. Correr la app
```bash
./mvnw spring-boot:run
```

La app corre en `http://localhost:8081`. Al iniciar por primera vez carga datos de prueba automáticamente.

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

**Usuarios de prueba:**
| Email | Password | Rol |
|---|---|---|
| `carlos@servilink.pe` | `password123` | CLIENT |
| `juan.rios@servilink.pe` | `password123` | PROFESSIONAL |
| `maria.condori@servilink.pe` | `password123` | PROFESSIONAL |
| `luis.paredes@servilink.pe` | `password123` | PROFESSIONAL |

### Usuarios ← NUEVO
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/users/me` | Sí | Ver mi perfil |
| PATCH | `/api/users/profile-picture` | Sí | Actualizar foto de perfil (URL externa) |
| DELETE | `/api/users/profile-picture` | Sí | Eliminar foto de perfil |

**Body para actualizar foto:**
```json
{ "profilePictureUrl": "https://res.cloudinary.com/demo/image/upload/sample.jpg" }
```

> **Servicios gratuitos compatibles:** Cloudinary (25 GB free), Imgur, ImgBB. El cliente sube la imagen al servicio externo y envía solo la URL resultante.

### Profesionales
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/professionals/nearby?lat=X&lon=Y&radius=10` | No | Buscar por cercanía (Haversine) |
| GET | `/api/professionals/search?lat=X&lon=Y&categoryId=1` | No | Buscar con filtros |
| GET | `/api/professionals/{id}` | No | Ver perfil |
| POST | `/api/professionals/profile` | PROFESSIONAL | Crear perfil profesional |
| PUT | `/api/professionals/profile` | PROFESSIONAL | Actualizar perfil |
| GET | `/api/professionals/me` | PROFESSIONAL | Mi perfil |

### Disponibilidad
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/availability/professional/{id}` | No | Ver horarios del profesional |
| GET | `/api/availability/professional/{id}/day?day=MONDAY` | No | Ver horarios por día |
| POST | `/api/availability` | PROFESSIONAL | Crear horario |
| PUT | `/api/availability/{id}` | PROFESSIONAL | Actualizar horario |
| DELETE | `/api/availability/{id}` | PROFESSIONAL | Eliminar horario |

**Body para crear/actualizar:**
```json
{ "dayOfWeek": "MONDAY", "startTime": "08:00", "endTime": "18:00" }
```

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

### Confirmación de Citas
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | `/api/confirmations/booking/{id}/generate` | Sí | Genera código de 6 dígitos (expira 48h) |
| POST | `/api/confirmations/confirm` | PROFESSIONAL | Confirmar con código |
| GET | `/api/confirmations/booking/{id}` | Sí | Ver estado |
| DELETE | `/api/confirmations/booking/{id}` | Sí | Cancelar |

### Mensajes / Chat — REST + WebSocket STOMP
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | `/api/messages/booking/{bookingId}` | Sí | Enviar mensaje (persiste en BD) |
| GET | `/api/messages/booking/{bookingId}` | Sí | Ver conversación completa |
| PATCH | `/api/messages/booking/{bookingId}/read` | Sí | Marcar como leídos |
| GET | `/api/messages/unread/count` | Sí | Contar no leídos |

**Body para enviar mensaje:**
```json
{ "content": "Hola, ¿a qué hora llegas?" }
```

**WebSocket STOMP (tiempo real):**
```
Conectar:        ws://localhost:8081/ws  (header Authorization: Bearer <token>)
Enviar mensaje:  /app/chat/{bookingId}
Suscribirse:     /topic/booking/{bookingId}
Notificaciones:  /user/queue/notifications
Errores:         /user/queue/errors
```

**Payload WebSocket:**
```json
{ "token": "Bearer <jwt>", "content": "Hola!" }
```

### Notificaciones
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/notifications` | Sí | Todas las notificaciones |
| GET | `/api/notifications/unread` | Sí | Solo no leídas |
| GET | `/api/notifications/unread/count` | Sí | Contador no leídas |
| PATCH | `/api/notifications/read-all` | Sí | Marcar todas como leídas |

### Pagos (requiere JWT)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/payments` | Procesar pago (MVP simulado) |
| GET | `/api/payments/booking/{bookingId}` | Ver pago de una reserva |

**Body de pago:**
```json
{ "bookingId": 1, "amount": 100.00, "method": "CARD" }
```
Métodos: `CARD`, `YAPE`, `BANK_TRANSFER`

### Reseñas (requiere JWT)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/reviews` | Crear reseña (solo reservas COMPLETED) |
| GET | `/api/reviews/professional/{id}` | Reseñas de un profesional |

---

## 🏗️ Arquitectura

```
src/main/java/com/example/demosass/
├── config/
│   ├── AppConfig.java
│   ├── DataInitializer.java
│   ├── SecurityConfig.java
│   ├── StartupPortLogger.java
│   └── WebSocketConfig.java          ← STOMP + JWT auth en CONNECT
├── controller/
│   ├── AuthController.java
│   ├── AvailabilityController.java
│   ├── BookingController.java
│   ├── BookingConfirmationController.java
│   ├── MapController.java
│   ├── MessageController.java
│   ├── NotificationController.java
│   ├── PaymentController.java
│   ├── ProfessionalController.java
│   └── UserController.java           ← NUEVO (perfil + foto de perfil)
├── domain/
│   ├── enums/
│   │   ├── BookingStatus.java
│   │   ├── DayOfWeek.java
│   │   ├── NotificationType.java
│   │   ├── PaymentMethod.java
│   │   ├── PaymentStatus.java
│   │   └── Role.java
│   ├── model/                        # 11 entidades JPA
│   │   ├── Availability.java
│   │   ├── Booking.java
│   │   ├── BookingConfirmation.java
│   │   ├── Category.java
│   │   ├── Message.java
│   │   ├── Notification.java
│   │   ├── Payment.java
│   │   ├── Professional.java
│   │   ├── Review.java
│   │   ├── Service.java
│   │   └── User.java
│   └── repository/                   # 11 repositorios
│       ├── AvailabilityRepository.java
│       ├── BookingConfirmationRepository.java
│       ├── BookingRepository.java
│       ├── CategoryRepository.java
│       ├── MessageRepository.java
│       ├── NotificationRepository.java
│       ├── PaymentRepository.java
│       ├── ProfessionalRepository.java
│       ├── ReviewRepository.java
│       ├── ServiceRepository.java
│       └── UserRepository.java
├── dto/
│   ├── request/
│   └── response/
│       └── Responses.java
├── exception/
│   ├── BadRequestException.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── security/
│   ├── JwtFilter.java
│   ├── JwtUtil.java
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── AuthService.java
│   ├── AvailabilityService.java
│   ├── BookingConfirmationService.java
│   ├── BookingService.java
│   ├── CategoryService.java
│   ├── FcmPushService.java           ← push simulado, listo para Firebase real
│   ├── GeoService.java
│   ├── MessageService.java
│   ├── NotificationService.java
│   ├── PaymentService.java
│   ├── ProfessionalService.java
│   └── ReviewService.java
└── websocket/
    └── ChatWebSocketController.java  ← STOMP en tiempo real
```

---

## 🗄️ Entidades (11)

```
User ──────────── Professional (1:1)
  │                   │
  │                   ├──── Service (N:M) ──── Category (N:1)
  │                   ├──── Availability (1:N)
  │                   └──── Booking (1:N)
  │                             │
  ├── Booking (client) ─────────┤
  │                             ├──── Payment (1:1)
  │                             ├──── Review (1:1)
  │                             ├──── BookingConfirmation (1:1)
  │                             └──── Message (1:N)
  │
  └──── Notification (1:N)
```

---

## 🌐 WebSocket — Flujo del chat

```
Cliente  ──CONNECT /ws (JWT en header)──►  WebSocketConfig valida token
         ◄──── sesión autenticada ──────

Cliente  ──SEND /app/chat/{bookingId}──►  ChatWebSocketController
                                               │
                                               ├─► MessageService.send() → PostgreSQL
                                               ├─► /topic/booking/{id}  (broadcast)
                                               └─► /user/{id}/queue/notifications
```

---

## 🧪 Tests (14 tests de integración)

```bash
./mvnw test
```

| Test | Qué verifica |
|---|---|
| `contextLoads` | El contexto Spring Boot levanta |
| `registerAndLoginFlow` | Registro → Login → JWT |
| `getCategoriesPublicEndpoint` | Endpoint público sin auth |
| `nearbySearchPublicEndpoint` | Búsqueda Haversine |
| `mapGeoDistanceEndpoint` | Distancia entre puntos |
| `notificationsRequireAuth` | GET /notifications sin JWT → 403 |
| `getNotificationsAuthenticated` | Notificaciones con JWT válido → 200 |
| `getUnreadNotificationsCount` | Contador de no leídas retorna número |
| `markAllNotificationsAsRead` | Marcar leídas retorna count |
| `messagesRequireAuth` | GET /messages sin JWT → 403 |
| `unreadMessagesCount` | Contador mensajes no leídos |
| `availabilityPublicEndpointReturnsOk` | Disponibilidad pública → 200 |
| `createAvailabilityRequiresProfessional` | CLIENT no puede crear horarios → 403 |
| `professionalCanCreateAvailability` | Flujo completo: perfil → disponibilidad → 201 |

---

## 🗓️ Roadmap

| Funcionalidad | Estado |
|---|---|
| Auth JWT (registro + login) | ✅ Completo |
| 11 entidades JPA + PostgreSQL | ✅ Completo |
| Búsqueda geográfica Haversine | ✅ Completo |
| Integración OpenStreetMap Nominatim | ✅ Completo |
| API REST completa (40+ endpoints) | ✅ Completo |
| Confirmación de citas (código 6 dígitos + @Scheduled) | ✅ Completo |
| Pagos MVP (CARD, YAPE, BANK_TRANSFER simulado) | ✅ Completo |
| Reseñas + auto-rating | ✅ Completo |
| Disponibilidad CRUD | ✅ Completo |
| Firebase FCM push (MVP simulado) | ✅ Completo |
| Mensajería REST + WebSocket STOMP en tiempo real | ✅ Completo |
| Notificaciones internas en BD | ✅ Completo |
| Foto de perfil con URL externa (sin S3) | ✅ Completo |

---

## 🔐 Seguridad

| Aspecto | Detalle |
|---|---|
| Auth HTTP | JWT stateless, BCrypt, @PreAuthorize por rol |
| Auth WebSocket | JWT validado en STOMP CONNECT via ChannelInterceptor |
| CORS | `allowedOriginPatterns("*")` — solo para desarrollo |
| Validación DTOs | @Valid con Jakarta (NotBlank, Email, Future, Min, Max) |
| Manejo de errores | GlobalExceptionHandler → 404/400/401/403/500 con ErrorResponse |
| open-in-view | `false` — sin lazy loading fuera de transacción |

---

*ServiLink • CS 2031 DBP 2026-1 • UTEC*