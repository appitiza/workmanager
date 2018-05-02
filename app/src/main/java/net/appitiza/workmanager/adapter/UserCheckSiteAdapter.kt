package net.appitiza.workmanager.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import net.appitiza.workmanager.R
import net.appitiza.workmanager.ui.activities.interfaces.UserSiteClick
import net.appitiza.workmanager.model.SiteListdata

class UserCheckSiteAdapter(context: Context, private var siteList: ArrayList<SiteListdata>, callback: UserSiteClick) : BaseAdapter() {

    private var context: Context? = context
    private var callback: UserSiteClick? = callback

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val view: View?
        val vh: UserCheckInSiteHolder

        if (convertView == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            view = inflater.inflate(R.layout.item_users_checkin_sitelist, parent, false)
            vh = UserCheckInSiteHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as UserCheckInSiteHolder
        }

        vh.tvTitle.text = siteList[position].sitename
        return view
    }

    override fun getItem(position: Int): Any {
        return siteList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return siteList.size
    }
}

private class UserCheckInSiteHolder(view: View?) {
    val tvTitle: TextView = view?.findViewById(R.id.tv_checkin_site_item_name) as TextView

}
