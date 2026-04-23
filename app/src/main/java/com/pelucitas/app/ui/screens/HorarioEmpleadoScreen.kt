package com.pelucitas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pelucitas.app.data.model.Appointment
import com.pelucitas.app.viewmodel.HorarioViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioEmpleadoScreen(
    vm: HorarioViewModel,
    onCerrarSesion: () -> Unit
) {
    val citas    by vm.citas.collectAsState()
    val cargando by vm.cargando.collectAsState()
    val empleado by vm.empleado.collectAsState()
    val hoy      = LocalDate.now()

    var confirmarSalida by remember { mutableStateOf(false) }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title   = { Text("Cerrar sesión") },
            text    = { Text("¿Seguro que quieres salir?") },
            confirmButton = {
                TextButton(onClick = { confirmarSalida = false; onCerrarSesion() }) {
                    Text("Sí, salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mi horario")
                        empleado?.let {
                            Text(
                                it.nombreCompleto,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { confirmarSalida = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            cargando -> Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            citas.isEmpty() -> Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tienes citas pendientes",
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                val proximas = citas
                    .filter { !it.date.isBefore(hoy) }
                    .sortedWith(compareBy({ it.date }, { it.startTime }))
                val porFecha = proximas.groupBy { it.date }

                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.fillMaxSize().padding(padding)
                ) {
                    if (porFecha.isEmpty()) {
                        item {
                            Text(
                                "Sin citas próximas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    porFecha.forEach { (fecha, citasDia) ->
                        item {
                            val esHoy  = fecha == hoy
                            val titulo = when {
                                esHoy -> "Hoy — ${fecha.dayOfMonth} de ${fecha.month.getDisplayName(TextStyle.FULL, Locale("es"))}"
                                else  -> fecha.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es"))
                                    .replaceFirstChar { it.uppercase() } +
                                        ", ${fecha.dayOfMonth} de " +
                                        fecha.month.getDisplayName(TextStyle.FULL, Locale("es"))
                            }
                            Text(
                                text     = titulo,
                                style    = if (esHoy) MaterialTheme.typography.titleMedium
                                           else MaterialTheme.typography.titleSmall,
                                color    = if (esHoy) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(citasDia, key = { it.id }) { cita ->
                            TarjetaTurno(cita)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaTurno(cita: Appointment) {
    val fmtHora = DateTimeFormatter.ofPattern("HH:mm")
    Surface(
        shape          = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Badge de hora
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(cita.startTime.format(fmtHora),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(cita.endTime.format(fmtHora),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(cita.service.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    cita.clientName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (cita.clientPhone.isNotBlank()) {
                    Text(
                        cita.clientPhone,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (cita.notes.isNotBlank()) {
                    Text(
                        "📝 ${cita.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
