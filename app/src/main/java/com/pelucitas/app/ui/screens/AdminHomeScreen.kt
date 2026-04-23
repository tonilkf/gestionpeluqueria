package com.pelucitas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.pelucitas.app.ui.theme.Terracotta

@Composable
fun AdminHomeScreen(
    onGestionarEmpleados: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val correo = FirebaseAuth.getInstance().currentUser?.email ?: ""
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        IconButton(
            onClick  = { confirmarSalida = true },
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(
            modifier              = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center
        ) {
            Icon(Icons.Default.ContentCut, contentDescription = null,
                tint = Terracotta, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(12.dp))
            Text("PeluCitas", style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground)
            Text("Panel de administración",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            if (correo.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(correo, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(64.dp))

            Button(
                onClick  = onGestionarEmpleados,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Group, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Gestionar empleados", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
