package github.yaa110.memento.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import github.yaa110.memento.App
import github.yaa110.memento.R
import github.yaa110.memento.adapter.DrawerAdapter
import github.yaa110.memento.db.Controller
import github.yaa110.memento.fragment.MainFragment
import github.yaa110.memento.fragment.template.RecyclerFragment
import github.yaa110.memento.inner.Animator
import github.yaa110.memento.inner.Formatter.formatDate
import github.yaa110.memento.model.Drawer
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), RecyclerFragment.Callbacks {
    private lateinit var drawerHolder: View
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var fragment: MainFragment
    private lateinit var toolbar: Toolbar
    private lateinit var selectionEdit: View
    private var handler = Handler()
    private var exitStatus = false
    private var runnable = Runnable { exitStatus = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        try {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        } catch (ignored: Exception) {
        }

        setupDrawer()

        selectionEdit = findViewById(R.id.selection_edit)
        selectionEdit.setOnClickListener { view: View? -> fragment.onEditSelected() }

        if (savedInstanceState == null) {
            fragment = MainFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(drawerHolder)) {
            drawerLayout.closeDrawers()
            return
        }

        if (fragment.selectionState) {
            fragment.toggleSelection(false)
            return
        }

        if (exitStatus) {
            finish()
        } else {
            exitStatus = true

            Snackbar.make(
                fragment.fab,
                R.string.exit_message,
                Snackbar.LENGTH_LONG
            ).show()

            handler.postDelayed(runnable, 3500)
        }
    }

    private fun setupDrawer() {
        // Set date in drawer
        findViewById<TextView>(R.id.drawer_date).text = formatDate()

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerHolder = findViewById(R.id.drawer_holder)
        val drawerList = findViewById<ListView>(R.id.drawer_list)

        // Navigation menu button
        findViewById<View>(R.id.nav_btn).setOnClickListener { view: View? ->
            drawerLayout.openDrawer(
                GravityCompat.START
            )
        }

        // Settings button
        findViewById<View>(R.id.settings_btn).setOnClickListener { view: View? ->
            onClickDrawer(
                Drawer.TYPE_SETTINGS
            )
        }

        // Set adapter of drawer
        drawerList.adapter = DrawerAdapter(
            applicationContext, object : DrawerAdapter.ClickListener {
            override fun onClick(type: Int) {
                this@MainActivity.onClickDrawer(type)
            }
        })
    }

    private fun onClickDrawer(type: Int) {
        drawerLayout.closeDrawers()

        try {
            handler.removeCallbacks(runnable)
        } catch (ignored: Exception) {
        }

        object : Thread() {
            override fun run() {
                try {
                    // wait for completion of drawer animation
                    sleep(500)

                    runOnUiThread {
                        when (type) {
                            Drawer.TYPE_ABOUT -> MaterialDialog.Builder(
                                this@MainActivity
                            )
                                .title(R.string.app_name)
                                .content(R.string.about_desc)
                                .positiveText(R.string.ok)
                                .onPositive { dialog: MaterialDialog, which: DialogAction? -> dialog.dismiss() }
                                .show()

                            Drawer.TYPE_BACKUP -> {
                                val calendar = Calendar.getInstance(Locale.US)
                                val filename = String.format(
                                    Locale.US,
                                    "memento-%d-%02d-%02d.%s",
                                    calendar[Calendar.YEAR],
                                    calendar[Calendar.MONTH],
                                    calendar[Calendar.DAY_OF_MONTH],
                                    App.BACKUP_EXTENSION
                                )
                                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                                intent.addCategory(Intent.CATEGORY_OPENABLE)
                                intent.putExtra(Intent.EXTRA_TITLE, filename)
                                intent.setType("*/*")
                                exportFileLauncher.launch(intent)
                            }

                            Drawer.TYPE_RESTORE -> {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                intent.addCategory(Intent.CATEGORY_OPENABLE)
                                intent.setType("*/*")
                                importFileLauncher.launch(intent)
                            }

                            Drawer.TYPE_SETTINGS ->                                 // TODO implement settings
                                MaterialDialog.Builder(this@MainActivity)
                                    .title(R.string.settings)
                                    .content(R.string.not_implemented)
                                    .positiveText(R.string.ok)
                                    .onPositive { dialog: MaterialDialog, which: DialogAction? -> dialog.dismiss() }
                                    .show()
                        }
                    }

                    interrupt()
                } catch (ignored: Exception) {
                }
            }
        }.start()
    }

    var exportFileLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            try {
                val intent = result.data
                saveBackupFile(intent!!.data!!)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    var importFileLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            try {
                val intent = result.data
                readBackupFile(intent!!.data!!)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
            }
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
        selectionEdit.visibility =
            if (state) View.VISIBLE else View.GONE
    }

    @Throws(Exception::class)
    private fun readBackupFile(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while ((inputStream!!.read(buffer).also { length = it }) != -1) {
            result.write(buffer, 0, length)
        }
        val json = JSONArray(result.toString("UTF-8"))
        inputStream.close()

        Controller.instance!!.readBackup(json)

        recreate()
    }

    private fun saveBackupFile(uri: Uri) {
        var fos: OutputStream? = null
        try {
            fos = applicationContext.contentResolver.openOutputStream(uri)
            fos!!.write("[".toByteArray(StandardCharsets.UTF_8))
            Controller.instance!!.writeBackup(fos)
            fos.write("]".toByteArray(StandardCharsets.UTF_8))
            fos.flush()
        } finally {
            fos?.close()
        }
    }
}
