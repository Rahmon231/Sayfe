package com.lemzeeyyy.sayfe.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.CheckedContactListener
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.database.SharedPrefs
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.RecipientContact

class PhonebookRecyclerAdapter(private val checkedContactListener: CheckedContactListener,private val context: Context)
    : RecyclerView.Adapter<PhonebookRecyclerAdapter.PhonebookViewHolder>() {


    private lateinit var fAuth: FirebaseAuth

    private val database = Firebase.firestore

    private val collectionReference = database.collection("Guardian Angels")

    var phoneBookData = listOf<RecipientContact>()

    var checkedList : MutableList<RecipientContact> = ArrayList()



    fun updatePhonebookData(phoneBookData : List<RecipientContact> ){
        this.phoneBookData = phoneBookData
        notifyDataSetChanged()
    }

    inner class PhonebookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var phoneNumber = itemView.findViewById<TextView>(R.id.contact_phone_id)
        var phoneName = itemView.findViewById<TextView>(R.id.contact_name_id)
        var checkBox = itemView.findViewById<CheckBox>(R.id.choose_contact_checkbox)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhonebookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item,parent,false)
        fAuth = Firebase.auth
        fAuth.currentUser?.uid?.let {
            collectionReference.document(it)
                .get()
                .addOnSuccessListener {
                    val data = it.toObject(GuardianData::class.java)
                    if (data != null) {
                        checkedList = data.guardianInfo
                    }

                }
        }

        return PhonebookViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhonebookViewHolder, position: Int) {
        val item = phoneBookData[position]
        holder.phoneNumber.setText(item.number)
        holder.phoneName.setText(item.name)



        if(phoneBookData.isNotEmpty() && phoneBookData != null ){
            Log.d("TAG", "onBindViewHolder: ${checkedList.size} ")
//                holder.checkBox.setOnClickListener {
//                    if (holder.checkBox.isChecked){
//                            if (checkedList.size < 5) {
//                                checkedList.add(phoneBookData[position])
//                                    Log.d("ADDED", "onBindViewHolder: ${checkedList.toString()} ")
//                                    Log.d("ADDED", "onBindViewHolder: ${checkedList.size} ")
//
//                            }else{
//                                holder.checkBox.isChecked = false
//                                Toast.makeText(context,"You can only add max of 5 angelis",Toast.LENGTH_SHORT)
//                                    .show()
//                            }
//                        }
//                        else{
//                            holder.checkBox.isEnabled = true
//                            checkedList.remove(phoneBookData[position])
//                            Log.d("REMOVED", "onBindViewHolder: ${checkedList.toString()} ")
//                            Log.d("REMOVED", "onBindViewHolder: ${checkedList.size} ")
//                        }
//                    checkedContactListener.onContactClick(checkedList)
//                }
            holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (holder.checkBox.isChecked){
                            if (checkedList.size < 5) {

                                phoneBookData[position].number  = phoneBookData[position].number.filter {
                                    !it.isWhitespace()
                                }.takeLast(10)

                                checkedList.add(phoneBookData[position])
                                Log.d("ADDED", "onBindViewHolder: ${checkedList.size} ")

                            }else{
                                holder.checkBox.isChecked = false
                                Toast.makeText(context,"You can only add max of 5 angelis",Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        else{
                            checkedList.remove(phoneBookData[position])
                            holder.checkBox.isChecked = false


                    Log.d("REMOVED", "onBindViewHolder: ${checkedList.size} ")
                        }
                    checkedContactListener.onContactClick(checkedList)

            }
        }
    }

    override fun getItemCount(): Int {
      return phoneBookData.size
    }


}