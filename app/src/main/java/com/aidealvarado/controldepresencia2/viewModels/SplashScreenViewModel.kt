package com.aidealvarado.controldepresenciaAPFM.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.Configuracion
import com.aidealvarado.controldepresenciaAPFM.models.TimeRecord
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

private val TAG = SplashScreenViewModel::class.java.simpleName
class SplashScreenViewModel(val app: Application) : AndroidViewModel(app) {
    var firebaseDatabase: FirebaseDatabase? = null
    var BASE_DE_DATOS: DatabaseReference? = null
    var auth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var listaTR = MutableLiveData<MutableList<TimeRecord>>()
    private lateinit var appUser: User
    var remoteConfig: FirebaseRemoteConfig? = null
    var apiKey:String = ""
    init {
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig!!.setConfigSettingsAsync(configSettings)
        remoteConfig!!.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updated = task.result
                remoteConfig!!.activate()
                apiKey = remoteConfig!!.get(key = "weatherApiKey").toString()
                Log.d(TAG, "remoteConfig: $apiKey")
                Configuracion.apiKey = apiKey
                Configuracion.remoteConfig = remoteConfig
            } else {
                Log.e(TAG, "Error recuperando la configuracion: ${task.exception}", )
            }

        }
    }
}