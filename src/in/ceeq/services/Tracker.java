package in.ceeq.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Tracker extends Service {

	public Tracker() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
