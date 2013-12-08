/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import in.ceeq.R;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

public class Ringer extends IntentService {

	public static final String ACTION = "action";
	public static final String ACTION_TYPE = "actionType";

	public enum SoundState {
		ON, OFF
	}

	public enum SoundType {
		SIREN, RING
	}

	private MediaPlayer player;
	private AudioManager audioManager;
	private SoundState soundState;
	private SoundType soundType;

	public Ringer() {
		super("ServiceAlarm");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_RINGTONE);
		soundType = (SoundType) intent.getExtras().get(ACTION_TYPE);

		switch (soundType) {
		case SIREN:
			player = MediaPlayer.create(this, R.raw.siren);
			break;
		case RING:
			player = MediaPlayer.create(this, uri);
			break;
		}

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		soundState = (SoundState) intent.getExtras().get(ACTION);

		switch (soundState) {
		case ON:
			while (true)
				player.start();
		case OFF:
			player.stop();
			break;
		}
	}
}
