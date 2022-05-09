package com.aidealvarado.controldepresenciaAPFM.ui.viewHolders

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.R.*
import com.aidealvarado.controldepresenciaAPFM.models.TimeRecord
import com.aidealvarado.controldepresenciaAPFM.models.UpdateTimeRecord
import kotlin.concurrent.timer

private val TAG = UpdateTimerecord_ViewHoldervar::class.java.simpleName
class UpdateTimerecord_ViewHoldervar (mView: View): RecyclerView.ViewHolder(mView) {
    var personaPidendoCambio = mView.findViewById<TextView>(id.txtPersonaPidiendoCambio)
    var dayTxt = mView.findViewById<TextView>(id.txtDiaDeEntradaCambio)
    var clockInTxt = mView.findViewById<TextView>(id.txtEntradaNuevaHora)
    var clockOutTxt = mView.findViewById<TextView>(id.txtSalidaNuevaHora)
    var timeTxt = mView.findViewById<TextView>(id.timeTXT)
    var imageV = mView.findViewById<ImageButton>(id.imgAcceptUpdateBTN)
    var cancelUpdate = mView.findViewById<ImageButton>(id.imgCancelUpdateBTN)
    lateinit var uuid: String
    var mItemListener : TimeItemListener? = null
    fun setOnClickListener(clickListener: TimeItemListener){
        mItemListener = clickListener
    }
    interface TimeItemListener {
        fun onItemClick(view: View?, position: Int)
        fun onCancelClick(view:View?, position: Int)
    }
    init {
        itemView.setOnClickListener { view -> mItemListener!!.onItemClick(view, adapterPosition) }
        imageV.setOnClickListener { view -> imageV.setOnClickListener { mItemListener!!.onItemClick(view,adapterPosition) } }
        cancelUpdate.setOnClickListener { view -> cancelUpdate.setOnClickListener { mItemListener!!.onCancelClick(view,adapterPosition) } }
    }
    fun setearDatos(context: Context,
                    persona:String,
                    day:String,
                    clockIn:String,
                    clockOut:String,
                    timeRecord: UpdateTimeRecord
    ){
        personaPidendoCambio.text = persona
        dayTxt.text = day
        clockInTxt.text = "${context.getString(string.nueva_entrada_item)}$clockIn"
        clockOutTxt.text = "${context.getString(string.nueva_salida_item)}$clockOut"
        uuid = timeRecord.id!!
        Log.d(TAG, "setearDatos: $timeRecord")
    }
}