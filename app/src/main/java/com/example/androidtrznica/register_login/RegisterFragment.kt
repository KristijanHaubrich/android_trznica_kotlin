package com.example.androidtrznica.register_login

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.androidtrznica.databinding.FragmentRegisterBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class RegisterFragment : Fragment() {

    lateinit var binding: FragmentRegisterBinding
    var database = FirebaseDatabase.getInstance().reference
    var auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        binding.apply {
            btnRegister.setOnClickListener { register() }
            tvLogin.setOnClickListener { goToLogin() }
        }
        return binding.root
    }

    private fun goToLogin() {
        val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    private fun register() {
        var username = binding.etUsername.text.toString()
        var password = binding.etPassword.text.toString().trim()
        var email = binding.etEmail.text.toString().trim()

        if (username.isEmpty()) {
            binding.etUsername.apply {
                setError("Korisničko ime je obavezno")
                requestFocus()
            }
            return
        }

        else if(username.contains(".") || username.contains("#") || username.contains("$") || username.contains("[") || username.contains("]")){
            binding.etUsername.apply {
                setError("Korisničko ime ne smije sadržavati '.', '#', '$', '[', ili ']'")
                requestFocus()
            }
            return
        }

        else if (password.isEmpty()) {
            binding.etPassword.apply {
                setError("Lozinka je obavezna")
                requestFocus()
            }
            return
        }

        else if (email.isEmpty()){
            binding.etEmail.apply {
                setError("Email je obavezan")
                requestFocus()
            }
            return
        }

        else if(password.length < 6){
            binding.etPassword.apply {
                setError("Lozinka treba imati barem 6 znakova")
                requestFocus()
            }
            return
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.apply {
                setError("Email je nevažeći")
                requestFocus()
            }
            return
        }

        else{
            database.child("Users").child(username).get().addOnSuccessListener {
                if(it.exists()){
                    binding.etUsername.apply {
                        setError("Korisničko ime već postoji")
                        requestFocus()
                    }
                }
                else{
                    val user = User(username,password,email,"null")
                    registerUser(user)
                    goToLogin()
                }
            }
        }

    }

    private fun registerUser(user:User) {
        val email = user.email.toString()
        val password = user.password.toString()
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
            if(it.isSuccessful) {
                Toast.makeText(this.context, "Korisnik je uspješno registriran", Toast.LENGTH_LONG).show()
                binding.apply {
                    etUsername.text.clear()
                    etEmail.text.clear()
                    etPassword.text.clear()
                }
                addUserToDatabase(user,auth.currentUser)
            }
        }.addOnFailureListener{
            Toast.makeText(this.context,"Korisnik nije uspješno registriran. Razlog: ${it.message.toString()}",Toast.LENGTH_LONG).show() }

    }

    private fun addUserToDatabase(user: User, currentUser: FirebaseUser?) {
        currentUser?.uid?.let {
            database.child("Users").child(it).setValue(user).addOnFailureListener{
                Toast.makeText(this.context,"Korisnik nije uspješno dodan u bazu. Razlog: ${it.message.toString()}",Toast.LENGTH_LONG).show()
            }
        }

    }


    companion object {
        fun create(): Fragment {
            return RegisterFragment()
        }
    }
}