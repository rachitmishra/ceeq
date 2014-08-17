package in.ceeq.home;

import in.ceeq.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DrawerAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;

	public static DrawerAdapter getInstance(LayoutInflater layoutInflater) {
		return new DrawerAdapter(layoutInflater);
	}

	public DrawerAdapter(LayoutInflater layoutInflater) {
		this.layoutInflater = layoutInflater;
	}

	@Override
	public int getCount() {
		return 12;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public static class ViewHolder {
		public static TextView headerTextView;
		public static TextView innerTextView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		switch (position) {
		case 1:
		case 5:
			convertView = layoutInflater.inflate(R.layout.drawer_action_header, null);
			ViewHolder.headerTextView = (TextView) convertView.findViewById(R.id.drawer_list_header);
			ViewHolder.headerTextView.setText(getText(position));
			break;
		default:
			convertView =  layoutInflater.inflate(R.layout.drawer_action_inner, null);
			ViewHolder.innerTextView = (TextView) convertView.findViewById(R.id.drawer_list_text);
			ViewHolder.innerTextView.setText(getText(position));
			ViewHolder.innerTextView.setCompoundDrawablesWithIntrinsicBounds(getInnerImage(position), 0, 0, 0);
			break;
		}
		return convertView;
	}

	public int getText(int position) {
		switch (position) {
		case 0:
			return R.string.my_device;
		case 1:
			return R.string.navigate;
		case 2:
			return R.string.tab_home;
		case 3:
			return R.string.tab_backup;
		case 4:
			return R.string.tab_security;
		case 5:
			return R.string.more;
		case 6:
			return R.string.privacy;
		case 7:
			return R.string.menu_feedback;
		case 8:
			return R.string.about;
		case 9:
			return R.string.rate;
		case 10:
			return R.string.settings;
		case 11:
			return R.string.help;
		case 12:
			return R.string.share;
		default:
			return R.string.rate;
		}
	}

	public int getInnerImage(int position) {
		switch (position) {
		case 0:
			return R.drawable.ic_stat_my_device;
		case 2:
			return R.drawable.ic_stat_home;
		case 3:
			return R.drawable.ic_stat_storage;
		case 4:
			return R.drawable.ic_stat_security;
		case 6:
			return R.drawable.ic_stat_privacy;
		case 7:
			return R.drawable.ic_stat_content_email;
		case 8:
			return R.drawable.ic_stat_action_about;
		case 9:
			return R.drawable.ic_stat_rating_important;
		case 10:
			return R.drawable.ic_stat_settings;
		case 11:
			return R.drawable.ic_stat_help;
		case 12:
			return R.drawable.ic_stat_share;
		default:
			return R.drawable.ic_stat_rating_important;
		}
	}
}