package net.appitiza.workmanager.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import net.appitiza.workmanager.R

class SiteTypesAdapter(context: Context, private var typeList: ArrayList<String>) : BaseAdapter() {

    private var context: Context? = context

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val view: View?
        val vh: SiteTypeHolder

        if (convertView == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            view = inflater.inflate(R.layout.item_admin_site_type, parent, false)
            vh = SiteTypeHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as SiteTypeHolder
        }

        vh.tvTitle.text = typeList[position]

        return view
    }

    override fun getItem(position: Int): Any {
        return typeList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return typeList.size
    }
}

private class SiteTypeHolder(view: View?) {
    val tvTitle: TextView = view?.findViewById(R.id.tv_site_type) as TextView

}
