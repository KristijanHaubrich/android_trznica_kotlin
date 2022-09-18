package com.example.androidtrznica.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidtrznica.databinding.FragmentMainBinding
import com.bumptech.glide.Glide
import com.example.androidtrznica.R
import com.example.androidtrznica.product.Product
import com.example.androidtrznica.product.ProductAdapter
import com.example.androidtrznica.register_login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainFragment : Fragment() {

    lateinit var binding: FragmentMainBinding
    val database = FirebaseDatabase.getInstance().reference
    val userAuth = FirebaseAuth.getInstance().currentUser
    var productList = arrayListOf<Product>()
    val fragContext = this.context
    lateinit var user:User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(layoutInflater)
        getUserData()
        setRecycleView()
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_add -> {
                    val action = MainFragmentDirections.actionMainFragmentToAddProductFragment()
                    findNavController().navigate(action)
                }
                R.id.nav_contacts -> {
                    val action = MainFragmentDirections.actionMainFragmentToContactsFragment()
                    findNavController().navigate(action)
                }
            }
            true
        }
        binding.apply {
            btnLogout.setOnClickListener { logout() }
            ivProfilePic.setOnClickListener{ changeProfilePic() }
            btnDelete.setOnClickListener { delete() }
        }

        return binding.root
    }

    private fun getUserData(){
        database.child("Users").child(userAuth?.uid!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                  user = snapshot.getValue(User::class.java)!!
                    getProductData()
                    Glide.with(binding.ivProfilePic.context).clear(binding.ivProfilePic)
                    Glide.with(binding.ivProfilePic.context).load(user.profilePic).placeholder(R.drawable.ic_contacts_dark).into(binding.ivProfilePic)
                    binding.tvUsername.text = user.username
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(fragContext,"Greška prilikom učitavanja proizvoda: ${error.message}",Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun setRecycleView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.hasFixedSize()
        setSearchView()
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
        val list: ArrayList<Product> = ArrayList()
        for (current in productList) {
            if (current.name!!.lowercase().contains(newText!!)) {
                list.add(current)
            }
        }
        val adapter = ProductAdapter(list)
        binding.recyclerView.setAdapter(adapter)

    }

    private fun getProductData() {
        database.child("Products").child(user.username.toString()).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    productList.clear()
                    for (current:DataSnapshot in snapshot.children){
                        if (current.exists()){
                            val currentProduct = current.getValue(Product::class.java)

                            productList.add(currentProduct!!)
                        }
                    }
                    binding.recyclerView.adapter = ProductAdapter(productList)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(fragContext,"Greška prilikom učitavanja proizvoda: ${error.message}",Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun changeProfilePic() {
        val action = MainFragmentDirections.actionMainFragmentToChangeProfilePicFragment()
        findNavController().navigate(action)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val action = MainFragmentDirections.actionMainFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    private fun delete(){
        val action = MainFragmentDirections.actionMainFragmentToDeleteFragment(user.username?:"null",userAuth?.uid?:"null")
        findNavController().navigate(action)
    }





    companion object {

        fun create(): Fragment {
            return MainFragment()
        }
    }
}