# TransAndina Flotilla

App Android nativa (Kotlin + Jetpack Compose) para gestión de mantenimiento
de la flotilla de vehículos de TransAndina. Backend en Supabase (Postgres +
Auth + Row Level Security).

Este documento es la guía para que cualquiera del equipo pueda clonar,
configurar y seguir desarrollando el proyecto sin tener que preguntar todo
desde cero.

---

## 1. Stack técnico

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Arquitectura**: MVVM simple — `UI (Compose) → ViewModel → Repository → Supabase`
- **Backend**: [Supabase](https://supabase.com) — Postgres, Auth, Row Level Security
- **SDK de Supabase**: `supabase-kt` (BOM `3.0.3`) — usa el paquete `auth-kt`,
  **no** `gotrue-kt` (ese nombre quedó obsoleto en versiones recientes del SDK)
- **Navegación**: Jetpack Navigation Compose, un solo `NavHost` con menú
  inferior que cambia según el rol del usuario

---

## 2. Estado actual — qué existe y qué no

### ✅ Construido y funcionando

| Módulo | Descripción |
|---|---|
| Login | Autentica con Supabase Auth y carga el rol del usuario |
| Menú inferior por rol | Conductor, mecánico y encargado ven pestañas distintas |
| Home (conductor) | Tarjeta con su vehículo asignado + semáforo de documentos |
| Vehículo (conductor) | Detalle del vehículo y estado de cada documento legal |
| Kilometraje (conductor) | Registrar kilometraje nuevo (validado en la base de datos) |
| Perfil | Ver y editar datos propios (nombre, cédula, teléfono, licencia). Rol y correo no editables. Cerrar sesión con confirmación |

### 🚧 Placeholders — la pantalla existe y navega, pero no tiene funcionalidad real

- `ui/mantenimiento/MantenimientoScreen.kt` — conductor y mecánico
- `ui/notificaciones/NotificacionesScreen.kt` — todos los roles
- `ui/encargado/EstadoScreen.kt` — panel de flotilla con semáforo
- `ui/encargado/HistorialScreen.kt` — historial de mantenimientos, filtrable
- `ui/encargado/ReportesScreen.kt` — costos totales por periodo
- `ui/encargado/UsuariosScreen.kt` — administrar conductores/mecánicos
- `ui/encargado/ReasignacionScreen.kt` — reasignar conductor a vehículo

### ❌ No existe todavía

- **Registro de usuarios desde la app** (conductor/mecánico se auto-registran).
  Hoy se crean a mano en Supabase — ver sección 2.4.
- **Registro de mantenimiento con fotos** (conductor y mecánico).
- **Vista de mecánico para elegir vehículo** (el mecánico no tiene "un"
  vehículo asignado, necesita poder buscar/elegir cualquiera de la flotilla).
- **Alertas automáticas de kilometraje próximo** — el esquema ya tiene la
  tabla `frecuencias_mantenimiento` para esto, pero nadie la consume todavía
  desde Kotlin.
- **Home del mecánico y del encargado** tienen contenido real solo como
  texto de relleno en `HomeScreen.kt` (funciones `HomeMecanicoContent` /
  `HomeEncargadoContent`) — reemplazar cuando se construyan `Estado` y el
  listado de vehículos del mecánico.


   orden, de menor a mayor complejidad.
