package github.yaa110.memento.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import github.yaa110.memento.R
import github.yaa110.memento.adapter.template.ModelAdapter
import github.yaa110.memento.model.Category
import github.yaa110.memento.widget.CategoryViewHolder

class CategoryAdapter(
    items: ArrayList<Category>,
    selected: ArrayList<Category>,
    listener: ClickListener<Category>
) : ModelAdapter<Category, CategoryViewHolder>(items, selected, listener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        )
    }
}
