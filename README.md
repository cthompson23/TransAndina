# TransAndina Flotilla

App Android nativa (Kotlin + Jetpack Compose) para gestionar el mantenimiento
de la flotilla de vehículos de TransAndina. El backend es Supabase (Postgres,
Auth y Row Level Security).

Esta guía sirve para clonar el proyecto, dejarlo corriendo y saber qué hay
construido.

---

## 1. Poner el proyecto a andar

1. Clonar el repositorio y abrirlo con Android Studio.
2. Copiar `local.properties.example` a `local.properties` y llenar:

   ```properties
   SUPABASE_URL=https://xxxxxxxx.supabase.co
   SUPABASE_PUBLISHABLE_KEY=...
   ```

   Los valores los tiene el equipo. `local.properties` no se sube al
   repositorio, y **nunca** se usa la clave `service_role`.
3. Sincronizar Gradle y correr la app en un emulador con Android 8 o superior.

Desde la terminal de macOS, Gradle necesita el JDK que trae Android Studio:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug        # compilar
./gradlew testDebugUnitTest    # pruebas unitarias
./gradlew lintDebug            # lint
```

---

## 2. Base de datos

El esquema completo está en `supabase/schema_flotilla.sql`. Los cambios
posteriores viven en `supabase/migrations/`, y se aplican **en orden de
nombre**, copiándolos en el SQL Editor de Supabase:

| Migración | Qué hace |
|---|---|
| `202609152200_mantenimientos.sql` | Columna `taller` y bucket de fotos |
| `202609171900_encargado.sql` | Estado de cuenta (activo/suspendido/desactivado), bloqueo de cuentas no activas, permiso de carga, alertas de gerencia, historial de reasignaciones y registro de administradores |
| `202609171910_frecuencias_iniciales.sql` | Categorías de mantenimiento y cada cuánto tocan |
| `202609172100_encargado_registra_mantenimientos.sql` | El encargado también registra mantenimientos |
| `202609172200_encargado_registra_kilometraje.sql` | El encargado también registra kilometraje |

Para probar reportes, historial y alertas sin capturar datos a mano:
`supabase/datos_ejemplo/mantenimientos_ejemplo.sql` inserta ocho
mantenimientos por vehículo, y `borrar_mantenimientos_ejemplo.sql` los quita.

Reglas del equipo: **no se toca el esquema desde el panel de Supabase**. Todo
cambio se escribe como migración en `supabase/migrations/`, se revisa y
después se ejecuta.

---

## 3. Los tres roles

| Rol | Qué puede hacer |
|---|---|
| **Conductor** | Ver el vehículo asignado y sus documentos, registrar kilometraje, ver su historial de mantenimientos y sus alertas |
| **Encargado de flota** | Todo lo de la flotilla: registrar y editar vehículos, reasignar conductores, registrar kilometraje y mantenimientos, ver alertas, generar reportes en PDF, administrar cuentas y registrar otros encargados |
| **Mecánico** | Rol todavía por definir; la base ya lo contempla en las políticas |

Quién puede hacer qué lo decide la base de datos con Row Level Security, no la
app. La app solo esconde lo que la persona no puede hacer.

---

## 4. Estado actual

### Construido

- **Autenticación**: login, registro, recuperación y cambio de contraseña. Una
  cuenta suspendida o desactivada no puede entrar.
- **Conductor**: inicio con su vehículo, detalle con pestañas (información,
  historial, kilometraje y documentos), registro de kilometraje y perfil.
- **Encargado** (cinco pestañas): flotilla con semáforo y búsqueda, detalle del
  vehículo con reasignación y edición, registro de vehículos y de
  mantenimientos con fotos, alertas calculadas con envío de avisos, reportes
  con exportación a PDF, consulta de usuarios, control de estado de cuentas y
  registro de administradores.

### Pendiente

- Mostrar las fotos de los mantenimientos ya subidos.
- Dar de baja un vehículo desde la app (la columna `activo` existe).
- "Mis alertas" del conductor: hoy la pantalla está vacía.
- Conectar el formulario de mantenimiento para el conductor (ya existe, solo
  falta enlazarlo).
- Definir y construir el rol de mecánico.

---

## 5. Cómo está organizado el código

```
UI (Composable *Screen) → ViewModel (StateFlow<*UiState>) → Repository → Supabase
```

- `ui/<funcionalidad>/` — una carpeta por pantalla o grupo de pantallas, con
  `XScreen.kt` y `XViewModel.kt`.
- `ui/components/` — componentes reutilizables (botones, campos, tarjetas,
  chips). Antes de crear uno nuevo, revisar si ya existe.
- `ui/theme/` — colores, tipografía y formas. Es el único lugar donde se
  declaran colores.
- `domain/` — cálculos puros y con pruebas: estado de los documentos, próximo
  mantenimiento, alertas de la flotilla y filtros de reportes.
- `data/model/`, `data/repository/` — modelos y acceso a Supabase.

El diseño sale del Figma "Prototipo". Como está dibujado para escritorio,
`docs/ADAPTACION_MOVIL.md` explica cómo se traduce cada pantalla a móvil, con
los node IDs, los colores y las decisiones que se han ido tomando. **Conviene
leerlo antes de tocar una pantalla.**

---

## 6. Convenciones

- Todo en español: la interfaz, los comentarios y los mensajes de commit.
- Los textos visibles van en `res/values/strings.xml`.
- Nunca se trabaja directo sobre `main`: una rama por funcionalidad
  (`feature/<descripcion>`).
- Antes de dar algo por terminado, `./gradlew assembleDebug` tiene que pasar.
