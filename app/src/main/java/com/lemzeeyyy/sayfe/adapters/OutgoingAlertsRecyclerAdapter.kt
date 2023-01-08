package com.lemzeeyyy.sayfe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lemzeeyyy.sayfe.NotificationBodyClickListener
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.model.IncomingAlertData
import com.lemzeeyyy.sayfe.model.OutgoingAlertData

class OutgoingAlertsRecyclerAdapter(private val notificationBodyClickListener: NotificationBodyClickListener) : RecyclerView.Adapter<OutgoingAlertsRecyclerAdapter.OutgoingViewHolder>() {
    var dataList = listOf<OutgoingAlertData>()
    fun updateDataList(dataList : List<OutgoingAlertData> ){
        this.dataList = dataList
        notifyDataSetChanged()
    }


    class OutgoingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var alertTime = itemView.findViewById<TextView>(R.id.out_alert_time_text)
        var alertLocation = itemView.findViewById<TextView>(R.id.out_alert_location_txt)
        var alertBody = itemView.findViewById<TextView>(R.id.outgoing_alert_body)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutgoingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.outgoing_alert_item,parent,false)
        return OutgoingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OutgoingViewHolder, position: Int) {
        val dataItem = dataList[position]
        holder.alertBody.setText(dataItem.alertBody)
        holder.alertTime.setText(dataItem.timestamp)
        holder.alertLocation.setText(dataItem.location)
        holder.alertBody.setOnClickListener {
            notificationBodyClickListener.onNotificationBodyClick(it)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}