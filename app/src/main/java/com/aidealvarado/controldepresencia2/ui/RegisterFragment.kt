package com.aidealvarado.controldepresenciaAPFM.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.aidealvarado.controldepresenciaAPFM.Configuracion
import com.aidealvarado.controldepresenciaAPFM.databinding.FragmentRegisterBinding
import com.aidealvarado.controldepresenciaAPFM.viewModels.LoginRegisterViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


private val TAG = RegisterFragment::class.java.simpleName

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: LoginRegisterViewModel

    private var pictureFullPath = ""
    private var pictureName = ""
    private lateinit var imageView: ImageView
    private var imageBitmap: Bitmap? = null
    private lateinit var cameraIamgeView: ImageView
    private val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1001


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater)
        viewModel = ViewModelProvider(requireActivity()).get(LoginRegisterViewModel::class.java)
        // Inflate the layout for this fragment
        with(binding) {
            cameraIamgeView = cameraImageViewIV
            registerBTN.setOnClickListener {
                if (viewModel.validatePassword(emailAddressTXT,passwordTXT,confirmPasswordTXT,requireContext())
                ) {
                    Log.d(TAG, "Ambas passwords son iguales")
                    //Inicializamos Registramos el usuario.
                    viewModel.registerUser(displayNameTXT, emailAddressTXT, passwordTXT,cameraImageViewIV)
                    Log.d(TAG,"onCreateView: registering ${ viewModel.hasNullOrEmptyDrawable(cameraImageViewIV)}")
                } else {
                    Log.d(TAG, "Ambas passwords son diferentes")
                }
            }
            cameraButton.setOnClickListener {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                activity?.startActivityFromFragment(this@RegisterFragment,cameraIntent,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            }
        }
        viewModel.userDetail.observe(viewLifecycleOwner) {
            if (it.uuid != null && Configuracion.userId != null ) {
                Log.d(TAG, "onCreateView: Registered")
                val action = RegisterFragmentDirections.fromRegisterToClockin(user = "")
                findNavController().navigate(action)
            } else {
                Log.d(TAG, "onCreateView: Error on registration")
            }
        }
        return binding.root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val bmp = data.extras!!["data"] as Bitmap?
                    val stream = ByteArrayOutputStream()
                    cameraIamgeView.setImageBitmap(bmp)

                }
            }
        } catch (e: Exception) {
            Toast.makeText(this.activity, e.toString() + "Something went wrong", Toast.LENGTH_LONG)
                .show()
        }
    }
}