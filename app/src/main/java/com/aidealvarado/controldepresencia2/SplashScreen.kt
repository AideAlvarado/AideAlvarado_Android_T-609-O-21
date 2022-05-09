package com.aidealvarado.controldepresenciaAPFM

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.aidealvarado.controldepresenciaAPFM.shared.SharedViewModel
import com.aidealvarado.controldepresenciaAPFM.viewModels.SplashScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.log
private val TAG = SplashScreen::class.java.simpleName
class SplashScreen : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var viewModel:SplashScreenViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        firebaseAuth = FirebaseAuth.getInstance()
        // Leemos el valor del API_KEY
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val apiKey = sharedPref.getString(AppConstants.API_KEY, "-").toString().also {
            Configuracion.apiKey = it
            Log.d(TAG, "onCreate: apiKey leida de las preferencias locales $it")
        }
        // La persistencia en Firebase se debe realizar una única vez en la actividad inicial
        Firebase.database.setPersistenceEnabled(true)

        // Inicializamos el viewModel de la vista, inicialmente nos permite recuperar los valores
        // de configuracion remota (apiKey)
        viewModel = ViewModelProvider(this).get(SplashScreenViewModel::class.java)
        val tiempo = 3000
        Handler().postDelayed({
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        },tiempo.toLong())
    }
}