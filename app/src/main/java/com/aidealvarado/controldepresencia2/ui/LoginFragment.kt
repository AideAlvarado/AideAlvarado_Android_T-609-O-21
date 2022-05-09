package com.aidealvarado.controldepresenciaAPFM.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.aidealvarado.controldepresenciaAPFM.Configuracion
import com.aidealvarado.controldepresenciaAPFM.databinding.FragmentLoginBinding
import com.aidealvarado.controldepresenciaAPFM.shared.SharedViewModel
import com.aidealvarado.controldepresenciaAPFM.utils.FirebaseUtils
import com.aidealvarado.controldepresenciaAPFM.viewModels.LoginRegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

private val TAG = LoginFragment::class.java.simpleName

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var loginBTN: Button
    private lateinit var registerBTN: Button
    private lateinit var viewModel: SharedViewModel //ViewModel compartido de la aplicacion
    private lateinit var loginViewModel: LoginRegisterViewModel
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater)
        //Inicializamos el datamodel compartido
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        loginViewModel =
            ViewModelProvider(requireActivity()).get(LoginRegisterViewModel::class.java)
        //viewModel.refreshData("20/1/2022")
        loginBTN = binding.loginBTN
        registerBTN = binding.registerBTN
        /* Comprobamos si el usuario ya está logado,
         si lo está navegaremos directamente a la pantalla inicial
         */
        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        
        if (currentUser != null) {
            Log.d(TAG, "onCreateView: Ya estamos logados $currentUser")
            // Ya estamos logados, asi que nos vamos a la vista de clockIn
            loginViewModel.setEnvironmentFromFirebaseAuth(currentUser)
            Log.d(TAG, "DisplayName ${Configuracion.displayName}")
            // Buscamos el valor del usuario en la tabla de registro de usuarios
         /*
            if (Configuracion.displayName.isNullOrBlank()) {
                goToClockIn(Configuracion.userEmail!!)
            } else {
                goToClockIn(Configuracion.displayName!!)
            }

          */
        } 
        else {
            Log.d(TAG, "onCreateView: No esta el usuario logueado")
        }

        Log.d(TAG, "Entrando")
        loginBTN.setOnClickListener {
            Log.d(TAG, "Clicked login")
            loginViewModel.loginUser(
                binding.emailAddressTXT,
                binding.passwordTXT,
                requireContext() as FragmentActivity
            )
        }
        registerBTN.setOnClickListener {
            Log.d(TAG, "Clicked register")
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment(email = "")
            findNavController().navigate(action)
        }

        loginViewModel.userDetail.observe(viewLifecycleOwner) {
            Log.d(TAG, "onCreateView: $it")
            if (auth.currentUser != null) {
                Log.d(TAG, "onCreateView: loginViewModel.userDetail.observe has changed $it")
                auth.currentUser?.displayName?.let { it1 -> goToClockIn(it1) }
            }
        }

        return binding.root

    }


    private fun goToClockIn(userName: String) {
        Log.d(TAG, "goToClockIn: username $userName")
        val action = LoginFragmentDirections.fromLoginToClockIn(user = userName)
        findNavController().navigate(action)
    }

}