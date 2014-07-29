package in.ceeq.commons;

import in.ceeq.R;
import in.ceeq.fragments.AboutApplicationFragment;
import in.ceeq.fragments.BackupFragment;
import in.ceeq.fragments.HomeFragment;
import in.ceeq.fragments.MyDeviceFragment;
import in.ceeq.fragments.PrivacyFragment;
import in.ceeq.fragments.SecurityFragment;
import in.ceeq.helpers.PreferencesHelper;

import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DrawerManager extends BaseAdapter implements
		ListView.OnItemClickListener {

	private static final int DRAWER_HEADER_VIEW = 1;
	private static final int DRAWER_LIST_VIEW = 2;
	private static final int DRAWER_PLUS_VIEW = 3;
	private FragmentManager fragmentManager;
	private DrawerLayout drawerLayout;
	private ListView actionList;
	private LayoutInflater layoutInflater;
	private Context context;

	public static DrawerManager getInstance(Context context,
			FragmentManager fragmentManager, DrawerLayout drawerLayout,
			ListView actionList) {
		return new DrawerManager(context, fragmentManager, drawerLayout,
				actionList);
	}

	public DrawerManager(Context context, FragmentManager fragmentManager,
			DrawerLayout drawerLayout, ListView actionList) {
		this.fragmentManager = fragmentManager;
		this.drawerLayout = drawerLayout;
		this.actionList = actionList;
		this.layoutInflater = ((Activity) context).getLayoutInflater();
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

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		TextView header, innerText;
		ImageView innerImage;
		switch (position) {
		case 1:
		case 6:
			return convertView = getView(DRAWER_PLUS_VIEW);
		case 2:
		case 7:
			convertView = getView(DRAWER_HEADER_VIEW);
			header = (TextView) convertView
					.findViewById(R.id.drawer_list_header);
			header.setText(getHeaderText(position));
			return convertView;
		default:
			convertView = getView(DRAWER_LIST_VIEW);
			innerText = (TextView) convertView
					.findViewById(R.id.drawer_list_text);
			innerText.setText(getInnerText(position));
			innerImage = (ImageView) convertView
					.findViewById(R.id.drawer_list_icon);
			innerImage.setBackgroundResource(getInnerImage(position));
			return convertView;

		}

	}

	public View getView(int type) {
		int layout;
		switch (type) {
		case DRAWER_PLUS_VIEW:
			layout = R.layout.drawer_action_plus;
			break;
		case DRAWER_HEADER_VIEW:
			layout = R.layout.drawer_action_header;
			break;
		default:
			layout = R.layout.drawer_action_inner;
			break;
		}

		return layoutInflater.inflate(layout, null);
	}

	public int getInnerText(int position) {
		switch (position) {
		case 0:
			return R.string.my_device;
		case 3:
			return R.string.tab_home;
		case 4:
			return R.string.tab_backup;
		case 5:
			return R.string.tab_security;
		case 8:
			return R.string.privacy;
		case 9:
			return R.string.menu_feedback;
		case 10:
			return R.string.about;
		case 11:
			return R.string.rate;
		default:
			return R.string.rate;
		}
	}

	public int getHeaderText(int position) {
		switch (position) {
		case 2:
			return R.string.navigate;
		case 7:
			return R.string.more;
		default:
			return R.string.more;
		}
	}

	public int getInnerImage(int position) {
		switch (position) {
		case 0:
			return R.drawable.ic_stat_my_device;
		case 3:
			return R.drawable.ic_stat_home;
		case 4:
			return R.drawable.ic_stat_storage;
		case 5:
			return R.drawable.ic_stat_security;
		case 8:
			return R.drawable.ic_stat_privacy;
		case 9:
			return R.drawable.ic_stat_content_email;
		case 10:
			return R.drawable.ic_stat_action_about;
		case 11:
			return R.drawable.ic_stat_rating_important;
		default:
			return R.drawable.ic_stat_rating_important;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		selectItem(position);

	}

	private void selectItem(int position) {

		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new MyDeviceFragment();
			break;
		case 3:
			fragment = new HomeFragment();
			break;
		case 4:
			fragment = new BackupFragment();
			break;
		case 5:
			fragment = new SecurityFragment();
			break;
		case 8:
			fragment = new PrivacyFragment();
			break;
		case 9:
			sendSupportMail();
			break;
		case 10:
			fragment = new AboutApplicationFragment();
			break;
		case 11:
			share();
			break;
		default:
			actionList.setItemChecked(position, false);
			return;
		}

		if (fragment != null)
			fragmentManager.beginTransaction()
					.replace(R.id.container, fragment).commit();
		actionList.setItemChecked(position, true);
		drawerLayout.closeDrawer(Gravity.START);
	}

	public void sendSupportMail() {
		Intent emailIntent = new Intent(Intent.ACTION_SEND)
				.setType(HTTP.PLAIN_TEXT_TYPE);
		emailIntent
				.putExtra(Intent.EXTRA_EMAIL, new String[] { context
						.getString(R.string.ceeq_support_email) });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion/Bugs");
		emailIntent.putExtra(
				Intent.EXTRA_TEXT,
				"[Ceeq Support \n User: "
						+ PreferencesHelper.getInstance(context).getString(
								"accountName") + "]");
		context.startActivity(emailIntent);
	}

	public void share() {
		Intent rateIntent = new Intent(Intent.ACTION_VIEW).setData(Uri
				.parse(context.getString(R.string.ceeq_play_link)));
		context.startActivity(rateIntent);
	}
}