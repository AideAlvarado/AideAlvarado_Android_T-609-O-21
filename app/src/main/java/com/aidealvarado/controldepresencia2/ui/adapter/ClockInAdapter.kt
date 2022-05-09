package com.aidealvarado.controldepresenciaAPFM.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.models.TimeRecord

private val TAG = ClockInAdapter::class.java.simpleName

class ClockInAdapter(
    val context: Context,
    val timeRecords: List<TimeRecord>,
    val itemListener: ClockInItemListener
) : RecyclerView.Adapter<ClockInAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val day: TextView = itemView.findViewById<TextView>(R.id.dayTtxt) //día del registro
        val clockIn: TextView = itemView.findViewById<TextView>(R.id.clockInTxt) // hora dle clockin
        val clockOut: TextView = itemView.findViewById<TextView>(R.id.clockOutTXT)
        val time: TextView = itemView.findViewById<TextView>(R.id.timeTXT)
        val editButton: ImageButton = itemView.findViewById<ImageButton>(R.id.imageButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = R.layout.time_layout_item
        val view = inflater.inflate(layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeRecord = timeRecords[position]
        with(holder) {
            day.text = timeRecord.day
            clockIn.text = timeRecord.clockIn
            clockOut.text = timeRecord.clockOut.toString()
            time.text = timeRecord.minutes.toString()
            editButton.setOnClickListener {
                itemListener.onEditButtonSelected(timeRecord)
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "Numero de ítems: ${timeRecords.size}")
        return timeRecords.size
    }

    interface ClockInItemListener {
        fun onEditButtonSelected(timeRecord: TimeRecord)
    }
}