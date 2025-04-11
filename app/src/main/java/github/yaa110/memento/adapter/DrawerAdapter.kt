package github.yaa110.memento.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import github.yaa110.memento.R
import github.yaa110.memento.model.Drawer

class DrawerAdapter(private val context: Context, private val listener: ClickListener) :
    BaseAdapter() {
    private lateinit var drawers: Array<Drawer>

    init {
        populate()
    }

    private fun populate() {
        drawers = arrayOf(
            Drawer(
                Drawer.TYPE_BACKUP,
                R.drawable.drawer_backup,
                R.string.backup),
            Drawer(
                Drawer.TYPE_RESTORE,
                R.drawable.drawer_restore,
                R.string.restore),
            Drawer.divider(),
            Drawer(
                Drawer.TYPE_ABOUT,
                R.drawable.drawer_about,
               R.string.about
            )
        )
    }

    override fun getCount(): Int {
        return drawers.size
    }

    override fun getItem(i: Int): Drawer {
        return drawers[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (drawers[position].type > 0) 1 else 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val drawer = getItem(position)
        var holder: ViewHolder? = null

        if (convertView == null) {
            val inflator =
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            when (drawer.type) {
                Drawer.TYPE_SPLITTER -> convertView =
                    inflator.inflate(R.layout.drawer_separator, parent, false)

                else -> {
                    convertView = inflator.inflate(R.layout.drawer_item, parent, false)
                    holder = ViewHolder()
                    holder.icon = convertView.findViewById(R.id.icon)
                    holder.title = convertView.findViewById(R.id.title)
                    holder.item = convertView.findViewById(R.id.item)
                    convertView.tag = holder
                }
            }
        }

        if (drawer.type > 0) {
            if (holder == null) {
                holder = convertView!!.tag as ViewHolder
            }

            holder.title!!.setText(drawer.title)
            holder.item!!.setOnClickListener { view: View? -> listener.onClick(drawer.type) }
            holder.icon!!.setImageResource(drawer.resId)
        }

        return convertView!!
    }

    override fun isEnabled(position: Int): Boolean {
        return drawers[position].type > 1
    }

    interface ClickListener {
        fun onClick(type: Int)
    }

    private inner class ViewHolder {
        var item: View? = null
        var title: TextView? = null
        var icon: ImageView? = null
    }
}
