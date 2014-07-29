package in.ceeq.fragments;

import in.ceeq.R;
import in.ceeq.actions.Phone;
import in.ceeq.helpers.PreferencesHelper;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyDeviceFragment extends Fragment {
	private Phone phoneHelper;
	private PreferencesHelper preferencesHelper;
	private Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container,
				false);
		context = getActivity();
		preferencesHelper = PreferencesHelper.getInstance(getActivity());
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
		text.setText(Phone.get(Phone.SIM_ID, context));
		text = (TextView) view.findViewById(R.id.c_imsi);
		text.setText(Phone.get(Phone.IMSI, context));
		text = (TextView) view.findViewById(R.id.c_iemi);
		text.setText(Phone.get(Phone.IEMI, context));
		text = (TextView) view.findViewById(R.id.c_gps);
		text.setText(booleanToString(Phone.enabled(Phone.GPS, context)));
		text = (TextView) view.findViewById(R.id.c_admin);
		text.setText(booleanToString(preferencesHelper
				.getBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS)));
		text = (TextView) view.findViewById(R.id.c_operator);
		text.setText(Phone.get(Phone.OPERATOR, context));
		text = (TextView) view.findViewById(R.id.c_size);
		text.setText(Phone.get(Phone.SIZE, context));
		text = (TextView) view.findViewById(R.id.c_pixels);
		text.setText(Phone.get(Phone.DENSITY, context));
		text = (TextView) view.findViewById(R.id.c_apps);
		text.setText(Phone.get(Phone.APP_COUNT, context) + "");

	}

	public String booleanToString(boolean value) {
		return (value) ? "ON" : "OFF";
	}

}

