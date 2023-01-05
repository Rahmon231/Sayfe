package com.lemzeeyyy.sayfe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lemzeeyyy.sayfe.CheckedContactListener
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.model.RecipientContact

class PhonebookRecyclerAdapter(private val checkedContactListener: CheckedContactListener) : RecyclerView.Adapter<PhonebookRecyclerAdapter.PhonebookViewHolder>() {



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
        return PhonebookViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhonebookViewHolder, position: Int) {
        val item = phoneBookData[position]
        holder.phoneNumber.setText(item.number)
        holder.phoneName.setText(item.name)

        if(phoneBookData.isNotEmpty() && phoneBookData != null){
            holder.checkBox.setOnClickListener {
                if (holder.checkBox.isChecked){
                    checkedList.add(phoneBookData[position])
                }else{
                    checkedList.remove(phoneBookData[position])
                }
                checkedContactListener.onContactClick(checkedList)
            }
        }
    }

    override fun getItemCount(): Int {
      return phoneBookData.size
    }


}