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
import com.example.androidtrznica.product.Product
import com.example.androidtrznica.register_login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class DeleteFragment : Fragment() {
    lateinit var binding: FragmentDeleteBinding
    lateinit var userUID: String
    lateinit var username: String
    val auth = FirebaseAuth.getInstance().currentUser
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
        deleteFromDatabase()
        FirebaseAuth.getInstance().getCurrentUser()?.delete()?.addOnSuccessListener {
            Toast.makeText(this.context,"Profil je uspješno obrisan", Toast.LENGTH_LONG).show()
        }
        val action = DeleteFragmentDirections.actionDeleteFragmentToLoginFragment()
       findNavController().navigate(action)
    }

    private fun deleteFromDatabase() {
        deleteProducts()
        deleteUserInformation()

    }

    private fun deleteProducts() {
        database.child("Products").child(username).get().addOnSuccessListener {
            if(it.exists()){
                for (current:DataSnapshot in it.children){
                    val product = current.getValue(Product::class.java)
                    if (product?.pic != "null"){
                        FirebaseStorage.getInstance().getReferenceFromUrl(product?.pic!!).delete().addOnFailureListener{
                            Toast.makeText(this.context,"Slika proizvoda ${product.name} nije uspješno obrisana. Razlog: ${it.message}.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        database.child("Products").child(username).removeValue()
    }

    private fun deleteUserInformation(){
        database.child("Users").child(auth?.uid!!).get().addOnSuccessListener {
            if (it.exists()){
                val user = it.getValue(User::class.java)
                if (user?.profilePic != "null"){
                    FirebaseStorage.getInstance().getReferenceFromUrl(user?.profilePic!!).delete().addOnFailureListener{
                        Toast.makeText(this.context,"Profilna slika korisnika ${username} nije uspješno obrisana. Razlog: ${it.message}.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        database.child("Users").child(auth.uid).removeValue()
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