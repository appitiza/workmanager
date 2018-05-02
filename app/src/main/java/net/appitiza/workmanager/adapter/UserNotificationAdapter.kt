package net.appitiza.workmanager.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_users_notification.view.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.ui.activities.interfaces.NotificationClick
import net.appitiza.workmanager.model.NotificationData
import net.appitiza.workmanager.utils.Utils

class UserNotificationAdapter(private val mList: ArrayList<NotificationData>, private val callback: NotificationClick) : RecyclerView.Adapter<UserNotificationAdapter.NotificationHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_users_notification, parent, false)
        return NotificationHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.bindItems(mList[position])
        holder.itemView.setOnClickListener {
            callback.onClick(mList[position])
        }
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return mList.size
    }

    //the class is hodling the list view
    class NotificationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(data: NotificationData) {
            itemView.tv_notification_title.text = data.title
            itemView.tv_notification_message.text = data.message
            itemView.tv_notification_time.text =  Utils.convertDate(Utils.getDateTimestamp(data.time).time, "dd MMM yyyy")



        }
    }

}