package in.ceeq.fragments;

import in.ceeq.R;
import in.ceeq.helpers.PreferencesHelper;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

public class SecurityManager extends Fragment implements
		OnMyLocationChangeListener {

	private GoogleMap map;
	private View view;
	private ToggleButton toggleButton;
	private PreferencesHelper preferencesHelper;

	public SecurityManager() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		preferencesHelper = new PreferencesHelper(getActivity());
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		try {
			view = inflater.inflate(R.layout.fragment_security, container,
					false);

			map = ((SupportMapFragment) getActivity()
					.getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			map.getUiSettings().setAllGesturesEnabled(false);
			map.getUiSettings().setZoomControlsEnabled(false);
			map.getUiSettings().setMyLocationButtonEnabled(false);
			map.setMyLocationEnabled(true);
			map.setOnMyLocationChangeListener(this);
			MapsInitializer.initialize(getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}

		restoreToggleStates(view);
		return view;
	}

	public void restoreToggleStates(View view) {

		toggleButton = (ToggleButton) view.findViewById(R.id.toggle_track);
		toggleButton.setChecked(preferencesHelper
				.getBoolean(PreferencesHelper.AUTO_TRACK_STATUS));
		toggleButton = (ToggleButton) view.findViewById(R.id.toggle_blip);
		toggleButton.setChecked(preferencesHelper
				.getBoolean(PreferencesHelper.AUTO_BLIP_STATUS));
	}

	@Override
	public void onMyLocationChange(Location newLocation) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				newLocation.getLatitude(), newLocation.getLongitude()), 15));
		try {
			IconGenerator iconGenerator = new IconGenerator(getActivity());
			Bitmap bitmap = iconGenerator.makeIcon("You");
			map.clear();
			map.addMarker(new MarkerOptions().position(
					new LatLng(newLocation.getLatitude(), newLocation
							.getLongitude())).icon(
					BitmapDescriptorFactory.fromBitmap(bitmap)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}