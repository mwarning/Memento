package github.yaa110.memento.fragment

import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import github.yaa110.memento.R
import github.yaa110.memento.activity.CategoryActivity
import github.yaa110.memento.adapter.CategoryAdapter
import github.yaa110.memento.adapter.template.ModelAdapter
import github.yaa110.memento.db.OpenHelper
import github.yaa110.memento.fragment.template.RecyclerFragment
import github.yaa110.memento.model.Category
import github.yaa110.memento.model.DatabaseModel

class MainFragment : RecyclerFragment<Category, CategoryAdapter>() {
    private var categoryDialogTheme: Int = Category.THEME_GREEN

    override val listener: ModelAdapter.ClickListener<DatabaseModel> =
        object : ModelAdapter.ClickListener<DatabaseModel> {
            override fun onClick(item: DatabaseModel, position: Int) {
                val intent = Intent(context, CategoryActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra(OpenHelper.COLUMN_ID, item.id)
                intent.putExtra(OpenHelper.COLUMN_TITLE, item.title)
                intent.putExtra(OpenHelper.COLUMN_THEME, item.theme)
                startActivityForResult(intent, CategoryActivity.REQUEST_CODE)
            }

            override fun onChangeSelection(haveSelected: Boolean) {
                toggleSelection(haveSelected)
            }

            override fun onCountSelection(count: Int) {
                onChangeCounter(count)
                activity!!.toggleOneSelection(count <= 1)
            }
        }

    override fun onClickFab() {
        categoryDialogTheme = Category.THEME_GREEN
        displayCategoryDialog(
            R.string.new_category,
            R.string.create,
            "",
            DatabaseModel.NEW_MODEL_ID,
            0
        )
    }

    fun onEditSelected() {
        if (selected.isNotEmpty()) {
            val item = selected.removeAt(0)!!
            val position = items!!.indexOf(item)
            refreshItem(position)
            toggleSelection(false)
            categoryDialogTheme = item.theme
            displayCategoryDialog(
                R.string.edit_category,
                R.string.edit,
                item.title,
                item.id,
                position
            )
        }
    }

    private fun displayCategoryDialog(
        @StringRes title: Int,
        @StringRes positiveText: Int,
        categoryTitle: String?,
        categoryId: Long,
        position: Int
    ) {
        val dialog = MaterialDialog.Builder(requireContext())
            .title(title)
            .positiveText(positiveText)
            .negativeText(R.string.cancel)
            .negativeColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
            .onPositive { dialog1: MaterialDialog, which: DialogAction? ->
                var inputTitle =
                    (dialog1.customView!!.findViewById<View>(R.id.title_txt) as EditText).text.toString()
                if (inputTitle.isEmpty()) {
                    inputTitle = "Untitled"
                }

                val category = Category()
                category.id = categoryId

                val isEditing = categoryId != DatabaseModel.NEW_MODEL_ID

                if (!isEditing) {
                    category.counter = 0
                    category.type = DatabaseModel.TYPE_CATEGORY
                    category.createdAt = System.currentTimeMillis()
                    category.isArchived = false
                }

                category.title = inputTitle
                category.theme = categoryDialogTheme
                object : Thread() {
                    override fun run() {
                        val id = category.save()
                        if (id != DatabaseModel.NEW_MODEL_ID) {
                            requireActivity().runOnUiThread {
                                if (isEditing) {
                                    val categoryInItems = items!![position]!!
                                    categoryInItems.theme = category.theme
                                    categoryInItems.title = category.title
                                    refreshItem(position)
                                } else {
                                    category.id = id
                                    addItem(category, position)
                                }
                            }
                        }

                        interrupt()
                    }
                }.start()
            }
            .onNegative { dialog2: MaterialDialog, which: DialogAction? -> dialog2.dismiss() }
            .customView(R.layout.dialog_category, true)
            .build()

        dialog.show()

        val view = dialog.customView

        (view!!.findViewById<View>(R.id.title_txt) as EditText).setText(categoryTitle)
        setCategoryDialogTheme(view, categoryDialogTheme)

        view.findViewById<View>(R.id.theme_red).setOnClickListener { v: View? ->
            setCategoryDialogTheme(
                view, Category.THEME_RED
            )
        }

        view.findViewById<View>(R.id.theme_pink).setOnClickListener { v: View? ->
            setCategoryDialogTheme(
                view, Category.THEME_PINK
            )
        }

        view.findViewById<View>(R.id.theme_purple).setOnClickListener { v: View? ->
            setCategoryDialogTheme(
                view, Category.THEME_PURPLE
            )
        }

        view.findViewById<View>(R.id.theme_amber).setOnClickListener { v: View? ->
            setCategoryDialogTheme(
                view, Category.THEME_AMBER
            )
        }

        view.findViewById<View>(R.id.theme_blue).setOnClickListener { v: View? ->
            setCategoryDialogTheme(
                view, Category.THEME_BLUE
            )
        }

        view.findViewById<View>(R.id.theme_cyan).setOnClickListener { v: View? ->
            setCategoryDialogTheme(
                view, Category.THEME_CYAN
            )
        }

        view.findViewById<View>(R.id.theme_orange).setOnClickListener { v: View? ->
            setCategoryDialogTheme(
                view, Category.THEME_ORANGE
            )
        }

        view.findViewById<View>(R.id.theme_teal).setOnClickListener { v: View? ->
            setCategoryDialogTheme(
                view, Category.THEME_TEAL
            )
        }

        view.findViewById<View>(R.id.theme_green).setOnClickListener { v: View? ->
            setCategoryDialogTheme(
                view, Category.THEME_GREEN
            )
        }
    }

    private fun setCategoryDialogTheme(view: View, theme: Int) {
        if (theme != categoryDialogTheme) {
            getThemeView(view, categoryDialogTheme).setImageResource(0)
        }

        getThemeView(view, theme).setImageResource(R.drawable.ic_checked)
        categoryDialogTheme = theme
    }

    private fun getThemeView(view: View, theme: Int): ImageView {
        return when (theme) {
            Category.THEME_AMBER -> view.findViewById(
                R.id.theme_amber
            )

            Category.THEME_BLUE -> view.findViewById(
                R.id.theme_blue
            )

            Category.THEME_CYAN -> view.findViewById(
                R.id.theme_cyan
            )

            Category.THEME_ORANGE -> view.findViewById(
                R.id.theme_orange
            )

            Category.THEME_PINK -> view.findViewById(
                R.id.theme_pink
            )

            Category.THEME_PURPLE -> view.findViewById(
                R.id.theme_purple
            )

            Category.THEME_RED -> view.findViewById(
                R.id.theme_red
            )

            Category.THEME_TEAL -> view.findViewById(
                R.id.theme_teal
            )

            else -> view.findViewById(R.id.theme_green)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && requestCode == CategoryActivity.REQUEST_CODE && resultCode == CategoryActivity.RESULT_CHANGE) {
            val position = data.getIntExtra("position", 0)
            items!![position].counter = data.getIntExtra(OpenHelper.COLUMN_COUNTER, 0)
            refreshItem(position)
        }
    }

    override val layout: Int
        get() = (R.layout.fragment_main)

    override val itemName: String
        get() = "category"

    override val adapterClass: Class<CategoryAdapter>
        get() = CategoryAdapter::class.java
}
