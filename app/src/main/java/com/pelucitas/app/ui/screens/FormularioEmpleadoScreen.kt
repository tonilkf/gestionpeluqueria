package com.pelucitas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pelucitas.app.data.model.Employee
import com.pelucitas.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioEmpleadoScreen(
    alias: String,           // vacío = nuevo empleado
    vm: AdminViewModel,
    onBack: () -> Unit,
    onGuardado: () -> Unit
) {
    val esNuevo    = alias.isBlank()
    val empleados  by vm.empleados.collectAsState()
    val estadoOp   by vm.estadoOp.collectAsState()

    // Pre-rellenar si es edición
    val empExistente = remember(alias, empleados) {
        if (!esNuevo) empleados.find { it.alias == alias } else null
    }

    var nombre       by remember { mutableStateOf(empExistente?.nombre      ?: "") }
    var apellidos    by remember { mutableStateOf(empExistente?.apellidos   ?: "") }
    var telefono     by remember { mutableStateOf(empExistente?.telefono    ?: "") }
    var aliasLocal   by remember { mutableStateOf(empExistente?.alias       ?: "") }
    var especialidad by remember { mutableStateOf(empExistente?.especialidad ?: "") }
    var genero       by remember { mutableStateOf(empExistente?.genero      ?: "M") }
    var contrasena   by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }

    // Navegar de vuelta cuando la operación termine con éxito
    LaunchedEffect(estadoOp) {
        if (estadoOp is AdminViewModel.EstadoOp.Ok) {
            vm.resetEstado()
            onGuardado()
        }
    }

    val cargando = estadoOp is AdminViewModel.EstadoOp.Cargando
    val errorMsg = (estadoOp as? AdminViewModel.EstadoOp.Error)?.msg

    val puedGuardar = nombre.isNotBlank() && aliasLocal.isNotBlank() &&
            (esNuevo && contrasena.length >= 6 || !esNuevo)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esNuevo) "Nuevo empleado" else "Editar empleado") },
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick  = {
                        val emp = Employee(
                            uid          = empExistente?.uid ?: "",
                            nombre       = nombre.trim(),
                            apellidos    = apellidos.trim(),
                            telefono     = telefono.trim(),
                            alias        = aliasLocal.trim().lowercase(),
                            genero       = genero,
                            especialidad = especialidad.trim(),
                            activo       = true
                        )
                        if (esNuevo) vm.crearEmpleado(emp, contrasena)
                        else vm.actualizarEmpleado(emp)
                    },
                    enabled  = puedGuardar && !cargando,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = MaterialTheme.shapes.medium
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (esNuevo) "Crear empleado" else "Guardar cambios")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            if (errorMsg != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text     = errorMsg,
                        color    = MaterialTheme.colorScheme.onErrorContainer,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value          = nombre,
                onValueChange  = { nombre = it },
                label          = { Text("Nombre *") },
                singleLine     = true,
                modifier       = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape          = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value         = apellidos,
                onValueChange = { apellidos = it },
                label         = { Text("Apellidos") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape         = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value         = telefono,
                onValueChange = { telefono = it },
                label         = { Text("Teléfono") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape         = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value         = aliasLocal,
                onValueChange = { if (esNuevo) aliasLocal = it },
                label         = { Text("Alias (id único) *") },
                singleLine    = true,
                enabled       = esNuevo,
                modifier      = Modifier.fillMaxWidth(),
                supportingText = if (!esNuevo) ({ Text("El alias no se puede cambiar") }) else null,
                shape         = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value         = especialidad,
                onValueChange = { especialidad = it },
                label         = { Text("Especialidad") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape         = MaterialTheme.shapes.medium
            )

            // Selector de género
            Text("Género", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("M" to "Mujer 👩", "H" to "Hombre 👨").forEach { (valor, etiqueta) ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected  = genero == valor,
                            onClick   = { genero = valor }
                        )
                        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Contraseña (solo en alta nueva)
            if (esNuevo) {
                OutlinedTextField(
                    value          = contrasena,
                    onValueChange  = { contrasena = it },
                    label          = { Text("Contraseña *") },
                    singleLine     = true,
                    modifier       = Modifier.fillMaxWidth(),
                    visualTransformation = if (verContrasena) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon   = {
                        IconButton(onClick = { verContrasena = !verContrasena }) {
                            Icon(
                                if (verContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    supportingText = { Text("Mínimo 6 caracteres") },
                    shape          = MaterialTheme.shapes.medium
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
