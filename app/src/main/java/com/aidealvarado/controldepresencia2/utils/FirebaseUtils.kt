package com.aidealvarado.controldepresenciaAPFM.utils

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.Configuracion
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.aidealvarado.controldepresenciaAPFM.viewModels.LoginRegisterViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

private val TAG = Firebase::class.java.simpleName

object FirebaseUtils {
    private var database: DatabaseReference = Firebase.database.reference

    fun setEnvironmentFromFirebaseAuth(currentUser: FirebaseUser) {
        Log.d(TAG, "setEnvironmentFromFirebaseAuth: $currentUser")
        Configuracion.userId = currentUser.uid
        Configuracion.userEmail = currentUser.email.toString()
        Configuracion.user = currentUser
        Configuracion.displayName = currentUser.displayName
        database
            .child(AppConstants.TABLE_USERS)
            .child(Configuracion.TENANT)
            .child(Configuracion.userEmail!!.replace(".", "_"))
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "setEnvironmentFromFirebaseAuth: Read item $it ${it.value}")
                Log.d(TAG, "setEnvironmentFromFirebaseAuth: ${it.childrenCount}")
                val user = it.getValue(User::class.java)
                Log.d(TAG, "setEnvironmentFromFirebaseAuth: $user")
                Configuracion.isManager = user?.esGerente ?: false
                Log.d(TAG, "setEnvironmentFromFirebaseAuth: isManager ${Configuracion.isManager}")
                Configuracion.userDetail = user
                
            }
            .addOnFailureListener {
                Log.e(TAG, "setEnvironmentFromFirebaseAuth: Error $it")
            }


    }
/*
    fun login(
        username: String,
        password: String,
        auth: FirebaseAuth,
        context: FragmentActivity
    ): Boolean {
        var viewModel = ViewModelProvider(context).get(LoginRegisterViewModel::class.java)
        var returnCode = false
        if (username.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(username, password)
                .addOnSuccessListener {
                    Log.d(TAG, "login: ${it.user}")
                    setEnvironmentFromFirebaseAuth(it.user!!)
                    Log.d(TAG, "login: ${it.additionalUserInfo}")
                    Log.d(TAG, "login: ${it.credential}")
                    auth.currentUser?.let { it1 -> setEnvironmentFromFirebaseAuth(it1) }
                    viewModel.user.postValue(username)
                    viewModel.isRegistered.postValue(true)
                    viewModel.isLogged.postValue(true)
                    returnCode = true
                }
                .addOnFailureListener {
                    Log.e(TAG, "login: $it")
                    returnCode = false
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }


        } else if (username.isEmpty()) {
            Toast.makeText(context, "Username is Empty", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(context, "Password is Empty", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(TAG, "login: This should not happen  Username>$username< Password >$password<")
        }
        return returnCode
    }

 */

    private fun uploadImageToFirebase(fileUri: Uri) {
        if (fileUri != null) {
            val fileName = UUID.randomUUID().toString() +".jpg"

            val database = FirebaseDatabase.getInstance()
            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")

            refStorage.putFile(fileUri)
                .addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            val imageUrl = it.toString()
                        }
                    })

                ?.addOnFailureListener(OnFailureListener { e ->
                    print(e.message)
                })
        }
    }
}