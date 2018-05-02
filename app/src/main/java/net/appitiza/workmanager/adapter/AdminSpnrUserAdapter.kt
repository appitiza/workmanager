package net.appitiza.workmanager.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import net.appitiza.workmanager.R
import net.appitiza.workmanager.ui.activities.interfaces.UserClick
import net.appitiza.workmanager.model.UserListdata

class AdminSpnrUserAdapter(context: Context, typeList: ArrayList<UserListdata>, callback: UserClick) : BaseAdapter() {

    private var userList = typeList
    private var context: Context? = context
    private var callback: UserClick? = callback

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val view: View?
        val vh: UserHolder

        if (convertView == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            view = inflater.inflate(R.layout.item_admin_spnr_user, parent, false)
            vh = UserHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as UserHolder
        }

        vh.tvTitle.text = userList[position].username
        return view
    }

    override fun getItem(position: Int): Any {
        return userList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return userList.size
    }
}

private class UserHolder(view: View?) {
    val tvTitle: TextView = view?.findViewById(R.id.tv_spnr_user_name) as TextView

}
