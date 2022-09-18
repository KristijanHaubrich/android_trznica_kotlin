package com.example.androidtrznica.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidtrznica.R
import com.example.androidtrznica.deleteUser.DeleteFragmentArgs
import com.example.androidtrznica.register_login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ContactAdapter(private val userList: ArrayList<User> ): RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    val database = FirebaseDatabase.getInstance().reference
    val userAuth = FirebaseAuth.getInstance().currentUser


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_item,parent,false)
        return ContactViewHolder(itemView)
    }

    private fun goToUserProfile(itemView: View?, username:String, profilePic:String) {
        val action = ContactsFragmentDirections.actionContactsFragmentToContactFragment(username,profilePic)
        itemView?.findNavController()?.navigate(action)

    }


    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentContact = userList[position]
        holder.tv_username.text = currentContact.username
        holder.tv_username.setOnClickListener { view -> goToUserProfile(view,currentContact.username!!,currentContact.profilePic!!) }
        Glide.with(holder.iv_profilePic).clear(holder.iv_profilePic)
        Glide.with(holder.iv_profilePic).load(currentContact.profilePic).placeholder(R.drawable.ic_contacts).into(holder.iv_profilePic)

    }



    override fun getItemCount(): Int {
        return userList.size
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tv_username = itemView.findViewById<TextView>(R.id.rvu_tv_name)
        val iv_profilePic = itemView.findViewById<ImageView>(R.id.rvu_profile_image)
    }



}