package com.aidealvarado.controldepresenciaAPFM.models

import java.io.Serializable

data class Pictures(
    var profilePicture:String? = "",
    var userName:String? = ""
): Serializable {
    companion object Factory {
        fun create(): User = User()
    }
}
