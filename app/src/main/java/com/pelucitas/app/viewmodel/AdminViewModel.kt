package com.pelucitas.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pelucitas.app.data.model.Employee
import com.pelucitas.app.data.repository.EmpleadoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val repo = EmpleadoRepository()

    val empleados: StateFlow<List<Employee>> = repo.empleados
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    sealed class EstadoOp {
        object Inactivo  : EstadoOp()
        object Cargando  : EstadoOp()
        object Ok        : EstadoOp()
        data class Error(val msg: String) : EstadoOp()
    }

    private val _estadoOp = MutableStateFlow<EstadoOp>(EstadoOp.Inactivo)
    val estadoOp: StateFlow<EstadoOp> = _estadoOp.asStateFlow()

    fun crearEmpleado(emp: Employee, contrasena: String) {
        viewModelScope.launch {
            _estadoOp.value = EstadoOp.Cargando
            try {
                repo.crear(emp, contrasena)
                _estadoOp.value = EstadoOp.Ok
            } catch (e: Exception) {
                _estadoOp.value = EstadoOp.Error(e.message ?: "Error al crear")
            }
        }
    }

    fun actualizarEmpleado(emp: Employee) {
        viewModelScope.launch {
            _estadoOp.value = EstadoOp.Cargando
            try {
                repo.actualizar(emp)
                _estadoOp.value = EstadoOp.Ok
            } catch (e: Exception) {
                _estadoOp.value = EstadoOp.Error(e.message ?: "Error al actualizar")
            }
        }
    }

    fun desactivarEmpleado(alias: String) {
        viewModelScope.launch {
            _estadoOp.value = EstadoOp.Cargando
            try {
                repo.desactivar(alias)
                _estadoOp.value = EstadoOp.Ok
            } catch (e: Exception) {
                _estadoOp.value = EstadoOp.Error(e.message ?: "Error al eliminar")
            }
        }
    }

    fun resetEstado() { _estadoOp.value = EstadoOp.Inactivo }
}
