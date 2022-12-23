package com.lemzeeyyy.sayfe

import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.util.forEach
import androidx.recyclerview.widget.RecyclerView
import com.lemzeeyyy.sayfe.model.RecipientContact

class PhonebookRecyclerAdapter : RecyclerView.Adapter<PhonebookRecyclerAdapter.PhonebookViewHolder>() {
    var phoneBookData = listOf<RecipientContact>()
    var checkBoxStateArray = SparseBooleanArray()
    companion object{
        var checkedList : MutableList<RecipientContact> = ArrayList()
    }


    fun updatePhonebookData(phoneBookData : List<RecipientContact> ){
        this.phoneBookData = phoneBookData
        notifyDataSetChanged()
    }

    inner class PhonebookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var phoneNumber = itemView.findViewById<TextView>(R.id.contact_phone_id)
        var phoneName = itemView.findViewById<TextView>(R.id.contact_name_id)
        var checkBox = itemView.findViewById<CheckBox>(R.id.choose_contact_checkbox)

        init {

            checkBox.setOnClickListener {
                if (!checkBoxStateArray.get(adapterPosition, false)) {
                    checkBox.isChecked = true
                    checkBoxStateArray.put(adapterPosition, true)

                    if (checkedList.size>=0){
                        checkedList.add(phoneBookData[adapterPosition])
                    }

                } else {
                    checkBox.isChecked = false
                    checkBoxStateArray.put(adapterPosition, false)

                    checkedList.remove(phoneBookData[adapterPosition])

                       // checkedList.removeAt(adapterPosition)

                }



            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhonebookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item,parent,false)
        return PhonebookViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhonebookViewHolder, position: Int) {
        val item = phoneBookData[position]

        holder.checkBox.isChecked = checkBoxStateArray.get(position,false)

        holder.phoneNumber.setText(item.number)
        holder.phoneName.setText(item.name)
    }

    override fun getItemCount(): Int {
      return phoneBookData.size
    }
}