package in.ceeq.fragments;

import hirondelle.date4j.DateTime;
import in.ceeq.R;
import in.ceeq.actions.Backup;
import in.ceeq.actions.Notifications;
import in.ceeq.activities.Home;
import in.ceeq.helpers.PhoneHelper;
import in.ceeq.helpers.PhoneHelper.Phone;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.receivers.DeviceAdmin;

import java.util.ArrayList;
import java.util.TimeZone;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.plus.PlusOneButton;

public class HomeFragment extends Fragment {

	public static final int STATUS_ALL = 0;
	public static final int STATUS_AUTO_BACKUP_DISABLED = 1;
	public static final int STATUS_AUTO_TRACK_DISABLED = 2;
	public static final int STATUS_GPS_DISABLED = 3;
	public static final int STATUS_DEVICE_ADMIN_DISABLED = 4;
	public static final int STATUS_BACKUP_DISABLED = 5;
	public static final int STATUS_SYNC = 6;

	private static final int PLUS_ONE_REQUEST_CODE = 9025;
	
	
	private PhoneHelper phoneHelper;
	private PreferencesHelper preferencesHelper;
	private View view;
	private int counter;
	private ExpandableListView notificationListView;
	private ExpandableListAdapter notificationListAdapter;
	private TextView statusText;
	private LinearLayout statusBox;
	private ArrayList<Integer> notificationList;
	private ToggleButton toggleButton;
	private PlusOneButton plusOneButton;
	

	public HomeFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_main, container, false);
		setupHelpers();

		notificationListView = (ExpandableListView) view
				.findViewById(R.id.notifications);
		notificationList = new ArrayList<Integer>();
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
				notificationList);
		notificationListView.setAdapter(notificationListAdapter);

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
				.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS)) {
			notificationList.add(STATUS_AUTO_BACKUP_DISABLED);
			counter++;
			return false;
		} else if (preferencesHelper
				.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS)
				& !isBackupDelayed()) {
			notificationList.add(STATUS_BACKUP_DISABLED);
			counter++;
			return false;
		} else if (!preferencesHelper
				.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS)
				& !isBackupDelayed()) {
			notificationList.add(STATUS_AUTO_BACKUP_DISABLED);
			notificationList.add(STATUS_BACKUP_DISABLED);
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
			notificationList.add(STATUS_DEVICE_ADMIN_DISABLED);
			counter++;
			return false;
		} else if (!gpsEnabled & deviceAdminEnabled) {
			notificationList.add(STATUS_GPS_DISABLED);
			counter++;
			return false;
		} else if (!gpsEnabled & !deviceAdminEnabled) {
			notificationList.add(STATUS_GPS_DISABLED);
			notificationList.add(STATUS_DEVICE_ADMIN_DISABLED);
			counter += 2;
			return false;
		}
		return false;
	}

	public class ListAdapter extends BaseExpandableListAdapter {

		protected static final int DEVICE_ADMIN_ACTIVATION_REQUEST = 0;
		public ArrayList<Integer> notifications = new ArrayList<Integer>();
		public int notificationCounter;
		public LayoutInflater inflater;
		public Context context;
		public boolean status;
		private ComponentName deviceAdminComponentName;

		public ListAdapter(Context context, int counter, boolean status,
				ArrayList<Integer> notifications) {
			this.context = context;
			this.notifications = notifications;
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

		public String getNotificationMessage(int status) {
			switch (status) {
			case STATUS_AUTO_BACKUP_DISABLED:
				return getString(R.string.status_note_1);
			case STATUS_AUTO_TRACK_DISABLED:
				return getString(R.string.status_note_2);
			case STATUS_GPS_DISABLED:
				return getString(R.string.status_note_3);
			case STATUS_DEVICE_ADMIN_DISABLED:
				return getString(R.string.status_note_4);
			case STATUS_BACKUP_DISABLED:
				return getString(R.string.status_note_5);
			default:
				return getString(R.string.status_note_0);
			}
		}

		public String getButtonLabel(int status) {
			switch (status) {
			case STATUS_AUTO_BACKUP_DISABLED:
				return getString(R.string.enable);
			case STATUS_BACKUP_DISABLED:
				return getString(R.string.backupButton);
			default:
				return getString(R.string.enable);
			}
		}

		public void onButtonPress(int status) {
			switch (status) {
			case STATUS_AUTO_BACKUP_DISABLED:
				Backup.getInstance(getActivity()).autoBackups(Backup.ON);
				break;
			case STATUS_AUTO_TRACK_DISABLED:
				preferencesHelper.setBoolean(
						PreferencesHelper.AUTO_TRACK_STATUS, true);
				break;
			case STATUS_GPS_DISABLED:

				startActivity(new Intent(
						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				break;
			case STATUS_DEVICE_ADMIN_DISABLED:
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
			case STATUS_SYNC:
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

