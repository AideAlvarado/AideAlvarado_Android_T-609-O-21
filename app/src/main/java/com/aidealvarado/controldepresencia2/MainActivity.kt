package com.aidealvarado.controldepresenciaAPFM

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.aidealvarado.controldepresenciaAPFM.models.Main
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
private val TAG = MainActivity::class.java.simpleName
class MainActivity : AppCompatActivity() {
    val permissions = listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    lateinit var dexter:DexterBuilder
    override fun onCreate(savedInstanceState: Bundle?) {
        //Firebase.database.setPersistenceEnabled(true)

        Configuracion.authority="$packageName.provider"

        super.onCreate(savedInstanceState)



        getPermission()

        setContentView(R.layout.activity_main)
    }
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result -> dexter.check()
    }
    // usando DEXTER para simplificar la gestion de permisos.
    //https://www.geeksforgeeks.org/multiple-runtime-permissions-in-android-with-kotlin-using-dexter/
    private fun getPermission() {
        dexter = Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    report.let {
                        Log.d(TAG, "onPermissionsChecked: $it.grantedPermissionResponses")
                        if (it.areAllPermissionsGranted()) {
                            Toast.makeText(this@MainActivity, "Permissions Granted", Toast.LENGTH_SHORT).show()
                        } else {
                            AlertDialog.Builder(this@MainActivity, R.style.Theme_AppCompat_Dialog).apply {
                                setMessage("La aplicacion usa la posicion para determinar el teletrabajo")
                                    .setCancelable(true)
                                    .setPositiveButton("Settings") { _, _ ->
                                        val reqIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .apply {
                                                val uri = Uri.fromParts("package", packageName, null)
                                                data = uri
                                            }

                                        resultLauncher.launch(reqIntent)
                                    }
                                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
                                val alert = this.create()
                                alert.show()
                            }
                        }
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest?>?, token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }
            }).withErrorListener{
                Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
            }
        dexter.check()
    }





}