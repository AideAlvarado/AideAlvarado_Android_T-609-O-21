package com.aidealvarado.controldepresenciaAPFM.models

typealias GeoCoder = ArrayList<WelcomeElement>

data class WelcomeElement (
    val name: String,
    val localNames: Map<String, String>,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String
)