package com.aidealvarado.controldepresenciaAPFM.ui.viewHolders

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.models.TimeRecord
import com.aidealvarado.controldepresenciaAPFM.models.User
import kotlinx.android.synthetic.main.people_item.view.*
private val TAG = TimeRecord_ViewHolder::class.java.simpleName
class TimeRecord_ViewHolder(var mView: View):RecyclerView.ViewHolder(mView) {
    var dayTxt = mView.findViewById<TextView>(R.id.dayTtxt)
    var clockInTxt = mView.findViewById<TextView>(R.id.clockInTxt)
    var clockOutTxt = mView.findViewById<TextView>(R.id.clockOutTXT)
    var timeTxt = mView.findViewById<TextView>(R.id.timeTXT)
    var imageV = mView.findViewById<ImageButton>(R.id.imageButton)
    lateinit var uuid: String
    var mItemListener : TimeItemListener? = null
    fun setOnClickListener(clickListener: TimeItemListener){
        mItemListener = clickListener
    }
    interface TimeItemListener {
        fun onItemClick(view: View?, position: Int)

    }
    init {
        itemView.setOnClickListener { view -> mItemListener!!.onItemClick(view, adapterPosition) }
        imageV.setOnClickListener { view -> imageV.setOnClickListener { mItemListener!!.onItemClick(view,adapterPosition) } }

    }
    fun setearDatos(context: Context,
                    day:String,
                    clockIn:String,
                    clockOut:String,
                    time:String,
                    timeRecord:TimeRecord ){
        dayTxt.text = day
        clockInTxt.text = clockIn
        clockOutTxt.text = clockOut
        timeTxt.text = time
        uuid = timeRecord.id!!
        Log.d(TAG, "setearDatos: $timeRecord")
    }
}