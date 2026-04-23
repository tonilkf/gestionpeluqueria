package com.pelucitas.app.data.repository

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pelucitas.app.data.model.Employee
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EmpleadoRepository {

    private val raiz = FirebaseDatabase.getInstance().getReference("empleados")

    /** Empleados activos — actualizados en tiempo real. */
    val empleados: Flow<List<Employee>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                trySend(snap.children.mapNotNull { it.toEmployee() }.filter { it.activo })
            }
            override fun onCancelled(error: DatabaseError) = close(error.toException())
        }
        raiz.addValueEventListener(listener)
        awaitClose { raiz.removeEventListener(listener) }
    }

    /** Crea cuenta Auth + nodo Firebase para el empleado. */
    suspend fun crear(emp: Employee, contrasena: String) {
        val appPrincipal = FirebaseApp.getInstance()
        val appSec = try {
            FirebaseApp.getInstance("sec")
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(appPrincipal.applicationContext, appPrincipal.options, "sec")!!
        }
        val authSec = FirebaseAuth.getInstance(appSec)
        val email = "${emp.alias}@pelucitas.internal"
        val resultado = authSec.createUserWithEmailAndPassword(email, contrasena).await()
        val nuevoUid = resultado.user?.uid ?: return
        authSec.signOut()

        raiz.child(emp.alias).setValue(emp.copy(uid = nuevoUid).toMap()).await()
        FirebaseDatabase.getInstance()
            .getReference("users/$nuevoUid")
            .setValue(mapOf("rol" to "empleado", "empleadoId" to emp.alias))
            .await()
    }

    /** Actualiza datos de un empleado (sin tocar Auth). */
    suspend fun actualizar(emp: Employee) {
        raiz.child(emp.alias).setValue(emp.toMap()).await()
    }

    /** Soft-delete: marca como inactivo. */
    suspend fun desactivar(alias: String) {
        raiz.child(alias).child("activo").setValue(false).await()
    }

    // ── Conversiones ──────────────────────────────────────────────────────────

    fun DataSnapshot.toEmployee(): Employee? = Employee(
        uid          = child("uid").getValue(String::class.java)          ?: "",
        nombre       = child("nombre").getValue(String::class.java)       ?: return null,
        apellidos    = child("apellidos").getValue(String::class.java)    ?: "",
        telefono     = child("telefono").getValue(String::class.java)     ?: "",
        alias        = child("alias").getValue(String::class.java)        ?: key ?: return null,
        genero       = child("genero").getValue(String::class.java)       ?: "M",
        photoUrl     = child("photoUrl").getValue(String::class.java)     ?: "",
        especialidad = child("especialidad").getValue(String::class.java) ?: "",
        activo       = child("activo").getValue(Boolean::class.java)     ?: true
    )

    private fun Employee.toMap() = mapOf(
        "uid"          to uid,
        "nombre"       to nombre,
        "apellidos"    to apellidos,
        "telefono"     to telefono,
        "alias"        to alias,
        "genero"       to genero,
        "photoUrl"     to photoUrl,
        "especialidad" to especialidad,
        "activo"       to activo
    )
}
