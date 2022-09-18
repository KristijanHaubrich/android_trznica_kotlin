package com.example.androidtrznica.contacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.androidtrznica.R
import com.example.androidtrznica.databinding.FragmentContactBinding
import com.example.androidtrznica.product.Product
import com.example.androidtrznica.product.ProductAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ContactFragment : Fragment() {
    lateinit var binding: FragmentContactBinding
    var productList = arrayListOf<Product>()
    val database = FirebaseDatabase.getInstance().reference
    private val args: ContactFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactBinding.inflate(layoutInflater)
        setRecycleView()
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    val action = ContactFragmentDirections.actionContactFragmentToMainFragment()
                    findNavController().navigate(action)
                }
                R.id.nav_add -> {
                    val action = ContactFragmentDirections.actionContactFragmentToAddProductFragment()
                    findNavController().navigate(action)
                }
                R.id.nav_contacts -> {
                    val action = ContactFragmentDirections.actionContactFragmentToContactsFragment()
                    findNavController().navigate(action)
                }
            }
            true
        }

        Glide.with(binding.ivProfilePic.context).clear(binding.ivProfilePic)
        Glide.with(binding.ivProfilePic.context).load(args.profilePic).placeholder(R.drawable.ic_contacts_dark).into(binding.ivProfilePic)

        binding.tvUsername.text =  args.username

        return binding.root
    }

    private fun setRecycleView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.hasFixedSize()
        fetchData()
        setSearchView()
    }

    private fun fetchData() {
        database.child("Products").child(args.username).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    productList.clear()
                    for (current: DataSnapshot in snapshot.children){
                        if (current.exists()){
                            val currentProduct = current.getValue(Product::class.java)

                            productList.add(currentProduct!!)
                        }
                    }
                    binding.recyclerView.adapter = ProductAdapter(productList)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity,"Greška prilikom učitavanja proizvoda: ${error.message}",
                    Toast.LENGTH_LONG).show()
            }

        })
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

    companion object {

        fun create(): Fragment {
            return ContactFragment()
        }
    }
}