package github.yaa110.memento.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;

import github.yaa110.memento.App;
import github.yaa110.memento.R;
import github.yaa110.memento.adapter.DrawerAdapter;
import github.yaa110.memento.db.Controller;
import github.yaa110.memento.fragment.MainFragment;
import github.yaa110.memento.fragment.template.RecyclerFragment;
import github.yaa110.memento.inner.Animator;
import github.yaa110.memento.inner.Formatter;
import github.yaa110.memento.model.Drawer;

public class MainActivity extends AppCompatActivity implements RecyclerFragment.Callbacks {
    public static final int PERMISSION_REQUEST = 3;
    public View drawerHolder;
    public Handler handler = new Handler();
    private DrawerLayout drawerLayout;
    private boolean exitStatus = false;
    public Runnable runnable = () -> exitStatus = false;
    private MainFragment fragment;
    private Toolbar toolbar;
    private View selectionEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (Exception ignored) {
        }

        setupDrawer();

        selectionEdit = findViewById(R.id.selection_edit);
        selectionEdit.setOnClickListener(view -> fragment.onEditSelected());

        if (savedInstanceState == null) {
            fragment = new MainFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(drawerHolder)) {
            drawerLayout.closeDrawers();
            return;
        }

        if (fragment.selectionState) {
            fragment.toggleSelection(false);
            return;
        }

        if (exitStatus) {
            finish();
        } else {
            exitStatus = true;

            Snackbar.make(fragment.fab != null ? fragment.fab : toolbar, R.string.exit_message, Snackbar.LENGTH_LONG).show();

            handler.postDelayed(runnable, 3500);
        }
    }

    private void setupDrawer() {
        // Set date in drawer
        ((TextView) findViewById(R.id.drawer_date)).setText(Formatter.formatDate());

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerHolder = findViewById(R.id.drawer_holder);
        ListView drawerList = findViewById(R.id.drawer_list);

        // Navigation menu button
        findViewById(R.id.nav_btn).setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        // Settings button
        findViewById(R.id.settings_btn).setOnClickListener(view -> onClickDrawer(Drawer.TYPE_SETTINGS));

        // Set adapter of drawer
        drawerList.setAdapter(new DrawerAdapter(
                getApplicationContext(),
                type -> onClickDrawer(type)
        ));
    }

    private void onClickDrawer(final int type) {
        drawerLayout.closeDrawers();

        try {
            handler.removeCallbacks(runnable);
        } catch (Exception ignored) {
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    // wait for completion of drawer animation
                    sleep(500);

                    runOnUiThread(() -> {
                        switch (type) {
                            case Drawer.TYPE_ABOUT:
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title(R.string.app_name)
                                        .content(R.string.about_desc)
                                        .positiveText(R.string.ok)
                                        .onPositive((dialog, which) -> dialog.dismiss())
                                        .show();
                                break;
                            case Drawer.TYPE_BACKUP: {
                                Calendar calendar = Calendar.getInstance(Locale.US);
                                String filename = String.format(Locale.US, "memento-%d-%02d-%02d.%s", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), App.BACKUP_EXTENSION);
                                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.putExtra(Intent.EXTRA_TITLE, filename);
                                intent.setType("*/*");
                                exportFileLauncher.launch(intent);
                                break;
                            }
                            case Drawer.TYPE_RESTORE: {
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("*/*");
                                importFileLauncher.launch(intent);
                                break;
                            }
                            case Drawer.TYPE_SETTINGS:
                                // TODO implement settings
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title(R.string.settings)
                                        .content(R.string.not_implemented)
                                        .positiveText(R.string.ok)
                                        .onPositive((dialog, which) -> dialog.dismiss())
                                        .show();
                                break;
                        }
                    });

                    interrupt();
                } catch (Exception ignored) {
                }
            }
        }.start();
    }

    ActivityResultLauncher<Intent> exportFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Intent intent = result.getData();
                        saveBackupFile(intent.getData());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    ActivityResultLauncher<Intent> importFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Intent intent = result.getData();
                        readBackupFile(intent.getData());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public void onChangeSelection(boolean state) {
        if (state) {
            Animator.create(getApplicationContext())
                    .on(toolbar)
                    .setEndVisibility(View.INVISIBLE)
                    .animate(R.anim.fade_out);
        } else {
            Animator.create(getApplicationContext())
                    .on(toolbar)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fade_in);
        }
    }

    @Override
    public void toggleOneSelection(boolean state) {
        selectionEdit.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private void readBackupFile(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = inputStream.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        JSONArray json = new JSONArray(result.toString("UTF-8"));
        inputStream.close();

        Controller.instance.readBackup(json);

        recreate();
    }

    private void saveBackupFile(Uri uri) throws Exception {
        OutputStream fos = null;
        try {
            fos = getApplicationContext().getContentResolver().openOutputStream(uri);
            fos.write("[".getBytes(StandardCharsets.UTF_8));
            Controller.instance.writeBackup(fos);
            fos.write("]".getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } finally {
            if (fos != null) fos.close();
        }
    }
}
