package in.ceeq.home.about;

import in.ceeq.R;
import in.ceeq.commons.Utils;
import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutDeviceFragment extends Fragment {
	private Context context;

	public static AboutDeviceFragment getInstance() {
		return new AboutDeviceFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);
		setActionBar();
		setupData(view);
		return view;
	}
	
	private void setActionBar(){
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.blue));
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("About Device");
	}

	public void setupData(View view) {
		TextView text = (TextView) view.findViewById(R.id.c_account);
		text.setText(Utils.getStringPrefs(context, Utils.ACCOUNT_USER_ID));
		text = (TextView) view.findViewById(R.id.c_name);
		text.setText(Utils.getStringPrefs(context, Utils.ACCOUNT_USER_NAME));
		text = (TextView) view.findViewById(R.id.c_sim);
		text.setText(Utils.getPhoneData(context, Utils.SIM_ID));
		text = (TextView) view.findViewById(R.id.c_imsi);
		text.setText(Utils.getPhoneData(context, Utils.IMSI));
		text = (TextView) view.findViewById(R.id.c_iemi);
		text.setText(Utils.getPhoneData(context, Utils.IEMI));
		text = (TextView) view.findViewById(R.id.c_gps);
		text.setText(booleanToString(Utils.enabled(Utils.GPS, context)));
		text = (TextView) view.findViewById(R.id.c_admin);
		text.setText(booleanToString(Utils.getBooleanPrefs(context, Utils.DEVICE_ADMIN_STATUS)));
		text = (TextView) view.findViewById(R.id.c_operator);
		text.setText(Utils.getPhoneData(context, Utils.OPERATOR));
		text = (TextView) view.findViewById(R.id.c_size);
		text.setText(Utils.getPhoneData(context, Utils.SIZE));
		text = (TextView) view.findViewById(R.id.c_pixels);
		text.setText(Utils.getPhoneData(context, Utils.DENSITY));
		text = (TextView) view.findViewById(R.id.c_apps);
		text.setText(Utils.getPhoneData(context, Utils.APP_COUNT) + "");

	}

	public String booleanToString(boolean value) {
		return (value) ? "ON" : "OFF";
	}

}
