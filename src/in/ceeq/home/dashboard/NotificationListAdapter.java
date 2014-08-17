package in.ceeq.home.dashboard;

import in.ceeq.R;
import in.ceeq.commons.Utils;
import in.ceeq.receivers.DeviceAdministrationReceiver;

import java.util.ArrayList;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

public class NotificationListAdapter extends BaseExpandableListAdapter {

	public ArrayList<Integer> notifications = new ArrayList<Integer>();
	public int notificationCounter;
	public LayoutInflater inflater;
	public Context context;
	public boolean status;
	private ComponentName deviceAdminComponentName;

	public NotificationListAdapter(Context context, int counter, boolean status, ArrayList<Integer> notifications) {
		this.context = context;
		this.notifications = notifications;
		this.notificationCounter = counter;
		this.status = status;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.deviceAdminComponentName = new ComponentName(context, DeviceAdministrationReceiver.class);
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
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		TextView text;
		Button button;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_status_inner, null);
		}

		text = (TextView) convertView.findViewById(R.id.n_text);
		if (notificationCounter == 0) {
			text.setText("You are protected");
		} else {
			text.setText(getNotificationMessage((notifications.get(childPosition))));
			text.setTag(R.string.intent_type, (notifications.get(childPosition)));
			button = (Button) convertView.findViewById(R.id.activate);
			button.setText(getButtonLabel((notifications.get(childPosition))));
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
		case Constants.STATUS_AUTO_BACKUP_DISABLED:
			return context.getString(R.string.status_note_1);
		case Constants.STATUS_AUTO_TRACK_DISABLED:
			return context.getString(R.string.status_note_2);
		case Constants.STATUS_GPS_DISABLED:
			return context.getString(R.string.status_note_3);
		case Constants.STATUS_DEVICE_ADMIN_DISABLED:
			return context.getString(R.string.status_note_4);
		case Constants.STATUS_BACKUP_DISABLED:
			return context.getString(R.string.status_note_5);
		default:
			return context.getString(R.string.status_note_0);
		}
	}

	public String getButtonLabel(int status) {
		switch (status) {
		case Constants.STATUS_AUTO_BACKUP_DISABLED:
			return context.getString(R.string.enable);
		case Constants.STATUS_BACKUP_DISABLED:
			return context.getString(R.string.backupButton);
		default:
			return context.getString(R.string.enable);
		}
	}

	public void onButtonPress(int status) {
		switch (status) {
		case Constants.STATUS_AUTO_BACKUP_DISABLED:
			Utils.scheduledBackup(context, true);
			break;
		case Constants.STATUS_AUTO_TRACK_DISABLED:
			Utils.setBooleanPrefs(context, Utils.AUTO_TRACK_STATUS, true);
			break;
		case Constants.STATUS_GPS_DISABLED:
			context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			break;
		case Constants.STATUS_DEVICE_ADMIN_DISABLED:
			((Activity)context).startActivityForResult(
					new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
							.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName)
							.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
									"Activating Device Administrator enables all the security features of the application."),
									Constants.DEVICE_ADMIN_ACTIVATION_REQUEST);
			break;
		case Constants.STATUS_SYNC:
			break;
		default:
			break;
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
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_status_outer, null);
		}

		TextView totalCounter = (TextView) convertView.findViewById(R.id.n_count);
		totalCounter.setText(notificationCounter + "");
		if (!status)
			totalCounter.setBackgroundResource(R.color.red);
		TextView header = (TextView) convertView.findViewById(R.id.n_header);
		header.setText(context.getString(R.string.notifications));
		if (!status)
			header.setTextColor(context.getResources().getColor(R.color.red));
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

}