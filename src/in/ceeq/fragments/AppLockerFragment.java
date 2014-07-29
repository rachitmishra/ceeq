package in.ceeq.fragments;

import hirondelle.date4j.DateTime;
import in.ceeq.R;
import in.ceeq.helpers.PreferencesHelper;

import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AppLockerFragment extends Fragment {


	public static final String INTENT_ACTION_MESSAGE = "in.ceeq.action.MESSAGE";
	
	private static final String BACKUP_TIME = "02:00:00";
	private PreferencesHelper preferencesHelper;
	private ToggleButton toggle;
	private TextView hours, mins, unit, div, lastBackupDate;
	private CountDownTimer timerClock;
	private View view;
	private LinearLayout timer;
	private ProgressBar progressBar;
	private LocalBroadcastManager localBroadcastManager;
	private BroadcastReceiver backupMessageReceiver;

	public AppLockerFragment() {

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

		timerClock = new CountDown(
				Integer.parseInt(mins.getText().toString()) * 60 * 1000,
				1 * 1000);
		timerClock.start();

		if (!preferencesHelper.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS)) {
			timer.setVisibility(View.GONE);
		} else {
			timer.setVisibility(View.VISIBLE);
		}

		lastBackupDate.setText(getLastBackupLabel());
		localBroadcastManager = LocalBroadcastManager
				.getInstance(getActivity());
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
				preferencesHelper.getString(PreferencesHelper.LAST_BACKUP_DATE))
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

	@Override
	public void onResume() {
		super.onResume();
		setupLocalBroadcastReceiver();
	}

	private void setupLocalBroadcastReceiver() {
		backupMessageReceiver = new BackupMessageReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(INTENT_ACTION_MESSAGE);
		localBroadcastManager.registerReceiver(backupMessageReceiver,
				intentFilter);
	}

	public class BackupMessageReceiver extends BroadcastReceiver {

		private static final String ACTION = "action";
		private static final int BACKUP_START = 1;
		private static final int BACKUP_FINISH = 2;

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();
			int action = data.getInt(ACTION);

			switch (action) {
			case BACKUP_START:
				progressBar.setVisibility(View.VISIBLE);
				break;
			case BACKUP_FINISH:
				progressBar.setVisibility(View.INVISIBLE);
				break;
			}
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		localBroadcastManager.unregisterReceiver(backupMessageReceiver);
	}

}