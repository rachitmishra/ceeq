package in.ceeq.helpers;

import android.content.Context;
import android.telephony.TelephonyManager;

public class PhoneHelper {

	public enum Phone {
		SIM_ID, NUMBER, IEMI, IMSI, OPERATOR, MANUFACTURER, MODEL, ANDROID_VERSION
	}

	private Context context;
	private TelephonyManager telephonyManager;

	public PhoneHelper(Context context) {
		this.context = context;
	}

	public static PhoneHelper getInstance(Context context) {
		return new PhoneHelper(context);
	}

	public String getData(Phone dataType) {
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String data = "Not Available.";

		switch (dataType) {
			case SIM_ID :
				data = telephonyManager.getSimSerialNumber();
				break;
			case NUMBER :
				if (telephonyManager.getLine1Number() != null)
					data = telephonyManager.getLine1Number();
				break;
			case IEMI :
				data = telephonyManager.getDeviceId();
				break;
			case IMSI :
				data = telephonyManager.getSubscriberId();
				break;
			case OPERATOR :
				data = telephonyManager.getSimOperatorName();
				break;
			case MANUFACTURER :
				data = android.os.Build.MANUFACTURER;
				break;
			case MODEL :
				data = android.os.Build.MODEL;
				break;
			case ANDROID_VERSION :
				data = android.os.Build.VERSION.RELEASE;
				break;
			default :
				data = "Not Available.";
				break;
		}

		return data;
	}
}
