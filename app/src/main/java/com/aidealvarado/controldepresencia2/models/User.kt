package com.aidealvarado.controldepresenciaAPFM.models

import java.io.Serializable

data class User(
    //var objectId: String? = null,
    var displayName: String? = null,
    var email: String? = null,
    var tenant: String? = null,
    var manager: String? = null,
    var estaActivado: Boolean? = null,
    var esGerente: Boolean? = null,
    var uuid:String? = null,
    var avatar:String? = ""
) : Serializable {
    companion object Factory {
        fun create(): User = User()
    }
}
