package net.appitiza.workmanager.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_admin_change_request.view.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.ui.activities.interfaces.ChangeRequestClick
import net.appitiza.workmanager.model.ChangeDeviceRequestData
import net.appitiza.workmanager.utils.Utils

class AdminChangeDeviceRequestAdapter(private val mContext : Context, private val mList: ArrayList<ChangeDeviceRequestData>, private val callback: ChangeRequestClick) : RecyclerView.Adapter<AdminChangeDeviceRequestAdapter.RequestHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_change_request, parent, false)
        return RequestHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: RequestHolder, position: Int) {
        holder.bindItems(mContext,mList[position])
        holder.itemView.setOnClickListener {
            if(mList[position].status == "Pending")
            callback.onClick(mList[position])
        }
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return mList.size
    }

    //the class is hodling the list view
    class RequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(context : Context,data: ChangeDeviceRequestData) {
            itemView.tv_name.text = data.displayName
            itemView.tv_imei.text = context.getString(R.string.current_new_imei,data.current_imei,data.imei)
            itemView.tv_reason.text = context.getString(R.string.changerequest_reason,data.reason)
            itemView.tv_time.text =  Utils.convertDate(Utils.getDateTimestamp(data.time).time, "dd MMM yyyy")
            itemView.tv_status.text = data.status


        }
    }

}