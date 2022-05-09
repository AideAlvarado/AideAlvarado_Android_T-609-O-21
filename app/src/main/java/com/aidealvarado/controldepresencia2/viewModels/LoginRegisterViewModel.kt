package com.aidealvarado.controldepresenciaAPFM.viewModels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.Configuracion
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.models.Pictures
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.aidealvarado.controldepresenciaAPFM.utils.FirebaseUtils
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.applyConnectionSpec
import org.w3c.dom.Text
import java.io.File
import java.util.*
import kotlin.math.log


private val TAG = LoginRegisterViewModel::class.java.simpleName

class LoginRegisterViewModel(val app: Application) : AndroidViewModel(app) {
    var user = MutableLiveData<String>()
    var isRegistered = MutableLiveData<Boolean>()
    var isLogged = MutableLiveData<Boolean>()
    var userDetail = MutableLiveData<User>()
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var mUsuario:String
    private lateinit var mPassword:String
    private var _downLoadUri: Uri? = null

    init {
        storageReference = FirebaseStorage.getInstance().reference
    }

    companion object {
        val auth = FirebaseAuth.getInstance()
    }

    fun validatePassword(name: EditText, pwd1: EditText, pwd2: EditText, context: Context): Boolean {
        if (name.text.toString().isEmpty()) {
            Log.d(TAG, "Name is  empty ")
            Toast.makeText(context, context.getString(R.string.name_cant_be_empty), Toast.LENGTH_SHORT).show()
            return false
        }
        return if (pwd1.text.toString() == pwd2.text.toString()) {
            true
        } else {
            Log.d(TAG, "Password1 :${pwd1.text} is different to : ${pwd2.text}")
            Toast.makeText(context, context.getString(R.string.both_passwords_must_be_the_same), Toast.LENGTH_SHORT).show()
            false
        }
    }

    //Registramos un usuario con el valor de displayName, email y password ,
    fun registerUser(displayName: EditText, name: EditText, pwd: EditText, imageView: ImageView) {
        var returnCode: Boolean = false
        var request: UserProfileChangeRequest
        Log.d(TAG, "registerUser: ${displayName.text} , ${name.text}, pwd: ${pwd.text} ")
        mUsuario = name.text.toString()
        mPassword = pwd.text.toString()
        Log.d(TAG, "registerUser: usuario:$mUsuario:, password:$mPassword:")
         auth.createUserWithEmailAndPassword(mUsuario, mPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "registerUser: Registrado con éxito")
                    val fUser = auth.currentUser!!
                    Log.d(TAG, "registerUser: ${fUser.email} , ${fUser.uid}")
                    Configuracion.user = auth.currentUser!!
                    val userUUID = auth.currentUser!!.uid
                    user.postValue(Configuracion.user.email)
                    isRegistered.postValue(true)
                    if (hasNullOrEmptyDrawable(imageView)) {
                        val bitmap = imageView.drawable.toBitmap()
                        saveImage(bitmap, userUUID)
                        val uri = uploadPhotoProfile(userUUID)
                        if (_downLoadUri != null) {
                            Log.d(TAG, "registerUser: download uri not found ")
                            request = UserProfileChangeRequest.Builder().setDisplayName(displayName.text?.toString()).build()
                        }
                        else
                        {
                            Log.d(TAG, "registerUser: There is a photo, but not download uri yet")
                            request = UserProfileChangeRequest.Builder().setDisplayName(displayName.text?.toString()).build()
                        }
                    } else {
                        Log.d(TAG, "registerUser: No photo yet")
                        request = UserProfileChangeRequest.Builder().setDisplayName(displayName.text?.toString()).build()
                    }
                    if (request != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                auth.currentUser?.updateProfile(request)
                                Log.d(TAG, "Updated displayName")
                            } catch (e: Exception) {
                                Log.e(TAG, "Exception :$e")
                            }
                        }
                    }
                    createUserInTable(displayName.text.toString(), name.text.toString(), Configuracion.TENANT, userUUID)
                    setEnvironmentFromFirebaseAuth(auth.currentUser!!)
                } else {
                    Log.e(TAG, "registerUser: Failed ${task.exception}")
                    isRegistered.postValue(returnCode)
                }
            }
    }

    fun setEnvironmentFromFirebaseAuth(currentUser: FirebaseUser) {
        Log.d(TAG, "setEnvironmentFromFirebaseAuth: >>${currentUser.email}<<")
        Configuracion.userId = currentUser.uid
        Log.d(TAG, "setEnvironmentFromFirebaseAuth: currentUserId:${Configuracion.userId}")
        Configuracion.userEmail = currentUser.email.toString()
        Log.d(TAG, "setEnvironmentFromFirebaseAuth: email >>${Configuracion.userEmail}<<")
        Configuracion.user = currentUser
        Configuracion.displayName = currentUser.displayName

        Log.d(TAG, "setEnvironmentFromFirebaseAuth: Configuracion :${Configuracion}")
        Log.d(TAG, "setEnvironmentFromFirebaseAuth:uid ${currentUser.uid}")
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val BASE_DE_DATOS = firebaseDatabase!!.getReference("users")
        val query = BASE_DE_DATOS!!
            .child("TENANT")
            .orderByChild("uuid")
            .equalTo(currentUser.uid)
            .get()
            .addOnSuccessListener {
                for(hijo in it.children.iterator()){
                    Log.d(TAG, "setEnvironmentFromFirebaseAuth: hijo $hijo")
                    val displayName = hijo.child("displayName").getValue(String::class.java)
                    val email = hijo.child("email").getValue(String::class.java)
                    val isEnabled = hijo.child("estaActivado").getValue(Boolean::class.java)
                    val isManager = hijo.child("esGerente").getValue(Boolean::class.java)
                    val manager = hijo.child("manager").getValue(String::class.java)
                    val tenant = hijo.child("tenant").getValue(String::class.java)
                    val uuid = hijo.child("uuid").getValue(String::class.java)
                    val avatar = hijo.child("avatar").getValue(String::class.java)
                    Log.d(TAG, "setEnvironmentFromFirebaseAuth: $displayName, $email,$isEnabled,$isManager, $manager, $uuid , $avatar")
                    Log.d(TAG, "setEnvironmentFromFirebaseAuth: Read item $it ${it.value}")
                   // Log.d(TAG, "setEnvironmentFromFirebaseAuth: ${it.childrenCount}")
                    var user = hijo.getValue(User::class.java)
                    Log.d(TAG, "setEnvironmentFromFirebaseAuth: user from getValue $user")
                    Log.d(TAG, "setEnvironmentFromFirebaseAuth: $hijo")
                    Configuracion.isManager = user?.esGerente ?: false
                    Log.d(TAG, "setEnvironmentFromFirebaseAuth: isManager ${Configuracion.isManager}")
                    Configuracion.userDetail = user
                    userDetail.postValue(user!!)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "setEnvironmentFromFirebaseAuth: Error $it")
            }
    }


    fun loginUser(name:EditText, passwordCtl:EditText,context: FragmentActivity){

        val username = name.text.toString()
        val password = passwordCtl.text.toString()
        Log.d(TAG, "loginUser: >>$username<< >>$password<<")
        //
        if (username.isNotEmpty() && password.isNotEmpty()) {
            Log.d(TAG, "loginUser: username & password not empty")
            auth.signInWithEmailAndPassword(username, password)
                .addOnSuccessListener {
                    Log.d(TAG, "login user  logeado con éxito: ${it.user}")
                    setEnvironmentFromFirebaseAuth(it.user!!)
                    Log.d(TAG, "login: aditional user info ${it.additionalUserInfo}")
                    Log.d(TAG, "login: ${it.credential}")
                    val email = it.user?.email
                    val extendedName = it.user?.displayName
                    val photoURL = it.user?.photoUrl
                    Log.d(TAG, "loginUser: email $email, disp: $extendedName , pht: $photoURL")
                    Log.d(TAG, "loginUser: username = $username")
                    user.postValue(username)
                    isRegistered.postValue(true)
                    isLogged.postValue(true)
                    auth.currentUser?.let { it1 ->
                        Log.d(TAG, "loginUser: $it1")
                        setEnvironmentFromFirebaseAuth(it1) }
                    userDetail
                }
                .addOnFailureListener {
                    Log.e(TAG, "login: failure ${it.localizedMessage}")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }


        } else if (username.isEmpty()) {
            Toast.makeText(context, "Username is Empty", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(context, "Password is Empty", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(TAG, "login: This should not happen  Username>$username< Password >$password<")
        }
        //
    }

    private fun uploadPhotoProfile(imageName: String) {
        Log.d(TAG, "uploadPhotoProfile: $imageName")
        //uploadImage(Uri.fromFile(File(app.cacheDir, imageName + ".png")))
        val fileUri = Uri.fromFile(File(app.cacheDir, imageName + ".png"))
        uploadImageForProfile(filePath = fileUri,user = auth.currentUser!!)
    }

    private fun updatePhoto(imageView: ImageView) {

    }

    fun createUserInTable(displayName: String, email: String, tenant: String, uid:String) {
        /*Creamos una tabla con los valores indicados para la administracion de los timeRecords
            Los usuarios se crean inicialmente sin adjudicarles un manager, en la organizacion
            definida en Configuration.TENANT,
            deshabilitados --> Hasta que un manager o administrador de la aplicacion los habilite
                no podran hacer nada
            isManager -> Por defecto no serán managers.

         */

        database = Firebase.database.reference
        val user = User(displayName,
            email,
            Configuracion.TENANT,
            "-",
            false,
            false,
            uid )
        Log.d(TAG, "createUserInTable: $user")
        database.child("users")
            .child(Configuracion.TENANT)
            .child(email.replace(".", "_"))
            .setValue(user)
            .addOnSuccessListener {

                Log.d(TAG, "Successfully Inserted $user")
            }.addOnFailureListener {
                Log.d(TAG, "Not Inserted $it")
            }
    }

    fun saveImageRecord(uid:String,urlPicture:String)
    {
        Log.d(TAG, "saveImageRecord: uid $uid,picture  $urlPicture")
        database = Firebase.database.reference
        val picture = Pictures(urlPicture,uid    )

        Log.d(TAG, "saveImageRecor: $picture")
        database.child("pictures")
            .child(Configuracion.TENANT)
            .child(uid)
            .setValue(picture)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully Inserted  picture $picture")
            }.addOnFailureListener {
                Log.d(TAG, "Not Inserted $it")
            }
    }
    fun hasNullOrEmptyDrawable(iv: ImageView): Boolean {
        val drawable: Drawable?
        drawable = iv.drawable
        return ((drawable != null) && (drawable is BitmapDrawable))
    }
    fun saveImage(bitmap: Bitmap, filename: String) {
        val file_path = app.cacheDir.path
        Log.d(TAG, "saveImage: $file_path")
        val dir = File(file_path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(app.cacheDir, filename + ".png")
        Log.d(TAG, "saveImage: file $file")
        file.writeBitmap(bitmap, Bitmap.CompressFormat.PNG, 85)

    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun addUploadRecordToDb(uri: String) {
        val db = FirebaseFirestore.getInstance()
        val data = HashMap<String, Any>()
        data["imageUrl"] = uri
        Log.d(TAG, "addUploadRecordToDb: URI $uri ")
        db.collection("posts")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "addUploadRecordToDb: Saved to DB $documentReference")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addUploadRecordToDb: Error saving to DB: ${e.localizedMessage}")
            }
    }

    private fun uploadImage(filePath: Uri) {
        val ref = storageReference.child("uploads/" + UUID.randomUUID().toString())
        Log.d(TAG, "uploadImage: storageReference $storageReference")
        val uploadTask = ref.putFile(filePath)
        Log.d(TAG, "uploadImage: uploadtask $uploadTask , filePath: $filePath")
        val urlTask =
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }

                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    _downLoadUri = downloadUri
                    addUploadRecordToDb(downloadUri.toString())
                } else {
                    // Handle failures
                }
            }.addOnFailureListener {

            }
    }

    private fun uploadImageForProfile(filePath: Uri,user:FirebaseUser) {
        val ref = storageReference.child("uploads/" + UUID.randomUUID().toString())
        Log.d(TAG, "uploadImage: storageReference $storageReference")
        val uploadTask = ref.putFile(filePath)
        Log.d(TAG, "uploadImage: uploadtask $uploadTask , filePath: $filePath")
        val urlTask =
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                val downloadUrl = task.result.storage.downloadUrl.addOnSuccessListener {
                    val v = UserProfileChangeRequest.Builder()
                        .setPhotoUri(it)
                        .build()
                    Configuracion.profileUri = it
                    Log.d(TAG, "uploadImageForProfile: ${v.photoUri}")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            auth.currentUser?.updateProfile(v)
                            saveImageRecord(user.uid ,it.toString())
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception :$e")
                        }
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "uploadImageForProfile: ${it.localizedMessage}", )
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    _downLoadUri = downloadUri
                    addUploadRecordToDb(downloadUri.toString())
                } else {
                    Log.e(TAG, "uploadImageForProfile: error ${task.exception?.localizedMessage}", )
                }
            }.addOnFailureListener {
                Log.e(TAG, "uploadImageForProfile: ${it.localizedMessage}", )
            }
    }
}