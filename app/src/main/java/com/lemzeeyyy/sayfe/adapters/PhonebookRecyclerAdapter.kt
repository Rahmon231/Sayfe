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


    var checkedListFromDb : MutableList<PhonebookContact> = mutableListOf()
    private var checkedList : MutableList<PhonebookContact> = mutableListOf()


    fun updatePhonebookData(phoneBookData : List<PhonebookContact> ){
        this.phoneBookData = phoneBookData
        notifyDataSetChanged()
    }

    fun triggerCheckedListInterface(){
        checkedContactListener.onContactClick(checkedList,checkedListFromDb)
        Toast.makeText(context,"Triggered",Toast.LENGTH_SHORT).show()
    }


    inner class PhonebookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var phoneNumber = itemView.findViewById<TextView>(R.id.contact_phone_id)
        var phoneName = itemView.findViewById<TextView>(R.id.contact_name_id)
        var checkBox = itemView.findViewById<CheckBox>(R.id.choose_contact_checkbox)



        init {
            fAuth = Firebase.auth
            val user = fAuth.currentUser
            val currentUserId = user?.uid
            if (currentUserId != null) {
                collectionReference.document(currentUserId)
                    .get()
                    .addOnSuccessListener {
                        val docSnapshot = it.toObject(GuardianData::class.java)
                        checkedListFromDb = docSnapshot?.guardianInfo ?: mutableListOf()
                        checkBox.setOnClickListener {

                            if (checkBox.isChecked ){
                                if ((checkedListFromDb.size + checkedList.size) < 5){


                                phoneBookData[adapterPosition].isChecked = true
                                phoneBookData[adapterPosition].number =   phoneBookData[adapterPosition].number.filter {
                                    !it.isWhitespace()
                                }

                                phoneBookData[adapterPosition].number = phoneBookData[adapterPosition].number.filter {
                                    it.isDigit()
                                }

                                    checkedList.add(phoneBookData[adapterPosition])

                                }else{
                                    checkBox.isChecked = false
                                    phoneBookData[adapterPosition].isChecked = false
                                    Toast.makeText(context,"You can only add max of 5 guardian angels",Toast.LENGTH_SHORT).show()
                                }


                            }
                            else{
                                checkBox.isChecked = false
                                phoneBookData[adapterPosition].isChecked = false
                                checkedList.remove(phoneBookData[adapterPosition])
                            }
                        }
                    }
                checkedList = mutableListOf()
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhonebookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item,parent,false)
        return PhonebookViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhonebookViewHolder, position: Int) {
        val checkedItem = phoneBookData[position]
        holder.phoneNumber.setText(checkedItem.number)
        holder.phoneName.setText(checkedItem.name)

        holder.checkBox.isChecked = checkedItem.isChecked
    }


    override fun getItemCount(): Int {
      return phoneBookData.size
    }
}