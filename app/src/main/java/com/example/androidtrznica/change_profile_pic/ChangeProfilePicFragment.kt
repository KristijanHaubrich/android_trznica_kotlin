package com.example.androidtrznica.change_profile_pic

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.androidtrznica.R
import com.example.androidtrznica.databinding.FragmentChangeProfilePicBinding
import com.example.androidtrznica.register_login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay


class ChangeProfilePicFragment : Fragment() {
    lateinit var binding: FragmentChangeProfilePicBinding
    val PICK_IMAGE_REQUEST = 1
    var imageUri: String = ""
    var storeRef = FirebaseStorage.getInstance().getReference("profilePics")
    var userAuth = FirebaseAuth.getInstance().currentUser
    var database = FirebaseDatabase.getInstance().reference
    lateinit var user:User


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getCurrentUser()
        binding = FragmentChangeProfilePicBinding.inflate(layoutInflater)
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    myProfile()
                }
                R.id.nav_add -> {
                    val action = ChangeProfilePicFragmentDirections.actionChangeProfilePicFragmentToAddProductFragment()
                    findNavController().navigate(action)
                }
                R.id.nav_contacts -> {
                    val action = ChangeProfilePicFragmentDirections.actionChangeProfilePicFragmentToContactsFragment()
                    findNavController().navigate(action)
                }
            }
            true
        }
        binding.apply {
            ivDelete.setOnClickListener { deleteProfilePic() }
            btnChangeProfilePic.setOnClickListener { changeProfilePic() }
            ivChange.setOnClickListener { openFileChooser() }
        }

        return binding.root
    }

    private fun openFileChooser() {
        var intent = Intent()
        intent.setType("Image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            super.onActivityResult(requestCode, resultCode, data)
            imageUri = data?.data!!.toString()
            Glide.with(this).load(imageUri).skipMemoryCache(true).into(binding.ivProfilePic)
        }
    }


    private fun myProfile() {
        val action = ChangeProfilePicFragmentDirections.actionChangeProfilePicFragmentToMainFragment()
        findNavController().navigate(action)
    }

    private fun changeProfilePic() {
        if(imageUri != ""){
            binding.btnChangeProfilePic.setClickable(false)
            storeRef.child(user.username.toString()).putFile(Uri.parse(imageUri)).addOnSuccessListener {
                storeRef.child(user.username.toString()).downloadUrl.addOnSuccessListener {
                    database.child("Users").child(userAuth?.uid!!).child("profilePic").setValue(it.toString()).addOnSuccessListener {
                        myProfile()
                        Toast.makeText(activity, "Uspješno promijenjena profilna slika", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        else{
            Toast.makeText(activity, "Nije odabrana slika", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getCurrentUser(){
        database.child("Users").child(userAuth?.uid!!).get().addOnSuccessListener {
            if (it.exists()){
                user = it.getValue(User::class.java)!!
            }
        }
    }


    private fun deleteProfilePic() {
        if (user.profilePic != "null"){
            FirebaseStorage.getInstance().getReferenceFromUrl(user.profilePic.toString()).delete().addOnSuccessListener {
                database.child("Users").child(userAuth?.uid!!).child("profilePic").setValue("null").addOnSuccessListener {
                    Toast.makeText(activity, "Profilna slika je uspješno obrisana", Toast.LENGTH_SHORT).show()
                    myProfile()
                }
            }
        }
        else{
            Toast.makeText(activity, "Nemate profilnu sliku", Toast.LENGTH_LONG).show()
        }
    }


    companion object {
        val Tag = "NewTask"

        fun create(): Fragment {
            return ChangeProfilePicFragment()
        }
    }
}

