package in.ceeq.activities;

import hirondelle.date4j.DateTime;
import in.ceeq.Launcher;
import in.ceeq.R;
import in.ceeq.actions.Backup;
import in.ceeq.actions.Backup.State;
import in.ceeq.actions.Notifications;
import in.ceeq.actions.Protect;
import in.ceeq.actions.Receiver;
import in.ceeq.actions.Receiver.ReceiverType;
import in.ceeq.actions.Restore;
import in.ceeq.actions.Upload;
import in.ceeq.actions.Wipe;
import in.ceeq.helpers.PhoneHelper;
import in.ceeq.helpers.PhoneHelper.Phone;
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
import java.util.ArrayList;
import java.util.TimeZone;

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
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bugsense.trace.BugSenseHandler;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.PlusOneButton;

public class Home extends FragmentActivity {

	public static final String MESSENGER = "in.ceeq.Home";
	public final static int SHOW = 1;
	public final static int HIDE = 0;
	private static final int DEVICE_ADMIN_ACTIVATION_REQUEST = 9014;
	private static ProgressBar progressBar;
	private PreferencesHelper preferencesHelper;
	private PhoneHelper phoneHelper;
	private DialogsHelper dialogsHelper;
	private boolean exit = false;
	private static final String SENDER_ID = "909602096750";
	private static final int PLUS_ONE_REQUEST_CODE = 9025;
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

	public enum DialogType {
		PROTECT, STEALTH, FEEDBACK, BACKUP, RESTORE, BLIP, WIPE, WIPE_EXTERNAL_STORAGE, WIPE_DEVICE, WIPE_EXTERNAL_STORAGE_AND_DEVICE, DEVICE_ADMIN
	}

	/**
	 * 
	 * Switch button states constant enum, used in switch cases to change or
	 * save button states.
	 * 
	 */
	public enum SwitchState {
		ON, OFF
	}

	public enum ProgressBarState {
		SHOW, HIDE
	}

	public enum ComponentState {
		DISABLE, ENABLE
	}

	public enum ToggleState {
		OFF, ON
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupActionbar();
		setupBugsense();
		setContentView(R.layout.activity_home);
		this.title = drawerTitle = getTitle();

		setupHelpers();
		checkPlayServices();
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

	public void setupBugsense() {
		BugSenseHandler.initAndStartSession(Home.this, "5996b3d9");
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
			TextView header, inner;
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
				inner = (TextView) convertView
						.findViewById(R.id.drawer_list_inner);
				inner.setText(getInnerText(position));
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
					phoneHelper.set(Phone.REGISTRATION_ID, registrationId);
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

	public static Handler messageHandler = new MessageHandler();

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
		if (!phoneHelper.enabled(Phone.PLAY_SERVICES)) {
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
			dialogsHelper.showDialog(DialogType.BACKUP);
			break;

		case R.id.restore:
			dialogsHelper.showDialog(DialogType.RESTORE);
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
			dialogsHelper.showDialog(DialogType.WIPE);
			break;

		case R.id.wipe_cache:
			Wipe.getInstance(this).cache();
			break;

		case R.id.b_feedback:
			dialogsHelper.showDialog(DialogType.FEEDBACK);
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
			dialogsHelper.showDialog(DialogType.STEALTH, toggleButton);
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
			dialogsHelper.showDialog(DialogType.PROTECT, toggleButton);
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
			Backup.getInstance(this).autoBackups(State.ON);
			preferencesHelper.setBoolean(PreferencesHelper.AUTO_BACKUP_STATUS,
					true);
		} else {
			Toast.makeText(this, "Automatic backups cancelled.",
					Toast.LENGTH_SHORT).show();
			Backup.getInstance(this).autoBackups(State.OFF);
			preferencesHelper.setBoolean(PreferencesHelper.AUTO_BACKUP_STATUS,
					false);
		}

	}

	private void setupAutoBlips(ToggleButton toggleButton) {
		if (toggleButton.isChecked()) {
			dialogsHelper.showDialog(DialogType.BLIP, toggleButton);
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
		checkPlayServices();
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

		private DialogType dialogType;

		public DialogsHelper(Context context) {
			this.context = context;
			this.activity = (Activity) context;
			preferencesHelper = PreferencesHelper.getInstance(context);
		}

		public void showDialog(DialogType dialogType) {
			this.dialogType = dialogType;
			alertDialogBuilder = new AlertDialog.Builder(context)
					.setTitle(getTitle());
			switch (dialogType) {
			case FEEDBACK:
				alertDialogBuilder.setView(getView(DialogType.FEEDBACK))
						.setPositiveButton(getPositiveButtonString(), this);
				break;
			case DEVICE_ADMIN:
				alertDialogBuilder.setView(getView(DialogType.DEVICE_ADMIN))
						.setPositiveButton(getPositiveButtonString(), this);
				break;
			case WIPE_DEVICE:
				alertDialogBuilder.setView(getView(DialogType.WIPE_DEVICE))
						.setPositiveButton(getPositiveButtonString(), this);
				break;
			case WIPE_EXTERNAL_STORAGE:
				alertDialogBuilder.setView(
						getView(DialogType.WIPE_EXTERNAL_STORAGE))
						.setPositiveButton(getPositiveButtonString(), this);
				break;
			case WIPE_EXTERNAL_STORAGE_AND_DEVICE:
				alertDialogBuilder.setView(
						getView(DialogType.WIPE_EXTERNAL_STORAGE_AND_DEVICE))
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

		public void showDialog(DialogType dialogType, ToggleButton toggleButton) {
			this.dialogType = dialogType;
			this.toggleButton = toggleButton;

			alertDialogBuilder = new AlertDialog.Builder(context)
					.setTitle(getTitle());

			switch (dialogType) {
			case BLIP:
				alertDialogBuilder.setView(getView(DialogType.BLIP));
				break;
			case DEVICE_ADMIN:
				alertDialogBuilder.setView(getView(DialogType.DEVICE_ADMIN));
				break;
			case PROTECT:
				alertDialogBuilder.setView(getView(DialogType.PROTECT));
				break;
			case STEALTH:
				alertDialogBuilder.setView(getView(DialogType.STEALTH));
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
			case BACKUP:
				return R.string.dialog_title_backup;
			case RESTORE:
				return R.string.dialog_title_restore;
			case WIPE:
				return R.string.dialog_title_wipe;
			case BLIP:
				return R.string.dialog_title_blip;
			case FEEDBACK:
				return R.string.dialog_title_feedback;
			case PROTECT:
				return R.string.dialog_title_protect;
			case STEALTH:
				return R.string.dialog_title_stealth;
			case DEVICE_ADMIN:
				return R.string.dialog_title_device_admin;
			default:
				return R.string.dialog_title_wipe_device;
			}
		}

		public int getChoices() {
			switch (dialogType) {
			case BACKUP:
			case RESTORE:
				return R.array.backup_options;
			case WIPE:
				return R.array.wipe_options;
			default:
				return 0;
			}
		}

		public View getView(DialogType dialogType) {
			this.dialogType = dialogType;
			inflater = activity.getLayoutInflater();
			switch (dialogType) {

			case BLIP:
				return inflater.inflate(R.layout.dialog_blips_info, null);

			case DEVICE_ADMIN:
				return inflater.inflate(R.layout.dialog_device_admin, null);

			case STEALTH:
				return inflater.inflate(R.layout.dialog_stealth_mode, null);

			case PROTECT:
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
			case WIPE_DEVICE:
				return inflater.inflate(R.layout.dialog_wipe, null);
			case WIPE_EXTERNAL_STORAGE:
				return inflater.inflate(R.layout.dialog_wipe_external_storage,
						null);
			case WIPE_EXTERNAL_STORAGE_AND_DEVICE:
				return inflater.inflate(R.layout.dialog_wipe, null);
			case FEEDBACK:
				feedbackView = inflater.inflate(R.layout.dialog_feedback, null);
				return feedbackView;
			default:
				feedbackView = inflater.inflate(R.layout.dialog_feedback, null);
				return feedbackView;

			}
		}

		public int getPositiveButtonString() {
			switch (dialogType) {
			case PROTECT:
				return R.string.save;
			case STEALTH:
				return R.string.enable;
			case BLIP:
				return R.string.okay;
			case FEEDBACK:
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
				case BACKUP:
					Backup.getInstance(context).backup(which);
					dialog.dismiss();
					break;
				case RESTORE:
					Restore.getInstance(context).restore(which);
					dialog.dismiss();
					break;
				case WIPE:
					switch (which) {
					case Wipe.EXTERNAL_STORAGE:
						showDialog(DialogType.WIPE_EXTERNAL_STORAGE);
						break;
					case Wipe.DEVICE:
						showDialog(DialogType.WIPE_DEVICE);
						break;
					case Wipe.EXTERNAL_STORAGE_AND_DEVICE:
						showDialog(DialogType.WIPE_EXTERNAL_STORAGE_AND_DEVICE);
						break;
					}
					dialog.dismiss();
					break;
				case BLIP:
					Receiver.getInstance(context).register(
							ReceiverType.LOW_BATTERY);
					preferencesHelper.setBoolean(
							PreferencesHelper.AUTO_BLIP_STATUS, true);
					showToast("Auto blips enabled.");
					break;
				case DEVICE_ADMIN:
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
				case FEEDBACK:
					EditText feedbackMessage = (EditText) feedbackView
							.findViewById(R.id.feedbackMessage);
					preferencesHelper.setString(
							PreferencesHelper.FEEDBACK_MESSAGE, feedbackMessage
									.getText().toString());
					Upload.getInstance(context).start(UploadType.FEEDBACK);
					break;
				case PROTECT:
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
				case STEALTH:
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
				case WIPE_DEVICE:
					if (preferencesHelper
							.getBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS))
						Wipe.getInstance(context).device();
					else
						showDialog(DialogType.DEVICE_ADMIN);
					break;
				case WIPE_EXTERNAL_STORAGE:
					Wipe.getInstance(context).externalStorage();
					break;
				case WIPE_EXTERNAL_STORAGE_AND_DEVICE:
					if (preferencesHelper
							.getBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS))
						Wipe.getInstance(context).deviceAndExternalStorage();
					else
						showDialog(DialogType.DEVICE_ADMIN);
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

	public static class HomeFragment extends Fragment {

		public enum Status {
			ALL, AUTO_BACKUP, AUTO_TRACK, GPS, DEVICE_ADMIN, BACKUP, SYNC
		}

		private PhoneHelper phoneHelper;
		private PreferencesHelper preferencesHelper;
		private View view;
		private int counter;
		private ExpandableListView notificationList;
		private ExpandableListAdapter notificationListAdapter;
		private TextView statusText;
		private LinearLayout statusBox;
		private ArrayList<Status> notification_list;
		private ToggleButton toggleButton;
		private PlusOneButton plusOneButton;

		public HomeFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			view = inflater.inflate(R.layout.fragment_main, container, false);
			setupHelpers();

			notificationList = (ExpandableListView) view
					.findViewById(R.id.notifications);
			notification_list = new ArrayList<Status>();
			statusText = (TextView) view.findViewById(R.id.statusText);
			statusBox = (LinearLayout) view.findViewById(R.id.statusBox);
			restoreToggleStates(view);
			if (setStatus() > 0) {
				statusText.setText(getString(R.string.app_status_bad));
				statusBox.setBackgroundResource(R.drawable.ic_bg_red);
				showNotification();
			} else {
				statusText.setText(getString(R.string.app_status_good));
				showNotification();
			}

			notificationListAdapter = new ListAdapter(this.getActivity(),
					counter,
					preferencesHelper.getBoolean(PreferencesHelper.APP_STATUS),
					notification_list);
			notificationList.setAdapter(notificationListAdapter);

			plusOneButton = (PlusOneButton) view
					.findViewById(R.id.plus_one_button);
			plusOneButton.initialize(
					"http://plus.google.com/116561373543243917689",
					PLUS_ONE_REQUEST_CODE);
			return view;
		}

		public void setupHelpers() {
			phoneHelper = PhoneHelper.getInstance(getActivity());
			preferencesHelper = PreferencesHelper.getInstance(this
					.getActivity());
		}

		public void showNotification() {
			if (preferencesHelper
					.getBoolean(PreferencesHelper.NOTIFICATIONS_STATUS))
				Notifications.getInstance(getActivity()).show();
		}

		public void restoreToggleStates(View v) {
			toggleButton = (ToggleButton) v.findViewById(R.id.toggle_protect);
			toggleButton.setChecked(preferencesHelper
					.getBoolean(PreferencesHelper.PROTECT_ME_STATUS));
			toggleButton = (ToggleButton) v.findViewById(R.id.toggle_stealth);
			toggleButton.setChecked(preferencesHelper
					.getBoolean(PreferencesHelper.STEALTH_MODE_STATUS));
		}

		public int setStatus() {
			counter = 0;
			boolean backupStatus = setBackupStatus();
			boolean securityStatus = setSecurityStatus();
			if (backupStatus & securityStatus) {
				preferencesHelper
						.setBoolean(PreferencesHelper.APP_STATUS, true);
			} else {
				preferencesHelper.setBoolean(PreferencesHelper.APP_STATUS,
						false);
			}
			return counter;
		}

		public boolean setBackupStatus() {
			if (preferencesHelper
					.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS)
					& isBackupDelayed()) {
				return true;
			} else if (!preferencesHelper
					.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS)
					& isBackupDelayed()) {
				notification_list.add(Status.AUTO_BACKUP);
				counter++;
				return false;
			} else if (preferencesHelper
					.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS)
					& !isBackupDelayed()) {
				notification_list.add(Status.BACKUP);
				counter++;
				return false;
			} else if (!preferencesHelper
					.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS)
					& !isBackupDelayed()) {
				notification_list.add(Status.AUTO_BACKUP);
				notification_list.add(Status.BACKUP);
				counter += 2;
				return false;
			}
			return false;
		}

		public boolean isBackupDelayed() {
			if (preferencesHelper.getString(PreferencesHelper.LAST_BACKUP_DATE)
					.isEmpty())
				return true;
			if (DateTime.now(TimeZone.getDefault()).numDaysFrom(
					new DateTime(preferencesHelper
							.getString(PreferencesHelper.LAST_BACKUP_DATE))) > -3)
				return true;

			return false;
		}

		public boolean setSecurityStatus() {
			boolean deviceAdminEnabled = preferencesHelper
					.getBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS);
			boolean gpsEnabled = phoneHelper.enabled(Phone.GPS);
			if (gpsEnabled & deviceAdminEnabled) {
				return true;
			} else if (gpsEnabled & !deviceAdminEnabled) {
				notification_list.add(Status.DEVICE_ADMIN);
				counter++;
				return false;
			} else if (!gpsEnabled & deviceAdminEnabled) {
				notification_list.add(Status.GPS);
				counter++;
				return false;
			} else if (!gpsEnabled & !deviceAdminEnabled) {
				notification_list.add(Status.GPS);
				notification_list.add(Status.DEVICE_ADMIN);
				counter += 2;
				return false;
			}
			return false;
		}

		public class ListAdapter extends BaseExpandableListAdapter {

			protected static final int DEVICE_ADMIN_ACTIVATION_REQUEST = 0;
			public ArrayList<Status> notifications = new ArrayList<Status>();
			public int notificationCounter;
			public LayoutInflater inflater;
			public Context context;
			public boolean status;
			private ComponentName deviceAdminComponentName;

			public ListAdapter(Context context, int counter, boolean status,
					ArrayList<Status> n_list) {
				this.context = context;
				this.notifications = n_list;
				this.notificationCounter = counter;
				this.status = status;
				this.inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				this.deviceAdminComponentName = new ComponentName(context,
						DeviceAdmin.class);
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				return null;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				return 0;
			}

			@Override
			public View getChildView(int groupPosition,
					final int childPosition, boolean isLastChild,
					View convertView, ViewGroup parent) {
				TextView text;
				Button button;
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.list_status_inner,
							null);
				}

				text = (TextView) convertView.findViewById(R.id.n_text);
				if (notificationCounter == 0) {
					text.setText("You are protected");
				} else {
					text.setText(getNotificationMessage((notifications
							.get(childPosition))));
					text.setTag(R.string.intent_type,
							(notifications.get(childPosition)));
					button = (Button) convertView.findViewById(R.id.activate);
					button.setText(getButtonLabel((notifications
							.get(childPosition))));
					button.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							onButtonPress((notifications.get(childPosition)));
						}
					});
				}
				return convertView;
			}

			public String getNotificationMessage(Status status) {
				switch (status) {
				case AUTO_BACKUP:
					return getString(R.string.status_note_1);
				case AUTO_TRACK:
					return getString(R.string.status_note_2);
				case GPS:
					return getString(R.string.status_note_3);
				case DEVICE_ADMIN:
					return getString(R.string.status_note_4);
				case BACKUP:
					return getString(R.string.status_note_5);
				default:
					return getString(R.string.status_note_0);
				}
			}

			public String getButtonLabel(Status status) {
				switch (status) {
				case AUTO_BACKUP:
					return getString(R.string.enable);
				case BACKUP:
					return getString(R.string.backupButton);
				default:
					return getString(R.string.enable);
				}
			}

			public void onButtonPress(Status status) {
				switch (status) {
				case AUTO_BACKUP:
					((Home) getActivity()).setupScheduledBackup(true);
					break;
				case AUTO_TRACK:
					preferencesHelper.setBoolean(
							PreferencesHelper.AUTO_TRACK_STATUS, true);
					break;
				case GPS:

					startActivity(new Intent(
							android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					break;
				case DEVICE_ADMIN:
					startActivityForResult(
							new Intent(
									DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
									.putExtra(
											DevicePolicyManager.EXTRA_DEVICE_ADMIN,
											deviceAdminComponentName)
									.putExtra(
											DevicePolicyManager.EXTRA_ADD_EXPLANATION,
											"Activating Device Administrator enables all the security features of the application."),
							DEVICE_ADMIN_ACTIVATION_REQUEST);
					break;
				case SYNC:
					break;
				default:
					break;
				}

				((Home) getActivity()).resetHome();
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				return notifications.size();
			}

			@Override
			public Object getGroup(int groupPosition) {
				return null;
			}

			@Override
			public int getGroupCount() {
				return 1;
			}

			@Override
			public void onGroupCollapsed(int groupPosition) {
				super.onGroupCollapsed(groupPosition);
			}

			@Override
			public void onGroupExpanded(int groupPosition) {
				super.onGroupExpanded(groupPosition);
			}

			@Override
			public long getGroupId(int groupPosition) {
				return 0;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.list_status_outer,
							null);
				}

				TextView totalCounter = (TextView) convertView
						.findViewById(R.id.n_count);
				totalCounter.setText(notificationCounter + "");
				if (!status)
					totalCounter.setBackgroundResource(R.drawable.ic_bg_red);
				TextView header = (TextView) convertView
						.findViewById(R.id.n_header);
				header.setText(getString(R.string.notifications));
				if (!status)
					header.setTextColor(getResources().getColor(R.color.red));
				return convertView;
			}

			@Override
			public boolean hasStableIds() {
				return false;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,
					int childPosition) {
				return false;
			}

		}
	}

	public static class BackupFragment extends Fragment {

		private static final String BACKUP_TIME = "02:00:00";
		private PreferencesHelper preferencesHelper;
		private ToggleButton toggle;
		private TextView hours, mins, unit, div, lastBackupDate;
		private CountDownTimer timerClock;
		private View view;
		private LinearLayout timer;

		public BackupFragment() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			preferencesHelper = new PreferencesHelper(this.getActivity());
			view = inflater.inflate(R.layout.fragment_backup, container, false);
			progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
			timer = (LinearLayout) view.findViewById(R.id.timerLayout);
			hours = (TextView) view.findViewById(R.id.hours);
			mins = (TextView) view.findViewById(R.id.mins);
			unit = (TextView) view.findViewById(R.id.unit);
			div = (TextView) view.findViewById(R.id.div);
			lastBackupDate = (TextView) view.findViewById(R.id.lastBackupDate);
			setTimerClock();
			restoreToggleStates(view);

			timerClock = new CountDown(Integer.parseInt(mins.getText()
					.toString()) * 60 * 1000, 1 * 1000);
			timerClock.start();

			if (!preferencesHelper
					.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS)) {
				timer.setVisibility(View.GONE);
			} else {
				timer.setVisibility(View.VISIBLE);
			}

			lastBackupDate.setText(getLastBackupLabel());

			return view;
		}

		public void setTimerClock() {
			div.setText(":");
			int numberOfHours = (int) DateTime.now(TimeZone.getDefault())
					.numSecondsFrom(new DateTime(BACKUP_TIME)) / 3600;
			int numberOfMinutes = (int) ((DateTime.now(TimeZone.getDefault())
					.numSecondsFrom(new DateTime(BACKUP_TIME)) / 60) % 60);
			if (numberOfHours == 0) {
				if (numberOfMinutes > 0) {
					hours.setText("00");
					mins.setText(String.format("%02d", 1 + numberOfMinutes));
					unit.setText("M");
				} else {
					hours.setText("23");
					mins.setText(String.format("%02d", 61 + numberOfMinutes));
					unit.setText("H");
				}

			} else if (numberOfHours > 0) {
				hours.setText(String.format("%02d", Math.abs(numberOfHours)));
				mins.setText(String.format("%02d", 1 + numberOfMinutes));
				unit.setText("H");
			} else if (numberOfHours < 0) {
				hours.setText(String.format("%02d", 23 + numberOfHours));
				mins.setText(String.format("%02d", 61 + numberOfMinutes));
				unit.setText("H");
			}
		}

		public String getLastBackupLabel() {
			if (preferencesHelper.getString(PreferencesHelper.LAST_BACKUP_DATE)
					.isEmpty())
				return getString(R.string.status_note_6);
			switch (new DateTime(
					preferencesHelper
							.getString(PreferencesHelper.LAST_BACKUP_DATE))
					.numDaysFrom(DateTime.today(TimeZone.getDefault()))) {
			case 0:
				return getString(R.string.status_note_7);
			case 1:
				return getString(R.string.status_note_8);
			case 2:
				return getString(R.string.status_note_9);
			case 3:
			case 4:
			case 5:
			case 6:
				return getString(R.string.status_note_10);
			case 7:
			case 8:
				return getString(R.string.status_note_11);
			default:
				return getString(R.string.status_note_11);
			}
		}

		public void restoreToggleStates(View v) {
			toggle = (ToggleButton) v.findViewById(R.id.toggle_backup);
			toggle.setChecked(preferencesHelper
					.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS));
		}

		public class CountDown extends CountDownTimer {

			public CountDown(long startTime, long interval) {
				super(startTime, interval);
			}

			@Override
			public void onTick(long millisUntilFinished) {
				mins.setText(String.format("%02d", millisUntilFinished
						/ (1000 * 60)));
			}

			@Override
			public void onFinish() {
				if (mins.getText().toString().equals("00")) {
					hours.setText(String.format("%02d",
							(Integer.parseInt(hours.getText().toString()) - 1)));
					mins.setText("59");
					unit.setText("H");
				}

				if (hours.getText().toString().equals("01")) {
					hours.setText("00");
					unit.setText("M");
				}

				if (hours.getText().toString().equals("00")) {
					hours.setText("23");
					mins.setText("59");
					unit.setText("H");
				}
			}
		}

	}

	public static class SecurityFragment extends Fragment implements
			OnMyLocationChangeListener {

		private GoogleMap map;
		private static View view;
		private ToggleButton toggleButton;
		private PreferencesHelper preferencesHelper;

		public SecurityFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			preferencesHelper = new PreferencesHelper(getActivity());
			if (view != null) {
				ViewGroup parent = (ViewGroup) view.getParent();
				if (parent != null)
					parent.removeView(view);
			}
			try {
				view = inflater.inflate(R.layout.fragment_security, container,
						false);

				map = ((SupportMapFragment) getActivity()
						.getSupportFragmentManager().findFragmentById(R.id.map))
						.getMap();
				map.getUiSettings().setAllGesturesEnabled(false);
				map.getUiSettings().setZoomControlsEnabled(false);
				map.getUiSettings().setMyLocationButtonEnabled(false);
				map.setMyLocationEnabled(true);
				map.setOnMyLocationChangeListener(this);
				MapsInitializer.initialize(getActivity());
			} catch (Exception e) {
				e.printStackTrace();
			}

			restoreToggleStates(view);
			return view;
		}

		public void restoreToggleStates(View view) {

			toggleButton = (ToggleButton) view.findViewById(R.id.toggle_track);
			toggleButton.setChecked(preferencesHelper
					.getBoolean(PreferencesHelper.AUTO_TRACK_STATUS));
			toggleButton = (ToggleButton) view.findViewById(R.id.toggle_blip);
			toggleButton.setChecked(preferencesHelper
					.getBoolean(PreferencesHelper.AUTO_BLIP_STATUS));
		}

		@Override
		public void onMyLocationChange(Location newLocation) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					newLocation.getLatitude(), newLocation.getLongitude()), 15));
		}
	}

	public static class AntiSpamFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_spams,
					container, false);
			return rootView;
		}
	}

	public static class PrivacyFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_privacy,
					container, false);
			return rootView;
		}
	}

	public static class AboutApplicationFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_about_app,
					container, false);
			return rootView;
		}
	}

	public static class MapFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_map, container,
					false);
			return view;
		}

	}

	public static class MyDeviceFragment extends Fragment {
		private PhoneHelper phoneHelper;
		private PreferencesHelper preferencesHelper;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_about, container,
					false);
			preferencesHelper = PreferencesHelper.getInstance(getActivity());
			phoneHelper = PhoneHelper.getInstance(getActivity());
			setupData(view);
			return view;
		}

		public void setupData(View view) {
			TextView text = (TextView) view.findViewById(R.id.c_account);
			text.setText(preferencesHelper
					.getString(PreferencesHelper.ACCOUNT_USER_ID));
			text = (TextView) view.findViewById(R.id.c_name);
			text.setText(preferencesHelper
					.getString(PreferencesHelper.ACCOUNT_USER_NAME));
			text = (TextView) view.findViewById(R.id.c_sim);
			text.setText(phoneHelper.get(Phone.SIM_ID));
			text = (TextView) view.findViewById(R.id.c_imsi);
			text.setText(phoneHelper.get(Phone.IMSI));
			text = (TextView) view.findViewById(R.id.c_iemi);
			text.setText(phoneHelper.get(Phone.IEMI));
			text = (TextView) view.findViewById(R.id.c_gps);
			text.setText(booleanToString(phoneHelper.enabled(Phone.GPS)));
			text = (TextView) view.findViewById(R.id.c_admin);
			text.setText(booleanToString(preferencesHelper
					.getBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS)));
			text = (TextView) view.findViewById(R.id.c_operator);
			text.setText(phoneHelper.get(Phone.OPERATOR));
			text = (TextView) view.findViewById(R.id.c_size);
			text.setText(phoneHelper.get(Phone.SIZE));
			text = (TextView) view.findViewById(R.id.c_pixels);
			text.setText(phoneHelper.get(Phone.DENSITY));
			text = (TextView) view.findViewById(R.id.c_apps);
			text.setText(phoneHelper.get(Phone.APP_COUNT) + "");

		}

		public String booleanToString(boolean value) {
			return (value) ? "ON" : "OFF";
		}

	}

	public class FBSessionStatus implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {

		}
	}

	public static class MessageHandler extends Handler {
		@Override
		public void handleMessage(Message message) {
			int state = message.arg1;
			switch (state) {
			case HIDE:
				progressBar.setVisibility(View.GONE);
				break;
			case SHOW:
				progressBar.setVisibility(View.VISIBLE);
				break;
			}
		}
	}
}