package com.lemzeeyyy.sayfe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lemzeeyyy.sayfe.NotificationBodyClickListener
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.model.IncomingAlertData

class IncomingAlertsRecyclerAdapter(private val notificationBodyClickListener: NotificationBodyClickListener) : RecyclerView.Adapter<IncomingAlertsRecyclerAdapter.IncomingViewHolder>() {

    var dataList = listOf<IncomingAlertData>()

    fun updateDataList(dataList : List<IncomingAlertData> ){
        this.dataList = dataList
        notifyDataSetChanged()
    }


    class IncomingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contactName = itemView.findViewById<TextView>(R.id.name_alert)
        var alertTime = itemView.findViewById<TextView>(R.id.alert_time_text)
        var alertLocation = itemView.findViewById<TextView>(R.id.alert_location_txt)
        var alertBody = itemView.findViewById<TextView>(R.id.alert_body)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.incoming_alert_item,parent,false)
        return IncomingViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomingViewHolder, position: Int) {
       val dataItem = dataList[position]
        holder.alertBody.setText(dataItem.alertBody)
        holder.alertTime.setText(dataItem.timestamp)
        holder.alertLocation.setText(dataItem.location)
        holder.contactName.setText(dataItem.senderName)
        holder.alertBody.setOnClickListener {
            notificationBodyClickListener.onNotificationBodyClick(it)
        }
    }

    override fun getItemCount(): Int {
       return dataList.size
    }
}