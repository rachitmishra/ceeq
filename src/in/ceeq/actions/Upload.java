/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.actions;

import in.ceeq.services.Uploader;
import in.ceeq.services.Uploader.UploadType;
import android.content.Context;
import android.content.Intent;

public class Upload {
	private Context context;

	public Upload(Context context) {
		this.context = context;
	}

	public static Upload getInstance(Context context) {
		return new Upload(context);
	}

	public void start(UploadType uploadType) {
		Intent startBackup = new Intent(context, Uploader.class).putExtra(
				Uploader.ACTION, UploadType.FEEDBACK);
		context.startService(startBackup);
	}
}
