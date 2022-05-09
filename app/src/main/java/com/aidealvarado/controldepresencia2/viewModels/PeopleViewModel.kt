package com.aidealvarado.controldepresenciaAPFM.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

private val TAG = PeopleViewModel::class.java.simpleName

class PeopleViewModel(val app: Application) : AndroidViewModel(app) {
    var userList = MutableLiveData<MutableList<User>>()
    val database = Firebase.database.reference
    var _taskList: MutableList<User>? = null

    init {

        Log.d(TAG, "Init the class: ")
        var _taskListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: $dataSnapshot")
                loadTaskList(dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "onCancelled: ${error.toException()}")
            }

        }
        database.addValueEventListener(_taskListener)
    }


    private fun loadTaskList(dataSnapshot: DataSnapshot) {
        Log.d(TAG, "loadTaskList: $dataSnapshot")
        val tasks = dataSnapshot.children.iterator()
        if (tasks.hasNext()) {
            _taskList!!.clear()
            val listIndex = tasks.next()
            val itemsIterator = listIndex.children.iterator()
            while (itemsIterator.hasNext()) {
                val currentItem = itemsIterator.next()
                val task = User.create()
                val map = currentItem.value as HashMap<String, Any>

                task.displayName = map.get("displayName") as String?
                task.email = map.get("email") as String?
                task.tenant = map.get("tenant") as String?
                task.manager = map.get("manager") as String?
                task.estaActivado = map.get("estaActivado") as Boolean?
                task.esGerente = map.get("esGerente") as Boolean?
                task.uuid = map.get("uuid") as String?
                _taskList!!.add(task)
            }
        }
        userList.postValue(_taskList)
    }
}
