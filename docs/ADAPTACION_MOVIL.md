# Adaptación móvil y sistema de diseño

Fuente de verdad visual: Figma **Prototipo**
`https://www.figma.com/design/ttsYOcKq9o5rMYscdwsDoR/Prototipo`

Este documento define **cómo se traduce el Figma a la app Android**, en
especial la sección D ("Plataforma web – Encargado de flota"), que se diseñó
para escritorio y ahora vive dentro de la app móvil.

---

## 1. Mapa del Figma (node IDs)

| Sección | Pantalla | Node ID |
|---|---|---|
| A. Registro / Inicio de sesión | IniciarSesión | `1:404` |
| | RecuperaciónContraseña | `1:387` |
| | NuevaContraseña | `1:352` |
| | RegistrarUser | `1:370` |
| B. Menú principal conductor | MenuPrincipalConductor (Home) | `1:1225` |
| | PerfilConductor | `28:163` |
| | EditarDatosConductor | `28:263` |
| | Alertas (del conductor) | `28:312` |
| | UnidadConductor (tabs: Información) | `28:418` |
| | EditarDatosUnidadConductor | `37:143` |
| C. Mantenimientos y kilometraje | MenuVehiculo (hub) | `70:133` |
| | RegistrarVehiculo | `49:118` |
| | RegistroMantenimiento | `49:161` |
| | HistorialMantenimientos (tab Historial) | `51:125` |
| | RegistroRecorrido (registrar km) | `51:174` |
| | HistorialKilometraje (tab Kilometraje) | `51:217` |
| | DocumentosVehiculo (tab Documentos) | `87:135` |
| D. Encargado (escritorio → **móvil**) | EstadoVehiculos | `102:158` |
| | HistorialVehiculo | `102:178` |
| | Reportes (filtros) | `102:198` |
| | ReporteGenerado | `102:218` |
| | PanelAlertas | `102:238` |
| | ConsultaUsuarios | `102:258` |
| | RegistroAdministrador | `102:278` |
| | ControlEstadoCuentas | `102:298` |
| | ReasignacionConductor | `102:318` |
| Componentes | Dropdown Menu (variantes) | `1:1113` |

---

## 2. Tokens de diseño (extraídos del archivo)

### Colores

| Token | Hex | Uso en Figma |
|---|---|---|
| `Navy` | `#0C2340` | Encabezados, fondo de auth, texto principal, pestaña activa |
| `Naranja` | `#C45C26` | Botón primario de auth ("Ingresar", "Enviar", "Crear cuenta"), "Editar datos" |
| `NaranjaLogo` | `#AA500F` | Logotipo |
| `Verde` | `#287721` | Botones de confirmar ("Guardar", "Registrar") |
| `GrisBoton` | `#4A4A4A` | Botón secundario ("Cancelar") |
| `Rojo` | `#AE2525` | Acción destructiva ("Cerrar sesión") |
| `Fondo` | `#D9D9D9` | Fondo de pantallas con sesión iniciada |
| `Superficie` | `#FFFFFF` | Tarjetas, inputs, barra inferior |
| `SuperficieSuave` | `#F7FAFC` | Filas / ítems dentro de una tarjeta |
| `TextoSecundario` | `#737D8C` | Etiquetas, subtítulos |
| `TextoTerciario` | `#6B6B6B` | Líneas de detalle en tarjetas |
| `Placeholder` | `#999999` | Texto de ayuda en inputs |
| `EstadoOk` | `#1C703D` (fondo al 15 %) | "Vigente", "Al día", "Activo" |
| `EstadoAviso` | `#B8780D` (fondo al 15 %) | "Por vencer", "Próximo", "Suspendido" |
| `EstadoCritico` | `#AD2626` (fondo al 15 %) | "Vencido", "Atrasado", "Crítica", "Desactivado" |
| `EstadoInfo` | `#0C2340` (fondo al 10 %) | "Informativa" |

Solo tema claro. **Sin dynamic color** (rompe los colores de marca en Android 12+).

### Tipografía

Roboto (fuente del sistema en Android, no hay que empaquetarla), salvo el logotipo.

| Estilo | Especificación | Uso |
|---|---|---|
| Logotipo | Rosarivo Regular 48 | "TransAndina" en login |
| `headlineMedium` | Bold 30 | Números grandes (KPIs, kilometraje actual) |
| `headlineSmall` | Bold 22 | Placa destacada |
| `titleLarge` | Bold 20 | Título del encabezado de pantalla |
| `titleMedium` | SemiBold 16 | Título de tarjeta |
| `bodyMedium` | Regular 13 | Texto general |
| `bodySmall` | Regular 12 | Detalles |
| `labelMedium` | Bold 12 | Chips de estado, pestañas |
| `labelSmall` | Regular 11 | Fechas, notas al pie |

Nota: el Figma dice "Trasandina" en el logo del login; es un error de tipeo, se usa **TransAndina**.

### Formas y espaciado

- Tarjetas: radio 12 dp, fondo blanco, sin sombra o sombra mínima.
- Inputs de formularios con sesión: forma de píldora (radio completo), fondo blanco.
- Inputs de auth: radio 8 dp.
- Botones: radio 8 dp, alto 48–52 dp.
- Chips de estado: radio 12 dp, fondo del color al 15 %, texto Bold 12 del color pleno.
- Márgenes laterales de pantalla: 16 dp. Separación entre tarjetas: 12 dp.

---

## 3. Componentes reutilizables (en `ui/components/`)

| Componente | Descripción |
|---|---|
| `TransAndinaTopBar` | Encabezado navy, título blanco centrado, flecha atrás opcional (ícono de círculo con flecha), subtítulo opcional |
| `TransAndinaBottomBar` | Barra blanca, íconos navy sin etiqueta; ítem activo en naranja |
| `BotonPrimario` / `BotonConfirmar` / `BotonSecundario` / `BotonDestructivo` | Naranja / verde / gris / rojo |
| `FilaBotonesFormulario` | "Cancelar" + acción principal, lado a lado, anclada abajo |
| `CampoTexto` / `CampoContrasena` | Label arriba, input blanco, ojo para contraseña |
| `CampoSeleccion` | Dropdown con el mismo aspecto que `CampoTexto` |
| `CampoFecha` | Abre `DatePicker` de Material 3, formato dd/mm/aaaa |
| `ChipEstado` | Recibe un enum `NivelEstado { OK, AVISO, CRITICO, INFO }` |
| `TarjetaTransAndina` | Contenedor blanco radio 12 |
| `TarjetaOpcionMenu` | Título + descripción + chevron "›" (hub de vehículo) |
| `DatoEtiquetado` | Etiqueta gris + valor (pantallas de detalle) |
| `PestanasSegmentadas` | Barra de pestañas tipo segmento (Información / Historial / Kilometraje / Documentos) |
| `TarjetaKpi` | Etiqueta + número grande, versión compacta |
| `EstadoVacio` | Mensaje centrado cuando no hay datos |

---

## 4. Reglas de traducción escritorio → móvil

| En escritorio | En móvil |
|---|---|
| Barra lateral de navegación | Barra inferior (máximo 5 ítems) + navegación en profundidad |
| Nombre del encargado abajo en la barra lateral | Pestaña **Perfil** |
| Tabla con columnas | Lista de tarjetas: dato principal en negrita arriba a la izquierda, chip de estado arriba a la derecha, 1–2 líneas de detalle abajo. Toda la tarjeta es tocable |
| "Ver historial ›" / "Ver vehículo ›" en cada fila | Tocar la tarjeta completa |
| Fila de 3 KPIs | Fila de 3 `TarjetaKpi` compactas con etiquetas cortas; si no caben, scroll horizontal |
| Panel lateral junto a una tabla | Pestaña adicional dentro de la misma pantalla |
| Formulario de 2 columnas | Una sola columna; botones anclados abajo |
| Filtros en una grilla | Pantalla de filtros propia en una columna, o `ModalBottomSheet` |
| Botón de acción en la barra superior ("Registrar administrador") | `FloatingActionButton` extendido |
| Opciones con radio buttons | Tarjetas seleccionables de ancho completo; la opción destructiva pide confirmación con `AlertDialog` |

---

## 5. Navegación por rol

**Conductor** (sin cambios en estructura): Inicio · Vehículo · Mantenimiento · Notificaciones · Perfil

**Mecánico** (sin cambios): Inicio · Mantenimiento · Notificaciones · Perfil

**Encargado** (antes 6 pestañas, ahora 5):

| Pestaña | Ícono | Contiene |
|---|---|---|
| Flotilla | `Dashboard` | EstadoVehiculos → toca un vehículo → **Detalle de vehículo** (HistorialVehiculo + Reasignar) |
| Alertas | `NotificationImportant` | PanelAlertas → toca una alerta → Detalle de vehículo |
| Reportes | `Assessment` | Filtros → ReporteGenerado |
| Usuarios | `People` | ConsultaUsuarios → ControlEstadoCuentas; FAB → RegistroAdministrador |
| Perfil | `Person` | Perfil existente |

"Historial por vehículo" y "Reasignación" dejan de ser pestañas porque siempre
se hacen **sobre un vehículo concreto**: se entra a ellas desde el detalle del vehículo.
Así se pasa de 6 a 5 pestañas sin perder ninguna función.

---

## 6. Pantalla por pantalla (sección D)

### Flotilla (`102:158`)
- TopBar "Estado de flotilla".
- 3 KPIs compactos: "Activos", "Mant. próximos", "Docs. por vencer".
- Buscador por placa o conductor + chips de filtro: Todos · Atrasado · Próximo · Al día.
- Lista de tarjetas de vehículo: **placa** + chip de estado; "Nissan Frontier · Carlos Fernández"; "Últ. mant. 25/08/2026 · 492 400 km".
- Tocar → Detalle de vehículo.

### Detalle de vehículo – vista encargado (`102:178`)
- TopBar con atrás: placa como título, "Nissan Frontier 2021" como subtítulo.
- Tarjeta resumen: tipo, capacidad, conductor asignado, km actual.
- `PestanasSegmentadas`: **Mantenimientos** · **Documentos** (el panel lateral de escritorio pasa a pestaña).
- Mantenimientos: tarjetas como en `51:125` (tipo · categoría / taller · costo / fecha).
- Acción "Reasignar conductor" (botón en la tarjeta resumen) → Reasignación.
- Reutilizar componentes de la vista de vehículo del conductor; no duplicar pantallas.

### Reasignación (`102:318`)
- TopBar con atrás "Reasignar conductor".
- Vehículo y conductor actual como datos de solo lectura (vienen del detalle).
- Campos: Nuevo conductor (`CampoSeleccion`, solo conductores activos sin vehículo), Fecha efectiva (`CampoFecha`), Motivo (texto multilínea).
- Texto de aviso: el conductor anterior pierde acceso y ambos reciben notificación.
- `FilaBotonesFormulario`: Cancelar · Confirmar reasignación (con `AlertDialog` de confirmación).
- Debajo: "Reasignaciones recientes" de ese vehículo como tarjetas "Diego Ramírez → Sofía Blanco · 22/08/2026".

### Alertas (`102:238`)
- Los 3 KPIs se convierten en chips de filtro con conteo: Críticas (3) · Próximas (5) · Al día (16). El chip "Todas" va primero.
- Lista ordenada por urgencia. Tarjeta con franja de color de 4 dp a la izquierda, título, detalle ("SCD-3421 · Nissan Frontier · venció el 10/08/2026") y chip de nivel.
- Tocar → Detalle de vehículo.

### Reportes (`102:198` y `102:218`)
- Pantalla de filtros en una columna: Vehículo, Tipo de mantenimiento, Taller, Fecha inicial, Fecha final.
- "Formato de salida" se elimina del formulario: en móvil el reporte se ve en pantalla y se exporta después.
- Botones: Limpiar filtros · Generar reporte.
- Resultado: tarjeta resumen ("38 mantenimientos", "₡ 4 812 500", rango de fechas), botones "Modificar filtros" y "Exportar PDF", y lista de tarjetas con fecha · placa, tipo, taller · responsable, costo alineado a la derecha.
- Exportar PDF usa `android.graphics.pdf.PdfDocument` + intent de compartir (fase posterior).

### Usuarios (`102:258`)
- Buscador ("Nombre, cédula o correo") + chips: Todos · Conductores · Mecánicos · Encargados.
- Tarjeta: nombre en negrita + chip de estado; "Conductor · 1-1111-1111"; correo.
- FAB extendido "Registrar administrador".
- Tocar → Estado de la cuenta.

### Estado de la cuenta (`102:298`)
- Tarjeta perfil: nombre, rol · cédula · correo, chip del estado actual.
- 3 tarjetas seleccionables: Activo, Suspendido, Desactivado, con su descripción.
- "Desactivado" muestra `AlertDialog`: es permanente y libera el vehículo asignado.
- `FilaBotonesFormulario`: Cancelar · Guardar cambios.

### Registrar administrador (`102:278`)
- Mismo patrón visual que Registro (`1:370`), pero con sesión iniciada: TopBar con atrás y fondo claro.
- Campos en una columna: nombre, cédula, correo, teléfono, contraseña, confirmar contraseña. Rol fijo "Encargado de flota" (no editable).
- **Guardado** (decidido el 17/09/2026): un cliente de Supabase secundario, que no guarda sesión, hace `signUp` con el correo y la contraseña. Así la sesión del encargado actual no cambia. Con el id que devuelve, el cliente principal hace un *upsert* en `usuarios` con `rol = encargado`. Lo permite la política `usuarios_insert_encargado` (migración `202609171900_encargado.sql`).

### Reglas de estado de cuenta (decididas el 17/09/2026)
- **Suspendido**: temporal y reversible. La cuenta no puede leer ni escribir datos.
- **Desactivado**: permanente (la base rechaza reactivarla) y libera el vehículo asignado, que queda en el historial de reasignaciones.
- La base de datos bloquea a las cuentas no activas. Aun así, Supabase Auth les deja iniciar sesión, así que después del login la app revisa `usuarios.estado` y, si no es `activo`, muestra el motivo y cierra la sesión.
- Un encargado no puede cambiar el estado de su propia cuenta.

### Reasignación: detalles de backend
- Se hace con el RPC `reasignar_conductor(vehiculo, nuevo_conductor, fecha_efectiva, motivo)`, que cambia el vehículo y guarda el historial en `reasignaciones` en un solo paso.
- La fecha efectiva es informativa: el cambio se aplica en el momento.
- La base rechaza a un conductor que no esté activo o que ya tenga un vehículo.
- Los dos conductores, el anterior y el nuevo, reciben una alerta automática.

### Implementación (17/09/2026)

| Pantalla | Archivo | Ruta |
|---|---|---|
| Flotilla | `ui/flotilla/FlotillaScreen.kt` | pestaña `estado` |
| Registrar / editar vehículo | `ui/flotilla/VehiculoFormularioScreen.kt` | `vehiculo-nuevo`, `vehiculo-editar/{id}` |
| Detalle (vista encargado) | `ui/vehiculo/VehiculoDetalleScreen.kt` con `esEncargado = true` | `vehiculo-detalle/{id}/{pestaña}` |
| Reasignación | `ui/flotilla/ReasignacionScreen.kt` | `reasignar/{id}` |
| Registrar mantenimiento | `ui/mantenimiento/RegistrarMantenimientoScreen.kt` (botón en la pestaña Historial del detalle) | `registrar-mantenimiento/{id}` |
| Alertas | `ui/alertas/AlertasFlotillaScreen.kt` | pestaña `alertas-flotilla` |
| Enviar aviso de gerencia | `ui/alertas/EnviarAvisoScreen.kt` | `enviar-aviso` |
| Reportes + PDF | `ui/reportes/ReportesScreen.kt`, `ExportadorPdfReporte.kt` | pestaña `reportes` |
| Usuarios | `ui/usuarios/UsuariosScreen.kt` | pestaña `usuarios` |
| Estado de la cuenta | `ui/usuarios/EstadoCuentaScreen.kt` | `estado-cuenta/{id}` |
| Registrar administrador | `ui/usuarios/RegistrarAdministradorScreen.kt` | `registrar-administrador` |

Decisiones tomadas al implementar:

- **Detalle del vehículo**: se reutiliza la pantalla del conductor con sus 4 pestañas, en lugar de crear una de 2. El encargado ve además el conductor asignado y los botones "Reasignar conductor" y "Editar vehículo". No ve "Registrar kilometraje", porque solo lo puede hacer el conductor (RLS).
- **Estado de cada vehículo** en Flotilla: se toma el peor entre el estado del mantenimiento y el de los documentos. Un documento vencido cuenta como "Atrasado".
- **KPI** "Mant. pendientes": cuenta los vehículos con mantenimiento próximo o atrasado.
- **Reglas de las alertas** (`domain/AlertasFlotilla.kt`):
  - Crítica: documento vencido o que vence en 7 días o menos, y mantenimiento atrasado.
  - Próxima: documento que vence en menos de 15 días, y mantenimiento a 1 000 km o 15 días.
  - Informativa: reasignaciones de los últimos 30 días.
- **Próximo mantenimiento**: solo se calcula para las categorías con al menos un servicio registrado.
- **Registrar vehículo** incluye las cuatro fechas de documentos, que son opcionales. Es la única forma de cargarlas.
- **Aviso de gerencia**: no tiene frame en el Figma. Es un formulario con destinatario (todos los conductores, todas las cuentas o una persona) y mensaje. Se entra desde el botón flotante de Alertas.
- **Desactivado** usa un chip gris (`EstadoNeutro`), como en el Figma `102:258`.
- **PDF**: tamaño carta, generado con `android.graphics.pdf` y compartido con `FileProvider` (autoridad `${applicationId}.archivos`).

---

## 7. Ajustes a pantallas de las secciones A–C

- **MenuVehiculo (`70:133`)**: la opción "Registrar vehículo" solo aparece para el encargado.
- **DocumentosVehiculo (`87:135`)**: cambiar el texto "desde la plataforma web" por "Los documentos los carga el encargado de flota".
- **Alertas del conductor (`28:312`)**: el título del Figma dice "Datos personales" por error; usar "Mis alertas".
- **Registro (`1:370`)**: el selector de rol solo ofrece Conductor y Mecánico.
- **RegistroMantenimiento (`49:161`)**: se agrega el campo obligatorio **"Kilometraje del servicio"**, que no está en el Figma y se precarga con el kilometraje actual del vehículo. La base lo exige (`mantenimientos.km`) y hace falta para estimar el próximo mantenimiento (decidido el 17/09/2026). Las categorías se leen de `frecuencias_mantenimiento` según el tipo de vehículo.
  - Lo pueden registrar el **encargado** (sobre cualquier vehículo, decidido el 17/09/2026) y el conductor (sobre el suyo). La pantalla es la misma; por ahora solo está conectada para el encargado, desde la pestaña Historial del detalle.
  - Campos además del Figma: **Taller** (obligatorio, es el "lugar") y **Descripción** (opcional). El **costo** es obligatorio, pero puede ser 0.
  - El kilometraje del servicio no puede ser mayor al actual del vehículo, porque el encargado no puede registrar kilometraje.
  - Debajo se muestra el **próximo servicio estimado** de la categoría elegida.
  - **Fotos**: hasta 3, JPG o PNG. Antes de subirlas se reducen a 1600 px y se guardan como JPEG en el bucket `mantenimientos`, con la ruta `<vehiculo>/<mantenimiento>/<uuid>.jpg`. Si una foto falla, el mantenimiento igual queda guardado y se avisa. Todavía no hay pantalla para ver las fotos.
- **DocumentosVehiculo (`87:135`)**: se muestra también "Permiso de carga" (`vehiculos.fecha_permiso_carga`).

---

## 8. Backend: qué existe y qué falta

Verificado contra la base real el 17/09/2026. Migraciones en `supabase/migrations/`:

| Migración | Qué hace | Estado |
|---|---|---|
| `202609151900_crear_perfil_usuario_desde_auth.sql` | Trigger que crea el perfil al registrarse | **No aplicar todavía**: rompe el registro actual (ver la advertencia en el archivo) |
| `202609152200_mantenimientos.sql` | Columna `taller` y bucket de fotos | Aplicada (17/09/2026) |
| `202609171900_encargado.sql` | Estado de cuenta, bloqueo de cuentas no activas, permiso de carga, alertas de gerencia, reasignaciones, registrar administrador | Aplicada (17/09/2026) |
| `202609171910_frecuencias_iniciales.sql` | Catálogo de categorías y frecuencias (la tabla estaba vacía) | Aplicada (17/09/2026) |
| `202609172100_encargado_registra_mantenimientos.sql` | El encargado también puede registrar mantenimientos | **Pendiente**: aplicar para que el encargado pueda guardar |

Datos de ejemplo (no son migraciones) en `supabase/datos_ejemplo/`: `mantenimientos_ejemplo.sql` inserta 8 mantenimientos por vehículo activo, y `borrar_mantenimientos_ejemplo.sql` los quita.

Decisiones de modelo:

- **Documentos**: siguen siendo fechas en `vehiculos` (marchamo, revisión técnica, seguro, permiso de carga). No se adjuntan archivos; "Ver documento ›" del Figma no se implementa.
- **Talleres**: texto libre en `mantenimientos.taller`, sin tabla propia.
- **Alertas**: las de documentos (vencido, por vencer) y las de mantenimiento (próximo, atrasado) se **calculan en la app** a partir de fechas, kilometraje y `frecuencias_mantenimiento`. En la tabla `alertas` quedan solo los avisos que ocurren una vez: reasignación, mantenimiento registrado y gerencia.
- **Próximo mantenimiento**: último mantenimiento de la categoría + `km_frecuencia` o + `dias_frecuencia`, lo que ocurra primero.
- **Reportes PDF**: se generan en el teléfono; no necesitan tablas.
