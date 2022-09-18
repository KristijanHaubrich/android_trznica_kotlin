package com.example.androidtrznica.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.androidtrznica.R
import com.example.androidtrznica.databinding.FragmentAddProductBinding
import com.example.androidtrznica.register_login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddProductFragment : Fragment() {

    lateinit var binding: FragmentAddProductBinding
    lateinit var storeRef : StorageReference
    val database = FirebaseDatabase.getInstance().reference
    val userAuth = FirebaseAuth.getInstance().currentUser
    var productPic = "null"
    val PICK_IMAGE_REQUEST = 1
    lateinit var user:User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getCurrentUser()
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        binding.btnAddProduct.setOnClickListener { addProduct() }
        binding.ivPickPic.setOnClickListener { openFileChooser() }
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    myProfile()
                }
                R.id.nav_contacts -> {
                    val action = AddProductFragmentDirections.actionAddProductFragmentToContactsFragment()
                    findNavController().navigate(action)
                }
            }
            true
        }

        return binding.root
    }

    private fun getCurrentUser(){
        database.child("Users").child(userAuth?.uid!!).get().addOnSuccessListener {
            if (it.exists()){
                user = it.getValue(User::class.java)!!
                storeRef = FirebaseStorage.getInstance().getReference("productPics").child(user.username.toString())
            }
        }
    }


    private fun myProfile() {
        val action = AddProductFragmentDirections.actionAddProductFragmentToMainFragment()
        findNavController().navigate(action)
    }

    private fun addProduct() {
        var name = binding.etName.text.toString().trim()
        var description = binding.etDescription.text.toString().trim()
        var price = binding.etPrice.text.toString().trim()

        if (name.isEmpty()){
            binding.etName.apply {
                setError("Ime proizvoda je obavezno")
                requestFocus()
            }
            return
        }

        else if(name.contains(".") || name.contains("#") || name.contains("$") || name.contains("[") || name.contains("]")){
            binding.etName.apply {
                setError("Ime proizvoda ne smije sadržavati '.', '#', '$', '[', ili ']'")
                requestFocus()
            }
            return
        }

        if (name.isEmpty()){
            binding.etName.apply {
                setError("Ime proizvoda je obavezno")
                requestFocus()
            }
            return
        }

        else if (description.isEmpty()){
            binding.etDescription.apply {
                setError("Opisa proizvoda je obavezan")
                requestFocus()
            }
            return
        }

        else if (price.isEmpty()){
            binding.etName.apply {
                setError("Cijena proizvoda je obavezna")
                requestFocus()
            }
            return
        }

        else{
            database.child("Products").child(user.username.toString()).child(name).get().addOnSuccessListener {
                if(it.exists()){
                    binding.etName.apply {
                        setError("Proizvod već postoji")
                        requestFocus()
                    }
                }
                else{
                    val product = Product(name,description,price,"null",user.username.toString())
                    uploadProduct(product)
                }
            }
        }
    }

    private fun uploadProduct(product: Product) {
        binding.btnAddProduct.setClickable(false)
        user.username?.let {
            database.child("Products").child(it).child(product.name!!).setValue(product).addOnFailureListener{
                Toast.makeText(this.context,"Proizvod nije uspješno dodan u bazu. Razlog: ${it.message.toString()}",Toast.LENGTH_LONG).show()
            }
        }
        if (productPic != "null"){
            storeRef.child(product.name!!).putFile(Uri.parse(productPic)).addOnSuccessListener {
                storeRef.child(product.name!!).downloadUrl.addOnSuccessListener {
                    database.child("Products").child(user.username!!).child(product.name!!).child("pic").setValue(it.toString())
                }
            }
            Glide.with(this).clear(binding.ivProductPic)
        }
        binding.apply {
            etDescription.text.clear()
            etName.text.clear()
            etPrice.text.clear()
        }
        Toast.makeText(this.context,"Proizvod je uspješno dodan",Toast.LENGTH_LONG).show()
        myProfile()
    }

    private fun openFileChooser() {
        var intent = Intent()
        intent.setType("Image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            super.onActivityResult(requestCode, resultCode, data)
            productPic = data?.data!!.toString()
            Glide.with(this).load(productPic).skipMemoryCache(true).into(binding.ivProductPic)
        }
    }

    companion object {

        fun create(): Fragment {
            return AddProductFragment()
        }
    }
}