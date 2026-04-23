package com.pelucitas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pelucitas.app.data.model.Employee
import com.pelucitas.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionEmpleadosScreen(
    vm: AdminViewModel,
    onBack: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (String) -> Unit
) {
    val empleados  by vm.empleados.collectAsState()
    val estadoOp   by vm.estadoOp.collectAsState()
    var eliminar   by remember { mutableStateOf<Employee?>(null) }

    // Mostrar snackbar si hay error
    val snackState = remember { SnackbarHostState() }
    LaunchedEffect(estadoOp) {
        if (estadoOp is AdminViewModel.EstadoOp.Error) {
            snackState.showSnackbar((estadoOp as AdminViewModel.EstadoOp.Error).msg)
            vm.resetEstado()
        }
    }

    // Diálogo de confirmación de baja
    eliminar?.let { emp ->
        AlertDialog(
            onDismissRequest = { eliminar = null },
            title   = { Text("Dar de baja") },
            text    = { Text("¿Desactivar a ${emp.nombreCompleto}?") },
            confirmButton = {
                TextButton(onClick = { vm.desactivarEmpleado(emp.alias); eliminar = null }) {
                    Text("Sí", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { eliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Empleados") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo empleado")
            }
        },
        snackbarHost    = { SnackbarHost(snackState) },
        containerColor  = MaterialTheme.colorScheme.background
    ) { padding ->
        if (empleados.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin empleados activos.\nPulsa + para añadir uno.",
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(
                    start  = 16.dp, end = 16.dp,
                    top    = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(empleados, key = { it.alias }) { emp ->
                    FilaEmpleado(
                        emp      = emp,
                        onEditar = { onEditar(emp.alias) },
                        onBajar  = { eliminar = emp }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaEmpleado(emp: Employee, onEditar: () -> Unit, onBajar: () -> Unit) {
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
            // Avatar
            Surface(
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text  = emp.avatarInitials.ifBlank { emp.avatarEmoji },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(emp.nombreCompleto, style = MaterialTheme.typography.titleSmall)
                Text(
                    emp.especialidad.ifBlank { "Sin especialidad" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "@${emp.alias}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onBajar) {
                Icon(Icons.Default.Delete, contentDescription = "Dar de baja",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
