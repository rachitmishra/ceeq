package in.ceeq.splash;

import in.ceeq.R;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ListAdapter extends BaseExpandableListAdapter {

		public ArrayList<String> helpList = new ArrayList<String>();
		public int notificationCounter;
		public LayoutInflater inflater;
		public Context context;
		public boolean status;

		public ListAdapter(Context context, ArrayList<String> h_list) {
			this.context = context;
			this.helpList = h_list;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			TextView text = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_help_inner, null);
			}

			text = (TextView) convertView.findViewById(R.id.n_text);
			text.setText(helpList.get(childPosition));
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(context, helpList.get(childPosition), Toast.LENGTH_SHORT).show();
				}
			});
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return helpList.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			return 1;
		}

		@Override
		public void onGroupCollapsed(int groupPosition) {
			super.onGroupCollapsed(groupPosition);
		}

		@Override
		public void onGroupExpanded(int groupPosition) {
			super.onGroupExpanded(groupPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_help_outer, null);
			}
			TextView header = (TextView) convertView.findViewById(R.id.n_header);
			header.setText(R.string.help);
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

	}