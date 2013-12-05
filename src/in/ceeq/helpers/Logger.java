package in.ceeq.helpers;

import android.util.Log;

public class Logger {
	/**
	 * Debug
	 * 
	 * @param message
	 */
	public static void d(String message) {
		Log.d("Developer", message);
	}

	/**
	 * Warn
	 * 
	 * @param message
	 */
	public static void w(String message) {
		Log.w("Developer", message);
	}

	/**
	 * Info
	 * 
	 * @param message
	 */
	public static void i(String message) {
		Log.i("Developer", message);
	}
}