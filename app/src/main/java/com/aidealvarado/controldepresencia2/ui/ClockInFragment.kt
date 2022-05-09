package com.aidealvarado.controldepresenciaAPFM.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.Configuracion
import com.aidealvarado.controldepresenciaAPFM.Configuracion.apiKey
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.databinding.FragmentClockInBinding
import com.aidealvarado.controldepresenciaAPFM.models.TimeRecord
import com.aidealvarado.controldepresenciaAPFM.shared.SharedViewModel
import com.aidealvarado.controldepresenciaAPFM.ui.adapter.ClockInAdapter
import com.aidealvarado.controldepresenciaAPFM.ui.viewHolders.TimeRecord_ViewHolder
import com.aidealvarado.controldepresenciaAPFM.viewModels.LoginRegisterViewModel
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.ObservableSnapshotArray
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.android.synthetic.main.fragment_clock_in.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.jar.Manifest


private val TAG = ClockInFragment::class.java.simpleName

class ClockInFragment : Fragment(), ClockInAdapter.ClockInItemListener {
    private val args: ClockInFragmentArgs by navArgs<ClockInFragmentArgs>()
    private lateinit var binding: FragmentClockInBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: SharedViewModel
    private lateinit var managerOptionMenu: MenuItem
    private lateinit var loginViewModel: LoginRegisterViewModel
    private lateinit var firebaseDatabase: FirebaseDatabase
    var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<TimeRecord, TimeRecord_ViewHolder>? = null
    var options: FirebaseRecyclerOptions<TimeRecord>? = null
    private lateinit var BASE_DE_DATOS: DatabaseReference
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var adapter: ClockInAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    // Posicion
    private  lateinit var fusedLocationClient: FusedLocationProviderClient

    // variables para recuperar las cadenas de texto
    private lateinit var clockedInStatus: String
    private lateinit var timeInitStr: String
    private var tiempoTotal: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (requireActivity() as AppCompatActivity).run {
            supportActionBar?.setDisplayHomeAsUpEnabled(
                false
            )
        }

        setHasOptionsMenu(true)
        binding = FragmentClockInBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        loginViewModel =
            ViewModelProvider(requireActivity()).get(LoginRegisterViewModel::class.java)
        binding.weatherDescription.text = ""
        viewModel.getWeatherData(city = Configuracion.city)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getPosition()
        recyclerView = binding.clkRecyclerView
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        Log.d(TAG, "onCreateView: ${user.email}")
        Log.d(TAG, "onCreateView: avatar foto ${user.photoUrl}")
        firebaseDatabase = FirebaseDatabase.getInstance()
        BASE_DE_DATOS = firebaseDatabase.getReference(AppConstants.TABLE_TIMERECORDS)

        val query = BASE_DE_DATOS
            .child(AppConstants.TENANT)
            .orderByChild("userId")
            .equalTo(user.uid)

        Log.d(TAG, "onCreateView: $query")
        options =
            FirebaseRecyclerOptions.Builder<TimeRecord>()
                .setQuery(query, TimeRecord::class.java)
                .build()

        firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<TimeRecord, TimeRecord_ViewHolder>(options!!) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): TimeRecord_ViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.time_layout_item, parent, false)
                    val viewHoder_nota = TimeRecord_ViewHolder(view)
                    viewHoder_nota.setOnClickListener(object :
                        TimeRecord_ViewHolder.TimeItemListener {
                        override fun onItemClick(view: View?, position: Int) {
                            val action =
                                ClockInFragmentDirections.toUpdateTimeRecord(getItem(position))
                            findNavController().navigate(action)
                        }
                    })
                    return viewHoder_nota
                }

                override fun onBindViewHolder(
                    holder: TimeRecord_ViewHolder,
                    position: Int,
                    timeRecord: TimeRecord
                ) {
                    val minutos = timeRecord.minutes ?: 0
                    val minutosStr = if(minutos>=0){ "${minutos / 60 }:${minutos % 60}" } else {"0"}
                    tiempoTotal += minutos
                    binding.checkBox2.text = if(tiempoTotal>0 ){
                        "Tiempo trabajado ${tiempoTotal / 60}:${tiempoTotal % 60}"
                    } else "Tiempo trabajado ..."
                    Log.d(TAG, "onBindViewHolder: minutosStr $minutosStr")
                    holder.setearDatos(
                        requireContext(),
                        timeRecord.day!!,
                        timeRecord.clockIn!!,
                        timeRecord.clockOut.toString(),
                        minutosStr,
                        timeRecord
                    )
                }

                override fun onDataChanged() {
                    super.onDataChanged()
                }
                override fun getSnapshots(): ObservableSnapshotArray<TimeRecord> {
                    return super.getSnapshots()
                }
                override fun onError(error: DatabaseError) {
                    super.onError(error)
                }
            }

        linearLayoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = firebaseRecyclerAdapter

        if (!args.user.isNullOrBlank()) {
            Log.d(TAG, "onCreateView: Actualizando el valor de textName")
            //binding.textBienvenido.text = "Bienvenido"
            binding.textName.text = args.user
            if(user?.photoUrl != null) {
                Glide.with(requireActivity()).load(user?.photoUrl).into(binding.avatarView)
            }
            if(Configuracion.profileUri != null) {
                Log.d(TAG, "onCreateView: ${Configuracion.profileUri}")
                Glide.with(requireActivity()).load(Configuracion.profileUri).into(binding.avatarView)
            }

        }
        // configurando los botones
        binding.clockInButton.setOnClickListener {
            // Inicializamos el valor del tiempo inicial correspondiente al clockIn
            val initTime = LocalDateTime.now()
            viewModel.setinitTime(initTime)
            // Informamos de que hemos seteado el clockIn
            viewModel.setClockIn(true)
            // guardamos en las preferencias locales el estado actual.
            savePreferences(true, initTime)
        }
        binding.clockOutButton.setOnClickListener {
            viewModel.setClockIn(false)
            viewModel.clockOut()
            // if (!hasRecycler) ActivityCompat.recreate(requireActivity())
            val endTime = LocalDateTime.now()
            savePreferences(false, endTime)
        }

        // Preparando el recycler view


        //viewModel.refreshData("20/1/2022")
        Log.d(TAG, viewModel.lista.toString())
        viewModel.CLOCKED_IN.observe(viewLifecycleOwner) {
            setButtons(it)
        }

        viewModel.initTime.observe(viewLifecycleOwner) {
            if (it != null) {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                binding.tiempoTrabajadoTXT.text = "Inicio de jornada ${it.format(formatter)}"
            }
        }
        viewModel.mutableCityName.observe(viewLifecycleOwner){
            setWeather()
        }
        readPreferences()
        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun getPosition(){
        /*
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

         */
        Log.d(TAG, "getPosition: ")
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it!=null){
                viewModel
                    .getCityName(lat= it.latitude.toString(), 
                                 lon = it.longitude.toString())
                Log.d(TAG, "getPosition: $it")
                Toast.makeText(requireContext(), "posicion lat= ${it.latitude}, lon= ${it.longitude}", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "getPosition: No hya posicion")
                Toast.makeText(requireContext(), "No hay posicion", Toast.LENGTH_SHORT).show()
            }
        }
            .addOnFailureListener {
                Log.e(TAG, "getPosition:  ${it.localizedMessage}", )
            }
    }

    private fun readPreferences() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val mClockIn: Boolean = sharedPref.getBoolean(AppConstants.CLOCK_IN_STATUS, false)
        if (mClockIn) {
            //teniamos un valor de clocked in, seguimos...
            val mInitTimeStr = sharedPref.getString(AppConstants.TIME_INIT_STATUS, "-").toString()
            if (!mInitTimeStr.equals("-")) {
                val mInitTime =
                    LocalDateTime.parse(mInitTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                // Posteamos en el livedata el tiempo inicial
                viewModel.initTime.postValue(mInitTime)
            }
            //Posteamos el clocked in
            viewModel.CLOCKED_IN.postValue(mClockIn)
        }
        var configuracionesRemotas = Configuracion.remoteConfig?.all?.toMap()
        // Chequeamos si ha habido algun cambio en el API Key y actualizamos los datos
        var apiKey = Configuracion.remoteConfig?.getString("weatherApiKey").also {
            if (it != null) {
            Configuracion.apiKey = it
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
            sharedPref?.edit()?.putString(AppConstants.API_KEY, it)?.apply()
        }
        }
    }

    private fun savePreferences(isClockedIn: Boolean, initTime: LocalDateTime?) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        //guardando las preferencias
        with(sharedPref.edit()) {
            putBoolean(AppConstants.CLOCK_IN_STATUS, isClockedIn)
            putString(
                AppConstants.TIME_INIT_STATUS,
                initTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            apply()
        }

    }

    fun listarRegistros() {
        Log.d(TAG, "listarRegistros: userId: ${user.uid}")
        val query = BASE_DE_DATOS.orderByChild("userId").equalTo(user.uid)
        options =
            FirebaseRecyclerOptions.Builder<TimeRecord>().setQuery(query, TimeRecord::class.java)
                .build()
        firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<TimeRecord, TimeRecord_ViewHolder>(options!!) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): TimeRecord_ViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.time_layout_item, parent, false)
                    val viewHolder_nota = TimeRecord_ViewHolder(view)
                    viewHolder_nota.setOnClickListener(object :
                        TimeRecord_ViewHolder.TimeItemListener {
                        override fun onItemClick(view: View?, position: Int) {
                            val timeRecord = getItem(position).copy()
                            val action = ClockInFragmentDirections.toUpdateTimeRecord(timeRecord)
                            findNavController().navigate(action)
                        }
                    })
                    return viewHolder_nota
                }

                override fun onBindViewHolder(
                    holder: TimeRecord_ViewHolder,
                    position: Int,
                    timeRecord: TimeRecord
                ) {
                    Log.d(TAG, "onBindViewHolder: ")
                    holder.setearDatos(
                        requireContext(),
                        timeRecord.day!!,
                        timeRecord.clockIn!!,
                        timeRecord.clockOut.toString(),
                        timeRecord.minutes.toString(),
                        timeRecord
                    )
                }

            }
        linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = firebaseRecyclerAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                val auth = FirebaseAuth.getInstance()
                auth.signOut()
                with(Configuracion) {
                    userId = null
                    userEmail = null
                    displayName = null
                    userDetail = null
                    isManager = false
                }
                Log.d(TAG, "onOptionsItemSelected: Login out")
                // y me vuelvo a la pantalla inicial
                val action = ClockInFragmentDirections.returnToLogin()
                findNavController().navigate(action)
            }
            R.id.action_view_tasks -> {
                Log.d(TAG, "onOptionsItemSelected: item action_view_tasks")
                if (Configuracion.isManager) {
                    Log.d(
                        TAG,
                        "onOptionsItemSelected: Configuracion.isManager == ${Configuracion.isManager}"
                    )
                    startActivity(

                        Intent(
                            requireContext(),
                            PeopleActivity::class.java
                        )
                    )
                }
            }
            R.id.pause -> {
                Log.d(TAG, "onOptionsItemSelected: Seleccionado el checklist")
                val action = ClockInFragmentDirections.fromClockToTasks(Configuracion.user.email)
                findNavController().navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
        /*
        Aqui desactivamos las opciones para los usuarios no manager (revision de fichaje del team)
         */
        val usuario = Configuracion.userDetail
        Log.d(TAG, "onCreateOptionsMenu: Configuracion.user ${Configuracion.userDetail}")
        val optionManager = menu.findItem(R.id.action_view_tasks)
        managerOptionMenu = optionManager

        if (Configuracion.userDetail?.esGerente == true) {
            Log.d(TAG, "onCreateOptionsMenu: esGerente ${Configuracion.userDetail}")
            optionManager.isVisible = true
        } else {
            Log.d(TAG, "onCreateOptionsMenu: ${Configuracion.userDetail}")
            optionManager.isVisible = false
        }

        Log.d(TAG, "Inflating menu $menu")
        super.onCreateOptionsMenu(menu, inflater)
    }
    fun setWeather() {
        if (viewModel.weatherIcon != "") {
            binding.weatherDescription.text = viewModel.weatherDescription
            val imgUrl = "https://openweathermap.org/img/wn/${viewModel.weatherIcon}@2x.png"
            Glide.with(requireActivity()).load(imgUrl).into(binding.weatherIcon)
        }
    }
    fun setButtons(clockedIn: Boolean) {
        viewModel.getWeatherData(city = Configuracion.city)
        setWeather()
        with(binding) {
            /*
            clockInButton.visibility = if (clockedIn) View.INVISIBLE else View.VISIBLE
            clockOutButton.visibility = if (clockedIn) View.VISIBLE else View.INVISIBLE
            */
            if (clockedIn) {
                clockInButton.visibility = View.INVISIBLE
                clockOutButton.visibility = View.VISIBLE
            } else {
                clockInButton.visibility = View.VISIBLE
                clockOutButton.visibility = View.INVISIBLE
            }
            //checkOptions()
        }
    }

    override fun onEditButtonSelected(timeRecord: TimeRecord) {
        Log.d(TAG, "Current item $timeRecord")
        viewModel.currentTimeRecord.postValue(timeRecord)
        val action = ClockInFragmentDirections.toUpdateTimeRecord(timeRecord)
        findNavController().navigate(action)
    }

    fun checkOptions() {

        Log.d(TAG, "checkOptions: ")
        if (Configuracion.isManager == true) {
            managerOptionMenu.isVisible = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseRecyclerAdapter != null) {
            Log.d(TAG, "onStart: Start listening")
            firebaseRecyclerAdapter!!.startListening()
        } else {
            Log.d(TAG, "onStart: not nill")
        }
    }

    private fun saveStatus(outState: Bundle) {
        if (viewModel.CLOCKED_IN.value == true) {
            /* tenemos clockin, guardamos el valor de clock_in
            y el valor del time_init
             */
            outState.putBoolean(
                AppConstants.CLOCK_IN_STATUS,
                viewModel.CLOCKED_IN.value!!
            )
            val initTime = viewModel
                .initTime.value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            outState.putString(AppConstants.TIME_INIT_STATUS, initTime)
        } else {
            outState.putBoolean(AppConstants.CLOCK_IN_STATUS, false)
            outState.putString(AppConstants.TIME_INIT_STATUS, "")
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        timeInitStr = savedInstanceState?.getString(AppConstants.TIME_INIT_STATUS).toString()
        clockedInStatus = savedInstanceState?.getBoolean(AppConstants.CLOCK_IN_STATUS).toString()
        Log.d(TAG, "onViewStateRestored: $clockedInStatus")
    }

}