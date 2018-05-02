package net.appitiza.workmanager.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_users_history.view.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.model.CurrentCheckIndata
import net.appitiza.workmanager.utils.Utils
import java.util.*

class UserHistoryAdapter(private var mContext: Context, private val mList: ArrayList<CurrentCheckIndata>, val salary :Int) : RecyclerView.Adapter<UserHistoryAdapter.NotificationHolder>() {
    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_users_history, parent, false)
        return NotificationHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.bindItems(mContext, mList[position],salary)

    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return mList.size
    }

    //the class is hodling the list view
    class NotificationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(mContext: Context, data: CurrentCheckIndata,salary :Int) {
            itemView.tv_historyitem_date.text = Utils.convertDate(data.checkintime!!.toLong(), "dd MMM yyyy")
            itemView.tv_historyitem_site.text = data.sitename


            if (data.checkintime != 0L) {
                if (data.checkouttime != 0L) {

                    val expected = ((data.checkouttime!!.toLong() - data.checkintime!!.toLong()) / (60L * 60L * 1000L)) * salary
                    itemView.tv_historyitem_hours.text = Utils.convertHours((data.checkouttime!!.toLong() - data.checkintime!!.toLong()))
                    if (data.payment.toString() != "null") {

                        if (data.payment!!.toInt() > expected) {
                            itemView.tv_historyitem_payment.text = mContext.getString(R.string.payment_info, data.payment!!.toInt(),expected, 0)


                        } else {
                            itemView.tv_historyitem_payment.text = mContext.getString(R.string.payment_info, data.payment!!.toInt(),expected, expected - data.payment!!.toInt())

                        }
                    } else {
                        itemView.tv_historyitem_payment.text = mContext.getString(R.string.not_checked_out)
                    }
                } else {
                    itemView.tv_historyitem_hours.text = mContext.getString(R.string.not_checked_out)
                }
            }


        }

    }


}