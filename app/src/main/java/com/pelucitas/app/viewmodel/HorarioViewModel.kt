package com.pelucitas.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pelucitas.app.data.model.Appointment
import com.pelucitas.app.data.model.Employee
import com.pelucitas.app.data.model.ServiceCatalog
import com.pelucitas.app.data.repository.EmpleadoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime

class HorarioViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseDatabase.getInstance()

    private val _citas    = MutableStateFlow<List<Appointment>>(emptyList())
    val citas: StateFlow<List<Appointment>> = _citas.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _empleado = MutableStateFlow<Employee?>(null)
    val empleado: StateFlow<Employee?> = _empleado.asStateFlow()

    private var listenerActivo: ValueEventListener? = null
    private var queryActiva: com.google.firebase.database.Query? = null

    init { cargarHorario() }

    private fun cargarHorario() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _cargando.value = true
            try {
                val empId = db.getReference("users/$uid/empleadoId").get().await()
                    .getValue(String::class.java) ?: return@launch

                val empSnap = db.getReference("empleados/$empId").get().await()
                val emp = with(EmpleadoRepository()) { empSnap.toEmployee() } ?: return@launch
                _empleado.value = emp

                escucharCitas(empId, emp)
            } catch (e: Exception) {
                _cargando.value = false
            }
        }
    }

    private fun escucharCitas(empId: String, emp: Employee) {
        val query = db.getReference("appointments").orderByChild("employeeId").equalTo(empId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                _citas.value = snap.children.mapNotNull { it.toCita(emp) }
                _cargando.value = false
            }
            override fun onCancelled(error: DatabaseError) {
                _cargando.value = false
            }
        }
        queryActiva   = query
        listenerActivo = listener
        query.addValueEventListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        listenerActivo?.let { queryActiva?.removeEventListener(it) }
    }

    // ── Conversión ────────────────────────────────────────────────────────────

    private fun DataSnapshot.toCita(emp: Employee): Appointment? {
        val serviceId = child("serviceId").getValue(String::class.java) ?: return null
        val servicio  = ServiceCatalog.all.find { it.id == serviceId }  ?: return null
        val diaEpoch  = child("dateEpochDay").getValue(Long::class.java)    ?: return null
        val segs      = child("startTimeSecond").getValue(Long::class.java) ?: return null
        return Appointment(
            id          = key ?: return null,
            userId      = child("userId").getValue(String::class.java)      ?: "",
            clientName  = child("clientName").getValue(String::class.java)  ?: "",
            clientPhone = child("clientPhone").getValue(String::class.java) ?: "",
            service     = servicio,
            employee    = emp,
            date        = LocalDate.ofEpochDay(diaEpoch),
            startTime   = LocalTime.ofSecondOfDay(segs),
            notes       = child("notes").getValue(String::class.java)       ?: ""
        )
    }
}
