package in.ceeq.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Syncer extends Service {
	public Syncer() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
