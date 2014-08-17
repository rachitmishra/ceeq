package in.ceeq.settings;

import in.ceeq.R;
import in.ceeq.commons.Utils;
import android.annotation.SuppressLint;
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
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;

class PreferencesFragment extends PreferenceFragment implements ConnectionCallbacks, OnConnectionFailedListener,
		OnPreferenceClickListener, OnPreferenceChangeListener {

	private static final int CONNECTION_FAILURE_REQUEST = 9020;
	private static final String CHANGE_CONTACT = "change_contact";
	private static final String ADD_EXTRA_CONTACT = "add_extra_contact";
	private static final String FACEBOOK_CONNECTED_PREF = "facebook_connected";
	private static final String GOOGLE_CONNECTED_PREF = "google_connected";
	private static final String UNINSTALL_PROTECTION_PREF = "uninstall_protection";
	private static final String NOTIFICATION_PREF = "notifications";

	private Preference changePrimaryContact, facebookConnect, googleConnect, notifications;
	private GoogleApiClient googleApiClient;
	private Context context;

	// public Session.StatusCallback statusCallback = new FBSessionStatus();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getActivity();
		addPreferencesFromResource(R.xml.app_preferences);
		setupGoogle();

		// setupFacebook(savedInstanceState);

		changePrimaryContact = (Preference) findPreference(CHANGE_CONTACT);
		changePrimaryContact.setOnPreferenceClickListener(this);

		facebookConnect = (Preference) findPreference(FACEBOOK_CONNECTED_PREF);
		facebookConnect.setOnPreferenceChangeListener(this);

		googleConnect = (Preference) findPreference(GOOGLE_CONNECTED_PREF);
		googleConnect.setOnPreferenceChangeListener(this);

		googleConnect = (Preference) findPreference(UNINSTALL_PROTECTION_PREF);
		googleConnect.setOnPreferenceChangeListener(this);

		notifications = (Preference) findPreference(NOTIFICATION_PREF);
		notifications.setOnPreferenceChangeListener(this);

	}

	public void setupGoogle() {
		googleApiClient = new GoogleApiClient.Builder(getActivity()).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_PROFILE).build();
	}

	// public void setupFacebook(Bundle savedInstanceState) {
	// com.facebook.Settings
	// .addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
	//
	// Session session = Session.getActiveSession();
	// if (session == null) {
	// if (savedInstanceState != null) {
	// session = Session.restoreSession(getActivity(), null,
	// statusCallback, savedInstanceState);
	// }
	// if (session == null) {
	// session = new Session(getActivity());
	// }
	// Session.setActiveSession(session);
	// if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
	// session.openForRead(new Session.OpenRequest(getActivity())
	// .setCallback(statusCallback));
	// }
	// }
	// }

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
				result.startResolutionForResult(getActivity(), CONNECTION_FAILURE_REQUEST);
			} catch (SendIntentException e) {
				googleApiClient.connect();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// Session.getActiveSession().addCallback(statusCallback);
		googleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		// Session.getActiveSession().removeCallback(statusCallback);
		googleApiClient.disconnect();
	}

	// public class FBSessionStatus implements Session.StatusCallback {
	//
	// @Override
	// public void call(Session session, SessionState state,
	// Exception exception) {
	//
	// }
	// }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Session.getActiveSession().onActivityResult(getActivity(), requestCode,
		// resultCode, data);
		switch (requestCode) {
		case Utils.CONTACT_REQUEST:
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = context.getContentResolver().query(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					Utils.setStringPrefs(context, Utils.EMERGENCY_CONTACT_NAME,
							c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
					Utils.setStringPrefs(context, Utils.EMERGENCY_CONTACT_NUMBER,
							c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

					Toast.makeText(context, "Great, New Contact chosen.", Toast.LENGTH_SHORT).show();
				}
			} else
				Toast.makeText(context, "Sorry, You cancelled !", Toast.LENGTH_SHORT).show();
			break;
		}

	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals(CHANGE_CONTACT)) {
			Utils.chooseContact((Activity) context);
		} else if (key.equals(ADD_EXTRA_CONTACT)) {
			Utils.chooseContact((Activity) context);
		}
		return true;
	}

	@SuppressLint("InflateParams")
	@Override
	public boolean onPreferenceChange(Preference preference, Object obj) {
		String key = preference.getKey();

		if (key.equals(NOTIFICATION_PREF)) {
			if ((Boolean) obj) {
				Utils.notification(Utils.NOTIFICATION_NOTIFY, context, Utils.DEFAULT_NOTIFICATION_ID);
			} else {
				Utils.notification(Utils.NOTIFICATION_NOTIFY, context, Utils.DEFAULT_NOTIFICATION_ID);
				Toast.makeText(context, R.string.toast_string_13, Toast.LENGTH_SHORT).show();
			}
		}

		if (key.equals(GOOGLE_CONNECTED_PREF)) {
			new AlertDialog.Builder(context).setTitle("Warning").setMessage(getString(R.string.help_note_38))
					.setPositiveButton(R.string.continue_, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							if (googleApiClient.isConnected()) {
								ResetTask.run(context, googleApiClient);
							}
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).setOnKeyListener(new OnKeyListener() {

						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
								dialog.cancel();
							}
							return false;
						}
					}).create().show();
		}

		if (key.equals(FACEBOOK_CONNECTED_PREF)) {
			if (Boolean.parseBoolean(obj.toString())) {
				// connectFacebook();
			} else {
				new AlertDialog.Builder(context).setTitle("Warning").setMessage(getString(R.string.help_note_39))
						.setPositiveButton(R.string.continue_, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// disConnectFacebook();
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						}).setOnKeyListener(new OnKeyListener() {

							@Override
							public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
								if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
									dialog.cancel();
								}
								return false;
							}
						}).create().show();
			}

		}

		if (key.equals(UNINSTALL_PROTECTION_PREF)) {
			if ((Boolean) obj) {
				new AlertDialog.Builder(context)
						.setTitle("")
						.setView(
								((Activity) context).getLayoutInflater().inflate(R.layout.dialog_uninstall_protect,
										null)).setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Utils.setBooleanPrefs(context, Utils.APP_UNINSTALL_PROTECTION, true);
							}
						}).create().show();
			} else {
				Toast.makeText(context, R.string.toast_string_10, Toast.LENGTH_SHORT).show();
				Utils.setBooleanPrefs(context, Utils.APP_UNINSTALL_PROTECTION, false);
			}
		}
		return true;
	}

	// public void connectFacebook() {
	// Session session = Session.getActiveSession();
	// if (!session.isOpened() && !session.isClosed()) {
	// session.openForRead(new Session.OpenRequest((Activity) context)
	// .setCallback(statusCallback));
	// } else {
	// Session.openActiveSession((Activity) context, true,
	// statusCallback);
	// }
	// }

	// public void disConnectFacebook() {
	// preferencesHelper.setBoolean(
	// PreferencesHelper.FACEBOOK_CONNECT_STATUS, false);
	// Session session = Session.getActiveSession();
	// / if (!session.isClosed()) {
	// session.closeAndClearTokenInformation();
	// }
	// }
}