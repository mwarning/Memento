package github.yaa110.memento.fragment.template

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import github.yaa110.memento.R
import github.yaa110.memento.adapter.template.ModelAdapter
import github.yaa110.memento.db.Controller
import github.yaa110.memento.db.OpenHelper
import github.yaa110.memento.inner.Animator
import github.yaa110.memento.model.Category
import github.yaa110.memento.model.DatabaseModel
import github.yaa110.memento.model.Note
import java.util.Arrays
import java.util.Collections
import java.util.Locale

abstract class RecyclerFragment<T : DatabaseModel?, A : ModelAdapter<*, *>?> : Fragment() {
    lateinit var fab: View
    lateinit var selectionToolbar: Toolbar
    var selectionState: Boolean = false
    var items: ArrayList<T>? = null
    var selected: ArrayList<T> = ArrayList()
    var activity: Callbacks? = null
    var categoryId: Long = DatabaseModel.NEW_MODEL_ID
    var categoryTitle: String? = null
    var categoryTheme: Int = 0
    var categoryPosition: Int = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var empty: View
    private lateinit var selectionCounter: TextView
    private var adapter: A? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab = view.findViewById(R.id.fab)
        recyclerView = view.findViewById(R.id.recyclerView)
        empty = view.findViewById(R.id.empty)
        selectionToolbar = requireActivity().findViewById(R.id.selection_toolbar)
        selectionCounter = selectionToolbar.findViewById(R.id.selection_counter)

        init(view)

        selectionToolbar.findViewById<View>(R.id.selection_back)
            .setOnClickListener { view3: View? ->
                toggleSelection(
                    false
                )
            }

        selectionToolbar.findViewById<View>(R.id.selection_delete)
            .setOnClickListener { view4: View? ->
                val undos = ArrayList(selected)
                toggleSelection(false)
                object : Thread() {
                    override fun run() {
                        val length = undos.size
                        val ids = arrayOfNulls<String>(length)
                        val sortablePosition = IntArray(length)

                        for (i in 0..<length) {
                            val item = undos[i]
                            ids[i] = String.format(Locale.US, "%d", item!!.id)
                            val position = items!!.indexOf(item)
                            item.position = position
                            sortablePosition[i] = position
                        }

                        Controller.instance!!.deleteNotes(
                            ids,
                            categoryId
                        )

                        Arrays.sort(sortablePosition)

                        requireActivity().runOnUiThread {
                            for (i in length - 1 downTo 0) {
                                items!!.removeAt(sortablePosition[i])
                                adapter!!.notifyItemRemoved(sortablePosition[i])
                            }
                            toggleEmpty()

                            val message = StringBuilder()
                            message.append(length).append(" ").append(this@RecyclerFragment.itemName)
                            if (length > 1) message.append("s were deleted")
                            else message.append(" was deleted.")
                            Snackbar.make(
                                (if (fab != null) fab else selectionToolbar),
                                message.toString(),
                                7000
                            )
                                .setAction(R.string.undo) { view1: View? ->
                                    object : Thread() {
                                        override fun run() {
                                            Controller.instance!!.undoDeletion()
                                            if (categoryId != DatabaseModel.NEW_MODEL_ID) {
                                                Controller.instance!!.addCategoryCounter(
                                                    categoryId,
                                                    length
                                                )
                                            }

                                            Collections.sort(undos) { t1: T, t2: T ->
                                                if (t1!!.position < t2!!.position) return@sort -1
                                                if (t1.position == t2.position) return@sort 0
                                                1
                                            }

                                            requireActivity().runOnUiThread {
                                                for (i in 0..<length) {
                                                    val item = undos[i]
                                                    addItem(item, item!!.position)
                                                }
                                            }
                                            interrupt()
                                        }
                                    }.start()
                                }
                                .show()
                        }

                        interrupt()
                    }
                }.start()
            }

        fab.setOnClickListener { view2: View? -> onClickFab() }

        val data = requireActivity().intent
        if (data != null) {
            // Get the parent data
            categoryId = data.getLongExtra(
                OpenHelper.COLUMN_ID,
                DatabaseModel.NEW_MODEL_ID
            )
            categoryTitle = data.getStringExtra(OpenHelper.COLUMN_TITLE)
            categoryTheme =
                data.getIntExtra(OpenHelper.COLUMN_THEME, Category.THEME_GREEN)
            categoryPosition = data.getIntExtra("position", 0)

            if (categoryTitle != null) {
                (requireActivity().findViewById<View>(R.id.title) as TextView).text =
                    categoryTitle
            }
        }

        loadItems()
    }

    fun onChangeCounter(count: Int) {
        selectionCounter.text = String.format(Locale.US, "%d", count)
    }

    fun toggleSelection(state: Boolean) {
        selectionState = state
        activity!!.onChangeSelection(state)
        if (state) {
            Animator.create(context)
                .on<Toolbar?>(selectionToolbar)
                .setStartVisibility(View.VISIBLE)
                .animate(R.anim.fade_in)
        } else {
            Animator.create(context)
                .on<Toolbar?>(selectionToolbar)
                .setEndVisibility(View.GONE)
                .animate(R.anim.fade_out)

            deselectAll()
        }
    }

    private fun deselectAll() {
        while (selected.isNotEmpty()) {
            adapter!!.notifyItemChanged(items!!.indexOf(selected.removeAt(0)))
        }
    }

    fun loadItems() {
        object : Thread() {
            override fun run() {
                try {
                    items = if (categoryId == DatabaseModel.NEW_MODEL_ID) {
                        // Get all categories
                        Category.all() as ArrayList<T>
                    } else {
                        // Get notes of the category by categoryId
                        Note.all(categoryId) as ArrayList<T>
                    }

                    adapter = this@RecyclerFragment.adapterClass.getDeclaredConstructor(
                        ArrayList::class.java,
                        ArrayList::class.java,
                        ModelAdapter.ClickListener::class.java
                    ).newInstance(items, selected, this@RecyclerFragment.listener)

                    requireActivity().runOnUiThread {
                        toggleEmpty()
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(
                            context,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    }
                } catch (ignored: Exception) {
                } finally {
                    interrupt()
                }
            }
        }.start()
    }

    private fun toggleEmpty() {
        if (items!!.isEmpty()) {
            empty.visibility = View.VISIBLE
            recyclerView.visibility = View.INVISIBLE
        } else {
            empty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun refreshItem(position: Int) {
        adapter!!.notifyItemChanged(position)
    }

    fun deleteItem(position: Int): T {
        val item = items!!.removeAt(position)
        adapter!!.notifyItemRemoved(position)
        toggleEmpty()
        return item
    }

    fun addItem(item: T, position: Int) {
        if (items!!.isEmpty()) {
            empty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        items!!.add(position, item)
        adapter!!.notifyItemInserted(position)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.activity = context as Callbacks
    }

    open fun init(view: View) {
    }

    abstract fun onClickFab()

    abstract val layout: Int

    abstract val itemName: String

    abstract val adapterClass: Class<A>

    abstract val listener: ModelAdapter.ClickListener<*>?

    interface Callbacks {
        fun onChangeSelection(state: Boolean)

        fun toggleOneSelection(state: Boolean)
    }
}
