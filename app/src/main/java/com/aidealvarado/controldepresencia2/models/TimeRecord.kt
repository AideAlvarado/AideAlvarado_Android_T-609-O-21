package com.aidealvarado.controldepresenciaAPFM.models

import java.io.Serializable

data class TimeRecord(
    val id:String? = null,
    val userId: String? = null,
    val day: String? = null,
    val clockIn: String? = null,
    val clockOut: String? = null,
    val minutes: Int? = null
) : Serializable {
    companion object Factory {
        fun create(): TimeRecord = TimeRecord()
    }
}
