package in.ceeq.about;

import in.ceeq.R;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutApplicationFragment extends Fragment {

	public static AboutApplicationFragment getInstance() {
		return new AboutApplicationFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_about_app,
				container, false);
		setActionBar();
		return rootView;
	}
	
	private void setActionBar(){
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.blue));
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("About");
	}
}

