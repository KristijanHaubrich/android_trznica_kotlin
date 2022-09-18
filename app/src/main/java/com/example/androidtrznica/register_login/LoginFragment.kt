package com.example.androidtrznica.register_login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidtrznica.contacts.ContactFragment
import com.example.androidtrznica.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class LoginFragment : Fragment() {
    lateinit var binding: FragmentLoginBinding
    lateinit var database: DatabaseReference
    lateinit var fragContext:Context
    var auth = FirebaseAuth.getInstance()

    override fun onAttach(context: Context) {
        fragContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        binding.apply {
            tvRegister.setOnClickListener { register() }
            btnLogin.setOnClickListener { login() }
        }
        return binding.root
    }

    private fun login() {
        var email = binding.etEmail.text.toString().trim()
        var password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()){
            binding.etEmail.apply {
                setError("Email je obavezan")
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

        else if (password.isEmpty()) {
            binding.etPassword.apply {
                setError("Lozinka je obavezna")
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


        else{
            database = FirebaseDatabase.getInstance().reference.child("Users")

            database.addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        var isLogged = false
                        for (current:DataSnapshot in snapshot.children ){
                            if(current.exists()) {
                                var currentUser = current.getValue(User::class.java)
                                if (currentUser?.email == email && currentUser?.password == password) {
                                    binding.apply {
                                        etEmail.text.clear()
                                        etPassword.text.clear()
                                    }
                                    loginUser(currentUser)
                                    isLogged = true
                                }
                            }
                        }
                        if (!isLogged){ Toast.makeText(activity, "Netočna lozinka ili email", Toast.LENGTH_LONG).show()}
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(activity, "Neuspjela prijava : ${error.message}", Toast.LENGTH_LONG).show()
                }

            })
        }
    }

    private fun loginUser(user: User) {
        val email = user.email.toString()
        val password = user.password.toString()
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
            if (it.isSuccessful){

                if (auth.currentUser!!.isEmailVerified){
                    lifecycleScope.launchWhenResumed {
                        val action = LoginFragmentDirections.actionLoginFragmentToMainFragment()
                        findNavController().navigate(action)
                    }
                }
                else{
                    auth.currentUser?.sendEmailVerification()?.addOnFailureListener{
                        Toast.makeText(fragContext, "Verifikacijski mail nije uspješno poslan: ${it.message.toString()}", Toast.LENGTH_LONG).show()
                        Log.e("Verification",it.message.toString())
                    }
                    Toast.makeText(fragContext, "PRVA PRIJAVA: Potvrdite email koji ste dobili na vašu email adresu", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun register() {
        val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
        findNavController().navigate(action)
    }

    companion object {
        fun create(): Fragment {
            return ContactFragment()
        }
    }
}