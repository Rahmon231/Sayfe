package com.lemzeeyyy.sayfe.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.CheckedContactListener
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.PhonebookContact
import com.lemzeeyyy.sayfe.model.RecipientContact


class PhonebookRecyclerAdapter(private val checkedContactListener: CheckedContactListener,private val context: Context)
    : RecyclerView.Adapter<PhonebookRecyclerAdapter.PhonebookViewHolder>() {

    private lateinit var fAuth: FirebaseAuth

    private val database = Firebase.firestore

    private val collectionReference = database.collection("Guardian Angels")

    private var phoneBookData = listOf<PhonebookContact>()


    var checkedListFromDb : MutableList<RecipientContact> = mutableListOf()
    private var checkedList : MutableList<RecipientContact> = mutableListOf()


    fun updatePhonebookData(phoneBookData : List<PhonebookContact> ){
        this.phoneBookData = phoneBookData
        notifyDataSetChanged()
    }

    fun triggerCheckedListInterface(){
        checkedContactListener.onContactClick(checkedList)
    }


    inner class PhonebookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var phoneNumber = itemView.findViewById<TextView>(R.id.contact_phone_id)
        var phoneName = itemView.findViewById<TextView>(R.id.contact_name_id)
        var checkBox = itemView.findViewById<CheckBox>(R.id.choose_contact_checkbox)

        init {
            checkBox.setOnClickListener {
                if (checkBox.isChecked ){
                    phoneBookData[adapterPosition].isChecked = true
                }
                else{
                    checkBox.isChecked = false
                    phoneBookData[adapterPosition].isChecked = false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhonebookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item,parent,false)
        fAuth = Firebase.auth
        fAuth.currentUser?.uid?.let {
            collectionReference.document(it)
                .get()
                .addOnSuccessListener {documentSnapshot->
                    val data = documentSnapshot.toObject(GuardianData::class.java)
                    if (data != null) {
                        checkedListFromDb = data.guardianInfo
                    }

                }
        }
            return PhonebookViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhonebookViewHolder, position: Int) {
        val checkedItem = phoneBookData[position]
        holder.phoneNumber.setText(checkedItem.number)
        holder.phoneName.setText(checkedItem.name)

        if (phoneBookData[position].isChecked){

        }
        else{

        }


//            holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
//                if (isChecked){
//
//                    checkedListFromDb.addAll(checkedList)
//                            if (checkedListFromDb.size < 5) {
//                                buttonView.isChecked = true
//                                checkedItem.number  = checkedItem.number.filter {
//                                    !it.isWhitespace()
//                                }.takeLast(10)
//                                checkedList.add(checkedItem)
//                            }else{
//                                buttonView.isChecked = false
//                                Toast.makeText(context,"You can only add max of 5 angelis",Toast.LENGTH_SHORT)
//                                    .show()
//
//                            }
//                }
//                else{
//                    checkedList.remove(checkedItem)
//                    buttonView.isChecked = false
//                }
//            }
            checkedList = mutableListOf()
        }

        //set empty state


    override fun getItemCount(): Int {
      return phoneBookData.size
    }
}