package com.aidealvarado.controldepresenciaAPFM.viewModels

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.models.TimeRecord
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

private val TAG = ClockInViewModel::class.java.simpleName

class ClockInViewModel(val app: Application) : AndroidViewModel(app) {

    var firebaseDatabase: FirebaseDatabase? = null
    var BASE_DE_DATOS: DatabaseReference? = null
    var auth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var listaTR = MutableLiveData<MutableList<TimeRecord>>()
    private lateinit var appUser: User

    init {
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        BASE_DE_DATOS = firebaseDatabase!!.getReference(AppConstants.TABLE_TIMERECORDS)
        val query = BASE_DE_DATOS!!
            .child(AppConstants.TENANT)
            .orderByChild("userId")
            .equalTo(user?.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                //}
                //.addListenerForSingleValueEvent(object:ValueEventListener{
                //  override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange:snapshot.value= ${snapshot.value}")

            }
    }
}
