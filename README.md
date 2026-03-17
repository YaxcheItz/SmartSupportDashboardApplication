# 🚀 Smart Support Dashboard - AI Powered 🛡️

> **Sistema inteligente de gestión de tickets de soporte técnico potenciado por Inteligencia Artificial (Google Gemini) para el análisis automático y priorización de incidentes en tiempo real.**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4+-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-21+-DD0031?style=for-the-badge&logo=angular&logoColor=white)](https://angular.io/)
[![Gemini AI](https://img.shields.io/badge/Gemini%20AI-Google-blue?style=for-the-badge&logo=google-gemini&logoColor=white)](https://ai.google.dev/)
[![Supabase](https://img.shields.io/badge/Supabase-Database-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white)](https://supabase.com/)

---

## ✨ Características Principales

### 🧠 Inteligencia Artificial con Gemini
- **Análisis Automático:** Cada ticket nuevo es analizado por la IA para determinar su **Categoría**, **Prioridad** y **Tono emocional** del cliente.
- **Resúmenes Inteligentes:** Generación de resúmenes concisos (máx. 15 palabras) para una lectura rápida del agente.
- **Sugerencias de Respuesta:** La IA propone respuestas profesionales y empáticas basadas en el contexto del problema.
- **Prevención de Tickets:** Sugerencias de soluciones rápidas antes de que el usuario envíe el ticket para reducir la carga de soporte.

### ⚡ Tiempo Real & Dashboard
- **WebSockets (Stomp/SockJS):** Actualizaciones instantáneas en el dashboard cuando entra un nuevo ticket o cambia su estado.
- **Gráficas Dinámicas:** Visualización de estadísticas por categorías y estados utilizando **Chart.js**.
- **Panel de Agente/Admin:** Gestión completa de tickets, asignación de responsables y resolución de casos.
- **Portal del Cliente:** Interfaz simplificada para que los usuarios sigan el estado de sus solicitudes.

### 🔒 Seguridad & Almacenamiento
- **JWT Authentication:** Seguridad robusta para proteger los datos de los usuarios y agentes.
- **Supabase Storage:** Gestión de archivos adjuntos (imágenes/documentos) integrada con Supabase.
- **H2/PostgreSQL:** Base de datos relacional para persistencia de datos.

---

## 💻 Tech Stack

### Backend
- **Java 21** & **Spring Boot 4.0**
- **Spring Security** (JWT)
- **Spring Data JPA** (Hibernate)
- **Spring WebSocket** (Stomp/SockJS)
- **Google Gemini API** (Vía RestTemplate)

### Frontend
- **Angular 21** (Standalone Components)
- **Reactive Forms** & **Signals** (State Management)
- **Chart.js** & **ng2-charts**
- **Vanilla CSS** (Variables & Modern Layouts)

---

## 🚀 Acceso Rápido para Reclutadores

He añadido botones de **Acceso Rápido** en la pantalla de login para facilitar la revisión del proyecto sin necesidad de registrarse manualmente.

| Rol | Usuario | Contraseña |
| :--- | :--- | :--- |
| **Administrador** | `admin` | `admin123` |
| **Agente** | `agente` | `agente123` |
| **Cliente** | `cliente` | `cliente123` |

> 💡 **Nota:** Al usar los botones en el frontend, el sistema te logueará automáticamente con estas credenciales.

---

## 🛠️ Instalación y Configuración

### Prerrequisitos
- JDK 21+
- Node.js 18+ & npm
- Cuenta de Google AI (API Key para Gemini)
- Proyecto en Supabase (API Key y URL)

### Backend Setup
1. Clonar el repositorio.
2. Crear un archivo `.env` en la raíz (usar `.env.example` como base).
3. Configurar tus credenciales:
   ```env
   GEMINI_API_KEY=tu_api_key_aquí
   SUPABASE_URL=tu_url_aquí
   SUPABASE_KEY=tu_key_aquí
   ```
4. Ejecutar con Maven: `./mvnw spring-boot:run`

### Frontend Setup
1. Ir a la carpeta: `cd smart-support-dashboard-ui`
2. Instalar dependencias: `npm install`
3. Iniciar servidor de desarrollo: `npm start`
4. Abrir `http://localhost:4200` en el navegador.

---

## 📂 Estructura del Proyecto

```bash
├── smart-support-dashboard-ui/  # Proyecto Angular (Frontend)
│   ├── src/app/components/      # Componentes (Login, Dashboard, Stats)
│   ├── src/app/services/        # Lógica de negocio y APIs
│   └── ...
├── src/main/java/com/yaxcherg/  # Proyecto Spring Boot (Backend)
│   ├── controller/              # Endpoints REST
│   ├── service/                 # Lógica de IA y Tickets
│   ├── model/                   # Entidades JPA
│   └── security/                # Configuración de JWT y Seguridad
└── pom.xml                      # Dependencias Maven
```

---

Desarrollado con ❤️ por [YaxcheItz](https://github.com/YaxcheItz)
