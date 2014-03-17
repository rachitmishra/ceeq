package in.ceeq.helpers;

import android.util.Log;

public class Logger {

	public static String STARTED = "Starting... ";
	public static String COMPLETED = "Completed... ";
	public static String FAILED = "Failed... ";
	public static String ELLIPSIZE = "... ";

	/**
	 * Log debug message.
	 * 
	 * @param message
	 */
	public static void d(String message) {
		Log.d("@ceeq", message);
	}

	/**
	 * Log warning message.
	 * 
	 * @param message
	 */
	public static void w(String message) {
		Log.w("@ceeq", message);
	}

	/**
	 * Log informative message.
	 * 
	 * @param message
	 */
	public static void i(String message) {
		Log.i("@ceeq", message);
	}

	/**
	 * Log action started message.
	 * 
	 * @param action
	 *            backup or restore
	 * @param actionType
	 *            contacts or call logs or messages or dictionary.
	 */
	public static void s(String message) {
		Log.i("@ceeq", STARTED + message + ELLIPSIZE);
	}

	/**
	 * Log action completed message.
	 * 
	 * @param action
	 *            backup or restore
	 * @param actionType
	 *            contacts or call logs or messages or dictionary.
	 */
	public static void c(String message) {
		Log.i("@ceeq", COMPLETED + message + ELLIPSIZE);
	}

	/**
	 * Log action failed message.
	 * 
	 * @param action
	 *            backup or restore
	 * @param actionType
	 *            contacts or call logs or messages or dictionary.
	 */
	public static void f(String message) {
		Log.e("@ceeq", FAILED + message + ELLIPSIZE);
	}
}