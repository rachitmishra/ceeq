package in.ceeq.activities;

import in.ceeq.Launcher;
import in.ceeq.R;
import in.ceeq.actions.Backup;
import in.ceeq.actions.Notifications;
import in.ceeq.actions.Protect;
import in.ceeq.actions.Receiver;
import in.ceeq.actions.Receiver.ReceiverType;
import in.ceeq.actions.Restore;
import in.ceeq.actions.Upload;
import in.ceeq.actions.Wipe;
import in.ceeq.fragments.AboutApplicationFragment;
import in.ceeq.fragments.BackupFragment;
import in.ceeq.fragments.HomeFragment;
import in.ceeq.fragments.MyDeviceFragment;
import in.ceeq.fragments.PrivacyFragment;
import in.ceeq.fragments.SecurityFragment;
import in.ceeq.helpers.PhoneHelper;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.receivers.DeviceAdmin;
import in.ceeq.services.Backups;
import in.ceeq.services.Locater.RequestType;
import in.ceeq.services.Tracker;
import in.ceeq.services.Uploader.UploadType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class Home extends FragmentActivity {

	public static final String MESSENGER = "in.ceeq.Home";
	private static final int DEVICE_ADMIN_ACTIVATION_REQUEST = 9014;
	private PreferencesHelper preferencesHelper;
	private PhoneHelper phoneHelper;
	private DialogsHelper dialogsHelper;
	private boolean exit = false;
	private static final String SENDER_ID = "909602096750";
	private Session.StatusCallback statusCallback = new FBSessionStatus();
	private FragmentManager fragmentManager;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView actionList;
	private CharSequence drawerTitle;
	private CharSequence title;
	private TextView userId, userName;
	private ImageView userImage;
	private RelativeLayout userLoading, userDetails;

	public static final int PROTECT_DIALOG = 0;
	public static final int STEALTH_DIALOG = 1;
	public static final int FEEDBACK_DIALOG = 2;
	public static final int BACKUP_DIALOG = 3;
	public static final int RESTORE_DIALOG = 4;
	public static final int BLIP_DIALOG = 5;
	public static final int WIPE_DIALOG = 6;
	public static final int WIPE_EXTERNAL_STORAGE_DIALOG = 7;
	public static final int WIPE_DEVICE_DIALOG = 8;
	public static final int WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG = 9;
	public static final int DEVICE_ADMIN_DIALOG = 10;
	public static final int WIPE = 11;

	public static final int OFF = 0;
	public static final int ON = 1;

	public static final int HIDE = 0;
	public static final int SHOW = 1;

	public static final int DISABLE = 0;
	public static final int ENABLE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupActionbar();
		setContentView(R.layout.activity_home);
		this.title = drawerTitle = getTitle();

		setupHelpers();
		setupFacebookConnect(savedInstanceState);
		setupHome();
		setupDrawer();

		if (preferencesHelper.getBoolean(PreferencesHelper.FIRST_LOGIN)) {
			setupFirstrun();
		}
	}

	public void setupHelpers() {
		preferencesHelper = new PreferencesHelper(this);
		phoneHelper = PhoneHelper.getInstance(this);
	}

	public void setupHome() {
		fragmentManager = getSupportFragmentManager();
		Fragment fragment = new HomeFragment();
		fragmentManager.beginTransaction().replace(R.id.container, fragment)
				.commit();

	}

	public void setupActionbar() {
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	/**
	 * setup the navigation drawer
	 */
	public void setupDrawer() {
		userId = (TextView) findViewById(R.id.user_id_drawer);
		userName = (TextView) findViewById(R.id.user_name_drawer);
		userImage = (ImageView) findViewById(R.id.user_image_drawer);
		userId.setText(preferencesHelper
				.getString(PreferencesHelper.ACCOUNT_USER_ID));
		userName.setText(preferencesHelper
				.getString(PreferencesHelper.ACCOUNT_USER_NAME));
		userLoading = (RelativeLayout) findViewById(R.id.user_loader);
		userDetails = (RelativeLayout) findViewById(R.id.user_details);
		new AsyncTask<String, String, Bitmap>() {

			@Override
			protected void onPreExecute() {
				userDetails.setVisibility(View.GONE);
				userLoading.setVisibility(View.VISIBLE);
			}

			@Override
			protected Bitmap doInBackground(String... url) {
				Bitmap bm = null;
				try {
					URL aURL = new URL(url[0]);
					URLConnection conn = aURL.openConnection();
					conn.connect();
					InputStream is = conn.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
					bm = BitmapFactory.decodeStream(bis);
					bis.close();
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return bm;
			}

			@Override
			protected void onPostExecute(Bitmap bitmap) {
				userImage.setImageBitmap(bitmap);
				userLoading.setVisibility(View.GONE);
				userDetails.setVisibility(View.VISIBLE);
			}

		}.execute(preferencesHelper
				.getString(PreferencesHelper.ACCOUNT_USER_IMAGE_URL));
		actionList = (ListView) findViewById(R.id.drawer_action_list);
		actionList.setAdapter(new DrawerManager());
		actionList.setOnItemClickListener(new DrawerManager());
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer, R.string.open_drawer,
				R.string.close_drawer) {

			public void onDrawerClosed(View view) {
				getActionBar().setTitle(title);
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(drawerTitle);
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
				drawerLayout.bringChildToFront(drawerView);
				drawerLayout.requestLayout();
			}

		};
		drawerLayout.setDrawerListener(drawerToggle);

	}

	public void setupFirstrun() {
		drawerLayout.openDrawer(Gravity.START);
		preferencesHelper.setBoolean(PreferencesHelper.FIRST_LOGIN, false);
	}

	private GoogleCloudMessaging gcm;

	public void setupGoogleCloudMessaging() {
		gcm = GoogleCloudMessaging.getInstance(this);
		new AsyncTask<Void, Void, Boolean>() {
			private String registrationId;

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(Home.this);
					}
					registrationId = gcm.register(SENDER_ID);
					phoneHelper.set(PhoneHelper.REGISTRATION_ID, registrationId);
					return true;
				} catch (IOException ex) {
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result)
					preferencesHelper.setBoolean(
							PreferencesHelper.GCM_REGISTRATION_STATUS, true);
				else
					preferencesHelper.setBoolean(
							PreferencesHelper.GCM_REGISTRATION_STATUS, false);
			}

		};

	}

	public void doPendingUploads() {
		if (!preferencesHelper
				.getBoolean(PreferencesHelper.ONLINE_ACCOUNT_STATUS)
				& preferencesHelper
						.getBoolean(PreferencesHelper.GCM_REGISTRATION_STATUS)) {
			// startService(new Intent(Home.this,
			// ServiceUploader.class).putExtra(
			// "action", PreferencesHelper.UPLOAD_TYPE_NEW));
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	public void setupFacebookConnect(Bundle savedInstanceState) {
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback));
			}
		}
	}

	public void connectFacebook() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}
	}

	public void checkPlayServices() {
		if (!phoneHelper.enabled(PhoneHelper.PLAY_SERVICES)) {
			startActivity(new Intent(this, GoogleServices.class).putExtra(
					"from", 1));
			this.finish();
		}
	}

	public void updateFacebookConnectPreferences() {

		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			preferencesHelper.setBoolean(
					PreferencesHelper.FACEBOOK_CONNECT_STATUS, true);
		} else {
			preferencesHelper.setBoolean(
					PreferencesHelper.FACEBOOK_CONNECT_STATUS, false);
		}
	}

	public void onButtonPressed(View v) {
		ToggleButton toggleButton;

		dialogsHelper = new DialogsHelper(this);

		switch (v.getId()) {

		case R.id.toggle_protect:
			toggleButton = (ToggleButton) v.findViewById(R.id.toggle_protect);
			setupProtectMe(toggleButton);
			break;

		case R.id.toggle_stealth:
			toggleButton = (ToggleButton) v.findViewById(R.id.toggle_stealth);
			setupStealthMode(toggleButton);
			break;

		case R.id.toggle_backup:
			toggleButton = (ToggleButton) v.findViewById(R.id.toggle_backup);
			setupScheduledBackup(toggleButton.isChecked());
			resetBackup();
			break;

		case R.id.b_backup:
			Backup.getInstance(this).backup(Backups.ACTION_TYPE_ALL);
			break;

		case R.id.backup:
			dialogsHelper.showDialog(BACKUP_DIALOG);
			break;

		case R.id.restore:
			dialogsHelper.showDialog(RESTORE_DIALOG);
			break;

		case R.id.b_view:
			startActivity(new Intent(this, ViewBackups.class));
			break;

		case R.id.toggle_track:
			toggleButton = (ToggleButton) v.findViewById(R.id.toggle_track);
			setupLocationTracker(toggleButton);
			break;

		case R.id.toggle_blip:
			toggleButton = (ToggleButton) v.findViewById(R.id.toggle_blip);
			setupAutoBlips(toggleButton);
			break;

		case R.id.wipe:
			dialogsHelper.showDialog(WIPE_DIALOG);
			break;

		case R.id.wipe_cache:
			Wipe.getInstance(this).cache();
			break;

		case R.id.b_feedback:
			dialogsHelper.showDialog(FEEDBACK_DIALOG);
			break;
		}

	}

	public void resetHome() {
		Fragment fragment = new HomeFragment();
		fragmentManager.beginTransaction().replace(R.id.container, fragment)
				.commit();
	}

	public void resetBackup() {
		Fragment fragment = new BackupFragment();
		fragmentManager.beginTransaction().replace(R.id.container, fragment)
				.commit();
	}

	private PackageManager packageManager;

	private void setupStealthMode(ToggleButton toggleButton) {
		packageManager = this.getPackageManager();
		if (toggleButton.isChecked()) {
			dialogsHelper.showDialog(STEALTH_DIALOG, toggleButton);
		} else {
			getPackageManager().setComponentEnabledSetting(
					new ComponentName(Home.this, Launcher.class),
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
			Notifications.getInstance(Home.this).show();
			Receiver.getInstance(this).unregister(ReceiverType.OUTGOING_CALLS);
			preferencesHelper.setBoolean(PreferencesHelper.STEALTH_MODE_STATUS,
					false);
			Toast.makeText(this, "Stealth Mode disabled.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void setupProtectMe(ToggleButton toggleButton) {
		if (toggleButton.isChecked()) {
			dialogsHelper.showDialog(PROTECT_DIALOG, toggleButton);
		} else {
			Protect.getInstance(this).disable();
			preferencesHelper.setBoolean(PreferencesHelper.PROTECT_ME_STATUS,
					toggleButton.isChecked());
			Toast.makeText(Home.this, "Protect me disabled.",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void setupScheduledBackup(boolean value) {

		if (value) {
			Toast.makeText(this,
					"Automatic backups started, everyday at 2:00 AM",
					Toast.LENGTH_SHORT).show();
			Backup.getInstance(this).autoBackups(Backup.ON);
			preferencesHelper.setBoolean(PreferencesHelper.AUTO_BACKUP_STATUS,
					true);
		} else {
			Toast.makeText(this, "Automatic backups cancelled.",
					Toast.LENGTH_SHORT).show();
			Backup.getInstance(this).autoBackups(Backup.OFF);
			preferencesHelper.setBoolean(PreferencesHelper.AUTO_BACKUP_STATUS,
					false);
		}

	}

	private void setupAutoBlips(ToggleButton toggleButton) {
		if (toggleButton.isChecked()) {
			dialogsHelper.showDialog(BLIP_DIALOG, toggleButton);
		} else {
			Receiver.getInstance(this).unregister(ReceiverType.LOW_BATTERY);
			preferencesHelper.setBoolean(PreferencesHelper.AUTO_BLIP_STATUS,
					toggleButton.isChecked());
			Toast.makeText(this, "Auto blips disabled.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void setupLocationTracker(ToggleButton toggle) {
		preferencesHelper.setBoolean(PreferencesHelper.AUTO_TRACK_STATUS,
				toggle.isChecked());
		if (toggle.isChecked()) {
			Toast.makeText(this, "Automatic tracking enabled.",
					Toast.LENGTH_SHORT).show();
			Intent startTracker = new Intent(this, Tracker.class);
			startTracker.putExtra(Tracker.ACTION, RequestType.TRACKER);
			startService(startTracker);
		} else {
			Toast.makeText(this, "Automatic tracking disabled.",
					Toast.LENGTH_SHORT).show();
			stopService(new Intent(this, Tracker.class));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//checkPlayServices();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	@Override
	public void onBackPressed() {
		if (exit)
			Home.this.finish();
		else {
			Toast.makeText(this, "Press Back again to Exit.",
					Toast.LENGTH_SHORT).show();
			exit = true;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					exit = false;
				}
			}, 3 * 1000);

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
		updateFacebookConnectPreferences();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.menuSettings:
			startActivity(new Intent(this, in.ceeq.activities.Settings.class));
			break;
		case R.id.actionHelp:
			startActivity(new Intent(this, Help.class));
			break;
		case R.id.menuShare:
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType(HTTP.PLAIN_TEXT_TYPE);
			shareIntent.putExtra(Intent.EXTRA_SUBJECT,
					"Ceeq Mobile Application - Device Security and Backup");
			shareIntent
					.putExtra(
							Intent.EXTRA_TEXT,
							"Hello, install Ceeq for free, enjoy your new security partner for Android.  \n\n");
			shareIntent.putExtra(Intent.EXTRA_TEXT,
					getString(R.string.ceeq_play_link));
			startActivity(Intent.createChooser(shareIntent, "Share to"));
			break;

		case R.id.menuExit:
			this.finish();

		}
		return false;
	}

	public class DialogsHelper implements DialogInterface.OnClickListener,
			DialogInterface.OnKeyListener {
		private ToggleButton toggleButton;
		private LayoutInflater inflater;
		private static final int NONE = -1;
		private AlertDialog.Builder alertDialogBuilder;
		private PreferencesHelper preferencesHelper;
		private View feedbackView, protectMeView;
		private Activity activity;
		private Context context;
		private ComponentName deviceAdminComponentName;

		private int dialogType;

		public DialogsHelper(Context context) {
			this.context = context;
			this.activity = (Activity) context;
			preferencesHelper = PreferencesHelper.getInstance(context);
		}

		public void showDialog(int dialogType) {
			this.dialogType = dialogType;
			alertDialogBuilder = new AlertDialog.Builder(context)
					.setTitle(getTitle());
			switch (dialogType) {
			case FEEDBACK_DIALOG:
				alertDialogBuilder.setView(getView(FEEDBACK_DIALOG))
						.setPositiveButton(getPositiveButtonString(), this);
				break;
			case DEVICE_ADMIN_DIALOG:
				alertDialogBuilder.setView(getView(DEVICE_ADMIN_DIALOG))
						.setPositiveButton(getPositiveButtonString(), this);
				break;
			case WIPE_DEVICE_DIALOG:
				alertDialogBuilder.setView(getView(WIPE_DEVICE_DIALOG))
						.setPositiveButton(getPositiveButtonString(), this);
				break;
			case WIPE_EXTERNAL_STORAGE_DIALOG:
				alertDialogBuilder.setView(
						getView(WIPE_EXTERNAL_STORAGE_DIALOG))
						.setPositiveButton(getPositiveButtonString(), this);
				break;
			case WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG:
				alertDialogBuilder.setView(
						getView(WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG))
						.setPositiveButton(getPositiveButtonString(), this);
				break;
			default:
				alertDialogBuilder.setSingleChoiceItems(getChoices(), NONE,
						this);
				break;
			}
			alertDialogBuilder
					.setNegativeButton(getNegativeButtonString(), this)
					.create().show();
		}

		public void showDialog(int dialogType, ToggleButton toggleButton) {
			this.dialogType = dialogType;
			this.toggleButton = toggleButton;

			alertDialogBuilder = new AlertDialog.Builder(context)
					.setTitle(getTitle());

			switch (dialogType) {
			case BLIP_DIALOG:
				alertDialogBuilder.setView(getView(BLIP_DIALOG));
				break;
			case DEVICE_ADMIN_DIALOG:
				alertDialogBuilder.setView(getView(DEVICE_ADMIN_DIALOG));
				break;
			case PROTECT_DIALOG:
				alertDialogBuilder.setView(getView(PROTECT_DIALOG));
				break;
			case STEALTH_DIALOG:
				alertDialogBuilder.setView(getView(STEALTH_DIALOG));
				break;
			default:
				break;

			}

			alertDialogBuilder
					.setPositiveButton(getPositiveButtonString(), this)
					.setNegativeButton(getNegativeButtonString(), this)
					.setOnKeyListener(this).create().show();
		}

		public int getTitle() {
			switch (dialogType) {
			case BACKUP_DIALOG:
				return R.string.dialog_title_backup;
			case RESTORE_DIALOG:
				return R.string.dialog_title_restore;
			case WIPE_DIALOG:
				return R.string.dialog_title_wipe;
			case BLIP_DIALOG:
				return R.string.dialog_title_blip;
			case FEEDBACK_DIALOG:
				return R.string.dialog_title_feedback;
			case PROTECT_DIALOG:
				return R.string.dialog_title_protect;
			case STEALTH_DIALOG:
				return R.string.dialog_title_stealth;
			case DEVICE_ADMIN_DIALOG:
				return R.string.dialog_title_device_admin;
			default:
				return R.string.dialog_title_wipe_device;
			}
		}

		public int getChoices() {
			switch (dialogType) {
			case BACKUP_DIALOG:
			case RESTORE_DIALOG:
				return R.array.backup_options;
			case WIPE:
				return R.array.wipe_options;
			default:
				return 0;
			}
		}

		public View getView(int dialogType) {
			this.dialogType = dialogType;
			inflater = activity.getLayoutInflater();
			switch (dialogType) {

			case BLIP_DIALOG:
				return inflater.inflate(R.layout.dialog_blips_info, null);

			case DEVICE_ADMIN_DIALOG:
				return inflater.inflate(R.layout.dialog_device_admin, null);

			case STEALTH_DIALOG:
				return inflater.inflate(R.layout.dialog_stealth_mode, null);

			case PROTECT_DIALOG:
				protectMeView = inflater.inflate(R.layout.dialog_protect_me,
						null);
				Button facebookConnect = (Button) protectMeView
						.findViewById(R.id.facebook_login);
				LinearLayout socialBox = (LinearLayout) protectMeView
						.findViewById(R.id.social_box);
				if (preferencesHelper
						.getBoolean(PreferencesHelper.FACEBOOK_CONNECT_STATUS)) {
					socialBox.setVisibility(View.GONE);
				} else {
					socialBox.setVisibility(View.VISIBLE);
				}
				facebookConnect.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Home.this.connectFacebook();
					}
				});
				EditText distressMessage = (EditText) protectMeView
						.findViewById(R.id.distressMessage);
				String storedMessage = preferencesHelper
						.getString(PreferencesHelper.DISTRESS_MESSAGE);
				if (!storedMessage.isEmpty())
					distressMessage.setText(storedMessage);
				return protectMeView;
			case WIPE_DEVICE_DIALOG:
				return inflater.inflate(R.layout.dialog_wipe, null);
			case WIPE_EXTERNAL_STORAGE_DIALOG:
				return inflater.inflate(R.layout.dialog_wipe_external_storage,
						null);
			case WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG:
				return inflater.inflate(R.layout.dialog_wipe, null);
			case FEEDBACK_DIALOG:
				feedbackView = inflater.inflate(R.layout.dialog_feedback, null);
				return feedbackView;
			default:
				feedbackView = inflater.inflate(R.layout.dialog_feedback, null);
				return feedbackView;

			}
		}

		public int getPositiveButtonString() {
			switch (dialogType) {
			case PROTECT_DIALOG:
				return R.string.save;
			case STEALTH_DIALOG:
				return R.string.enable;
			case BLIP_DIALOG:
				return R.string.okay;
			case FEEDBACK_DIALOG:
				return R.string.send;
			default:
				return R.string.continue_;
			}
		}

		public int getNegativeButtonString() {
			switch (dialogType) {
			default:
				return R.string.cancel;
			}
		}

		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK
					&& event.getAction() == KeyEvent.ACTION_UP) {
				resetToggle(toggleButton);
				dialog.dismiss();
			}
			return false;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			deviceAdminComponentName = new ComponentName(context,
					DeviceAdmin.class);

			switch (which) {

			case Dialog.BUTTON_NEGATIVE:
				resetToggle(toggleButton);
				dialog.dismiss();
				break;

			case Dialog.BUTTON_POSITIVE:
			default:
				switch (dialogType) {
				case BACKUP_DIALOG:
					Backup.getInstance(context).backup(which);
					dialog.dismiss();
					break;
				case RESTORE_DIALOG:
					Restore.getInstance(context).restore(which);
					dialog.dismiss();
					break;
				case WIPE_DIALOG:
					switch (which) {
					case Wipe.EXTERNAL_STORAGE:
						showDialog(WIPE_EXTERNAL_STORAGE_DIALOG);
						break;
					case Wipe.DEVICE:
						showDialog(WIPE_DEVICE_DIALOG);
						break;
					case Wipe.EXTERNAL_STORAGE_AND_DEVICE:
						showDialog(WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG);
						break;
					}
					dialog.dismiss();
					break;
				case BLIP_DIALOG:
					Receiver.getInstance(context).register(
							ReceiverType.LOW_BATTERY);
					preferencesHelper.setBoolean(
							PreferencesHelper.AUTO_BLIP_STATUS, true);
					showToast("Auto blips enabled.");
					break;
				case DEVICE_ADMIN_DIALOG:
					activity.startActivityForResult(
							new Intent(
									DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
									.putExtra(
											DevicePolicyManager.EXTRA_DEVICE_ADMIN,
											deviceAdminComponentName)
									.putExtra(
											DevicePolicyManager.EXTRA_ADD_EXPLANATION,
											activity.getString(R.string.help_note_25)),
							DEVICE_ADMIN_ACTIVATION_REQUEST);
					break;
				case FEEDBACK_DIALOG:
					EditText feedbackMessage = (EditText) feedbackView
							.findViewById(R.id.feedbackMessage);
					preferencesHelper.setString(
							PreferencesHelper.FEEDBACK_MESSAGE, feedbackMessage
									.getText().toString());
					Upload.getInstance(context).start(UploadType.FEEDBACK);
					break;
				case PROTECT_DIALOG:
					Protect.getInstance(context).enable();
					EditText distressMessage = (EditText) protectMeView
							.findViewById(R.id.distressMessage);
					preferencesHelper.setString(
							PreferencesHelper.DISTRESS_MESSAGE, distressMessage
									.getText().toString());
					preferencesHelper.setBoolean(
							PreferencesHelper.PROTECT_ME_STATUS, true);
					showToast("Protect me enabled. Just press power button 10 times for help.");
					break;
				case STEALTH_DIALOG:
					Notifications.getInstance(context).remove();
					preferencesHelper.setBoolean(
							PreferencesHelper.NOTIFICATIONS_STATUS, false);
					try {
						packageManager
								.setComponentEnabledSetting(
										new ComponentName(Home.this,
												Launcher.class),
										PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
										PackageManager.DONT_KILL_APP);
						try {
							startActivity(new Intent(Home.this, Launcher.class));
						} catch (Exception e) {
							// Let it be 3:)
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					Receiver.getInstance(context).register(
							ReceiverType.OUTGOING_CALLS);
					preferencesHelper.setBoolean(
							PreferencesHelper.STEALTH_MODE_STATUS, true);
					showToast("Stealth Mode enabled.");
					break;
				case WIPE_DEVICE_DIALOG:
					if (preferencesHelper
							.getBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS))
						Wipe.getInstance(context).device();
					else
						showDialog(DEVICE_ADMIN_DIALOG);
					break;
				case WIPE_EXTERNAL_STORAGE_DIALOG:
					Wipe.getInstance(context).externalStorage();
					break;
				case WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG:
					if (preferencesHelper
							.getBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS))
						Wipe.getInstance(context).deviceAndExternalStorage();
					else
						showDialog(DEVICE_ADMIN_DIALOG);
					break;
				default:
					break;
				}
				break;
			}

		}

		public void resetToggle(ToggleButton toggleButton) {
			if (toggleButton != null)
				toggleButton.setChecked(false);
		}

		public void showToast(String message) {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}

	}
	
	public static class ViewHolder {
		TextView header;
		TextView innerText;
		ImageView innerImage;
	}

	public class DrawerManager extends BaseAdapter implements
			ListView.OnItemClickListener {

		public DrawerManager() {
		}

		@Override
		public int getCount() {
			return 12;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}



		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			TextView header, innerText;
			ImageView innerImage;
			switch (position) {
			case 1:
			case 6:
				convertView = getLayoutInflater().inflate(
						R.layout.drawer_action_plus, null);
				return convertView;
			case 2:
			case 7:
				convertView = getLayoutInflater().inflate(
						R.layout.drawer_action_header, null);
				header = (TextView) convertView
						.findViewById(R.id.drawer_list_header);
				header.setText(getHeaderText(position));
				return convertView;
			default:
				convertView = getLayoutInflater().inflate(
						R.layout.drawer_action_inner, null);
				innerText = (TextView) convertView
						.findViewById(R.id.drawer_list_text);
				innerText.setText(getInnerText(position));
				innerImage = (ImageView) convertView
						.findViewById(R.id.drawer_list_icon);
				innerImage.setBackgroundResource(getInnerImage(position));
				return convertView;

			}

		}

		public int getInnerText(int position) {
			switch (position) {
			case 0:
				return R.string.my_device;
			case 3:
				return R.string.tab_home;
			case 4:
				return R.string.tab_backup;
			case 5:
				return R.string.tab_security;
			case 8:
				return R.string.privacy;
			case 9:
				return R.string.menu_feedback;
			case 10:
				return R.string.about;
			case 11:
				return R.string.rate;
			default:
				return R.string.rate;
			}
		}

		public int getHeaderText(int position) {
			switch (position) {
			case 2:
				return R.string.navigate;
			case 7:
				return R.string.more;
			default:
				return R.string.more;
			}
		}

		public int getInnerImage(int position) {
			switch (position) {
			case 0:
				return R.drawable.ic_stat_my_device;
			case 3:
				return R.drawable.ic_stat_home;
			case 4:
				return R.drawable.ic_stat_storage;
			case 5:
				return R.drawable.ic_stat_security;
			case 8:
				return R.drawable.ic_stat_privacy;
			case 9:
				return R.drawable.ic_stat_content_email;
			case 10:
				return R.drawable.ic_stat_action_about;
			case 11:
				return R.drawable.ic_stat_rating_important;
			default:
				return R.drawable.ic_stat_rating_important;
			}
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);

		}

		private void selectItem(int position) {

			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment = new MyDeviceFragment();
				break;
			case 3:
				fragment = new HomeFragment();
				break;
			case 4:
				fragment = new BackupFragment();
				break;
			case 5:
				fragment = new SecurityFragment();
				break;
			case 8:
				fragment = new PrivacyFragment();
				break;
			case 9:
				Intent emailIntent = new Intent(Intent.ACTION_SEND)
						.setType(HTTP.PLAIN_TEXT_TYPE);
				emailIntent
						.putExtra(
								Intent.EXTRA_EMAIL,
								new String[] { getString(R.string.ceeq_support_email) });
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion/Bugs");
				emailIntent.putExtra(
						Intent.EXTRA_TEXT,
						"[Ceeq Support \n User: "
								+ preferencesHelper.getString("accountName")
								+ "]");
				startActivity(emailIntent);
				break;
			case 10:
				fragment = new AboutApplicationFragment();
				break;
			case 11:
				Intent rateIntent = new Intent(Intent.ACTION_VIEW).setData(Uri
						.parse(getString(R.string.ceeq_play_link)));
				startActivity(rateIntent);
				break;
			default:
				actionList.setItemChecked(position, false);
				return;
			}

			if (fragment != null)
				fragmentManager.beginTransaction()
						.replace(R.id.container, fragment).commit();
			actionList.setItemChecked(position, true);
			drawerLayout.closeDrawer(Gravity.START);
		}

	}

	public class FBSessionStatus implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {

		}
	}
}