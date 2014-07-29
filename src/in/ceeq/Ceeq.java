package in.ceeq;

import com.bugsense.trace.BugSenseHandler;
import com.crashlytics.android.Crashlytics;

import android.app.Application;

public class Ceeq extends Application {
		
	public void onCreate() {
		super.onCreate();
		BugSenseHandler.initAndStartSession(getApplicationContext(), "5996b3d9");
		Crashlytics.start(this);
	}
}
