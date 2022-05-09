package com.aidealvarado.controldepresenciaAPFM.viewModels

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

private val TAG = EditUserViewModel::class.java.simpleName

class EditUserViewModel(val app: Application) : AndroidViewModel(app) {

    var firebaseDatabase: FirebaseDatabase? = null
    var BASE_DE_DATOS: DatabaseReference? = null
    var auth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var listaManager = MutableLiveData<MutableList<String>>()
    private lateinit     var appUser:User
    init {
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        BASE_DE_DATOS = firebaseDatabase!!.getReference(AppConstants.TABLE_USERS)
        val query = BASE_DE_DATOS!!
            .child("TENANT")
            .orderByChild("esGerente")
            .equalTo(true)
            .get()
            .addOnSuccessListener { snapshot ->
                //}
                //.addListenerForSingleValueEvent(object:ValueEventListener{
                //  override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange:snapshot.value= ${snapshot.value}")
                val children = snapshot!!.children
                val listaManagers =  arrayListOf<String>()
                listaManagers.add("Manager")
                val usuario = snapshot!!.getValue(User::class.java)
                Log.d(TAG, " usuario $usuario: ")
                Log.d(TAG, "children: ${children} ")
                Log.d(TAG, "onDataChange: ${children.count()}")
                Log.d(
                    TAG, "onDataChange: ${
                        children.map {

                        }
                    }"
                )
                Log.d(TAG, "onDataChange: $children")
                for(hijo in snapshot.children.iterator()){
                    Log.d(TAG, "hijo: $hijo")
                    hijo.child("email").getValue(String::class.java)?.let { listaManagers.add(it)
                        Log.d(TAG, "Añadido: $it")}
                }
                listaManagers.add("--")
                Log.d(TAG, "listaManagers:$listaManagers ")
                listaManager.postValue(listaManagers)

                for(ds in snapshot.children){
                    val esGerente = ds.child("esGerente").getValue(Boolean::class.java)
                    Log.d(TAG, "Esgerente :  $esGerente ")
                }
            }

    }
fun saveUser(activeuser:User, app: AppCompatActivity){
    val query = BASE_DE_DATOS?.child(AppConstants.TENANT)?.orderByChild("uuid")?.equalTo(activeuser.uuid)
    query?.addListenerForSingleValueEvent(object :ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.d(TAG, "onDataChange: actualizando datos ${snapshot.children.count()}")
            Log.d(TAG, "onDataChange: actualizando datos ${snapshot.children}")
            for(ds in snapshot.children){
                Log.d(TAG, "onDataChange: $activeuser")
                ds.ref.child("esGerente").setValue(activeuser.esGerente)
                ds.ref.child("estaActivado").setValue(activeuser.estaActivado)
                ds.ref.child("manager").setValue(activeuser.manager)

            }
            app.onBackPressed()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled: $error")
        }

    } )
}

}
