package in.ceeq.commons;

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
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.receivers.DeviceAdmin;
import in.ceeq.services.Uploader.UploadType;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DialogHelper implements DialogInterface.OnClickListener,
		DialogInterface.OnKeyListener {

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
	private static final int NONE = -1;
	private static final int DEVICE_ADMIN_ACTIVATION_REQUEST = 9014;

	private ToggleButton toggleButton;
	private LayoutInflater inflater;
	private AlertDialog.Builder alertDialogBuilder;
	private PreferencesHelper preferencesHelper;
	private View feedbackView, protectMeView;
	private Activity activity;
	private Context context;
	private ComponentName deviceAdminComponentName;

	private int dialogType;

	public static DialogHelper getInstance(Context context) {
		return new DialogHelper(context);
	}

	public DialogHelper(Context context) {
		this.context = context;
		this.activity = (Activity) context;
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
			alertDialogBuilder.setView(getView(WIPE_EXTERNAL_STORAGE_DIALOG))
					.setPositiveButton(getPositiveButtonString(), this);
			break;
		case WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG:
			alertDialogBuilder.setView(
					getView(WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG))
					.setPositiveButton(getPositiveButtonString(), this);
			break;
		default:
			alertDialogBuilder.setSingleChoiceItems(getChoices(), NONE, this);
			break;
		}
		alertDialogBuilder.setNegativeButton(getNegativeButtonString(), this)
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

		alertDialogBuilder.setPositiveButton(getPositiveButtonString(), this)
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
			protectMeView = inflater.inflate(R.layout.dialog_protect_me, null);
			// Button facebookConnect = (Button) protectMeView
			// .findViewById(R.id.facebook_login);
			LinearLayout socialBox = (LinearLayout) protectMeView
					.findViewById(R.id.social_box);
			if (preferencesHelper
					.getBoolean(PreferencesHelper.FACEBOOK_CONNECT_STATUS)) {
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
			return inflater
					.inflate(R.layout.dialog_wipe_external_storage, null);
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
		deviceAdminComponentName = new ComponentName(context, DeviceAdmin.class);

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
				Receiver.getInstance(context)
						.register(ReceiverType.LOW_BATTERY);
				preferencesHelper.setBoolean(
						PreferencesHelper.AUTO_BLIP_STATUS, true);
				showToast("Auto blips enabled.");
				break;
			case DEVICE_ADMIN_DIALOG:
				activity.startActivityForResult(
						new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
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
				preferencesHelper.setString(PreferencesHelper.FEEDBACK_MESSAGE,
						feedbackMessage.getText().toString());
				Upload.getInstance(context).start(UploadType.FEEDBACK);
				break;
			case PROTECT_DIALOG:
				Protect.getInstance(context).enable();
				EditText distressMessage = (EditText) protectMeView
						.findViewById(R.id.distressMessage);
				preferencesHelper.setString(PreferencesHelper.DISTRESS_MESSAGE,
						distressMessage.getText().toString());
				preferencesHelper.setBoolean(
						PreferencesHelper.PROTECT_ME_STATUS, true);
				showToast("Protect me enabled. Just press power button 10 times for help.");
				break;
			case STEALTH_DIALOG:
				Notifications.getInstance(context).remove();
				preferencesHelper.setBoolean(
						PreferencesHelper.NOTIFICATIONS_STATUS, false);
				try {
					context.getPackageManager().setComponentEnabledSetting(
							new ComponentName(context, Launcher.class),
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
							PackageManager.DONT_KILL_APP);
					try {
						context.startActivity(new Intent(context, Launcher.class));
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