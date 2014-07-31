package in.ceeq.actions;

import java.io.File;
import java.lang.reflect.Method;

import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;

public class Wipe {

	public final static int EXTERNAL_STORAGE = 0;
	public final static int DEVICE = 1;
	public final static int EXTERNAL_STORAGE_AND_DEVICE = 2;
	private ProgressDialog progressDialog;
	private Context context;
	private PackageManager packageManager;
	private DevicePolicyManager devicePolicyManager;

	public Wipe(Context context) {
		this.context = context;
		devicePolicyManager = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
	}

	public static Wipe getInstance(Context context) {
		return new Wipe(context);
	}

	public void cache() {
		progressDialog = new ProgressDialog(context);
		packageManager = context.getPackageManager();
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				progressDialog.setMessage("Wiping Cache...");
				progressDialog.show();
			}

			@Override
			protected void onPostExecute(Void res) {
				progressDialog.dismiss();
			}

			@Override
			protected Void doInBackground(Void... params) {
				Method[] methods = packageManager.getClass()
						.getDeclaredMethods();
				for (Method m : methods) {
					if (m.getName().equals("freeStorageAndNotify")) {
						try {
							long desiredFreeStorage = Long.MAX_VALUE;
							m.invoke(packageManager, desiredFreeStorage, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
				return null;
			}
		}.execute();
	}

	public void device() {
		devicePolicyManager.wipeData(0);
	}

	public void deviceAndExternalStorage() {
		devicePolicyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
	}

	private File deleteMatchingFile;
	public void externalStorage() {
		progressDialog = new ProgressDialog(context);
		deleteMatchingFile = new File(Environment.getExternalStorageDirectory()
				.toString());

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				progressDialog.setMessage("Wiping External Storage...");
				progressDialog.show();
			}

			@Override
			protected void onPostExecute(Void res) {
				progressDialog.dismiss();
			}

			@Override
			protected Void doInBackground(Void... params) {
				try {
					File[] filenames = deleteMatchingFile.listFiles();
					if (filenames != null && filenames.length > 0) {
						for (File tempFile : filenames) {
							if (tempFile.isDirectory()) {
								directory(tempFile.toString());
								tempFile.delete();
							} else {
								tempFile.delete();
							}
						}
					} else {
						deleteMatchingFile.delete();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute();

	}

	public void directory(String name) {
		File directoryFile = new File(name);
		File[] filenames = directoryFile.listFiles();
		if (filenames != null && filenames.length > 0) {
			for (File tempFile : filenames) {
				if (tempFile.isDirectory()) {
					directory(tempFile.toString());
					tempFile.delete();
				} else {
					tempFile.delete();
				}
			}
		} else {
			directoryFile.delete();
		}
	}

}
