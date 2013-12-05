/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;

import in.ceeq.R;

public class Ringer extends IntentService {

	public static final String ACTION = "action";
	public enum SirenState {
		ON, OFF
	}
	private MediaPlayer player;
	private AudioManager audioManager;
	private SirenState alarmState;
	public Ringer() {
		super("ServiceAlarm");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		player = MediaPlayer.create(this, R.raw.siren);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		alarmState = (SirenState) intent.getExtras().get(ACTION);
		switch (alarmState) {
			case ON :
				while (true)
					player.start();
			case OFF :
				player.stop();
				break;
		}
	}
}
