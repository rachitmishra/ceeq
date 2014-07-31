/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.activities;

import in.ceeq.R;
import in.ceeq.actions.Choose;
import in.ceeq.actions.Notifications;
import in.ceeq.actions.Reset;
import in.ceeq.helpers.PreferencesHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;

public class Settings extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new Preferences()).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

class Preferences extends PreferenceFragment implements ConnectionCallbacks,
		OnConnectionFailedListener {

	private static final int CONNECTION_FAILURE_REQUEST = 9020;
	private PreferencesHelper preferencesHelper;
	private Preference changePrimaryContact, facebookConnect, googleConnect,
			notifications;
	private GoogleApiClient googleApiClient;
//	public Session.StatusCallback statusCallback = new FBSessionStatus();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.app_preferences);
		preferencesHelper = new PreferencesHelper(this.getActivity());
		setupGoogle();
		setupBugsense();

		//setupFacebook(savedInstanceState);

		changePrimaryContact = (Preference) findPreference("changePrimaryContact");
		changePrimaryContact
				.setOnPreferenceClickListener(new ChangeContactListener(
						getActivity()));

		facebookConnect = (Preference) findPreference("facebookConnected");
		facebookConnect
				.setOnPreferenceChangeListener(new FacebookConnectListener(
						getActivity()));

		googleConnect = (Preference) findPreference("googleConnected");
		googleConnect.setOnPreferenceChangeListener(new GoogleConnectListener(
				getActivity(), googleApiClient));

		googleConnect = (Preference) findPreference("uninstallProtection");
		googleConnect
				.setOnPreferenceChangeListener(new UninstallProtectionToggle(
						getActivity()));

		notifications = (Preference) findPreference("notifications");
		notifications.setOnPreferenceChangeListener(new NotificationsToggle(
				getActivity()));

	}

	public void setupBugsense() {
		BugSenseHandler.initAndStartSession(getActivity(), "5996b3d9");
	}

	public void setupGoogle() {
		googleApiClient = new GoogleApiClient.Builder(getActivity())
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this).addApi(Plus.API)
		.addScope(Plus.SCOPE_PLUS_PROFILE).build();
	}

//	public void setupFacebook(Bundle savedInstanceState) {
//		com.facebook.Settings
//				.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
//
//		Session session = Session.getActiveSession();
//		if (session == null) {
//			if (savedInstanceState != null) {
//				session = Session.restoreSession(getActivity(), null,
//						statusCallback, savedInstanceState);
//			}
//			if (session == null) {
//				session = new Session(getActivity());
//			}
//			Session.setActiveSession(session);
//			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
//				session.openForRead(new Session.OpenRequest(getActivity())
//						.setCallback(statusCallback));
//			}
//		}
//	}

	@Override
	public void onConnected(Bundle connectionHint) {
		/**
		 * we have nothing to do on google+ connected
		 */
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(getActivity(),
						CONNECTION_FAILURE_REQUEST);
			} catch (SendIntentException e) {
				googleApiClient.connect();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		//Session.getActiveSession().addCallback(statusCallback);
		googleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		//Session.getActiveSession().removeCallback(statusCallback);
		googleApiClient.disconnect();
	}

//	public class FBSessionStatus implements Session.StatusCallback {
//
//		@Override
//		public void call(Session session, SessionState state,
//				Exception exception) {
//
//		}
//	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Session.getActiveSession().onActivityResult(getActivity(), requestCode,
		//		resultCode, data);
		switch (requestCode) {
		case Choose.CONTACT_ACTIVATION_REQUEST:
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = this.getActivity().getContentResolver()
						.query(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					preferencesHelper
							.setString(
									"emergencyName",
									c.getString(c
											.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
					preferencesHelper
							.setString(
									"emergencyNumber",
									c.getString(c
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

					Toast.makeText(getActivity(), "Great, New Contact chosen.",
							Toast.LENGTH_SHORT).show();
				}
			} else
				Toast.makeText(getActivity(), "Aww, You cancelled !",
						Toast.LENGTH_SHORT).show();
			break;
		}

	}

	class ChangeContactListener implements OnPreferenceClickListener {

		private Activity activity;

		public ChangeContactListener(Activity activity) {
			this.activity = activity;
		}

		@Override
		public boolean onPreferenceClick(android.preference.Preference arg0) {
			Choose.getInstance(activity).contact();
			return false;
		}
	}

	class FacebookConnectListener implements OnPreferenceChangeListener {

		private Context context;
		private PreferencesHelper preferencesHelper;

		public FacebookConnectListener(Context context) {
			this.context = context;
			preferencesHelper = new PreferencesHelper(context);
		}

		@Override
		public boolean onPreferenceChange(Preference arg0, Object obj) {
			if (Boolean.parseBoolean(obj.toString())) {
				//connectFacebook();
			} else {
				new AlertDialog.Builder(context)
						.setTitle("Warning")
						.setMessage(
								"Please note, your friends won't be able to help you in trouble times.")
						.setPositiveButton(R.string.continue_,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
									//	disConnectFacebook();
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								}).setOnKeyListener(new OnKeyListener() {

							@Override
							public boolean onKey(DialogInterface dialog,
									int keyCode, KeyEvent event) {
								if (keyCode == KeyEvent.KEYCODE_BACK
										&& event.getAction() == KeyEvent.ACTION_UP) {
									dialog.cancel();
								}
								return false;
							}
						}).create().show();
			}

			return true;
		}

		//public void connectFacebook() {
		//	Session session = Session.getActiveSession();
		//	if (!session.isOpened() && !session.isClosed()) {
		//		session.openForRead(new Session.OpenRequest((Activity) context)
		//				.setCallback(statusCallback));
		//	} else {
		//		Session.openActiveSession((Activity) context, true,
		//				statusCallback);
		//	}
		//}

		//public void disConnectFacebook() {
		//	preferencesHelper.setBoolean(
		//			PreferencesHelper.FACEBOOK_CONNECT_STATUS, false);
		//	Session session = Session.getActiveSession();
		///	if (!session.isClosed()) {
		//		session.closeAndClearTokenInformation();
		//	}
		//}
	}

	class GoogleConnectListener implements OnPreferenceChangeListener {

		private Context context;
		private GoogleApiClient googleApiClient;

		public GoogleConnectListener(Context context,
				GoogleApiClient googleApiClient) {
			this.context = context;
			this.googleApiClient = googleApiClient;
		}

		@Override
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			new AlertDialog.Builder(context)
					.setTitle("Warning")
					.setMessage(
							"Please note, this will reset the application preferences, although the backup files will be kept. You will have to reconnect google to use this application.")
					.setPositiveButton(R.string.continue_,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									if (googleApiClient.isConnected()) {
										Reset.getInstance(context,
												googleApiClient).reset();
									}
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).setOnKeyListener(new OnKeyListener() {

						@Override
						public boolean onKey(DialogInterface dialog,
								int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_BACK
									&& event.getAction() == KeyEvent.ACTION_UP) {
								dialog.cancel();
							}
							return false;
						}
					}).create().show();
			return true;
		}

	}

	@Override
	public void onConnectionSuspended(int cause) {
		googleApiClient.connect();
	}

}

class ChangePinNumber extends DialogPreference {

	private EditText oldPinNumber;
	private EditText newPinNumber;
	private PreferencesHelper preferencesHelper;
	private Context context;

	public ChangePinNumber(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
		setDialogLayoutResource(R.layout.dialog_new_pin);
		preferencesHelper = new PreferencesHelper(context);
		this.context = context;
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		Button positive = ((AlertDialog) getDialog())
				.getButton(DialogInterface.BUTTON_POSITIVE);
		positive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (oldPinNumber.getText().toString()
						.equals(preferencesHelper.getString("pinNumber"))
						& newPinNumber.length() >= 6) {
					Toast.makeText(context, "Great, New PIN saved.",
							Toast.LENGTH_SHORT).show();
					preferencesHelper.setString("pinNumber", newPinNumber
							.getText().toString());
					((AlertDialog) getDialog()).dismiss();
				} else if (oldPinNumber.length() == 0) {
					Toast.makeText(context, "Please, Enter old PIN.",
							Toast.LENGTH_SHORT).show();
				} else if (newPinNumber.length() == 0) {
					Toast.makeText(context, "Please, Enter new PIN.",
							Toast.LENGTH_SHORT).show();
				} else if (newPinNumber.length() <= 6) {
					Toast.makeText(context, "New PIN should be of 6 digits.",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, "Sorry, Incorrect old PIN.",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	protected void onBindDialogView(View v) {
		newPinNumber = (EditText) v.findViewById(R.id.newPinNumber);
		oldPinNumber = (EditText) v.findViewById(R.id.oldPinNumber);

		oldPinNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence text, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2,
					int arg3) {
				if (text.length() >= 6
						& text.toString().equals(
								preferencesHelper.getString("pinNumber"))) {
					oldPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.ic_yes, 0);
				} else {
					oldPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.ic_no, 0);
				}

			}

		});

		newPinNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() < 6) {
					newPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.ic_no, 0);
				} else {
					newPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.ic_yes, 0);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence text, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2,
					int arg3) {
				if (text.length() >= 6
						& text.toString().equals(
								preferencesHelper.getString("pinNumber"))) {
					newPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.ic_yes, 0);
				} else {
					newPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.ic_no, 0);
				}

			}

		});
	}

}

class UninstallProtectionToggle implements OnPreferenceChangeListener {

	private PreferencesHelper preferencesHelper;
	private Context context;
	private Activity activity;

	public UninstallProtectionToggle(Context context) {
		this.context = context;
		this.activity = (Activity) context;
		preferencesHelper = new PreferencesHelper(context);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object obj) {
		if (Boolean.parseBoolean(obj.toString())) {
			new AlertDialog.Builder(context)
					.setTitle("")
					.setView(
							activity.getLayoutInflater().inflate(
									R.layout.dialog_uninstall_protect, null))
					.setPositiveButton(R.string.okay,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									preferencesHelper
											.setBoolean(
													PreferencesHelper.APP_UNINSTALL_PROTECTION,
													true);
								}
							}).create().show();
		} else {
			Toast.makeText(context, R.string.toast_string_10,
					Toast.LENGTH_SHORT).show();
			preferencesHelper.setBoolean(
					PreferencesHelper.APP_UNINSTALL_PROTECTION, false);
		}
		return true;
	}

}

class NotificationsToggle implements OnPreferenceChangeListener {

	private Context context;

	public NotificationsToggle(Context context) {
		this.context = context;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object obj) {
		if (Boolean.parseBoolean(obj.toString())) {
			Notifications.getInstance(context).show();
		} else {
			Notifications.getInstance(context).remove();
			Toast.makeText(context, R.string.toast_string_13,
					Toast.LENGTH_SHORT).show();
		}
		return true;
	}
}

class ChangeEmergencyMessage extends DialogPreference {

	private EditText newMessage;
	private PreferencesHelper preferencesHelper;
	private Context context;

	public ChangeEmergencyMessage(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
		setDialogLayoutResource(R.layout.dialog_new_message);
		preferencesHelper = new PreferencesHelper(context);
		this.context = context;
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		Button positive = ((AlertDialog) getDialog())
				.getButton(DialogInterface.BUTTON_POSITIVE);
		positive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (newMessage.length() > 0) {
					Toast.makeText(context,
							"Great, New Emergency message saved.",
							Toast.LENGTH_SHORT).show();
					preferencesHelper.setString("emergencyMessage", newMessage
							.getText().toString());
					((AlertDialog) getDialog()).dismiss();
				} else {
					Toast.makeText(context,
							"Please, Enter new emergency message.",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	protected void onBindDialogView(View v) {
		newMessage = (EditText) v.findViewById(R.id.newMessage);
		String storedMessage = preferencesHelper
				.getString(PreferencesHelper.DISTRESS_MESSAGE);
		if (!storedMessage.isEmpty())
			newMessage.setText(storedMessage);
	}
}

class ChangeDistressMessage extends DialogPreference {

	private EditText newMessage;
	private PreferencesHelper preferencesHelper;
	private Context context;

	public ChangeDistressMessage(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
		setDialogLayoutResource(R.layout.dialog_new_message);
		preferencesHelper = new PreferencesHelper(context);
		this.context = context;
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		Button positive = ((AlertDialog) getDialog())
				.getButton(DialogInterface.BUTTON_POSITIVE);
		positive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (newMessage.length() > 0) {
					Toast.makeText(context,
							"Great, New Emergency message saved.",
							Toast.LENGTH_SHORT).show();
					preferencesHelper.setString("emergencyMessage", newMessage
							.getText().toString());
					((AlertDialog) getDialog()).dismiss();
				} else {
					Toast.makeText(context,
							"Please, Enter new emergency message.",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	protected void onBindDialogView(View v) {
		newMessage = (EditText) v.findViewById(R.id.newMessage);
		String storedMessage = preferencesHelper
				.getString(PreferencesHelper.DISTRESS_MESSAGE);
		if (!storedMessage.isEmpty())
			newMessage.setText(storedMessage);
	}
}
