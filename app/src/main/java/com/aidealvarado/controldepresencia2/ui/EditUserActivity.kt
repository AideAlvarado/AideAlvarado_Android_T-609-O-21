package com.aidealvarado.controldepresenciaAPFM.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.databinding.ActivityEditUserBinding
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.aidealvarado.controldepresenciaAPFM.viewModels.EditUserViewModel

private val TAG = EditUserActivity::class.java.simpleName
class EditUserActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var id_usuario :String
    private lateinit var displayName :String
    private lateinit var email :String
    private lateinit var tenant :String
    private lateinit var manager :String
    var isManager :Boolean = false
    var isEnabled :Boolean = false
    private lateinit var user:User
    private lateinit var binding: ActivityEditUserBinding
    private lateinit var viewModel: EditUserViewModel
    var languages = arrayOf("Java","PHP", "Kotlin","JavaScript")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditUserViewModel::class.java)

        binding = ActivityEditUserBinding.inflate(layoutInflater)
        binding.cancelEdt.setOnClickListener {onBackPressed()}
        binding.isEnabledSWCHEDT.setOnClickListener {setUserEnabled(binding.isEnabledSWCHEDT.isChecked )}
        binding.isManagerChk.setOnClickListener{setManagerEnabled(binding.isManagerChk.isChecked)}
        binding.saveBtnEdt.setOnClickListener {
            Log.d(TAG, "onCreate:saveBtnEdt $user")
            saveUser()
        }
        binding.spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,languages)
        binding.spinner.onItemSelectedListener = this
        viewModel.listaManager.observe(this) {
            Log.d(TAG, "onCreate: listaManager.observer $it")
            binding.spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,it)

        }
        setContentView(binding.root)
        recuperarDatos()
    }

    private fun recuperarDatos() {
        val intent = intent.extras
        id_usuario= intent!!.getString( AppConstants.UUID_USER ) ?: ""

        displayName = intent.getString(AppConstants.DISPLAY_NAME ) ?: ""
        binding.userNameEDT2.setText(displayName)
        email =  intent.getString(AppConstants.EMAIL) ?: ""
        binding.userEmailEDT.setText(email)
        tenant =  intent.getString(AppConstants.TENANT ) ?: ""
        manager =  intent.getString(AppConstants.MANAGER) ?: ""
        binding.managerEDT.setText(manager)
        isManager= intent.getBoolean(AppConstants.IS_MANAGER) ?: false
        binding.isManagerChk.isChecked = isManager
        isEnabled = intent.getBoolean(AppConstants.IS_ENABLED) ?: false
        binding.isEnabledSWCHEDT.isChecked = isEnabled
        user = User(displayName,email,tenant,manager,isEnabled,isManager,id_usuario)
    }

    fun setUserEnabled(value:Boolean)    {
        Log.d(TAG, "setUserEnabled: $value")
        user.estaActivado = value
    }
    fun setManagerEnabled(value: Boolean){
        Log.d(TAG, "setManagerEnabled: $value")
        user.esGerente = value
    }
    fun setManager(nameManager : String){
        Log.d(TAG, "setManager:$nameManager")
        manager = nameManager
        user.manager = nameManager
    }
    fun saveUser(){
        Log.d(TAG, "saveUser: $user")
        viewModel.saveUser(user,this@EditUserActivity)
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d(TAG, "onItemSelected: Posicion $position")
        Log.d(TAG, "onItemSelected: ${parent?.getItemAtPosition(position)}")
        setManager( parent?.getItemAtPosition(position).toString())
        if (position> 0){
            binding.managerEDT.setText( manager )
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d(TAG, "onNothingSelected: ")
    }

}