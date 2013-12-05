/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.activities;

import in.ceeq.R;
import in.ceeq.helpers.CameraHelper;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Runner;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Lockscreen extends Activity {
	private PreferencesHelper preferencesHelper;
	private SurfaceView surfaceView;
	private TextView error;
	private BroadcastReceiver onTopReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_lockscreen);
		preferencesHelper = new PreferencesHelper(this);
		TextView phone = (TextView) findViewById(R.id.uwarn);
		error = (TextView) findViewById(R.id.showError);
		phone.setText("Call " + preferencesHelper.getString("emergencyNumber")
				+ " to contact the owner of the phone.");
		registerReceiver(onTopReceiver, new IntentFilter(
				"in.ceeq.ACTION_BACKUP"));
		surfaceView = (SurfaceView) findViewById(R.id.sv);
		// if (Cameras.hasFrontCamera())
		// new Cameras(sv, this).takepic();
		preferencesHelper.setBoolean("locked", true);
		startService(new Intent(this, Runner.class).putExtra("action", 1));
	}

	public void unlock(View v) {

		switch (v.getId()) {
			case R.id.unlock :
				EditText unlock = (EditText) findViewById(R.id.type2);
				String key = unlock.getText().toString();

				unlock.addTextChangedListener(new TextWatcher() {

					@Override
					public void afterTextChanged(Editable text) {
						// if (text.length() == 6)
						// if (Cameras.hasFrontCamera())
						// new Cameras(sv, getBaseContext()).takepic();

					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
					}

					@Override
					public void onTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
					}
				});

				if (key.equals(preferencesHelper.getString("pinNumber"))) {
					stopService(new Intent(this, Runner.class));
					Lockscreen.this.finish();

				} else {
					if (CameraHelper.hasFrontCamera())
						// new Cameras(sv, this).takepic();
						error.setText("Wrong Passcode !!");
				}
				break;
			case R.id.unlock_b :
		}
	}

	@Override
	protected void onPause() {
		// setUpOnTopAlarms(ON);
		// startActivity(new Intent(this, LockScreen_.class));
		super.onPause();

	}

	@Override
	protected void onStop() {
		// setUpOnTopAlarms(ON);
		// startActivity(new Intent(this, LockScreen_.class));

		super.onStop();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA)
			return true;
		else if (keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_POWER)
			return true;
		else if (keyCode == KeyEvent.KEYCODE_SEARCH)
			return true;
		else if (keyCode == KeyEvent.KEYCODE_APP_SWITCH)
			return true;
		else
			return true;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent keyEvent) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
			return true;
		else if (keyCode == KeyEvent.KEYCODE_POWER)
			return true;
		else if (keyCode == KeyEvent.KEYCODE_BACK)
			return true;
		else if (keyCode == KeyEvent.KEYCODE_CAMERA)
			return true;
		return true;
	}

	@Override
	public void onBackPressed() {
	}

	@Override
	protected void onResume() {
		super.onResume();
		startService(new Intent(this, Runner.class).putExtra("action", 2));
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (!hasFocus) {
			Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
			sendBroadcast(closeDialog);
		}
	}

}
