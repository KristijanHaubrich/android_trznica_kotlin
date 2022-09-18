package com.example.androidtrznica.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidtrznica.R
import com.example.androidtrznica.register_login.User
import com.example.androidtrznica.userProfile.MainFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class ProductAdapter(private val productList: ArrayList<Product> ): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    val database = FirebaseDatabase.getInstance().reference
    val userAuth = FirebaseAuth.getInstance().currentUser
    var profilePic:String = "null"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.product_item,parent,false)
        return ProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = productList[position]
        getProfilePic(holder,currentProduct)

        database.child("Users").child(userAuth?.uid!!).child("username").get().addOnSuccessListener {
            if (it.exists()){
                if (currentProduct.owner == it.getValue(String::class.java)) holder.iv_delete.visibility = View.VISIBLE
            }
        }

        holder.tv_nameOfProduct.text = currentProduct.name
        holder.tv_descripiton.text = "Opis: " + currentProduct.description
        holder.tv_price.text = "Cijena: " + currentProduct.price

        Glide.with(holder.iv_productPic.context).clear(holder.iv_productPic)
        Glide.with(holder.iv_productPic.context).load(currentProduct.pic).placeholder(R.drawable.ic_app_icon_bright).into(holder.iv_productPic)

        holder.iv_delete.setOnClickListener { view -> deleteProduct(view,currentProduct.name,currentProduct.owner,currentProduct.pic) }

    }

    private fun deleteProduct(view: View?, name: String?, owner: String?, pic: String?) {
        val action = MainFragmentDirections.actionMainFragmentToDeleteProductFragment(name?: "null",owner?: "null",pic?: "null")
        view?.findNavController()?.navigate(action)
    }

    private fun getProfilePic(holder: ProductViewHolder, currentProduct: Product) {
        database.child("Users").get().addOnSuccessListener {
            if (it.exists()){
                for(current:DataSnapshot in it.children){
                    if (current.exists()){
                        val user = current.getValue(User::class.java)
                        if (user?.username.toString() == currentProduct.owner){
                            profilePic = user?.profilePic!!
                            Glide.with(holder.iv_profilePic.context).clear(holder.iv_profilePic)
                            Glide.with(holder.iv_profilePic.context).load(profilePic).placeholder(R.drawable.ic_contacts).into(holder.iv_profilePic)
                        }
                    }

                }
            }

        }
    }


    override fun getItemCount(): Int {
        return productList.size
    }

    class ProductViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){

        val tv_nameOfProduct = itemView.findViewById<TextView>(R.id.rv_tv_name)
        val tv_descripiton = itemView.findViewById<TextView>(R.id.rv_tv_description)
        val tv_price = itemView.findViewById<TextView>(R.id.rv_tv_price)

        val iv_profilePic = itemView.findViewById<ImageView>(R.id.rv_profile_image)
        val iv_productPic = itemView.findViewById<ImageView>(R.id.rv_iv_product_image)
        val iv_delete = itemView.findViewById<ImageView>(R.id.rv_iv_delete)

    }






}



