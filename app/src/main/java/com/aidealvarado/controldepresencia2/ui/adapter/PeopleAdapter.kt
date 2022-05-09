package com.aidealvarado.controldepresenciaAPFM.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.aidealvarado.controldepresenciaAPFM.ui.PeopleFragment
import com.bumptech.glide.Glide

private val TAG = PeopleAdapter::class.java.simpleName

class PeopleAdapter(
    val context: Context,
    val peopleList: MutableList<User>,
    val itemListener: PeopleFragment
) : RecyclerView.Adapter<PeopleAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val displayName: TextView = itemView.findViewById<TextView>(R.id.txtDisplayName)
        val isManagerSW: SwitchCompat = itemView.findViewById(R.id.isManagerSWCHT)
        val isEnabledSW: SwitchCompat = itemView.findViewById(R.id.isEnabledSWCH)
        val imageView : ImageView = itemView.findViewById(R.id.detailUserAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val layout = R.layout.people_item
        val view = inflater.inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = peopleList[position]
        Log.d(TAG, "onBindViewHolder: person")
        with(holder) {
            displayName.text = person.displayName
            isManagerSW.isChecked = person.esGerente ?: false
            isEnabledSW.isChecked = person.estaActivado ?: false
            if (person.avatar.isNullOrEmpty()) {
                imageView.setImageResource(R.drawable.ic_user_avatar_svgrepo_com)
            } else {
                Glide.with(context).load(person.avatar).into(imageView)
            }
            isManagerSW.setOnClickListener {
             // itemListener.onIsManagerSwitch(person)
            }
            isEnabledSW.setOnClickListener {
               // itemListener.onIsEnabledSwitch(person)

            }
        }
    }

    override fun getItemCount(): Int = peopleList.size

    interface PeopleItemListener {
        fun onIsManagerSwitch(peopleRecord: User)
        fun onIsEnabledSwitch(peopleRecord: User)
    }
}