package in.ceeq.home.dashboard;

import hirondelle.date4j.DateTime;
import in.ceeq.LauncherActivity;
import in.ceeq.R;
import in.ceeq.commons.Utils;
import in.ceeq.home.backup.BackupFragment;
import in.ceeq.home.security.SecurityFragment;
import in.ceeq.receivers.OutgoingCallsReceiver;
import in.ceeq.receivers.PowerButtonReceiver;
import in.ceeq.services.ProtectorService;
import in.ceeq.services.ProtectorService.ProtectorType;

import java.util.ArrayList;
import java.util.TimeZone;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.plus.PlusOneButton;

public class DashboardFragment extends Fragment implements DrawerListener, View.OnClickListener,
		DialogInterface.OnClickListener, DialogInterface.OnKeyListener {

	private static final int PLUS_ONE_REQUEST_CODE = 9025;
	public static final int DIALOG_TYPE_FEEDBACK = 1;
	public static final int DIALOG_TYPE_PROTECT = 2;
	public static final int DIALOG_TYPE_STEALTH = 3;
	public static final int DIALOG_TYPE_LOCKER = 4;
	public static final int DIALOG_TYPE_PROXIMITY = 5;

	private View view;
	private int counter;
	private ExpandableListView notificationAdapter;
	private ExpandableListAdapter notificationListAdapter;
	private TextView statusText;
	private LinearLayout statusBox;
	private ArrayList<Integer> notificationList;
	private ToggleButton protectMe;
	private ToggleButton stealthMode;
	private ToggleButton applicationLocker;
	private ToggleButton proximityAlarm;
	private Button sendFeedback;
	private PlusOneButton plusOneButton;
	private Button backupButton, securityButton;
	private FragmentManager fragmentManager;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private Context context;
	private AlertDialog.Builder alertDialogBuilder;
	private LayoutInflater layoutInflater;
	private int dialogType;

	public static DashboardFragment getInstance() {
		return new DashboardFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		alertDialogBuilder = new AlertDialog.Builder(context);
		layoutInflater = getActivity().getLayoutInflater();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_dashboard, container, false);
		setUpUi(view);
		setupListeners();
		restoreToggleStates(view);
		setHasOptionsMenu(true);
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		return view;
	}

	private void setUpUi(View view) {
		// notificationAdapter = (ExpandableListView) view.findViewById(R.id.notifications);
		notificationList = new ArrayList<Integer>();
		statusText = (TextView) view.findViewById(R.id.statusText);
		statusBox = (LinearLayout) view.findViewById(R.id.statusBox);
		backupButton = (Button) view.findViewById(R.id.backupButton);
		securityButton = (Button) view.findViewById(R.id.securityButton);
		sendFeedback = (Button) view.findViewById(R.id.feedbackButton);
		protectMe = (ToggleButton) view.findViewById(R.id.toggle_protect);
		stealthMode = (ToggleButton) view.findViewById(R.id.toggle_stealth);
		applicationLocker = (ToggleButton) view.findViewById(R.id.toggle_locker);
		proximityAlarm = (ToggleButton) view.findViewById(R.id.toggle_proximity);

		if (setStatus() > 0) {
			statusText.setText(getString(R.string.app_status_bad));
			statusBox.setBackgroundResource(R.color.red);
			getActivity().getActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.red));
			showNotification();
		} else {
			statusText.setText(getString(R.string.app_status_good));
			statusBox.setBackgroundResource(R.color.green);
			getActivity().getActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.green));
			showNotification();
		}
		notificationListAdapter = new NotificationListAdapter(context, counter, Utils.getBooleanPrefs(context,
				Utils.APP_STATUS), notificationList);
		// notificationAdapter.setAdapter(notificationListAdapter);

		plusOneButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
		plusOneButton.initialize("http://plus.google.com/116561373543243917689", PLUS_ONE_REQUEST_CODE);
	}

	public void restoreToggleStates(View v) {
		protectMe.setChecked(Utils.getBooleanPrefs(context, Utils.PROTECT_ME_STATUS));
		stealthMode.setChecked(Utils.getBooleanPrefs(context, Utils.STEALTH_MODE_STATUS));
		stealthMode = (ToggleButton) v.findViewById(R.id.toggle_stealth);
		stealthMode.setChecked(Utils.getBooleanPrefs(context, Utils.STEALTH_MODE_STATUS));
		applicationLocker = (ToggleButton) v.findViewById(R.id.toggle_locker);
		applicationLocker.setChecked(Utils.getBooleanPrefs(context, Utils.STEALTH_MODE_STATUS));
		proximityAlarm = (ToggleButton) v.findViewById(R.id.toggle_proximity);
		proximityAlarm.setChecked(Utils.getBooleanPrefs(context, Utils.STEALTH_MODE_STATUS));
	}

	public void setupListeners() {
		fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

		backupButton.setOnClickListener(this);
		securityButton.setOnClickListener(this);
		sendFeedback.setOnClickListener(this);
		stealthMode.setOnClickListener(this);
		protectMe.setOnClickListener(this);
		applicationLocker.setOnClickListener(this);
		proximityAlarm.setOnClickListener(this);
	}

	public void showNotification() {
		if (Utils.getBooleanPrefs(context, Utils.NOTIFICATIONS_STATUS))
			Utils.notification(Utils.NOTIFICATION_NOTIFY, context, 0);
	}

	public int setStatus() {
		counter = 0;
		boolean backupStatus = setBackupStatus();
		boolean securityStatus = setSecurityStatus();
		if (backupStatus & securityStatus) {
			Utils.setBooleanPrefs(context, Utils.APP_STATUS, true);
		} else {
			Utils.setBooleanPrefs(context, Utils.APP_STATUS, false);
		}
		return counter;
	}

	public boolean setBackupStatus() {
		if (Utils.getBooleanPrefs(context, Utils.AUTO_BACKUP_STATUS) & isBackupDelayed()) {
			return true;
		} else if (!Utils.getBooleanPrefs(context, Utils.AUTO_BACKUP_STATUS)) {
			notificationList.add(Constants.STATUS_AUTO_BACKUP_DISABLED);
			counter++;
			return false;
		} else if (Utils.getBooleanPrefs(context, Utils.AUTO_BACKUP_STATUS) & !isBackupDelayed()) {
			notificationList.add(Constants.STATUS_BACKUP_DISABLED);
			counter++;
			return false;
		} else if (!Utils.getBooleanPrefs(context, Utils.AUTO_BACKUP_STATUS) & !isBackupDelayed()) {
			notificationList.add(Constants.STATUS_AUTO_BACKUP_DISABLED);
			notificationList.add(Constants.STATUS_BACKUP_DISABLED);
			counter += 2;
			return false;
		}
		return false;
	}

	public boolean isBackupDelayed() {
		if (Utils.getStringPrefs(context, Utils.LAST_BACKUP_DATE).isEmpty())
			return true;
		if (DateTime.now(TimeZone.getDefault()).numDaysFrom(
				new DateTime(Utils.getStringPrefs(context, Utils.LAST_BACKUP_DATE))) > -3)
			return true;

		return false;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return true;
	}

	public boolean setSecurityStatus() {
		boolean deviceAdminEnabled = Utils.getBooleanPrefs(context, Utils.DEVICE_ADMIN_STATUS);
		boolean gpsEnabled = Utils.enabled(Utils.GPS, context);
		if (gpsEnabled & deviceAdminEnabled) {
			return true;
		} else if (gpsEnabled & !deviceAdminEnabled) {
			notificationList.add(Constants.STATUS_DEVICE_ADMIN_DISABLED);
			counter++;
			return false;
		} else if (!gpsEnabled & deviceAdminEnabled) {
			notificationList.add(Constants.STATUS_GPS_DISABLED);
			counter++;
			return false;
		} else if (!gpsEnabled & !deviceAdminEnabled) {
			notificationList.add(Constants.STATUS_GPS_DISABLED);
			notificationList.add(Constants.STATUS_DEVICE_ADMIN_DISABLED);
			counter += 2;
			return false;
		}
		return false;
	}

	@Override
	public void onDrawerClosed(View arg0) {

	}

	@Override
	public void onDrawerOpened(View arg0) {
	}

	@Override
	public void onDrawerSlide(View drawerView, float slideOffset) {
		drawerLayout.bringChildToFront(drawerView);
		drawerLayout.requestLayout();
	}

	@Override
	public void onDrawerStateChanged(int arg0) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backupButton:
			fragmentManager.beginTransaction().replace(R.id.container, BackupFragment.getInstance()).commit();
			break;
		case R.id.securityButton:
			fragmentManager.beginTransaction().replace(R.id.container, SecurityFragment.getInstance()).commit();
			break;
		case R.id.feedbackButton:
			showFeedbackDialog();
			break;
		case R.id.toggle_protect:
			dialogType = DIALOG_TYPE_PROTECT;
			if (((ToggleButton) v).isChecked()) {
				showProtectMeDialog();
			} else {
				setUpProtectMe(false);
			}
			break;
		case R.id.toggle_stealth:
			dialogType = DIALOG_TYPE_STEALTH;
			if (((ToggleButton) v).isChecked()) {
				showStealthModeDialog();
			} else {
				setUpStealthMode(false);
			}

			break;
		case R.id.toggle_locker:
			dialogType = DIALOG_TYPE_LOCKER;
			break;
		case R.id.toggle_proximity:
			dialogType = DIALOG_TYPE_PROXIMITY;
			break;
		}
	}

	private void showFeedbackDialog() {
		alertDialogBuilder.setTitle(R.string.dialog_title_feedback).setPositiveButton(getString(R.string.send), this)
				.setNegativeButton(getString(R.string.cancel), this).setOnKeyListener(this)
				.setView(layoutInflater.inflate(R.layout.dialog_feedback, null)).create().show();

	}

	private void showProtectMeDialog() {
		alertDialogBuilder.setTitle(R.string.dialog_title_protect).setPositiveButton(getString(R.string.save), this)
				.setNegativeButton(getString(R.string.cancel), this).setOnKeyListener(this);
		View protectMeView = layoutInflater.inflate(R.layout.dialog_protect_me, null);
		// Button facebookConnect = (Button) protectMeView
		// .findViewById(R.id.facebook_login);
		LinearLayout socialBox = (LinearLayout) protectMeView.findViewById(R.id.social_box);
		if (Utils.getBooleanPrefs(context, Utils.FACEBOOK_CONNECT_STATUS)) {
			socialBox.setVisibility(View.GONE);
		} else {
			socialBox.setVisibility(View.VISIBLE);
		}
		// facebookConnect.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Home.this.connectFacebook();
		// }
		// });
		EditText distressMessage = (EditText) protectMeView.findViewById(R.id.distressMessage);
		String storedMessage = Utils.getStringPrefs(context, Utils.DISTRESS_MESSAGE);
		if (!storedMessage.isEmpty())
			distressMessage.setText(storedMessage);
		alertDialogBuilder.setView(protectMeView).create().show();
	}

	private void showStealthModeDialog() {
		alertDialogBuilder.setTitle(R.string.dialog_title_stealth).setPositiveButton(getString(R.string.enable), this)
				.setNegativeButton(getString(R.string.cancel), this).setOnKeyListener(this)
				.setView(layoutInflater.inflate(R.layout.dialog_stealth_mode, null)).create().show();
	}

	private void setUpProtectMe(boolean status) {
		Utils.setBooleanPrefs(context, Utils.PROTECT_ME_STATUS, status);
		if (status) {
			setupPowerButtonReceiver(true);
			Toast.makeText(context, "Protect me enabled. Just press power button 10 times for help.",
					Toast.LENGTH_SHORT).show();
		} else {
			setupPowerButtonReceiver(false);
			Toast.makeText(context, "Protect me disabled.", Toast.LENGTH_SHORT).show();
		}
	}

	private void setUpStealthMode(boolean status) {
		Utils.setBooleanPrefs(context, Utils.STEALTH_MODE_STATUS, status);
		Utils.setBooleanPrefs(context, Utils.NOTIFICATIONS_STATUS, status);
		if (status) {
			Toast.makeText(context, "Stealth Mode enabled.", Toast.LENGTH_SHORT).show();
			context.getPackageManager().setComponentEnabledSetting(
					new ComponentName("in.ceeq", "in.ceeq.LauncherActivity"),
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			Utils.removeAllNotifications(context);
			setupOutgoingCallsReceiver(true);
			try {
				startActivity(new Intent(context, LauncherActivity.class));
			} catch (Exception e) {
				// Let it be 3:)
			}
		} else {
			context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, LauncherActivity.class),
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			context.getPackageManager().setComponentEnabledSetting(
					new ComponentName("in.ceeq", "in.ceeq.LauncherActivity"),
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			Utils.showNotifications(context);
			setupOutgoingCallsReceiver(false);
			Toast.makeText(context, "Stealth Mode disabled.", Toast.LENGTH_SHORT).show();
		}
	}

	private void setupOutgoingCallsReceiver(boolean status) {
		ComponentName componentName = new ComponentName(getActivity(), OutgoingCallsReceiver.class);
		if (status) {
			try {
				context.getPackageManager().setComponentEnabledSetting(componentName,
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (componentName != null)
					context.getPackageManager().setComponentEnabledSetting(componentName,
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void startProtectorService() {
		Intent startProtector = new Intent(context, ProtectorService.class);
		startProtector.putExtra(ProtectorService.ACTION, ProtectorType.START);
		context.startService(startProtector);
	}

	public void stopProtectorService() {
		if (Utils.getBooleanPrefs(context, Utils.PROTECT_ME_STATUS)) {
			Intent stopProtector = new Intent(context, ProtectorService.class);
			context.stopService(stopProtector);
		}
	}

	private void setupPowerButtonReceiver(boolean status) {
		BroadcastReceiver power = new PowerButtonReceiver();
		if (status) {
			try {
				IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
				filter.addAction(Intent.ACTION_SCREEN_OFF);
				context.registerReceiver(power, filter);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				context.unregisterReceiver(power);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
		case DIALOG_TYPE_PROTECT:
			setUpProtectMe(true);
			break;
		case DIALOG_TYPE_STEALTH:
			setUpStealthMode(true);
			break;
		case DIALOG_TYPE_FEEDBACK:
			sendFeedbackMessage();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			dialog.dismiss();
			switch (dialogType) {
			case DIALOG_TYPE_PROTECT:
				protectMe.setChecked(false);
				break;
			case DIALOG_TYPE_STEALTH:
				stealthMode.setChecked(false);
				break;
			}
			break;
		}
	}

	private void sendFeedbackMessage() {

	}

	// public void updateFacebookConnectPreferences() {
	//
	// Session session = Session.getActiveSession();
	// if (session.isOpened()) {
	// preferencesHelper.setBoolean(
	// PreferencesHelper.FACEBOOK_CONNECT_STATUS, true);
	// } else {
	// preferencesHelper.setBoolean(
	// PreferencesHelper.FACEBOOK_CONNECT_STATUS, false);
	// }

	// @Override
	// public void onStart() {
	// super.onStart();
	// Session.getActiveSession().addCallback(statusCallback);
	// }

	// @Override
	// public void onStop() {
	// super.onStop();
	// Session.getActiveSession().removeCallback(statusCallback);
	// }

	// @Override
	// protected void onResume() {
	// super.onResume();
	// checkPlayServices();
	// }

	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	// super.onSaveInstanceState(outState);
	// Session session = Session.getActiveSession();
	// Session.saveSession(session, outState);
	// }

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// super.onActivityResult(requestCode, resultCode, data);
	// // Session.getActiveSession().onActivityResult(this, requestCode,
	// resultCode, data);
	// updateFacebookConnectPreferences();
	// }

	// public void setupFacebookConnect(Bundle savedInstanceState) {
	// Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
	//
	// Session session = Session.getActiveSession();
	// if (session == null) {
	// if (savedInstanceState != null) {
	// session = Session.restoreSession(this, null, statusCallback,
	// savedInstanceState);
	// }
	// if (session == null) {
	// session = new Session(this);
	// }
	// Session.setActiveSession(session);
	// if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
	// session.openForRead(new Session.OpenRequest(this)
	// .setCallback(statusCallback));
	// }
	// }

	// private Session.StatusCallback statusCallback = new FBSessionStatus();
	// }
	//
	// public void connectFacebook() {
	// Session session = Session.getActiveSession();
	// if (!session.isOpened() && !session.isClosed()) {
	// session.openForRead(new Session.OpenRequest(this)
	// .setCallback(statusCallback));
	// } else {
	// Session.openActiveSession(this, true, statusCallback);
	// }
	// }
}
