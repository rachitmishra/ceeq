/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.help;

import in.ceeq.R;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		setupActionbar();
		setupBugsense();
		createHelpQuestions();
		createHelpAnswers();
		setupHelplist();
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.blue));
	}

	private ExpandableListView helpList;
	private ExpandableListAdapter helpListAdapter;

	public void setupHelplist() {
		helpList = (ExpandableListView) findViewById(R.id.helpList);
		helpListAdapter = new ListAdapter(this, help_list, text_list);
		helpList.setAdapter(helpListAdapter);
	}

	public void setupBugsense() {
		BugSenseHandler.initAndStartSession(HelpActivity.this, "5996b3d9");
	}

	ArrayList<String> help_list, text_list;

	public void createHelpQuestions() {
		help_list = new ArrayList<String>();
		help_list.add("What is Ceeq ?");
		help_list.add("Why enable GPS ?");
		help_list.add("Why enable Device Administrator ?");
		help_list.add("What are Remote Commands ?");
		help_list.add("Why so many permissions ?");
		help_list.add("What is Protect Me ?");
		help_list.add("What is Online Sync ?");
		help_list.add("What is One Click Backup");
		help_list.add("What is Stealth Mode");
		help_list.add("What about my privacy & data ?");
		help_list.add("Contact the developer");
	}

	public void createHelpAnswers() {
		text_list = new ArrayList<String>();
		text_list.add(getString(R.string.help_note_7));
		text_list.add(getString(R.string.help_note_8));
		text_list.add(getString(R.string.help_note_9));
		text_list.add(getString(R.string.help_note_10));
		text_list.add(getString(R.string.help_note_11));
		text_list.add(getString(R.string.help_note_12));
		text_list.add(getString(R.string.help_note_13));
		text_list.add(getString(R.string.help_note_14));
		text_list.add(getString(R.string.help_note_15));
		text_list.add(getString(R.string.help_note_16));
		text_list.add(getString(R.string.help_note_17));

	}

	public class ListAdapter extends BaseExpandableListAdapter {

		public ArrayList<String> h_list, i_list = new ArrayList<String>();
		public LayoutInflater inflater;
		public Context context;

		public ListAdapter(Context context, ArrayList<String> h_list,
				ArrayList<String> i_list) {
			this.context = context;
			this.h_list = h_list;
			this.i_list = i_list;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		public View getChildView(int groupPosition, final int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView text = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_help_inner, null);
			}

			text = (TextView) convertView.findViewById(R.id.n_text);
			text.setText(text_list.get(groupPosition));
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			return h_list.size();
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
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_help_outer, null);
			}

			TextView header = (TextView) convertView
					.findViewById(R.id.n_header);
			header.setText(h_list.get(groupPosition));

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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionbar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowHomeEnabled(false);
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
