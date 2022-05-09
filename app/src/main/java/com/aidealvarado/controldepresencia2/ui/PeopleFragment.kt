package com.aidealvarado.controldepresenciaAPFM.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aidealvarado.controldepresenciaAPFM.AppConstants
import com.aidealvarado.controldepresenciaAPFM.R
import com.aidealvarado.controldepresenciaAPFM.databinding.FragmentPeopleBinding
import com.aidealvarado.controldepresenciaAPFM.models.User
import com.aidealvarado.controldepresenciaAPFM.ui.adapter.PeopleAdapter
import com.aidealvarado.controldepresenciaAPFM.ui.viewHolders.Person_ViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


private val TAG = PeopleFragment::class.java.simpleName

class PeopleFragment : Fragment() {
    private val args: PeopleFragmentArgs by navArgs()
    private lateinit var binding: FragmentPeopleBinding
    private lateinit var recyclerView: RecyclerView
    var firebaseDatabase: FirebaseDatabase? = null
    var BASE_DE_DATOS: DatabaseReference? = null
    var linearLayoutManager: LinearLayoutManager? = null

    // private var viewModel = arrayListOf<User>()
    ///private lateinit var viewModel: PeopleViewModel
    private var adapter: PeopleAdapter? = null
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<User, Person_ViewHolder>? = null
    var options: FirebaseRecyclerOptions<User>? = null
    var auth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as AppCompatActivity).run {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            recyclerView = findViewById(R.id.myReciclerV)
        }
        setHasOptionsMenu(true)
        //viewModel = ViewModelProvider(requireActivity()).get(PeopleViewModel::class.java)
        if (!args.tenant.isNullOrBlank()) {
            Log.e(TAG, "onCreateView: No argument TENANT")
            Log.d(TAG, "onCreateView: $args")
        }
        // Inflate the layout for this fragment
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        BASE_DE_DATOS = firebaseDatabase!!.getReference(AppConstants.TABLE_USERS)
        //Listar usuarios
        val query = BASE_DE_DATOS!!.orderByChild(AppConstants.TENANT)
        options = FirebaseRecyclerOptions.Builder<User>().setQuery(query, User::class.java).build()
        firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<User, Person_ViewHolder>(options!!) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    position: Int
                ): Person_ViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.people_item, parent, false)
                    val viewHolder_person = Person_ViewHolder(view)
                    val displayName = getItem(position).displayName
                    val email = getItem(position).email
                    val isEnabled = getItem(position).estaActivado
                    val isManager = getItem(position).esGerente
                    val manager = getItem(position).manager
                    val tenant = getItem(position).tenant
                    val usuario = getItem(position)
                    Log.d(TAG, "onCreateViewHolder: usuario $usuario")
                    return viewHolder_person
                }

                override fun onBindViewHolder(
                    holder: Person_ViewHolder,
                    position: Int,
                    model: User
                ) {
                    //    holder.setearDatos(requireContext(), model.displayName!!,model.esGerente!!,model.estaActivado!!,model)
                }

            }
        //adapter = viewModel.userList.value?.let { PeopleAdapter(requireContext(), it, this) }
        linearLayoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false        )
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = firebaseRecyclerAdapter
    }
}

