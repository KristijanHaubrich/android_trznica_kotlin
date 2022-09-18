package com.example.androidtrznica.deleteProduct

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.androidtrznica.contacts.ContactFragment
import com.example.androidtrznica.databinding.FragmentDeleteProductBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class DeleteProductFragment : Fragment() {
    lateinit var binding: FragmentDeleteProductBinding
    private val args: DeleteProductFragmentArgs by navArgs()
    lateinit var owner:String
    lateinit var productName:String
    lateinit var productPic:String
    var store = FirebaseStorage.getInstance()
    var database = FirebaseDatabase.getInstance().getReference()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeleteProductBinding.inflate(layoutInflater)
        productName = args.productName
        owner = args.productOwner
        productPic = args.productPic
        binding.apply {
            tvBanner.setText("Sigurno želite obrisati proizvod ${productName}?")
            btnBack.setOnClickListener { goBack() }
            btnDelete.setOnClickListener { deleteProduct() }
        }
        return binding.root
    }

    private fun goBack() {
        val action = DeleteProductFragmentDirections.actionDeleteProductFragmentToMainFragment()
        findNavController().navigate(action)
    }

    private fun deleteProduct() {
        if (productPic != "null"){
            store.getReferenceFromUrl(productPic).delete().addOnSuccessListener {
                database.child("Products").child(owner).child(productName).removeValue().addOnSuccessListener {
                    Toast.makeText(this.context,"Proizvod je uspješno obrisan.", Toast.LENGTH_LONG).show()
                }
            }
        }
        goBack()
    }


    companion object {
        val Tag = "NewTask"

        fun create(): Fragment {
            return ContactFragment()
        }
    }
}