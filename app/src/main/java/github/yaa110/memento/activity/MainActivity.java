package github.yaa110.memento.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import github.yaa110.memento.App;
import github.yaa110.memento.R;
import github.yaa110.memento.adapter.DrawerAdapter;
import github.yaa110.memento.db.Controller;
import github.yaa110.memento.dialog.ImportDialog;
import github.yaa110.memento.dialog.SaveDialog;
import github.yaa110.memento.fragment.MainFragment;
import github.yaa110.memento.fragment.template.RecyclerFragment;
import github.yaa110.memento.inner.Animator;
import github.yaa110.memento.inner.Formatter;
import github.yaa110.memento.model.Drawer;

public class MainActivity extends AppCompatActivity implements RecyclerFragment.Callbacks {
	public static final int PERMISSION_REQUEST = 3;

	private DrawerLayout drawerLayout;
	public View drawerHolder;
	private boolean exitStatus = false;

	private MainFragment fragment;
	private Toolbar toolbar;
	private View selectionEdit;
	private boolean permissionNotGranted = false;
	private boolean checkForPermission = true;

	public Handler handler = new Handler();
	public Runnable runnable = () -> exitStatus = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
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

		if (checkForPermission) {
			checkForPermission = false;
			if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				new MaterialDialog.Builder(this)
					.title(R.string.permission)
					.content(R.string.storage_permission)
					.positiveText(R.string.request)
					.negativeText(R.string.cancel)
					.negativeColor(ContextCompat.getColor(this, R.color.secondary_text))
					.onPositive((dialog, which) -> {
						dialog.dismiss();
						requestPermission();
					})
					.onNegative((dialog, which) -> {
						dialog.dismiss();
						displayPermissionError();
					})
					.show();
			}
		}
	}

	@Override
	public void onBackPressed() {
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
		} catch (Exception ignored) {}

		new Thread() {
			@Override
			public void run() {
				try {
					// wait for completion of drawer animation
					sleep(500);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							switch (type) {
								case Drawer.TYPE_ABOUT:
									new MaterialDialog.Builder(MainActivity.this)
										.title(R.string.app_name)
										.content(R.string.about_desc)
										.positiveText(R.string.ok)
										.onPositive((dialog, which) -> dialog.dismiss())
										.show();
									break;
								case Drawer.TYPE_BACKUP:
									backupData();
									break;
								case Drawer.TYPE_RESTORE:
									restoreData();
									break;
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
						}
					});

					interrupt();
				} catch (Exception ignored) {
				}
			}
		}.start();
	}

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

	private void restoreData() {
		ImportDialog.newInstance(
			R.string.restore,
			new String[]{App.BACKUP_EXTENSION},
			new ImportDialog.ImportListener() {
				@Override
				public void onSelect(final String path) {
					new Thread() {
						@Override
						public void run() {
							try {
								readBackupFile(path);

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										fragment.loadItems();

										Snackbar.make(fragment.fab != null ? fragment.fab : toolbar, R.string.data_restored, Snackbar.LENGTH_LONG).show();
									}
								});
							} catch (final Exception e){
								runOnUiThread(() -> new MaterialDialog.Builder(MainActivity.this)
									.title(R.string.restore_error)
									.positiveText(R.string.ok)
									.content(e.getMessage())
									.onPositive((dialog, which) -> dialog.dismiss())
									.show());
							} finally {
								interrupt();
							}
						}
					}.start();
				}

				@Override
				public void onError(String msg) {
					new MaterialDialog.Builder(MainActivity.this)
						.title(R.string.restore_error)
						.positiveText(R.string.ok)
						.content(msg)
						.onPositive((dialog, which) -> dialog.dismiss())
						.show();
				}
			}
		).show(getSupportFragmentManager(), "");
	}

	private void backupData() {
		SaveDialog.newInstance(
			R.string.backup,
			"memento",
			App.BACKUP_EXTENSION,
			new SaveDialog.SaveListener() {
				@Override
				public void onSelect(final String path) {
					new Thread() {
						@Override
						public void run() {
							try {
								saveBackupFile(path);

								runOnUiThread(() -> new MaterialDialog.Builder(MainActivity.this)
									.title(R.string.backup)
									.positiveText(R.string.ok)
									.content(getString(R.string.backup_saved, path))
									.onPositive((dialog, which) -> dialog.dismiss())
									.show());
							} catch (final Exception e) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										new MaterialDialog.Builder(MainActivity.this)
											.title(R.string.backup_error)
											.positiveText(R.string.ok)
											.content(e.getMessage())
											.onPositive((dialog, which) -> dialog.dismiss())
											.show();
									}
								});
							} finally {
								interrupt();
							}
						}
					}.start();
				}

				@Override
				public void onError() {

				}

				@Override
				public void onCancel() {

				}
			}
		).show(getSupportFragmentManager(), "");
	}

	private void readBackupFile(String path) throws Exception {
		DataInputStream dis = new DataInputStream(new FileInputStream(path));
		byte[] backup_data = new byte[dis.available()];
		dis.readFully(backup_data);
		JSONArray json = new JSONArray(new String(backup_data));
		dis.close();

		Controller.instance.readBackup(json);
	}

	private void saveBackupFile(String path) throws Exception {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path);
			fos.write("[".getBytes("UTF-8"));
			Controller.instance.writeBackup(fos);
			fos.write("]".getBytes("UTF-8"));
			fos.flush();
		} finally {
			if (fos != null) fos.close();
		}
	}

	private void requestPermission() {
		ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
	}

	private void displayPermissionError() {
		new MaterialDialog.Builder(this)
			.title(R.string.permission_error)
			.content(R.string.permission_error_desc)
			.negativeText(R.string.request)
			.positiveText(R.string.continue_anyway)
			.negativeColor(ContextCompat.getColor(this, R.color.secondary_text))
			.onPositive((dialog, which) -> {
				dialog.dismiss();
				permissionNotGranted = false;
			})
			.onNegative((dialog, which) -> {
				dialog.dismiss();
				requestPermission();
			})
			.show();
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		if (permissionNotGranted) {
			permissionNotGranted = false;
			displayPermissionError();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0])) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(getApplicationContext(), R.string.permission_granted, Toast.LENGTH_SHORT).show();
			} else {
				permissionNotGranted = true;
			}
		}
	}
}
