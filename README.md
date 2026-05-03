🔧 SERVILINK
Propuesta de Proyecto — CS 2031 Desarrollo Basado en Plataformas
Semana 3 • UTEC 2026-1


Integrante
Código
Tadeo Joaquín Cárdenas Soto.
202510004
José Enrique Hilario Ruiz Lam.
202510050
Sebastian Falvy Mendoza.
202510469
Joel Rodrigo Eulogio Coquil.
202510112
Miguel Adrian Espinoza Arnero.
202320031


1. El Problema y la Solución
Problema:  Las personas que necesitan servicios domésticos (plomería, electricidad, limpieza, jardinería) enfrentan una experiencia fragmentada y poco confiable al buscar profesionales: buscan en grupos de WhatsApp, piden referencias a conocidos o contratan sin verificación alguna. No existe una forma ágil, visual y segura de conectar con proveedores disponibles cerca de ti.

Solución:  ServiLink — una app móvil y web estilo "swipe" que conecta a usuarios con profesionales de servicios domésticos verificados. El cliente desliza perfiles de proveedores cercanos (como Tinder, pero para contratar un gasfitero), ve su reputación en tiempo real, reserva y paga desde la misma plataforma.


2. Funcionalidades Principales
Todas las funcionalidades que el usuario podrá realizar:
Registrarse como Cliente o Profesional (con verificación de identidad para proveedores)
Explorar profesionales cercanos con vista de "cards" deslizables filtradas por categoría, distancia y disponibilidad
Ver perfil completo del profesional: fotos, info, calificación, especialidades, precio estimado y disponibilidad en calendario
Solicitar y reservar un servicio con hora y lugar acordados
Pagar de forma segura con tarjeta, Yape o transferencia bancaria
Chat integrado entre cliente y profesional tras la reserva
Calificar y dejar reseña al finalizar el servicio

MVP (funcionalidades imprescindibles):
Registro y autenticación de usuarios (cliente y profesional)
Búsqueda y exploración de profesionales por geolocalización y categoría
Reserva de servicio con confirmación
Sistema básico de pagos integrado


3. Estructura de Datos
Entidades principales del sistema (8 entidades):
Entidad
Descripción breve
Usuario
Datos base: nombre, email, foto, rol (cliente/profesional)
Profesional
Extiende Usuario: especialidad, zona de cobertura, tarifa base, certificados
Servicio
Tipo de trabajo ofrecido: categoría, descripción, precio referencial
Reserva
Solicitud de un cliente a un profesional para una fecha/hora específica
Pago
Transacción asociada a una Reserva: monto, método, estado
Reseña
Calificación (1-5) y comentario dejado por el cliente tras el servicio
Categoría
Clasificación del tipo de servicio: plomería, electricidad, limpieza, etc.
Disponibilidad
Horarios disponibles del profesional por día de semana


Relaciones clave:
Un Usuario puede hacer muchas Reservas (1:N)
Un Profesional puede ofrecer muchos Servicios y un Servicio puede ser ofrecido por muchos Profesionales (N:M)
Una Reserva pertenece a un Cliente y a un Profesional, con un Servicio y un Pago asociado (N:M implícita)
Un Profesional tiene muchos registros de Disponibilidad (1:N)
Una Reserva puede tener una sola Reseña (1:1)


4. Aspectos Técnicos
Servicios externos a integrar:
Google Maps / Geolocation API — mostrar profesionales cercanos en mapa y calcular distancias
Stripe / MercadoPago — procesamiento seguro de pagos con tarjeta y billeteras digitales
Firebase Cloud Messaging (FCM) — notificaciones push (confirmación de reserva, mensajes nuevos)
Twilio / WhatsApp Business API — confirmaciones de cita por SMS o WhatsApp
AWS S3 / Cloudinary — almacenamiento de fotos de perfil y portafolio de profesionales

Operaciones asincrónicas (que requieren manejo especial):
Procesamiento de pagos: depende de APIs externas con latencia variable
Cálculo de rutas y distancias: consultas a Google Maps en tiempo real
Envío de notificaciones push y SMS masivos
Generación de reportes de historial de servicios y facturación
Verificación de identidad de profesionales (validación de documentos en background)


5. Mockups / Wireframes
Los wireframes se han diseñado para reflejar una experiencia tipo "card swipe" (similar a Tinder) pero orientada a contratar servicios domésticos. A continuación, se describe el flujo y las pantallas clave:

Pantallas principales:
Home / Explorar: Grid de tarjetas de profesionales con foto, nombre, categoría, calificación y distancia. Filtros por categoría y disponibilidad en la parte superior.
Perfil del Profesional: Foto grande, galería de trabajos, calificación detallada, servicios que ofrece, precio                     estimado, disponibilidad semanal y botón "Reservar ahora".
Flujo de Reserva: Selector de fecha/hora → confirmación de servicio → resumen → pasarela de pago → confirmación con código QR.
Dashboard del Cliente: Mis reservas activas e historial, acceso a chat con el profesional y opción de calificar servicios completados.




6. Reflexión Final
Parte más desafiante:
La integración en tiempo real de geolocalización con filtrado dinámico de profesionales disponibles según distancia y horario será el mayor reto técnico. Además, implementar un sistema de pagos seguro con manejo de errores, reembolsos y estados de transacción asincrónica implica una lógica de backend robusta que va más allá de un CRUD básico.

Experiencia previa del equipo:
Tenemos experiencia construyendo APIs REST con Spring Boot y bases de datos relacionales. Hemos trabajado con React para frontend básico, pero nunca hemos integrado una pasarela de pagos real ni optimizado consultas geográficas. Tampoco hemos implementado WebSockets para chat en tiempo real, lo cual representa un reto nuevo que queremos abordar.


ServiLink • CS 2031 DBP 2026-1 • Tadeo Cárdenas :)Otras opciones open source que pueden explorar para lo que buscan pueden ser:
leaflet : 
open street map: idea suena muy interesante y está bien planteada. Sería bueno que tengan una distribución de prioridades de las funcionalidades que piensan implementar, por ejemplo la confirmación de citas podrían primero tratar de implementarlo dentro de la app y luego usar la API externa si es que les alcanza el tiempo, lo cual sería uno de los últimos puntos a cubrir.
Los lectores de este archivo pueden ver los comentarios y las sugerencias
de los profesores y compañeros en el siguiente enlace: Leaflet
an open-source JavaScript library
for mobile-friendly interactive maps

Overview Tutorials Docs Download Plugins Blog
August 16, 2025 — Leaflet 2.0.0-alpha.1 has been released!
Leaflet is the leading open-source JavaScript library for mobile-friendly interactive maps. Weighing just about 42 KB of JS, it has all the mapping features most developers ever need.

Leaflet is designed with simplicity, performance and usability in mind. It works efficiently across all major desktop and mobile platforms, can be extended with lots of plugins, has a beautiful, easy to¡Bienvenido a OpenStreetMap!
OpenStreetMap es un mapa del mundo, creado por gente como tú y de uso libre bajo una licencia abierta.

El alojamiento cuenta con el respaldo de Fastly, Miembros corporativos de OSMF y otros socios.

Al utilizar este sitio web u otra infraestructura proporcionada por la Fundación OpenStreetMap, aceptas los Términos de uso.
