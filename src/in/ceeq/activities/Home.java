package in.ceeq.activities;

import hirondelle.date4j.DateTime;
import in.ceeq.Launcher;
import in.ceeq.R;
import in.ceeq.actions.Backup;
import in.ceeq.actions.Protect;
import in.ceeq.actions.Receiver;
import in.ceeq.actions.Receiver.ReceiverType;
import in.ceeq.actions.Restore;
import in.ceeq.actions.Wipe;
import in.ceeq.helpers.Helpers;
import in.ceeq.helpers.NotificationsHelper;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.receivers.DeviceAdmin;
import in.ceeq.services.Backups;
import in.ceeq.services.Tracker;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.protocol.HTTP;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
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
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.InflateException;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
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

	public enum Dialog {
		PROTECT, STEALTH, FEEDBACK, BACKUP, RESTORE, BLIP, WIPE, WIPE_EXTERNAL_STORAGE, WIPE_DEVICE, WIPE_EXTERNAL_STORAGE_AND_DEVICE, DEVICE_ADMIN
	}

	public enum ToggleState {
		OFF, ON
	}

	private Pager pagerAdapter;
	private ViewPager pager;
	private AlertDialog.Builder builder;
	private LayoutInflater inflater;
	private static LinearLayout timer;
	private static ProgressBar progressBar;
	private PreferencesHelper preferencesHelper;
	private Helpers helpers;
	private boolean exit = false;
	private static final String SENDER_ID = "909602096750";
	private static final int PLUS_ONE_REQUEST_CODE = 9025;
	private Session.StatusCallback statusCallback = new FBSessionStatus();

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
		setupPager();
		setupDrawer();

		if (preferencesHelper.getBoolean(PreferencesHelper.FIRST_LOGIN)) {
			setupFirstrun();
		}
	}

	/**
	 * instantiate the helpers required in this activity.
	 */
	public void setupHelpers() {
		preferencesHelper = new PreferencesHelper(this);
		helpers = Helpers.getInstance(this);
	}

	/**
	 * set up the view pager
	 */
	public void setupPager() {
		pagerAdapter = new Pager(getSupportFragmentManager(), this);
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(pagerAdapter);
		pager.requestTransparentRegion(pager);
	}

	/**
	 * set up the action bar
	 */

	public void setupActionbar() {
		// get the action bar, and hide the title
		getActionBar().setDisplayShowTitleEnabled(false);
		// get the action bar, set different logo/icon
		getActionBar().setIcon(R.drawable.ic_app_action_logo);
		// get the action bar, set home as clickable and button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	/**
	 * setup bugsense bug tracking in the app
	 */
	public void setupBugsense() {
		BugSenseHandler.initAndStartSession(Home.this, "5996b3d9");
	}

	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView actionList;
	private CharSequence drawerTitle;
	private CharSequence title;
	private TextView userId, userName;
	private ImageView userImage;
	private RelativeLayout userLoading, userDetails;
	private PlusOneButton plusOneButton;

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
		actionList.setAdapter(new DrawerAdapter());
		actionList.setOnItemClickListener(new DrawerMenuClickListener());
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
		};
		drawerLayout.setDrawerListener(drawerToggle);

	}

	/**
	 * The drawer adapter class for creating the Dialog view, mainly for
	 * inflating the listview in dialog.
	 * 
	 * @author Lucky
	 * 
	 */
	public class DrawerAdapter extends BaseAdapter {

		public DrawerAdapter() {

		}

		@Override
		public int getCount() {
			return 11;
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
			switch (position) {
			case 1:
				convertView = getLayoutInflater().inflate(
						R.layout.drawer_action_plus, null);
				plusOneButton = (PlusOneButton) convertView
						.findViewById(R.id.plus_one_button);
				plusOneButton.initialize(
						"http://plus.google.com/116561373543243917689",
						PLUS_ONE_REQUEST_CODE);
				return convertView;
			case 2:
			case 6:
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
				return convertView;
			}

		}

		public int getInnerText(int position) {
			switch (position) {
			case 0:
				return R.string.disconnect;
			case 3:
				return R.string.tab_home;
			case 4:
				return R.string.tab_backup;
			case 5:
				return R.string.tab_security;
			case 7:
				return R.string.privacy;
			case 8:
				return R.string.menu_feedback;
			case 9:
				return R.string.about;
			case 10:
				return R.string.rate;
			default:
				return R.string.rate;
			}
		}

		public int getHeaderText(int position) {
			switch (position) {
			case 2:
				return R.string.navigate;
			case 6:
				return R.string.more;
			default:
				return R.string.more;
			}
		}

	}

	/**
	 * if this is first run show the drawer once
	 */
	public void setupFirstrun() {
		drawerLayout.openDrawer(Gravity.START);
		preferencesHelper.setBoolean(PreferencesHelper.FIRST_LOGIN, false);
	}

	private GoogleCloudMessaging gcm;

	public void doRegisterGcm() {
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
					helpers.storeRegistrationId(registrationId);
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

	private class DrawerMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}

		private void selectItem(int position) {
			switch (position) {
			case 0:
				break;
			case 3:
			case 4:
			case 5:
				pager.setCurrentItem(position - 3);
				break;
			case 7:
				builder.setView(inflater.inflate(R.layout.dialog_privacy, null))
						.setPositiveButton(R.string.close, null).create()
						.show();
				break;
			case 8:
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
			case 9:
				builder.setView(inflater.inflate(R.layout.dialog_about, null))
						.setPositiveButton(R.string.close, null).create()
						.show();
				break;
			case 10:
				Intent rateIntent = new Intent(Intent.ACTION_VIEW).setData(Uri
						.parse(getString(R.string.ceeq_play_link)));
				startActivity(rateIntent);
				break;
			}

			actionList.setItemChecked(position, true);
			drawerLayout.closeDrawer(Gravity.START);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		this.title = title;
		getActionBar().setTitle(this.title);
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
		if (!helpers.isGooglePlayConnected()) {
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
					PreferencesHelper.FACEBOOK_CONNECT_STATUS, true);
		}
	}

	public void onButtonPressed(View v) {
		ToggleButton toggle;
		builder = new AlertDialog.Builder(this);
		switch (v.getId()) {

		case R.id.toggle_protect:
			toggle = (ToggleButton) v.findViewById(R.id.toggle_protect);
			setupProtectMe(toggle);
			break;

		case R.id.toggle_stealth:
			toggle = (ToggleButton) v.findViewById(R.id.toggle_stealth);
			setupStealthMode(toggle);
			break;

		case R.id.toggle_backup:
			toggle = (ToggleButton) v.findViewById(R.id.toggle_backup);
			setupScheduledBackup(toggle);
			break;

		case R.id.b_backup:
			Backup.getInstance(this).backup(Backups.ACTION_TYPE_ALL);
			break;

		case R.id.backup:
			new DialogHelper().showDialog(Dialog.BACKUP);
			break;

		case R.id.restore:
			new DialogHelper().showDialog(Dialog.RESTORE);
			break;

		case R.id.b_view:
			startActivity(new Intent(this, ViewBackups.class));
			break;

		case R.id.toggle_track:
			toggle = (ToggleButton) v.findViewById(R.id.toggle_track);
			setupLocationTracker(toggle);
			break;

		case R.id.toggle_blip:
			toggle = (ToggleButton) v.findViewById(R.id.toggle_blip);
			setupAutoBlips(toggle);
			break;

		case R.id.wipe:
			new DialogHelper().showDialog(Dialog.WIPE);
			break;

		case R.id.wipe_cache:
			Wipe.getInstance(this).cache();
			break;

		case R.id.b_feedback:
			new DialogHelper().showDialog(Dialog.FEEDBACK);
			break;
		}

	}

	private DialogHelper dialogHelper;

	private PackageManager packageManager;

	/**
	 * set up stealth mode
	 * 
	 * @param toggle
	 */

	private void setupStealthMode(ToggleButton toggle) {
		packageManager = this.getPackageManager();
		if (toggle.isChecked()) {
			dialogHelper = new DialogHelper(toggle);
			builder.setView(dialogHelper.getView(Dialog.STEALTH))
					.setPositiveButton(R.string.enable,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									NotificationsHelper
											.getInstance(Home.this)
											.hideNotification(
													NotificationsHelper.DEFAULT_NOTIFICATION_ID);
									preferencesHelper
											.setBoolean(
													PreferencesHelper.NOTIFICATIONS_STATUS,
													false);

									try {
										packageManager
												.setComponentEnabledSetting(
														new ComponentName(
																Home.this,
																Launcher.class),
														PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
														PackageManager.DONT_KILL_APP);
										try {
											startActivity(new Intent(Home.this,
													Launcher.class));
										} catch (Exception e) {
										}
									} catch (Exception e) {
										e.printStackTrace();
									}

									Receiver.getInstance(Home.this).register(
											ReceiverType.OUTGOING_CALLS);
									preferencesHelper
											.setBoolean(
													PreferencesHelper.STEALTH_MODE_STATUS,
													true);
									Toast.makeText(Home.this,
											"Stealth Mode enabled.",
											Toast.LENGTH_SHORT).show();

								}
							})
					.setNegativeButton(R.string.cancel,
							dialogHelper.new DialogNegativeButtonPressed())
					.setOnKeyListener(
							dialogHelper.new DialogBackButtonPressed())
					.create().show();
		} else {
			getPackageManager().setComponentEnabledSetting(
					new ComponentName(Home.this, Launcher.class),
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
			NotificationsHelper.getInstance(Home.this).defaultNotification();
			Receiver.getInstance(this).unregister(ReceiverType.OUTGOING_CALLS);
			preferencesHelper.setBoolean(PreferencesHelper.STEALTH_MODE_STATUS,
					false);
			Toast.makeText(this, "Stealth Mode disabled.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void setupProtectMe(ToggleButton toggle) {
		if (toggle.isChecked()) {
			dialogHelper = new DialogHelper(toggle);
			builder.setView(dialogHelper.getView(Dialog.PROTECT))
					.setPositiveButton(R.string.enable,
							dialogHelper.new DialogPositiveButtonPressed())
					.setNegativeButton(R.string.cancel,
							dialogHelper.new DialogNegativeButtonPressed())
					.setOnKeyListener(
							dialogHelper.new DialogBackButtonPressed())
					.create().show();
		} else {
			Protect.getInstance(this).disable();
			preferencesHelper.setBoolean(PreferencesHelper.PROTECT_ME_STATUS,
					toggle.isChecked());
			Toast.makeText(Home.this, "Protect me disabled.",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void setupScheduledBackup(ToggleButton toggle) {
		preferencesHelper.setBoolean(PreferencesHelper.AUTO_BACKUP_STATUS,
				toggle.isChecked());
		if (toggle.isChecked()) {
			timer.setVisibility(View.VISIBLE);
			Toast.makeText(this,
					"Automatic backups started, everyday at 2:00 AM",
					Toast.LENGTH_SHORT).show();
			helpers.setupAlarms(SwitchState.ON);
		} else {
			timer.setVisibility(View.GONE);
			Toast.makeText(this, "Automatic backups cancelled.",
					Toast.LENGTH_SHORT).show();
			helpers.setupAlarms(SwitchState.OFF);
		}
	}

	private void setupAutoBlips(ToggleButton toggle) {
		if (toggle.isChecked()) {
			new DialogHelper().showDialog(Dialog.BLIP);
		} else {
			Receiver.getInstance(this).unregister(ReceiverType.LOW_BATTERY);
			preferencesHelper.setBoolean(PreferencesHelper.AUTO_BLIP_STATUS,
					toggle.isChecked());
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
			startService(new Intent(this, Tracker.class));
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
		builder = new AlertDialog.Builder(this);
		inflater = this.getLayoutInflater();
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

	public class DialogHelper {
		private ToggleButton toggle;
		private LayoutInflater inflater;
		private static final int NONE = -1;
		private Dialog dialogType;

		public DialogHelper() {
		}

		public DialogHelper(ToggleButton toggle) {
			this.toggle = toggle;
		}

		public void showDialog(Dialog dialogType) {
			this.dialogType = dialogType;
			builder = new AlertDialog.Builder(Home.this).setTitle(getTitle());

			switch (dialogType) {
			case BACKUP:
			case RESTORE:
			case WIPE:
				builder.setSingleChoiceItems(getChoices(), NONE,
						new DialogOptionSelect());
				break;

			case FEEDBACK:
				builder.setView(getView(Dialog.FEEDBACK));
				builder.setPositiveButton(getPositiveButtonString(),
						new DialogPositiveButtonPressed());
			default:
				break;
			}

			builder.setNegativeButton(getNegativeButtonString(),
					new DialogNegativeButtonPressed()).setOnKeyListener(
					new DialogBackButtonPressed());
			builder.create().show();
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
			case STEALTH:
				return R.string.dialog_title_stealth;
			default:
				return 0;
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

		public View getView(Dialog dialogType) {
			this.dialogType = dialogType;
			inflater = Home.this.getLayoutInflater();
			switch (dialogType) {
			case BLIP:
				return inflater.inflate(R.layout.dialog_blips_info, null);
			case FEEDBACK:
				return inflater.inflate(R.layout.dialog_feedback, null);
			case STEALTH:
				return inflater.inflate(R.layout.dialog_stealth_mode, null);
			case PROTECT:
				View view = inflater.inflate(R.layout.dialog_protect_me, null);
				Button facebookConnect = (Button) view
						.findViewById(R.id.facebook_login);
				LinearLayout socialBox = (LinearLayout) view
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
				return view;
			default:
				break;
			}
			return null;
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
				return 0;
			}
		}

		public int getNegativeButtonString() {
			switch (dialogType) {
			case BACKUP:
			case RESTORE:
			case STEALTH:
			case BLIP:
			case FEEDBACK:
			case WIPE:
				return R.string.cancel;
			default:
				return 0;
			}
		}

		class DialogOptionSelect implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int action) {
				onOptionSelect(action);
				dialog.dismiss();
			}

			public void onOptionSelect(int data) {
				switch (dialogType) {
				case BACKUP:
					Backup.getInstance(Home.this).backup(data);
					break;
				case RESTORE:
					Restore.getInstance(Home.this).restore(data);
					break;
				case WIPE:
					break;
				default:
					break;

				}
			}
		}

		public class DialogPositiveButtonPressed implements
				DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int id) {
				onPositiveButtonPressed();
				dialog.dismiss();
			}

			public void onPositiveButtonPressed() {
				switch (dialogType) {
				case BLIP:
					break;
				case DEVICE_ADMIN:
					break;
				case FEEDBACK:

					break;
				case PROTECT:
					Protect.getInstance(Home.this).enable();
					preferencesHelper.setBoolean(
							PreferencesHelper.PROTECT_ME_STATUS, true);
					showToast("Protect me enabled. Just press power button 10 times for help.");
					break;
				case STEALTH:

					break;
				case WIPE:
					break;
				case WIPE_DEVICE:
					break;
				case WIPE_EXTERNAL_STORAGE:
					break;
				case WIPE_EXTERNAL_STORAGE_AND_DEVICE:
					break;
				default:
					break;

				}
			}
		}

		public void showToast(String message) {
			Toast.makeText(Home.this, message, Toast.LENGTH_SHORT).show();
		}

		public class DialogNegativeButtonPressed implements
				DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int id) {
				if (toggle != null)
					toggle.setChecked(false);
				dialog.dismiss();
			}
		}

		public class DialogBackButtonPressed implements OnKeyListener {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& event.getAction() == KeyEvent.ACTION_UP) {
					if (toggle != null)
						toggle.setChecked(false);
					dialog.dismiss();
				}
				return false;
			}
		}

	}

	public static class HomeFragment extends Fragment {
		public enum Status {
			ALL, AUTO_BACKUP, AUTO_TRACK, GPS, DEVICE_ADMIN, BACKUP, SYNC
		}

		private Helpers helpers;
		private PreferencesHelper preferencesHelper;
		private View view;
		private int counter;
		private ExpandableListView notificationList;
		private ExpandableListAdapter notificationListAdapter;
		private TextView statusText;
		private LinearLayout statusBox;
		private ArrayList<Status> notification_list;
		private ToggleButton toggle;
		private NotificationsHelper notificationsHelper;

		public HomeFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			view = inflater.inflate(R.layout.fragment_main, container, false);
			preferencesHelper = new PreferencesHelper(this.getActivity());
			helpers = Helpers.getInstance(getActivity());
			notificationsHelper = new NotificationsHelper(this.getActivity());
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
			return view;
		}

		public void showNotification() {
			if (preferencesHelper
					.getBoolean(PreferencesHelper.NOTIFICATIONS_STATUS))
				notificationsHelper.defaultNotification();
		}

		public void restoreToggleStates(View v) {
			toggle = (ToggleButton) v.findViewById(R.id.toggle_protect);
			toggle.setChecked(preferencesHelper
					.getBoolean(PreferencesHelper.PROTECT_ME_STATUS));
			toggle = (ToggleButton) v.findViewById(R.id.toggle_stealth);
			toggle.setChecked(preferencesHelper
					.getBoolean(PreferencesHelper.STEALTH_MODE_STATUS));
		}

		public int setStatus() {
			counter = 0;
			boolean backupStatus = setBackupStatus();
			boolean securityStatus = setSecurityStatus();
			if (backupStatus & securityStatus)
				preferencesHelper
						.setBoolean(PreferencesHelper.APP_STATUS, true);
			else
				preferencesHelper.setBoolean(PreferencesHelper.APP_STATUS,
						false);
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

			if (helpers.hasGpsEnabled() & helpers.hasDeviceAdminEnabled()) {
				return true;
			} else if (helpers.hasGpsEnabled()
					& !helpers.hasDeviceAdminEnabled()) {
				notification_list.add(Status.DEVICE_ADMIN);
				counter++;
				return false;
			} else if (!helpers.hasGpsEnabled()
					& helpers.hasDeviceAdminEnabled()) {
				notification_list.add(Status.GPS);
				counter++;
				return false;
			} else if (!helpers.hasGpsEnabled()
					& !helpers.hasDeviceAdminEnabled()) {
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
							AlertDialog.Builder alert = getDialog((notifications
									.get(childPosition)));
							alert.create().show();
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

			public AlertDialog.Builder getDialog(Status status) {
				switch (status) {
				case AUTO_BACKUP:
					return new AlertDialog.Builder(
							HomeFragment.this.getActivity())
							.setTitle("Enable AutoBackup")
							.setMessage("Activate auto backups ?")
							.setPositiveButton(R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											preferencesHelper
													.setBoolean(
															PreferencesHelper.AUTO_BACKUP_STATUS,
															true);
											setStatus();
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
				case AUTO_TRACK:
					return new AlertDialog.Builder(
							HomeFragment.this.getActivity())
							.setTitle("Activate AutoTrack")
							.setMessage("Activate auto tracking ?")
							.setPositiveButton(R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											preferencesHelper
													.setBoolean(
															PreferencesHelper.AUTO_TRACK_STATUS,
															true);
											setStatus();
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
				case GPS:
					return new AlertDialog.Builder(
							HomeFragment.this.getActivity())
							.setTitle("Activate GPS")
							.setMessage("Activate GPS ?")
							.setPositiveButton(R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											startActivity(new Intent(
													android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
											setStatus();
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
				case DEVICE_ADMIN:
					return new AlertDialog.Builder(
							HomeFragment.this.getActivity())
							.setTitle("Activate Device Admin")
							.setMessage("Activate Device Admin ?")
							.setPositiveButton(R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
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
											setStatus();
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
				case SYNC:
				default:
					return null;
				}
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

		public BackupFragment() {

		}

		public interface UpdateStatus {
			public void updateStatus();
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

		public void checkBackupFiles() {

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
		private View view;
		private ToggleButton toggle;
		private PreferencesHelper preferencesHelper;

		public SecurityFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			preferencesHelper = new PreferencesHelper(this.getActivity());

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
			} catch (GooglePlayServicesNotAvailableException e) {
				e.printStackTrace();
			} catch (InflateException e) {

			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			restoreToggleStates(view);
			return view;
		}

		public void restoreToggleStates(View v) {
			toggle = (ToggleButton) v.findViewById(R.id.toggle_track);
			toggle.setChecked(preferencesHelper
					.getBoolean(PreferencesHelper.AUTO_TRACK_STATUS));
			toggle = (ToggleButton) v.findViewById(R.id.toggle_blip);
			toggle.setChecked(preferencesHelper
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

	public static class AntivirusFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_antivirus,
					container, false);
			return view;
		}

	}

	public static class AboutDeviceFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_about,
					container, false);
			return rootView;
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

	public class Pager extends FragmentPagerAdapter {

		public Pager(FragmentManager fm, Context context) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment = new HomeFragment();
				break;
			case 1:
				fragment = new BackupFragment();
				break;
			case 2:
				fragment = new SecurityFragment();
				break;
			case 3:
				fragment = new AntivirusFragment();
				break;
			case 4:
				fragment = new AboutDeviceFragment();
				break;
			case 5:
				fragment = new AntiSpamFragment();
				break;
			}

			return fragment;
		}

		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.tab_home).toUpperCase(l);
			case 1:
				return getString(R.string.tab_backup).toUpperCase(l);
			case 2:
				return getString(R.string.tab_security).toUpperCase(l);
			case 3:
				return getString(R.string.tab_spam).toUpperCase(l);
			case 4:
				return getString(R.string.about).toUpperCase(l);
			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

}
