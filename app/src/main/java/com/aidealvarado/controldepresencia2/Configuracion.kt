package com.aidealvarado.controldepresenciaAPFM

import android.net.Uri
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.RemoteConfigComponent
import java.time.LocalDateTime

object Configuracion {
    val IS_MANAGER ="IS_MANAGER"
    lateinit var user: FirebaseUser
    val TENANT = "TENANT"
    var userId: String? = null
    var userEmail: String? = null
    var displayName: String? = null
    var userDetail: User? = null
    var isManager: Boolean = false
    var authority: String = ""
    var clockedIn: Boolean = false
    var initTime: LocalDateTime? = null
    var apiKey:String = ""
    var remoteConfig: FirebaseRemoteConfig? = null
    var baseURL:String = "https://api.openweathermap.org/"
    var city:String = "Madrid"
    var permisosGPS: Boolean = false
    var profileUri:Uri? = null
}