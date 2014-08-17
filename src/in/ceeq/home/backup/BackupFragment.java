package in.ceeq.home.backup;

import hirondelle.date4j.DateTime;
import in.ceeq.R;
import in.ceeq.commons.Utils;
import in.ceeq.explorer.ExplorerActivity;
import in.ceeq.services.BackupService;

import java.util.TimeZone;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class BackupFragment extends Fragment implements DialogInterface.OnClickListener, View.OnClickListener,
		DialogInterface.OnKeyListener, OnItemSelectedListener {

	public static final String INTENT_ACTION_STATUS = "in.ceeq.action.STATUS";
	public static final long ONE_SECOND = 1000;
	public static final int DIALOG_TYPE_BACKUP = 1;
	public static final int DIALOG_TYPE_RESTORE = 2;
	public static final int DIALOG_TYPE_APPLICATION_BACKUP = 3;

	private static final String AUTO_BACKUP_TIME = "02:00:00";
	private TextView hours, mins, unit, div, lastBackupDate;
	private CountDownTimer timerClock;
	private View view;
	private LinearLayout timer;
	private ProgressBar progressBar;
	private LocalBroadcastManager localBroadcastManager;
	private BroadcastReceiver backupMessageReceiver;
	private Button oneTouchBackup;
	private Button backup;
	private Button restore;
	private Button openExplorer;
	private Button applicationBackup;
	private ToggleButton autoBackup;
	private AlertDialog.Builder alertDialogBuilder;
	private Context context;
	private int dialogType;

	public static BackupFragment getInstance() {
		return new BackupFragment();
	}

	public BackupFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
		alertDialogBuilder = new AlertDialog.Builder(getActivity());
		context = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_backup, container, false);
		setUpUi();
		setUpListeners();
		setUpActionBar();
		setUpTimerClock();

		timerClock = new CountDown(Integer.parseInt(mins.getText().toString()) * 60 * 1000, ONE_SECOND);
		timerClock.start();

		if (!Utils.getBooleanPrefs(context, Utils.AUTO_BACKUP_STATUS)) {
			timer.setVisibility(View.GONE);
		} else {
			timer.setVisibility(View.VISIBLE);
		}

		if (Utils.getStringPrefs(context, Utils.LAST_BACKUP_DATE).isEmpty()) {
			lastBackupDate.setText(getString(R.string.status_note_6));
		} else {
			lastBackupDate.setText("Last backup was " + getLastBackupLabel());
		}

		return view;
	}

	private void setUpUi() {
		oneTouchBackup = (Button) view.findViewById(R.id.oneTouchBackup);
		backup = (Button) view.findViewById(R.id.backup);
		restore = (Button) view.findViewById(R.id.restore);
		applicationBackup = (Button) view.findViewById(R.id.applicationBackup);
		openExplorer = (Button) view.findViewById(R.id.startExplorer);
		autoBackup = (ToggleButton) view.findViewById(R.id.toggle_backup);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		timer = (LinearLayout) view.findViewById(R.id.timerLayout);
		hours = (TextView) view.findViewById(R.id.hours);
		mins = (TextView) view.findViewById(R.id.mins);
		unit = (TextView) view.findViewById(R.id.unit);
		div = (TextView) view.findViewById(R.id.div);
		lastBackupDate = (TextView) view.findViewById(R.id.lastBackupDate);
	}

	private void setUpActionBar() {
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.blue));
		actionBar.setDisplayShowHomeEnabled(false);

		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Backups");
	}

	private void setUpListeners() {
		oneTouchBackup.setOnClickListener(this);
		backup.setOnClickListener(this);
		restore.setOnClickListener(this);
		applicationBackup.setOnClickListener(this);
		openExplorer.setOnClickListener(this);
		autoBackup.setOnClickListener(this);
	}

	public void setUpTimerClock() {
		div.setText(":");
		int numberOfHours = (int) DateTime.now(TimeZone.getDefault()).numSecondsFrom(new DateTime(AUTO_BACKUP_TIME)) / 3600;
		int numberOfMinutes = (int) ((DateTime.now(TimeZone.getDefault())
				.numSecondsFrom(new DateTime(AUTO_BACKUP_TIME)) / 60) % 60);
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

	/**
	 * 
	 * Return last backup label string.
	 * 
	 * @return String Last backup label
	 */
	public String getLastBackupLabel() {

		int numberOfElapsedDays = new DateTime(Utils.getStringPrefs(context, Utils.LAST_BACKUP_DATE))
				.numDaysFrom(DateTime.today(TimeZone.getDefault()));
		switch (numberOfElapsedDays) {
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
		autoBackup.setChecked(Utils.getBooleanPrefs(context, Utils.AUTO_BACKUP_STATUS));
	}

	public class CountDown extends CountDownTimer {

		public CountDown(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mins.setText(String.format("%02d", millisUntilFinished / (1000 * 60)));
		}

		@Override
		public void onFinish() {
			if (mins.getText().toString().equals("00")) {
				hours.setText(String.format("%02d", (Integer.parseInt(hours.getText().toString()) - 1)));
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
		intentFilter.addAction(INTENT_ACTION_STATUS);
		localBroadcastManager.registerReceiver(backupMessageReceiver, intentFilter);
	}

	public class BackupMessageReceiver extends BroadcastReceiver {

		private static final String ACTION_STATUS = "action_status";

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();
			boolean status = data.getBoolean(ACTION_STATUS);

			if (status) {
				progressBar.setVisibility(View.VISIBLE);
			} else {
				progressBar.setVisibility(View.INVISIBLE);
			}
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		localBroadcastManager.unregisterReceiver(backupMessageReceiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.toggle_backup:
			Utils.scheduledBackup(getActivity(), autoBackup.isChecked());
			break;

		case R.id.oneTouchBackup:
			backup();
			break;

		case R.id.backup:
			dialogType = DIALOG_TYPE_BACKUP;
			showBackupDialog();
			break;

		case R.id.restore:
			dialogType = DIALOG_TYPE_RESTORE;
			showRestoreDialog();
			break;

		case R.id.applicationBackup:
			dialogType = DIALOG_TYPE_APPLICATION_BACKUP;
			break;

		case R.id.startExplorer:
			getActivity().startActivity(new Intent(getActivity(), ExplorerActivity.class));
			break;
		}
	}

	private void showBackupDialog() {
		alertDialogBuilder.setTitle(R.string.dialog_title_backup)
				.setSingleChoiceItems(R.array.backup_options, -1, this).setNegativeButton(R.string.cancel, this)
				.create().show();
	}

	private void showRestoreDialog() {
		alertDialogBuilder.setTitle(R.string.dialog_title_restore)
				.setSingleChoiceItems(R.array.backup_options, -1, this).setNegativeButton(R.string.cancel, this)
				.create().show();
	}

	private void backup() {
		Intent startBackup = new Intent(getActivity(), BackupService.class);
		startBackup.setAction(BackupService.ACTION_BACKUP);
		startBackup.putExtra(BackupService.ACTION_DATA, BackupService.ACTION_DATA_ALL);
		getActivity().startService(startBackup);
	}

	private void backup(int data) {
		Intent startBackup = new Intent(getActivity(), BackupService.class);
		startBackup.setAction(BackupService.ACTION_BACKUP);
		startBackup.putExtra(BackupService.ACTION_DATA, data);
		getActivity().startService(startBackup);
	}

	public void restore(int data) {
		Intent startRestore = new Intent(getActivity(), BackupService.class);
		startRestore.setAction(BackupService.ACTION_RESTORE);
		startRestore.putExtra(BackupService.ACTION_DATA, data);
		getActivity().startService(startRestore);
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_NEGATIVE:
			dialog.dismiss();
			break;
		case DialogInterface.BUTTON_POSITIVE:
		default:
			switch (dialogType) {
			case DIALOG_TYPE_BACKUP:
				backup(which);
				break;
			case DIALOG_TYPE_RESTORE:
				restore(which);
				break;
			case DIALOG_TYPE_APPLICATION_BACKUP:
				break;
			}
			dialog.dismiss();
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Utils.d("Here");
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
}