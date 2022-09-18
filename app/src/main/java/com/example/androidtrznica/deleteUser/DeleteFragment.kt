package com.example.androidtrznica.deleteUser

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.androidtrznica.contacts.ContactFragment
import com.example.androidtrznica.databinding.FragmentDeleteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class DeleteFragment : Fragment() {
    lateinit var binding: FragmentDeleteBinding
    lateinit var userUID: String
    lateinit var username: String
    private val args: DeleteFragmentArgs by navArgs()
    val database = FirebaseDatabase.getInstance().getReference()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userUID = args.userUID
        username = args.username
        binding = FragmentDeleteBinding.inflate(layoutInflater)
        binding.apply {
            btnDelete.setOnClickListener { delete() }
            btnBack.setOnClickListener { back() }
        }
        return binding.root
    }

    private fun delete() {
        FirebaseAuth.getInstance().getCurrentUser()?.delete()?.addOnSuccessListener {
            deleteFromDatabase()
        }
        val action = DeleteFragmentDirections.actionDeleteFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    private fun deleteFromDatabase() {
        database.child("Users").child(userUID).removeValue().addOnSuccessListener {
            database.child("Products").child(username).removeValue().addOnSuccessListener {
                Toast.makeText(this.context,"Profil je uspješno obrisan.", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun back() {
        val action = DeleteFragmentDirections.actionDeleteFragmentToMainFragment()
        findNavController().navigate(action)
    }

    companion object {

        fun create(): Fragment {
            return ContactFragment()
        }
    }
}