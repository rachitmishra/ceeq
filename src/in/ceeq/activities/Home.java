package in.ceeq.activities;

import in.ceeq.Launcher;
import in.ceeq.R;
import in.ceeq.actions.Backup;
import in.ceeq.actions.Notifications;
import in.ceeq.actions.Phone;
import in.ceeq.actions.Protect;
import in.ceeq.actions.Receiver;
import in.ceeq.actions.Receiver.ReceiverType;
import in.ceeq.actions.Wipe;
import in.ceeq.commons.DialogHelper;
import in.ceeq.commons.DrawerManager;
import in.ceeq.fragments.BackupFragment;
import in.ceeq.fragments.HomeFragment;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Backups;
import in.ceeq.services.Locater.RequestType;
import in.ceeq.services.Tracker;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.protocol.HTTP;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class Home extends FragmentActivity {

	public static final String MESSENGER = "in.ceeq.Home";
	private PreferencesHelper preferencesHelper;
	private Phone phoneHelper;
	private DialogHelper dialogsHelper;
	private static final int NONE = -1;
	private boolean exit = false;
	private static final String SENDER_ID = "909602096750";
	//private Session.StatusCallback statusCallback = new FBSessionStatus();
	private FragmentManager fragmentManager;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView actionList;
	private CharSequence drawerTitle;
	private CharSequence title;
	private TextView userId, userName;
	private ImageView userImage;
	private RelativeLayout userLoading, userDetails;

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
		//setupFacebookConnect(savedInstanceState);
		setupHome();
		setupDrawer();

		if (preferencesHelper.getBoolean(PreferencesHelper.FIRST_LOGIN)) {
			setupFirstrun();
		}
	}

	public void setupHelpers() {
		preferencesHelper = new PreferencesHelper(this);
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
		actionList.setAdapter(DrawerManager.getInstance(this, fragmentManager, drawerLayout, actionList));
		actionList.setOnItemClickListener(DrawerManager.getInstance(this, fragmentManager, drawerLayout, actionList));
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
					Phone.set(Phone.REGISTRATION_ID, registrationId, Home.this);
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

//	public void setupFacebookConnect(Bundle savedInstanceState) {
//		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
//
//		Session session = Session.getActiveSession();
//		if (session == null) {
//			if (savedInstanceState != null) {
//				session = Session.restoreSession(this, null, statusCallback,
//						savedInstanceState);
//			}
//			if (session == null) {
//				session = new Session(this);
//			}
//			Session.setActiveSession(session);
//			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
//				session.openForRead(new Session.OpenRequest(this)
//						.setCallback(statusCallback));
//			}
//		}
//	}
//
//	public void connectFacebook() {
//		Session session = Session.getActiveSession();
//		if (!session.isOpened() && !session.isClosed()) {
//			session.openForRead(new Session.OpenRequest(this)
//					.setCallback(statusCallback));
//		} else {
//			Session.openActiveSession(this, true, statusCallback);
//		}
//	}

	public void checkPlayServices() {
		if (!Phone.enabled(Phone.PLAY_SERVICES, this)) {
			startActivity(new Intent(this, GooglePlus.class).putExtra(
					GooglePlus.FROM, GooglePlus.HOME));
			this.finish();
		}
	}

//	public void updateFacebookConnectPreferences() {
//
//		Session session = Session.getActiveSession();
//		if (session.isOpened()) {
//			preferencesHelper.setBoolean(
//					PreferencesHelper.FACEBOOK_CONNECT_STATUS, true);
//		} else {
//			preferencesHelper.setBoolean(
//					PreferencesHelper.FACEBOOK_CONNECT_STATUS, false);
//		}
//	}

	public void onButtonPressed(View v) {
		ToggleButton toggleButton;

		dialogsHelper = DialogHelper.getInstance(this);

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
			dialogsHelper.showDialog(DialogHelper.BACKUP_DIALOG);
			break;

		case R.id.restore:
			dialogsHelper.showDialog(DialogHelper.RESTORE_DIALOG);
			break;

		case R.id.b_view:
			startActivity(new Intent(this, Explorer.class));
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
			dialogsHelper.showDialog(DialogHelper.WIPE_DIALOG);
			break;

		case R.id.wipe_cache:
			Wipe.getInstance(this).cache();
			break;

		case R.id.b_feedback:
			dialogsHelper.showDialog(DialogHelper.FEEDBACK_DIALOG);
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
			dialogsHelper.showDialog(DialogHelper.STEALTH_DIALOG, toggleButton);
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
			dialogsHelper.showDialog(DialogHelper.PROTECT_DIALOG, toggleButton);
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
			dialogsHelper.showDialog(DialogHelper.BLIP_DIALOG, toggleButton);
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
		//Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		//Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//checkPlayServices();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//Session session = Session.getActiveSession();
		//Session.saveSession(session, outState);
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
		//Session.getActiveSession().onActivityResult(this, requestCode,
		//		resultCode, data);
		//updateFacebookConnectPreferences();
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
	
	public static class ViewHolder {
		TextView header;
		TextView innerText;
		ImageView innerImage;
	}
}