package in.ceeq.fragments;

import in.ceeq.R;
import in.ceeq.helpers.PhoneHelper;
import in.ceeq.helpers.PreferencesHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyDeviceFragment extends Fragment {
	private PhoneHelper phoneHelper;
	private PreferencesHelper preferencesHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container,
				false);
		preferencesHelper = PreferencesHelper.getInstance(getActivity());
		phoneHelper = PhoneHelper.getInstance(getActivity());
		setupData(view);
		return view;
	}

	public void setupData(View view) {
		TextView text = (TextView) view.findViewById(R.id.c_account);
		text.setText(preferencesHelper
				.getString(PreferencesHelper.ACCOUNT_USER_ID));
		text = (TextView) view.findViewById(R.id.c_name);
		text.setText(preferencesHelper
				.getString(PreferencesHelper.ACCOUNT_USER_NAME));
		text = (TextView) view.findViewById(R.id.c_sim);
		text.setText(phoneHelper.get(PhoneHelper.SIM_ID));
		text = (TextView) view.findViewById(R.id.c_imsi);
		text.setText(phoneHelper.get(PhoneHelper.IMSI));
		text = (TextView) view.findViewById(R.id.c_iemi);
		text.setText(phoneHelper.get(PhoneHelper.IEMI));
		text = (TextView) view.findViewById(R.id.c_gps);
		text.setText(booleanToString(phoneHelper.enabled(PhoneHelper.GPS)));
		text = (TextView) view.findViewById(R.id.c_admin);
		text.setText(booleanToString(preferencesHelper
				.getBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS)));
		text = (TextView) view.findViewById(R.id.c_operator);
		text.setText(phoneHelper.get(PhoneHelper.OPERATOR));
		text = (TextView) view.findViewById(R.id.c_size);
		text.setText(phoneHelper.get(PhoneHelper.SIZE));
		text = (TextView) view.findViewById(R.id.c_pixels);
		text.setText(phoneHelper.get(PhoneHelper.DENSITY));
		text = (TextView) view.findViewById(R.id.c_apps);
		text.setText(phoneHelper.get(PhoneHelper.APP_COUNT) + "");

	}

	public String booleanToString(boolean value) {
		return (value) ? "ON" : "OFF";
	}

}

