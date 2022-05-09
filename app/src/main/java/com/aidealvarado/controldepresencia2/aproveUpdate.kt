package com.aidealvarado.controldepresenciaAPFM

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aidealvarado.controldepresenciaAPFM.databinding.FragmentAproveUpdateBinding
import com.aidealvarado.controldepresenciaAPFM.shared.SharedViewModel

private val TAG = aproveUpdate::class.java.simpleName
class aproveUpdate : Fragment() {
    private val args: aproveUpdateArgs by navArgs<aproveUpdateArgs>()
    private lateinit var binding: FragmentAproveUpdateBinding
    private lateinit var viewModel: SharedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAproveUpdateBinding.inflate(inflater,container,false)
        //binding.captionTXT.text = args.updateRecord.displayName
        binding.txtDisplayNameAppr.text = args.updateRecord.displayName
        binding.editTextDateAppr.setText( args.updateRecord.day)
        binding.currentEntryTimeAppr.setText(args.updateRecord.clockIn)
        binding.currentExitTimeAppr.setText(args.updateRecord.clockOut)
        binding.newEntryTimeAppr.setText(args.updateRecord.requestEntry)
        binding.newExitTimeAppr.setText(args.updateRecord.requestEntry)
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        binding.confirmBTN.setOnClickListener {
            Log.d(TAG, "onCreateView: Aprobando la solicitud")
            autorizarCambio()
        }
        binding.cancelUpdateBTNAppr.setOnClickListener {
            Log.d(TAG, "onCreateView: Cancelando la petición")
            findNavController().navigateUp()

        }
        binding.denyUpdateBTN.setOnClickListener {
            Log.d(TAG, "onCreateView: Denegado")
            denegarCambio()
        }
        return binding.root
    }

    private fun denegarCambio() {
        viewModel.denegarCambio(args.updateRecord)
        findNavController().navigateUp()
    }

    fun autorizarCambio(){
        // Actualizar el registro actual en Firebase Realtime
        viewModel.updateFirebase(args.updateRecord)
        // Marcar la peticion como aprobada
        viewModel.aprobarCambio(args.updateRecord)
        findNavController().navigateUp()
    }
}