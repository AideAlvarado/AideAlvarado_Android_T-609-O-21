package com.aidealvarado.controldepresenciaAPFM.ui.bindingAdapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.w3c.dom.Text

@BindingAdapter("duracion")
fun minutosAduracion(view:TextView,value:Int){
    val horas = value / 60
    val minutos = value % 60
    view.text = "$horas:$minutos"

}