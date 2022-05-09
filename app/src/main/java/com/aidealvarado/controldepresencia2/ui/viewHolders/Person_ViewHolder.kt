package com.aidealvarado.controldepresenciaAPFM.ui.viewHolders

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.people_item.view.*
import java.net.URI

private val TAG = Person_ViewHolder::class.java.simpleName
class Person_ViewHolder(var mView: View):RecyclerView.ViewHolder(mView) {


    var displayName_lbl = mView.findViewById<TextView>(R.id.txtDisplayName)
    var isManager_sw = mView.findViewById<TextView>(R.id.isManagerSWCHT)
    var isEnabled_sw = mView.findViewById<TextView>(R.id.isEnabledSWCH)
    var avatarImg = mView.findViewById<ImageView>(R.id.detailUserAvatar)
    lateinit var uuid: String
    var mItemListener : PeopleItemListener? = null
    fun setOnClickListener(clickListener: PeopleItemListener){
        mItemListener = clickListener
    }
    interface PeopleItemListener {
        fun onItemClick(view: View?, position: Int)
        fun onItemLongClick(view: View?, position: Int)
    }
    init {
        itemView.setOnClickListener { view -> mItemListener!!.onItemClick(view, adapterPosition) }
        itemView.setOnLongClickListener { view ->
            mItemListener!!.onItemLongClick(view, adapterPosition)
            false
        }

    }
    fun setearDatos(context: Context, displayName:String, isManager:Boolean, isEnabled:Boolean, user:User ){
        displayName_lbl.text = displayName
        isManager_sw.isEnabled = isManager
        isEnabled_sw.isEnabled = isEnabled
        uuid = user.uuid.toString()
        if(!user.avatar.isNullOrEmpty()) {
            Glide.with(context).load(user.avatar).into(avatarImg)
        }
        Log.d(TAG, "setearDatos: $user")
    }
}