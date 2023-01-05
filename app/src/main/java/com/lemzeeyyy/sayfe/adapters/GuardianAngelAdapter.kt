package com.lemzeeyyy.sayfe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.model.RecipientContact

class GuardianAngelAdapter : RecyclerView.Adapter<GuardianAngelAdapter.GuardianAngelViewHolder>() {
    var guardianList : MutableList<RecipientContact> = ArrayList()
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Guardian Angels")

    fun updateGuardianAngelsList(guardianList: MutableList<RecipientContact>){
        this.guardianList = guardianList
        notifyDataSetChanged()
    }


    inner class GuardianAngelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var phoneNumber = itemView.findViewById<TextView>(R.id.contact_phone_id)
        var phoneName = itemView.findViewById<TextView>(R.id.contact_name_id)
        var checkBox = itemView.findViewById<CheckBox>(R.id.choose_contact_checkbox)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianAngelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item,parent,false)
        return GuardianAngelViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuardianAngelViewHolder, position: Int) {
        val item = guardianList[position]


        holder.phoneNumber.setText(item.number)
        holder.phoneName.setText(item.name)
        holder.checkBox.visibility = View.INVISIBLE
    }

    override fun getItemCount(): Int {
       return guardianList.size
    }

}