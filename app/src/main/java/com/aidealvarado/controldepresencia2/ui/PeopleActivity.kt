package com.aidealvarado.controldepresenciaAPFM.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.aidealvarado.controldepresenciaAPFM.ui.viewHolders.Person_ViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

private val TAG = PeopleActivity::class.java.simpleName
class PeopleActivity : AppCompatActivity() {
    var recyclerViewNotas: RecyclerView? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var BASE_DE_DATOS: DatabaseReference? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<User, Person_ViewHolder>? = null
    var options: FirebaseRecyclerOptions<User>? = null
    var auth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)
        val actionBar = supportActionBar
        with(actionBar) {
            this!!.title = "Lista de usuarios"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        recyclerViewNotas = findViewById(R.id.listarUsuariosRV)
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        BASE_DE_DATOS = firebaseDatabase!!.getReference("users")
        ListarNotasUsuarios()
    }

    private fun ListarNotasUsuarios() {
        Log.d(TAG, "ListarNotasUsuarios: -----------------")
        val query = BASE_DE_DATOS!!
            .child("TENANT")
        Log.d(TAG, "ListarNotasUsuarios: $query")
        options = FirebaseRecyclerOptions
            .Builder<User>()
            .setQuery(query, User::class.java).build()
        Log.d(TAG, "ListarNotasUsuarios: ${options}")
        firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<User, Person_ViewHolder>(options!!) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): Person_ViewHolder {
                    val view = LayoutInflater
                        .from(parent.context)
                        .inflate(
                            R.layout.people_item,
                            parent,
                            false
                        )
                    val viewHolder_nota = Person_ViewHolder(view)
                    Log.d(TAG, "onCreateViewHolder: $viewHolder_nota")

                    viewHolder_nota.setOnClickListener(object : Person_ViewHolder.PeopleItemListener{

                        override fun onItemClick(view: View?, position: Int) {
                            Toast.makeText(this@PeopleActivity,"On item click",Toast.LENGTH_LONG).show()

                            val id_usuario = getItem(position).uuid
                            val displayName = getItem(position).displayName
                            val email = getItem(position).email
                            val tenant = getItem(position).tenant
                            val manager = getItem(position).manager
                            val isManager = getItem(position).esGerente
                            val isEnabled = getItem(position).estaActivado

                            val intent = Intent(this@PeopleActivity,EditUserActivity::class.java)


                            intent.putExtra( AppConstants.UUID_USER ,id_usuario)
                            intent.putExtra(AppConstants.DISPLAY_NAME ,displayName)
                            intent.putExtra(AppConstants.EMAIL, email)
                            intent.putExtra(AppConstants.TENANT ,tenant)
                            intent.putExtra(AppConstants.MANAGER, manager)
                            intent.putExtra(AppConstants.IS_MANAGER,isManager)
                            intent.putExtra(AppConstants.IS_ENABLED,isEnabled)
                            startActivity(intent)
                        }

                        override fun onItemLongClick(view: View?, position: Int) {

                            val id_usuario = getItem(position).uuid
                            val displayName = getItem(position).displayName
                            val email = getItem(position).email
                            val tenant = getItem(position).tenant
                            val manager = getItem(position).manager
                            val isManager = getItem(position).esGerente
                            val isEnabled = getItem(position).estaActivado

                            val intent = Intent(this@PeopleActivity,EditUserActivity::class.java)


                            intent.putExtra( AppConstants.UUID_USER ,id_usuario)
                            intent.putExtra(AppConstants.DISPLAY_NAME ,displayName)

                            intent.putExtra(AppConstants.EMAIL, email)
                            intent.putExtra(AppConstants.TENANT ,tenant)
                            intent.putExtra(AppConstants.MANAGER, manager)
                            intent.putExtra(AppConstants.IS_MANAGER,isManager)
                            intent.putExtra(AppConstants.IS_ENABLED,isEnabled)
                            startActivity(intent)
                        }
                    }
                    )

                    return viewHolder_nota
                }

                override fun onBindViewHolder(
                    holder: Person_ViewHolder,
                    position: Int,
                    user: User
                ) {
                    Log.d(TAG, "onBindViewHolder: $user")
                    holder.setearDatos(
                        applicationContext,
                        user.displayName!!,
                        user.esGerente!!,
                        user.estaActivado!!,
                        user
                    )
                }

            }
        linearLayoutManager =
            LinearLayoutManager(this@PeopleActivity, LinearLayoutManager.VERTICAL, false)
        linearLayoutManager!!.reverseLayout = true
        linearLayoutManager!!.stackFromEnd = true
        recyclerViewNotas!!.layoutManager = linearLayoutManager
        recyclerViewNotas!!.adapter = firebaseRecyclerAdapter
    }

    override fun onStart() {
        super.onStart()
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter!!.startListening()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}