package com.aidealvarado.controldepresenciaAPFM

object AppConstants {
    const val TAKE_PHOTO_REQUEST: Int = 2
    const val PICK_PHOTO_REQUEST: Int = 1
    val UUID_USER: String = "uuid"
    val DISPLAY_NAME = "displayName"
    val EMAIL = "email"
    val TENANT = "TENANT"
    val MANAGER = "manager"
    val IS_MANAGER = "esGerente"
    val IS_ENABLED = "esActivo"

    val TABLE_USERS = "users"
    val TABLE_TIMERECORDS = "timeRecords"
    val TABLE_TASKS = "tareas"

    val STATUS_PENDING = 0
    val STATUS_CONFIRMED = 1
    val STATUS_REJECTED = 2
    // salvado de estodo de la actividad
    val CLOCK_IN_STATUS ="clockinStatus"
    val TIME_INIT_STATUS ="time_init_status"
    // salvando el API KEY
    val API_KEY = "weatherApiKey"

}