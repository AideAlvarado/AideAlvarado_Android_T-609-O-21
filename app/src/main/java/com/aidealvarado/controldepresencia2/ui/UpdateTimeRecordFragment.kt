package com.aidealvarado.controldepresenciaAPFM.ui

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aidealvarado.controldepresenciaAPFM.Configuracion
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.databinding.FragmentUpdateTimeRecordBinding
import com.aidealvarado.controldepresenciaAPFM.shared.SharedViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TAG = UpdateTimeRecordFragment::class.java.simpleName

class UpdateTimeRecordFragment : Fragment(), TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: FragmentUpdateTimeRecordBinding
    private lateinit var viewModel: SharedViewModel
    private val args: UpdateTimeRecordFragmentArgs by navArgs<UpdateTimeRecordFragmentArgs>()
    var currentEntry: EditText? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Activamos la tecla de retorno
        (requireActivity() as AppCompatActivity).run {
           supportActionBar?.setDisplayHomeAsUpEnabled(true)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER

        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER

        setHasOptionsMenu(false)
        Log.d(TAG, "$args")
        Log.d(TAG, "onCreateView: ${args.timeRecord}")
        val timeRecord = args.timeRecord

        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        viewModel.currentTimeRecord.postValue(timeRecord)
        binding = FragmentUpdateTimeRecordBinding.inflate(
            inflater,
            container,
            false
        )
        with(binding) {
            editTextDate?.setText(timeRecord.day.toString())
            currentEntryTime?.setText(timeRecord.clockIn)
            currentExitTime?.setText(timeRecord.clockOut)
            newEntryTime?.setText("")
            newExitTime?.setText("")
        }
        binding.newEntryTime?.setText("")
        binding.newExitTime?.setText("")
        Log.d(TAG, "onCreateView: ${Configuracion.userDetail?.manager}")
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.cancelUpdateBTN?.setOnClickListener {
            cancelNavigation()
        }
        binding.confirmBTN.setOnClickListener {
            pedirCambio()
        }
        binding.setNewEntryBTN?.setOnClickListener {
            currentEntry = binding.newEntryTime
            val hora = seleccionarHora(binding.currentEntryTime?.text.toString())
            Log.d(TAG, "onCreateView: $hora")

        }
        binding.setExitBTN?.setOnClickListener {
            currentEntry = binding.newExitTime
            val hora = seleccionarHora(binding.currentExitTime?.text.toString())
            Log.d(TAG, "onCreateView: $hora")
        }
        viewModel.taskId.observe(viewLifecycleOwner) {
            Log.d(TAG, "onCreateView: it $it")
            if(!it.isNullOrBlank()) {
                Log.d(TAG, "onCreateView: ${it}")
                val status = viewModel.taskId.value.toString().split("\t")
                Log.d(TAG, "onCreateView: $status")
                if (status[0].equals("success")) {
                    Toast.makeText(
                        requireContext(),
                        "Petición de cambio registrada",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error ${status[1]}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                findNavController().navigateUp()
                // seteamos la variable a null para la reentrada
                viewModel.taskId.postValue(null)
            }
        }
        return binding.root
    }

    private fun seleccionarHora(time: String): String {
        if (time.isEmpty())
            return ""
        else {
            var hora: Int
            var minuto: Int
            val splited = time.split(":")
            if (splited.size != 2) {

                hora = splited[0].toInt()
                minuto = splited[1].toInt()
            } else {
                hora = 12
                minuto = 12
            }
            val timePicker = TimePickerDialog(
                requireContext(),
                this,
                hora,
                minuto, true
            )
            timePicker.show()
            return ""
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "Clicked $item")
        Log.d(TAG, "${item.itemId}")
        if (item.itemId == android.R.id.home) {
            cancelNavigation()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cancelNavigation() {
        findNavController().navigateUp()
    }

    fun pedirCambio() {
        // validamos que la hora sea correcta
        val newEntryTimeTxt = binding.newEntryTime?.text.toString()
        val newExitTimeTxt = binding.newExitTime?.text.toString()
        if (!viewModel.validarHoraONulo(newEntryTimeTxt)) {
            Log.d(TAG, "pedirCambio: newEntry  $newEntryTimeTxt")
            muestraMensajeError(getString(R.string.intro_a_new_entry_time))
        }
        else if (!viewModel.validarHoraONulo(newExitTimeTxt)) {
            Log.d(TAG, "pedirCambio: newExit $newExitTimeTxt ")
            muestraMensajeError(getString(R.string.intro_a_new_exit_time))
        }
        else {
            val minutosIniciales = viewModel.horaAMinutos(newEntryTimeTxt)
            val minutosFinales = viewModel.horaAMinutos(newExitTimeTxt)
            if ((minutosFinales > 0) && (minutosIniciales >0) && (minutosFinales < minutosIniciales) ) {
                    muestraMensajeError(getString(R.string.hora_de_salida_menor_que_entrada))
            }  else {
            //
            viewModel.requestTimeChange(
                viewModel.currentTimeRecord.value!!,
                newEntryTimeTxt,
                newExitTimeTxt,
                Configuracion.userDetail?.manager.toString()

            )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        Log.d(TAG, "onTimeSet: $hourOfDay, $minute")
        val horaActual = formateaHora(hourOfDay)
        val minutoActual = formateaHora(minute)
        currentEntry?.setText("$horaActual:$minutoActual")
        val requestTime =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        Log.d(TAG, "onTimeSet:currentEntry:${currentEntry?.text} currentTime $requestTime")

    }

    private fun formateaHora(hourOfDay: Int): String {
        val horaActual = if (hourOfDay < 10) "0$hourOfDay" else "$hourOfDay"
        return horaActual
    }

    fun muestraMensajeError(mensaje:String){
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
    }

}