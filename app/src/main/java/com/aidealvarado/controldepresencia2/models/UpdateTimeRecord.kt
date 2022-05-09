package com.aidealvarado.controldepresenciaAPFM.models

import java.io.Serializable

data class UpdateTimeRecord(
    val id:String? = null,
    val userId: String? = null,
    val displayName:String? = null,
    val day: String? = null,
    val clockIn: String? = null,
    val clockOut: String? = null,
    val requestEntry: String? = null,
    val requestExit: String? = null,
    val minutes: Int? = null,
    val managerId:String? = null,
    val requestedTimeUpdate:String? = null,
    val confirmedTimeUpdate:String?= null,
    val requestId:String? = null,
    val status: Int? = null
) : Serializable {
    companion object Factory {
        fun create(): UpdateTimeRecord = UpdateTimeRecord()
    }
}
