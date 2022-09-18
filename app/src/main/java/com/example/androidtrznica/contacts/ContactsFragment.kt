package com.example.androidtrznica.contacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.androidtrznica.R
import com.example.androidtrznica.databinding.FragmentContactsBinding
import com.example.androidtrznica.product.Product
import com.example.androidtrznica.product.ProductAdapter
import com.example.androidtrznica.register_login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ContactsFragment : Fragment() {
    lateinit var binding: FragmentContactsBinding
    val database = FirebaseDatabase.getInstance().reference
    val userAuth = FirebaseAuth.getInstance().currentUser
    var userList = arrayListOf<User>()
    val fragContext = this.context


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactsBinding.inflate(layoutInflater)
        getRecyclerData()
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    val action = ContactsFragmentDirections.actionContactsFragmentToMainFragment()
                    findNavController().navigate(action)
                }
                R.id.nav_add -> {
                    val action = ContactsFragmentDirections.actionContactsFragmentToAddProductFragment()
                    findNavController().navigate(action)
                }
            }
            true
        }
        return binding.root
    }

    private fun getRecyclerData(){
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.hasFixedSize()
        setSearchView()
        fetchData()

    }

    private fun setSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                search(p0?.lowercase())
                return false
            }

        })
    }

    private fun search(newText: String?) {
        val list: ArrayList<User> = ArrayList()
        for (current in userList) {
            if (current.username!!.lowercase().contains(newText!!)) {
                list.add(current)
            }
        }
        val adapter = ContactAdapter(list)
        binding.recyclerView.setAdapter(adapter)

    }

    private fun fetchData() {
        database.child("Users").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                if (snapshot.exists()){
                    for (current in snapshot.children){
                        if (current.exists()){
                            val user = current.getValue(User::class.java)
                            if(!(userAuth?.uid == current.key)) userList.add(user!!)
                        }
                    }
                }
                binding.recyclerView.adapter = ContactAdapter(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(fragContext,"Greška prilikom učitavanja korisnika: ${error.message}",
                    Toast.LENGTH_LONG).show()
            }

        })
    }


    companion object {
        val Tag = "NewTask"

        fun create(): Fragment {
            return ContactsFragment()
        }
    }
}