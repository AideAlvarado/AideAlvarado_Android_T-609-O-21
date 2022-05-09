package com.aidealvarado.controldepresenciaAPFM

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aidealvarado.controldepresenciaAPFM.databinding.FragmentTasksBinding
import com.aidealvarado.controldepresenciaAPFM.models.UpdateTimeRecord
import com.aidealvarado.controldepresenciaAPFM.shared.SharedViewModel
import com.aidealvarado.controldepresenciaAPFM.ui.viewHolders.UpdateTimerecord_ViewHoldervar
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.log

private val TAG = TasksFragment::class.java.simpleName

class TasksFragment : Fragment() {
    private val args: TasksFragmentArgs by navArgs<TasksFragmentArgs>()
    private lateinit var binding: FragmentTasksBinding
    private lateinit var viewModel: SharedViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var firebaseDatabase: FirebaseFirestore
    var options: FirestoreRecyclerOptions<UpdateTimeRecord>? = null
    private lateinit var linearLayoutManager: LinearLayoutManager
    var firebaseRecyclerAdapter: FirestoreRecyclerAdapter<UpdateTimeRecord,UpdateTimerecord_ViewHoldervar>? = null
    var retorno = false
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Habilitar el menu
        (requireActivity() as AppCompatActivity).run {
            supportActionBar?.setDisplayHomeAsUpEnabled(
                false
            )
        }
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        recyclerView = binding.tasksRecyclerView
        // recuperando los datos de autorizacion
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        // preparamos la consulta
        firebaseDatabase = FirebaseFirestore.getInstance()


        val collectionName = "${AppConstants.TABLE_TASKS}/${AppConstants.TENANT}/${args.manager}"
        Log.d(TAG, "onCreateView: $collectionName")
        val query = firebaseDatabase.collection(collectionName)
            .whereEqualTo("status", AppConstants.STATUS_PENDING)
        Log.d(TAG, "onCreateView: query $query")
        options = FirestoreRecyclerOptions.Builder<UpdateTimeRecord>()
            .setQuery(query, UpdateTimeRecord::class.java).build()
        
        firebaseRecyclerAdapter =
            object: FirestoreRecyclerAdapter<UpdateTimeRecord, UpdateTimerecord_ViewHoldervar>(options!!){
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): UpdateTimerecord_ViewHoldervar {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.update_timerecord,parent, false)
                    val viewHolder_Nota = UpdateTimerecord_ViewHoldervar(view)
                    viewHolder_Nota.setOnClickListener( object: UpdateTimerecord_ViewHoldervar.TimeItemListener {
                        override fun onItemClick(view: View?, position: Int) {
                            Log.d(TAG, "onItemClick: valor de view: $view position $position")
                            Log.d(TAG, "onItemClick: ${getItem(position)}")
                            val action = TasksFragmentDirections.toAuthorizeUpdate(getItem(position))
                            findNavController().navigate(action)
                        }

                        override fun onCancelClick(view: View?, position: Int) {
                            Log.d(TAG, "onCancelClick:  valor de view: $view position $position")
                            requireActivity().onBackPressed()
                        }

                    })
                    return viewHolder_Nota
                }

                override fun onBindViewHolder(
                    holder: UpdateTimerecord_ViewHoldervar,
                    position: Int,
                    model: UpdateTimeRecord
                ) {
                    Log.d(TAG, "onBindViewHolder: onBindViewHolder ${getItem(position)}")
                    holder.setearDatos(
                        requireContext(),
                        model.displayName ?: "--",
                        model.day!!,
                        model.requestEntry ?: "--",
                        model.requestExit ?: "--",
                        model
                    )
                }
            }
        linearLayoutManager = LinearLayoutManager(requireActivity(),
            LinearLayoutManager.VERTICAL,
            false)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = firebaseRecyclerAdapter
        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }
        return binding.root
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

}