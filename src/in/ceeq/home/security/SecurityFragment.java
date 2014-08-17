package in.ceeq.home.security;

import in.ceeq.R;
import in.ceeq.commons.Utils;
import in.ceeq.receivers.BatteryStateReceiver;
import in.ceeq.services.LocationService;
import in.ceeq.services.LocationService.RequestType;
import in.ceeq.services.TrackerService;

import java.io.File;
import java.lang.reflect.Method;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

public class SecurityFragment extends Fragment implements OnMyLocationChangeListener, DialogInterface.OnClickListener,
		View.OnClickListener, DialogInterface.OnKeyListener {

	public static final int WIPE_DEVICE = 0;
	public static final int EXTERNAL_STORAGE = 1;
	public static final int EXTERNAL_STORAGE_AND_DEVICE = 2;
	private GoogleMap map;
	private MapView mapView;
	private Button wipe;
	private Button wipeCache;
	private ToggleButton autoBlip;
	private ToggleButton autoLocate;
	private ProgressDialog progressDialog;
	private DevicePolicyManager devicePolicyManager;
	private File deleteMatchingFile;
	private AlertDialog.Builder alertDialogBuilder;
	private LayoutInflater layoutInflater;
	private PackageManager packageManager;
	private ComponentName componentName;
	private Context context;

	public static SecurityFragment getInstance() {
		return new SecurityFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		alertDialogBuilder = new AlertDialog.Builder(context);
		layoutInflater = ((Activity) context).getLayoutInflater();
		packageManager = context.getPackageManager();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setActionBar();
		View view = inflater.inflate(R.layout.fragment_security, container, false);

		autoBlip = (ToggleButton) view.findViewById(R.id.toggle_blip);
		autoLocate = (ToggleButton) view.findViewById(R.id.toggle_locate);
		wipe = (Button) view.findViewById(R.id.wipe);
		wipeCache = (Button) view.findViewById(R.id.wipe_cache);

		mapView = (MapView) view.findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);
		mapView.onResume();
		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
		map = mapView.getMap();
		setupMap();

		setupListeners();
		return view;
	}
	
	private void setActionBar(){
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.blue));
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Security");
	}

	private void setupMap() {
		map.getUiSettings().setAllGesturesEnabled(false);
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setMyLocationButtonEnabled(false);
		map.setMyLocationEnabled(true);
		map.setOnMyLocationChangeListener(this);
	}

	private void setupListeners() {
		wipe.setOnClickListener(this);
		wipeCache.setOnClickListener(this);
		autoBlip.setOnClickListener(this);
		autoLocate.setOnClickListener(this);
	}

	public void showWipeDialog() {
		alertDialogBuilder.setTitle(R.string.dialog_title_wipe).setSingleChoiceItems(R.array.wipe_options, -1, this)
				.setNegativeButton(R.string.cancel, this).create().show();
	}

	public void wipeDevice() {
		devicePolicyManager.wipeData(0);
	}

	public void wipeDeviceAndExternalStorage() {
		devicePolicyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
	}

	public void wipeExternalStorage() {
		progressDialog = new ProgressDialog(getActivity());
		deleteMatchingFile = new File(Environment.getExternalStorageDirectory().toString());

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

	private void wipeCache() {
		progressDialog = new ProgressDialog(getActivity());
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
				Method[] methods = getActivity().getPackageManager().getClass().getDeclaredMethods();
				for (Method m : methods) {
					if (m.getName().equals("freeStorageAndNotify")) {
						try {
							long desiredFreeStorage = Long.MAX_VALUE;
							m.invoke(getActivity().getPackageManager(), desiredFreeStorage, null);
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

	public void restoreToggleStates(View view) {
		autoLocate.setChecked(Utils.getBooleanPrefs(context, Utils.AUTO_TRACK_STATUS));
		autoBlip.setChecked(Utils.getBooleanPrefs(context, Utils.AUTO_BLIP_STATUS));
	}

	@Override
	public void onMyLocationChange(Location newLocation) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(newLocation.getLatitude(), newLocation.getLongitude()), 15));
		try {
			IconGenerator iconGenerator = new IconGenerator(getActivity());
			Bitmap bitmap = iconGenerator.makeIcon("You");
			map.clear();
			map.addMarker(new MarkerOptions().position(
					new LatLng(newLocation.getLatitude(), newLocation.getLongitude())).icon(
					BitmapDescriptorFactory.fromBitmap(bitmap)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case EXTERNAL_STORAGE:
			showWipeExternalStorageDialog();
			break;
		case WIPE_DEVICE:
			showWipeDeviceDialog();
			break;
		case EXTERNAL_STORAGE_AND_DEVICE:
			showWipeDeviceAndExternalStorageDialog();
			break;
		case Dialog.BUTTON_NEGATIVE:
			dialog.dismiss();
			break;
		case Dialog.BUTTON_POSITIVE:
			startBlipService();
			break;
		}
	}

	public void showWipeExternalStorageDialog() {
		alertDialogBuilder.setView(layoutInflater.inflate(R.layout.dialog_wipe, null));
		alertDialogBuilder.setPositiveButton(R.string.continue_, this).setNegativeButton(R.string.cancel, this);
	}

	public void showWipeDeviceDialog() {
		alertDialogBuilder.setView(layoutInflater.inflate(R.layout.dialog_wipe_external_storage, null));
		alertDialogBuilder.setPositiveButton(R.string.continue_, this).setNegativeButton(R.string.cancel, this);
	}

	public void showWipeDeviceAndExternalStorageDialog() {
		alertDialogBuilder.setView(layoutInflater.inflate(R.layout.dialog_wipe, null));
		alertDialogBuilder.setPositiveButton(R.string.continue_, this).setNegativeButton(R.string.cancel, this);
	}

	private void showBlipDialog() {
		alertDialogBuilder.setTitle(R.string.dialog_title_blip).setPositiveButton(R.string.continue_, this)
				.setNegativeButton(R.string.cancel, this)
				.setView(layoutInflater.inflate(R.layout.dialog_blips_info, null));
		alertDialogBuilder.create().show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wipe:
			showWipeDialog();
			break;
		case R.id.wipe_cache:
			wipeCache();
			break;
		case R.id.toggle_locate:
			setupLocationTracker(autoLocate.isChecked());
			break;
		case R.id.toggle_blip:
			setupAutoBlips(autoBlip.isChecked());
			break;
		}
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			dialog.dismiss();
		}
		return true;
	}

	private void setupAutoBlips(boolean status) {
		componentName = new ComponentName(getActivity(), BatteryStateReceiver.class);
		Utils.setBooleanPrefs(context, Utils.AUTO_BLIP_STATUS, status);
		if (status) {
			Toast.makeText(getActivity(), "Auto blips enabled.", Toast.LENGTH_SHORT).show();
			showBlipDialog();
			try {
				if (componentName != null)
					packageManager.setComponentEnabledSetting(componentName,
							PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (componentName != null)
					packageManager.setComponentEnabledSetting(componentName,
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Utils.setBooleanPrefs(context, Utils.AUTO_BLIP_STATUS, autoBlip.isChecked());
			Toast.makeText(getActivity(), "Auto blips disabled.", Toast.LENGTH_SHORT).show();
		}
	}

	private void startBlipService() {
		Intent getLocation = new Intent(getActivity(), LocationService.class);
		getLocation.putExtra(LocationService.ACTION, RequestType.MESSAGE);
		getActivity().startService(getLocation);
	}

	private void setupLocationTracker(boolean status) {
		Utils.setBooleanPrefs(context, Utils.AUTO_TRACK_STATUS, status);

		if (status) {
			Toast.makeText(getActivity(), "Automatic tracking enabled.", Toast.LENGTH_SHORT).show();
			Intent startTracker = new Intent(getActivity(), TrackerService.class);
			startTracker.putExtra(TrackerService.ACTION, RequestType.TRACKER);
			getActivity().startService(startTracker);
		} else {
			Toast.makeText(getActivity(), "Automatic tracking disabled.", Toast.LENGTH_SHORT).show();
			getActivity().stopService(new Intent(getActivity(), TrackerService.class));
		}
	}
}