package github.yaa110.memento.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import github.yaa110.memento.R
import github.yaa110.memento.model.Folder

class FolderAdapter(private val items: ArrayList<Folder>?, private val listener: ClickListener) :
    RecyclerView.Adapter<FolderAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items!![position]

        holder.title.text = item.name
        if (item.isDirectory) {
            holder.icon.setImageResource(if (item.isBack) R.drawable.back_folder else R.drawable.ic_folder)
        } else {
            holder.icon.setImageResource(R.drawable.ic_file)
        }

        holder.holder.setOnClickListener { view: View? -> listener.onClick(item) }
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    interface ClickListener {
        fun onClick(item: Folder)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var holder: View = itemView.findViewById(R.id.holder)
        var title: TextView = itemView.findViewById(R.id.title)
        var icon: ImageView =
            itemView.findViewById(R.id.icon)
    }
}
