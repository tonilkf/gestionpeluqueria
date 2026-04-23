# PeluCitas

App Android para la gestión de citas en una peluquería. Los clientes reservan su cita en pocos pasos, los empleados consultan su horario diario y el administrador gestiona el equipo.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Estado | ViewModel + StateFlow |
| Backend | Firebase Realtime Database |
| Autenticación | Firebase Auth |
| Arquitectura | MVVM |

---

## Roles

| Rol | Acceso |
|---|---|
| **Cliente** | Reservar citas, ver y cancelar sus citas |
| **Empleado** | Ver su propio horario de trabajo (solo lectura) |
| **Administrador** | Crear, editar y eliminar empleados |

El rol se almacena en Firebase bajo `users/{uid}/rol`.

> **Primer administrador:** créalo registrándote normalmente en la app y luego establece manualmente `users/{uid}/rol = "admin"` en la Firebase Console.

---

## Flujo de reserva 
```
Inicio → Servicios → Estilista → Fecha → Hora → Datos → Confirmación
```

Los huecos disponibles se calculan en tiempo real: se generan slots de 30 minutos dentro del horario del salón y se descartan los que se solapan con citas ya existentes de la estilista elegida.

**Horario del salón:** mañana 09:00–14:30 · tarde 17:00–21:30

---

## Servicios disponibles

| Servicio | Duración | Precio |
|---|---|---|
| Corte de pelo | 30 min | 15 € |
| Lavado + Corte | 45 min | 22 € |
| Tinte | 120 min | 55 € |
| Mechas | 150 min | 70 € |
| Tratamiento capilar | 60 min | 30 € |
| Peinado | 45 min | 28 € |
| Tinte + Corte | 150 min | 65 € |

---

## Estructura del proyecto

```
app/src/main/java/com/pelucitas/app/
├── data/
│   ├── model/
│   │   ├── Cita.kt            — Modelo de cita (tipealias Appointment)
│   │   ├── Empleado.kt        — Modelo de empleado (tipealias Employee)
│   │   ├── Rol.kt             — Enum ADMIN / EMPLEADO / CLIENTE
│   │   └── Service.kt         — Modelo Servicio + CatalogoServicios
│   └── repository/
│       ├── EmpleadoRepository.kt  — CRUD de empleados en Firebase
│       └── FirebaseRepository.kt  — Citas: guardar, eliminar, consultar
├── util/
│   └── TimeSlotGenerator.kt   — Generación y filtrado de slots horarios
├── viewmodel/
│   ├── BookingViewModel.kt    — Estado del flujo de reserva
│   ├── AdminViewModel.kt      — Gestión de empleados (admin)
│   └── HorarioViewModel.kt    — Horario del empleado autenticado
└── ui/
    ├── theme/                 — Colores, tipografía, tema
    ├── navigation/
    │   └── NavGraph.kt        — Grafo de navegación + objeto Rutas
    └── screens/
        ├── AuthScreen.kt
        ├── HomeScreen.kt
        ├── ServicesScreen.kt
        ├── EmployeeScreen.kt
        ├── DatePickerScreen.kt
        ├── TimeSlotScreen.kt
        ├── ClientFormScreen.kt
        ├── ConfirmationScreen.kt
        ├── MyAppointmentsScreen.kt
        ├── AdminHomeScreen.kt
        ├── GestionEmpleadosScreen.kt
        ├── FormularioEmpleadoScreen.kt
        └── HorarioEmpleadoScreen.kt
```

---

## Estructura Firebase

```
users/
  {uid}/
    rol: "admin" | "empleado" | "cliente"
    empleadoId: "alias"        ← solo para rol "empleado"

empleados/
  {alias}/
    uid, nombre, apellidos, telefono, alias,
    genero, photoUrl, especialidad, activo

appointments/
  {id}/
    userId, clientName, clientPhone,
    serviceId, employeeId,
    dateEpochDay, startTimeSecond, notes
```

---

## Paleta de colores

| Color | Hex |
|---|---|
| Crema | `#F5F0E8` |
| Terracota | `#A0674E` |
| Marrón oscuro | `#3A2518` |
| Verde salvia | `#7A9E7E` |

---

## Configuración inicial

1. Añade el proyecto a [Firebase Console](https://console.firebase.google.com) y descarga `google-services.json` en `app/`.
2. Activa **Authentication → Email/Password**.
3. Crea la base de datos en **Realtime Database**.
4. Registra al administrador desde la app (registro normal) y luego en la consola establece `users/{uid}/rol = "admin"`.
5. El administrador ya puede crear empleados desde la app. Los empleados se autentican con `alias@pelucitas.internal` y la contraseña que el admin configure.
