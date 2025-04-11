package github.yaa110.memento.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import github.yaa110.memento.R
import github.yaa110.memento.db.OpenHelper
import github.yaa110.memento.fragment.CategoryFragment
import github.yaa110.memento.fragment.template.RecyclerFragment
import github.yaa110.memento.inner.Animator
import github.yaa110.memento.model.Category

class CategoryActivity : AppCompatActivity(), RecyclerFragment.Callbacks {
    private lateinit var toolbar: Toolbar
    private var fragment: CategoryFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(
            Category.getStyle(
                intent.getIntExtra(
                    OpenHelper.COLUMN_THEME,
                    Category.THEME_GREEN
                )
            )
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        try {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        } catch (ignored: Exception) {
        }

        toolbar.findViewById<View>(R.id.back_btn)
            .setOnClickListener { view: View? -> onBackPressed() }

        if (savedInstanceState == null) {
            fragment = CategoryFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment!!)
                .commit()
        }
    }

    override fun onChangeSelection(state: Boolean) {
        if (state) {
            Animator.create(applicationContext)
                .on(toolbar)
                .setEndVisibility(View.INVISIBLE)
                .animate(R.anim.fade_out)
        } else {
            Animator.create(applicationContext)
                .on(toolbar)
                .setStartVisibility(View.VISIBLE)
                .animate(R.anim.fade_in)
        }
    }

    override fun toggleOneSelection(state: Boolean) {
    }

    override fun onBackPressed() {
        if (fragment!!.isFabOpen) {
            fragment!!.toggleFab(true)
            return
        }

        if (fragment!!.selectionState) {
            fragment!!.toggleSelection(false)
            return
        }

        val data = Intent()
        data.putExtra("position", fragment!!.categoryPosition)
        data.putExtra(OpenHelper.COLUMN_COUNTER, fragment!!.items!!.size)
        setResult(RESULT_CHANGE, data)
        finish()
    }

    companion object {
        const val REQUEST_CODE: Int = 1
        const val RESULT_CHANGE: Int = 101
    }
}
