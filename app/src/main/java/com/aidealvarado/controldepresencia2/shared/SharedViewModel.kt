package com.aidealvarado.controldepresenciaAPFM.shared

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.Configuracion
import com.aidealvarado.controldepresenciaAPFM.models.TimeRecord
import com.aidealvarado.controldepresenciaAPFM.models.UpdateTimeRecord
import com.aidealvarado.controldepresenciaAPFM.utils.HTTPLogger
import com.aidealvarado.controldepresenciaAPFM.weatherAPI.APIService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

private val TAG = SharedViewModel::class.java.simpleName

class SharedViewModel(val app: Application) : AndroidViewModel(app) {
    val currentTimeRecord = MutableLiveData<TimeRecord>()
    val timeRecords = MutableLiveData<MutableList<TimeRecord>>()
    var lista = arrayListOf<TimeRecord>()
    val CLOCKED_IN = MutableLiveData<Boolean>()
    val CLOCKED_OUT = MutableLiveData<Boolean>()
    val initTime = MutableLiveData<LocalDateTime?>()
    val initDay = MutableLiveData<String>()
    val mutableCityName = MutableLiveData<String>()
    var firebaseDatabase: FirebaseDatabase? = null
    var BASE_DE_DATOS: DatabaseReference? = null
    var auth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var listaManager = MutableLiveData<MutableList<TimeRecord>>()
    var taskId = MutableLiveData<String>()
    var weatherIcon:String = ""
    var weatherDescription:String = ""
    var cityName:String = ""
    init {
        CLOCKED_IN.postValue(false)
        CLOCKED_OUT.postValue(false)
        initTime.postValue(null)

        //
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        BASE_DE_DATOS = firebaseDatabase!!.getReference(AppConstants.TABLE_TIMERECORDS)

        val query = BASE_DE_DATOS!!
            .child(AppConstants.TENANT)
            .orderByChild("userId")
            .equalTo(auth?.currentUser?.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val children = snapshot!!.children
                val lstManager = arrayListOf<TimeRecord>()
                for (hijo in snapshot.children.iterator()) {
                    Log.d(TAG, "hijo: $hijo")
                    Log.d(TAG, "hijo.value: ${hijo.value}")

                    //hijo.getValue(TimeRecord::class.java)?.let { lstManager.add(it) }
                    val timeRecord = TimeRecord(
                        id = hijo.child("id").getValue(String::class.java).toString(),
                        userId = hijo.child("userId").getValue(String::class.java).toString(),
                        day = hijo.child("day").getValue(String::class.java).toString(),
                        clockIn = hijo.child("clockIn").getValue(String::class.java).toString(),
                        clockOut = hijo.child("clockOut").getValue(String::class.java),
                        minutes = hijo.child("minutes").getValue(Int::class.java)
                    )
                    lstManager.add(timeRecord)
                }
                listaManager.postValue(lstManager)
            }
            .addOnFailureListener {
                Log.d(TAG, "init.onFailure: ${it.localizedMessage} ")
            }
    }

    fun refreshData(day: String) {
        Log.d(TAG, "Refrescando datos , $day")
        fakeList(day)
    }

    fun fakeList(day: String) {
        Log.d(TAG, "---> Inicializando lista fake")
        Log.d(TAG, "${timeRecords.value?.size}")
        for (i in 1..10) {
            val timeRecord: TimeRecord = TimeRecord("$i", "usuario$i", day, "9:20", "9:30", i)
            Log.d(TAG, "Añadiendo el registo $i")
            lista.add(timeRecord)
        }
        Log.d(TAG, lista.toString())
        timeRecords.postValue(lista)
        Log.d(TAG, "${timeRecords.value?.size}")
    }

    fun setinitTime(time: LocalDateTime) {
        initTime.postValue(time)
    }

    fun setClockIn(clockedIn: Boolean) {
        Log.d(TAG, "Set clockIn $clockedIn")
        if (clockedIn) {
            CLOCKED_IN.postValue(true)
        } else {
            CLOCKED_IN.postValue(false)
        }
    }

    @OptIn(ExperimentalTime::class)
    fun clockOut() {
        val timeDiff = Duration.between(initTime.value, LocalDateTime.now())
        val tiempoTranscurrido = timeDiff.toKotlinDuration()
        Log.d(
            TAG,
            "Tiempo transcurrido en minutos ${tiempoTranscurrido.toInt(DurationUnit.MINUTES)} segundos ${
                tiempoTranscurrido.toInt(DurationUnit.SECONDS)
            }"
        )
        // Creamos un timeRecord
        val timeEntryUUid = UUID.randomUUID().toString()
        val test = initTime.value?.let {
            val newTimeRecord = TimeRecord(
                timeEntryUUid,
                auth!!.currentUser!!.uid,
                it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                it.format(DateTimeFormatter.ofPattern("HH:mm")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                tiempoTranscurrido.toInt(DurationUnit.MINUTES)
            )
            BASE_DE_DATOS!!
                .child(AppConstants.TENANT)
                .child(timeEntryUUid)
                .setValue(newTimeRecord)
                .addOnSuccessListener {
                    Log.d(TAG, "clockOut: Saved to Firebase $newTimeRecord")

                }.addOnFailureListener {
                    Log.d(TAG, "clockOut: Not inserted, error ${it.localizedMessage}")
                }
            /* Este código es un poco "sucio", pero lo que buscamos es que, en el caso de que el primer
                registro del dia se añada correctamente a la lista, que en el primer caso es nula

                -Queda obsoletada por el uso de Firebase como backend.
             */

            /*
            if (timeRecords.value == null) {
                var emptylist = arrayListOf<TimeRecord>()
                emptylist.add(newTimeRecord)
                timeRecords.postValue(emptylist)
            } else {
                val nTimeRecords = timeRecords.value.let {
                    it?.add(newTimeRecord)
                    timeRecords.postValue(it)
                }

            }
            */
        }
    }

    fun requestTimeChange(
        currentTime: TimeRecord,
        newEntryTime: String,
        newExitTime: String,
        manager: String
    ) {
        var confirmedTime = ""
        var requestStatus = AppConstants.STATUS_PENDING
        val requestTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        var autoAutorizacion = false
        // validamos el manager
        if (manager.isNullOrBlank() || manager.equals("Manager")) {
            // Si no hay manager defindio se "autoAprueba" la peticion.
            Log.d(TAG, "requestTimeChange: Es manager, se auto autoriza")
            autoAutorizacion = true
            confirmedTime = requestTime
            requestStatus = AppConstants.STATUS_CONFIRMED
        }

        val uuidRequest = UUID.randomUUID().toString()

        var requestChangeTimeRecord = UpdateTimeRecord(
            currentTime.id,
            currentTime.userId,
            Configuracion.displayName ?: "--",
            currentTime.day,
            currentTime.clockIn,
            currentTime.clockOut,
            newEntryTime,
            newExitTime,
            currentTime.minutes,
            manager,
            requestTime,
            confirmedTime,
            uuidRequest,
            requestStatus
        )
        //Actualizamos la peticion en la base de datos.
        publicarTarea(requestChangeTimeRecord)
        // si es un manager, se la autoaprueba
        if(autoAutorizacion){
            aprobarCambio(requestChangeTimeRecord)
        }
    }

    fun publicarTarea(updTimeRecord: UpdateTimeRecord) {
        // Publicar la actualizacion en Firebase Realtime database
        var DB = firebaseDatabase!!.getReference(AppConstants.TABLE_TASKS)
        DB.child(AppConstants.TENANT)
            .child(updTimeRecord.requestId!!)
            .setValue(updTimeRecord)
            .addOnSuccessListener {
                Log.d(TAG, "clockOut: Saved to Firebase $updTimeRecord")
                mailToManager(updTimeRecord)
                taskId.postValue("success\t ${updTimeRecord.requestId}")
            }.addOnFailureListener {
                Log.d(TAG, "clockOut: Not inserted, error ${it.localizedMessage}")
                taskId.postValue("error \t ${it.localizedMessage}")
            }
        // Publicar la tarea en Firestore
        var fDb = FirebaseFirestore.getInstance()
            .document("${AppConstants.TABLE_TASKS}/${AppConstants.TENANT}")
        fDb
            .collection(updTimeRecord.managerId.toString())
            .document(updTimeRecord.requestId.toString())
            .set(updTimeRecord)

    }

    private fun mailToManager(updTimeRecord: UpdateTimeRecord) {
        // TODO: Enviar un correo al manager.
    }

    fun updateFirebase(up: UpdateTimeRecord) {
        Log.d(TAG, "updateFirebase: $up ")
        // Actualizamos la entrada en la base de datos correspondiente al update record.
        //
        // si la nueva entrada o salida son nulas, dejamos las que habia en la entrada
        val newEntryTxt = if (up.requestEntry.isNullOrEmpty()) up.clockIn else up.requestEntry
        val newExitTxt = if (up.requestExit.isNullOrEmpty()) up.clockOut else up.requestExit

        val minutosEntrada = horaAMinutos(newEntryTxt ?: "")
        val minutosSalida = horaAMinutos(newExitTxt ?: "")
        val minutos = if (minutosEntrada > minutosSalida) {
            0
        } else {
            minutosSalida - minutosEntrada
        }
        var dB = firebaseDatabase!!.getReference(AppConstants.TABLE_TIMERECORDS)
        val timeRecord = TimeRecord(
            up.id,
            up.userId,
            up.day,
            newEntryTxt,
            newExitTxt,
            minutos
        )
        dB.child(AppConstants.TENANT).child(up.id!!)
            .setValue(timeRecord)
            .addOnSuccessListener {
                Log.d(TAG, "updateFirebase: Actualizado con éxtio")
            }
            .addOnFailureListener {
                Log.d(TAG, "clockOut: Not inserted, error ${it.localizedMessage}")
            }
    }

    /**
     * Se aprueba el cambio, se actualiza la tarea y el registro de firebase
     */
    fun aprobarCambio(updateRecord: UpdateTimeRecord) {
        updateTask(updateRecord, true)
        updateFirebase(updateRecord)
    }

    private fun updateTask(upd: UpdateTimeRecord, approved: Boolean) {
        // Publicar la tarea en Firestore
        val fDb = FirebaseFirestore.getInstance()
            .document("${AppConstants.TABLE_TASKS}/${AppConstants.TENANT}")
        val status = if (approved) {
            AppConstants.STATUS_CONFIRMED
        } else {
            AppConstants.STATUS_REJECTED
        }
        // si la nueva entrada o salida son nulas, dejamos las que habia en la entrada
        val newEntryTxt = if (upd.requestEntry.isNullOrEmpty()) upd.clockIn else upd.requestEntry
        val newExitTxt = if (upd.requestExit.isNullOrEmpty()) upd.clockOut else upd.requestExit

        val minutosEntrada = horaAMinutos(newEntryTxt ?: "")
        val minutosSalida = horaAMinutos(newExitTxt ?: "")
        val minutos = if (minutosEntrada > minutosSalida) {
            0
        } else {
            minutosSalida - minutosEntrada
        }
        val confirmedTimeUpdate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        fDb
            .collection(upd.managerId.toString())
            .document(upd.requestId.toString())
            .set(
                UpdateTimeRecord(
                    upd.id,
                    upd.userId,
                    upd.displayName,
                    upd.day,
                    upd.clockIn,
                    upd.clockOut,
                    newEntryTxt,
                    newExitTxt,
                    minutos,
                    upd.managerId,
                    upd.requestedTimeUpdate,
                    confirmedTimeUpdate,
                    upd.requestId,
                    status
                )
            )
    }

    /**
     * El cambio ha sido denegado por el manager, se almacena en la  bd.
     */
    fun denegarCambio(updateRecord: UpdateTimeRecord) {
        updateTask(updateRecord, false)
    }

    /**
     * genera un entero con -1 si no es una hora válida o
     * el numero de minutos de la jornada si lo es
     */
    fun horaAMinutos(stringHora: String): Int {
        var split = stringHora.split(":")
        if (split.size == 2) {
            return try {
                val hora = split[0].toInt()
                val minutos = split[1].toInt()
                minutos + hora * 60
            } catch (e: Exception) {
                -1
            }
        }
        return -1
    }

    /**
     * Devuelve verdadero si es un string en el formato HH:mm representando una
     * hora válida o una cadena vacía.
     */
    fun validarHoraONulo(hora: String): Boolean {
        if (hora.isNullOrEmpty()) {
            return true
        }
        val split = hora.split(":")
        if (split.size == 2) {
            try {
                val hora = split[0].toInt()
                val minutos = split[1].toInt()
                return !(hora < 0 || hora >= 24 || minutos < 0 || minutos > 60)
            } catch (e: Exception) {
                return false
            }
        } else {
            return true
        }
    }

    /*
    leer los datos de weatherMap
     */
    fun getWeatherData(city:String){
        val retrofit = Retrofit.Builder()
            .baseUrl(Configuracion.baseURL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(HTTPLogger.getLogger())
            .build()
        Configuracion.city = city
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getWeather(city = Configuracion.city,apiKey = Configuracion.apiKey)
            withContext(Dispatchers.Main){
                if (response.isSuccessful) {
                    val items = response.body()?.weather?.first()
                    Log.d(TAG, "getWeatherData: $items")
                    weatherIcon = items?.icon.toString()
                    weatherDescription = items?.description.toString()
                    Log.d(TAG, "getWeatherData: icon = $weatherIcon , description = $weatherDescription")
                    mutableCityName.postValue(Configuracion.city)
                }
            }
        }

    }

    fun getCityName(lat:String, lon:String){

        val retrofit = Retrofit.Builder()
            .baseUrl(Configuracion.baseURL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(HTTPLogger.getLogger())
            .build()

        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getCityName (lat = lat,lon= lon,apiKey = Configuracion.apiKey)
            withContext(Dispatchers.Main){
                if (response.isSuccessful) {

                    val items = response.body()?.weather?.first()
                    Log.d(TAG, "getCityName: $items")
                    cityName = response.body()?.name.toString()
                    Configuracion.city = cityName
                    Log.d(TAG, "getCityName: city $cityName")
                    getWeatherData(city = cityName)
                }
            }
        }
    }
}