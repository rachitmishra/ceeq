package in.ceeq.helpers;

import in.ceeq.helpers.PhoneHelper.Phone;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.SmsManager;

public class MessagesHelper {

	private Helpers helpers;
	private SmsManager smsManager;
	private PreferencesHelper preferencesHelper;
	private PhoneHelper phoneHelper;
	private ContentResolver resolver;
	private Context context;

	/**
	 * 
	 * Message types that may be requested to be send by the helper
	 * 
	 */
	public enum MessageType {
		SIM_CHANGE, LOCATION, PROTECT_ME, NEW_LOCATION, CALLS, NOW, FAIL
	}

	public MessagesHelper(Context context) {
		this.context = context;
		helpers = Helpers.getInstance(context);
		preferencesHelper = PreferencesHelper.getInstance(context);
		phoneHelper = PhoneHelper.getInstance(context);
		smsManager = SmsManager.getDefault();
	}

	/**
	 * Get an instance of the helper
	 * 
	 * @param context
	 * @return
	 */
	public static MessagesHelper getInstance(Context context) {
		return new MessagesHelper(context);
	}

	/**
	 * Send message based on message type
	 * 
	 * @param deliverTo
	 * @param messageType
	 */
	public void sendMessage(String deliverTo, MessageType messageType) {
		String message = "";
		switch (messageType) {
		case CALLS:
			message = getCallsMessage();
			break;
		case LOCATION:
			message = getLastLocationMessage();
			Logger.d(message);
			break;
		case NEW_LOCATION:
			message = getNewLocationMessage();
			Logger.d(message);
			break;
		case NOW:
			message = getDetailsMessage();
			Logger.d(message);
			break;
		case PROTECT_ME:
			message = getProtectMeMessage();
			Logger.d(message);
			break;
		case SIM_CHANGE:
			message = getSimChangeMessage();
			break;
		case FAIL:
			message = getFailedChangeMessage();
			break;
		default:
			break;
		}
		if (!message.isEmpty())
			smsManager.sendTextMessage(deliverTo, null, message, null, null);
	}

	private String getFailedChangeMessage() {
		return "Sorry, The PIN entered by you is incorrect.";
	}

	/**
	 * Create a call log message message
	 * 
	 * @return
	 */
	public String getCallsMessage() {
		return "Last 10 calls from device are : " + getCalls(10);
	}

	/**
	 * Create a last location message
	 * 
	 * @return
	 */
	public String getLastLocationMessage() {
		return "Last location of device is : " + getLocationMessage();
	}

	/**
	 * Create a new location message
	 * 
	 * @return
	 */
	public String getNewLocationMessage() {
		return "Device location has changed. New location is : "
				+ getLocationMessage();
	}

	/**
	 * Create raw location message
	 * 
	 * @return
	 */
	public String getLocationMessage() {
		return preferencesHelper
				.getString(PreferencesHelper.LAST_LOCATION_LATITUDE)
				+ ", "
				+ preferencesHelper
						.getString(PreferencesHelper.LAST_LOCATION_LATITUDE);
	}

	public static final String NUMBER = CallLog.Calls.NUMBER;
	public static final String DURATION = CallLog.Calls.DURATION;
	public static final String TYPE = CallLog.Calls.TYPE;
	private final Uri URI = CallLog.Calls.CONTENT_URI;

	/**
	 * Get last n calls from the logs
	 * 
	 * @return String
	 */
	public String getCalls(int n) {
		resolver = context.getContentResolver();
		Cursor cs = resolver.query(URI, null, null, null, null);
		StringBuffer sb = new StringBuffer();
		int count = 0;
		try {
			if (cs.moveToFirst()) {
				do {
					if (cs.getInt(cs.getColumnIndex(TYPE)) != 3) {
						String number = cs.getString(cs.getColumnIndex(NUMBER));
						String duration = cs.getString(cs
								.getColumnIndex(DURATION));
						int durations = (Integer.parseInt(duration) / 60);
						sb.append(number + " " + durations + "mins\n");
						count++;
					}
				} while (cs.moveToNext() && count < n);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cs.close();
		}
		return sb.toString();
	}

	/**
	 * Create SIM changed message
	 * 
	 * @return
	 */
	public String getSimChangeMessage() {
		return preferencesHelper.getString("emergencyMessage") + "\n"
				+ "New Sim Number : " + phoneHelper.getData(Phone.SIM_ID)
				+ "\n" + "New Sim Operator : "
				+ phoneHelper.getData(Phone.OPERATOR) + "\n"
				+ "New Sim Subscriber Id : " + phoneHelper.getData(Phone.IMSI)
				+ "\n" + "Your Device IEMI: " + phoneHelper.getData(Phone.IEMI)
				+ "\n";
	}

	/**
	 * Create protect me message
	 * 
	 * @return
	 */
	public String getProtectMeMessage() {
		return "Help "
				+ preferencesHelper
						.getString(PreferencesHelper.ACCOUNT_USER_NAME)
				+ preferencesHelper
						.getString(PreferencesHelper.DISTRESS_MESSAGE)
				+ "\n"
				+ "Last User Location : "
				+ getLocationMessage()
				+ "\n"
				+ "Battery Status : "
				+ (helpers.getBatteryLevel() * 100)
				+ "%"
				+ "\nCeeq will send you regular location updates every 10 minutes.\n";
	}

	/**
	 * Get current phone details
	 * 
	 * @return
	 */
	public String getDetailsMessage() {
		return "Current \n" + "Sim Number : "
				+ phoneHelper.getData(Phone.SIM_ID) + "\n" + "Sim Operator : "
				+ phoneHelper.getData(Phone.OPERATOR) + "\n"
				+ "Sim Subscriber Id : " + phoneHelper.getData(Phone.IMSI)
				+ "\n" + "Location :" + getLocationMessage() + "\n";
	}
}
