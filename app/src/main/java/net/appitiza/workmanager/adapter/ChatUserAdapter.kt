package net.appitiza.workmanager.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_users.view.*
import kotlinx.android.synthetic.main.item_users_notification.view.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.ui.activities.interfaces.NotificationClick
import net.appitiza.workmanager.model.NotificationData
import net.appitiza.workmanager.model.UserListdata
import net.appitiza.workmanager.ui.activities.interfaces.ChatUserClick
import net.appitiza.workmanager.utils.Utils

class ChatUserAdapter(private val mList: ArrayList<UserListdata>, private val callback: ChatUserClick) : RecyclerView.Adapter<ChatUserAdapter.ChatUserHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return ChatUserHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ChatUserHolder, position: Int) {
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
    class ChatUserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(data: UserListdata) {
            itemView.tv_item_user_name.text = data.username
            itemView.tv_item_user_status.text = data.status
            itemView.tv_item_user_seen.text =  Utils.convertDate(data.time, "dd MMM yyyy")



        }
    }

}