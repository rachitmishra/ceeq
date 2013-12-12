/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.activities;

import in.ceeq.R;
import in.ceeq.helpers.FilesHelper;
import in.ceeq.helpers.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewBackups extends Activity {

	public static final int CONTACT_FILE = 1;
	public static final int MESSAGE_FILE = 2;
	public static final int CALLS_FILE = 3;
	public static final int DICTIONARY_FILE = 4;

	private HashMap<String, ArrayList<String>> fileProps;
	private ArrayList<String> fileNames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_backups);
		setupActionbar();
		setupHelpers();
		setupFileGrid();
	}

	private void setupActionbar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
	}

	private FilesHelper filesHelper;

	private void setupHelpers() {
		filesHelper = new FilesHelper(this);
	}

	private GridView gridView;
	private GridAdapter gridViewAdapter;

	private void setupFileGrid() {
		if (filesHelper.haveBackupFiles()) {
			findViewById(R.id.help01).setVisibility(TextView.GONE);
		} else {
			findViewById(R.id.gridView).setVisibility(GridView.GONE);
			findViewById(R.id.help02).setVisibility(TextView.GONE);
			Toast.makeText(this, "Sorry, No backup files found.",
					Toast.LENGTH_SHORT).show();
		}
		gridView = (GridView) findViewById(R.id.gridView);
		gridViewAdapter = new GridAdapter();
		gridView.setAdapter(gridViewAdapter);
		gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		gridView.setMultiChoiceModeListener(new SelectListener());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class GridAdapter extends BaseAdapter {
		public LayoutInflater inflater;

		public GridAdapter() {
			if (filesHelper.haveBackupFiles()) {
				fileNames = new ArrayList<String>(filesHelper.getFileNames(
						filesHelper.getFiles(FilesHelper.BACKUP_PATH)).keySet());
				fileProps = filesHelper.getFileNames(filesHelper
						.getFiles(FilesHelper.BACKUP_PATH));
				this.inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
		}

		public int getCount() {
			if (fileNames != null)
				return fileNames.size();
			else
				return 0;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView displayName;
			ImageView displayImage;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.grid_columns, null);
			}
			displayImage = (ImageView) convertView
					.findViewById(R.id.gDisplayImage);
			displayName = (TextView) convertView
					.findViewById(R.id.gDisplayName);

			if (fileNames.get(position).contains("contact")) {
				displayImage.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_ceeq_contact));
				displayName.setTag(R.string.file_name, fileNames.get(position));
				displayName.setTag(R.string.file_type, CONTACT_FILE);
			}

			else if (fileNames.get(position).contains("message")) {
				displayImage.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_ceeq_message));
				displayName.setTag(R.string.file_name, fileNames.get(position));
				displayName.setTag(R.string.file_type, MESSAGE_FILE);
			}

			else if (fileNames.get(position).contains("call")) {
				displayImage.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_ceeq_calls));
				displayName.setTag(R.string.file_name, fileNames.get(position));
				displayName.setTag(R.string.file_type, CALLS_FILE);
			}

			else if (fileNames.get(position).contains("dictionary")) {
				displayImage.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_ceeq_dictionary));
				displayName.setTag(R.string.file_name, fileNames.get(position));
				displayName.setTag(R.string.file_type, DICTIONARY_FILE);

			}
			displayName.setTextSize(12);
			displayName.setTextColor(getResources().getColor(
					R.color.text_medium));
			displayName.setText(fileNames.get(position)
					.replaceAll("[A-Za-z_]", "").substring(0, 8)
					.replaceAll("-", "."));

			return convertView;
		}
	}

	public class SelectListener implements MultiChoiceModeListener {

		private ArrayList<Integer> selectedFiles = new ArrayList<Integer>();

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			if (checked) {
				gridView.getChildAt(position).setBackgroundColor(
						getResources().getColor(R.color.light_blue));
				selectedFiles.add((Integer) position);
			} else {
				gridView.getChildAt(position).setBackgroundColor(
						getResources().getColor(R.color.background));
				selectedFiles.remove((Integer) position);
			}
		}

		public int getTotalSize(ArrayList<Integer> selectedFiles) {
			int total = 0;
			for (Integer i : selectedFiles)
				total += Integer.parseInt(fileProps.get(
						gridView.getChildAt(selectedFiles.get(i))
								.findViewById(R.id.gDisplayName)
								.getTag(R.string.file_name).toString()).get(1));
			return total;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {

			case R.id.restore:
				String fileName;
				int fileType;
				try {
					for (int i = 0; i < selectedFiles.size(); i++) {
						fileType = (Integer) gridView.getChildAt(
								selectedFiles.get(i))
								.getTag(R.string.file_type);
						fileName = gridView.getChildAt(selectedFiles.get(i))
								.getTag(R.string.file_name).toString();
						switch (fileType) {
						case 1:
							Logger.d(fileName);
							break;
						case 2:
							break;
						case 3:
							break;
						case 4:
							break;
						}
					}

					if (selectedFiles.size() == 1)
						Toast.makeText(ViewBackups.this,
								selectedFiles.size() + " file restored.",
								Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(ViewBackups.this,
								selectedFiles.size() + " files restored.",
								Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					selectedFiles.clear();
					if (filesHelper.haveBackupFiles()) {
						fileNames = new ArrayList<String>(
								filesHelper
										.getFileNames(
												filesHelper
														.getFiles(FilesHelper.BACKUP_PATH))
										.keySet());
					}
					gridViewAdapter.notifyDataSetChanged();
					gridView.setAdapter(gridViewAdapter);
				}
				return true;

			case R.id.delete:
				String[] deleteFiles = new String[selectedFiles.size()];
				try {
					for (int i = 0; i < selectedFiles.size(); i++) {
						deleteFiles[i] = gridView
								.getChildAt(selectedFiles.get(i))
								.findViewById(R.id.gDisplayName)
								.getTag(R.string.file_name).toString();
					}
					filesHelper
							.deleteFile(FilesHelper.BACKUP_PATH, deleteFiles);
					if (selectedFiles.size() == 1)
						Toast.makeText(ViewBackups.this,
								selectedFiles.size() + " file deleted.",
								Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(ViewBackups.this,
								selectedFiles.size() + " files deleted.",
								Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					selectedFiles.clear();
					if (filesHelper.haveBackupFiles()) {
						fileNames = new ArrayList<String>(
								filesHelper
										.getFileNames(
												filesHelper
														.getFiles(FilesHelper.BACKUP_PATH))
										.keySet());
					} else {
						findViewById(R.id.help02).setVisibility(TextView.GONE);
						gridView.setVisibility(TextView.GONE);
						findViewById(R.id.help01).setVisibility(
								TextView.VISIBLE);
					}
					gridViewAdapter.notifyDataSetChanged();
					gridView.setAdapter(gridViewAdapter);
				}
				return true;
			case R.id.props:
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ViewBackups.this);
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v;
				TextView text;
				builder.setTitle("Properties");
				if (selectedFiles.size() > 1) {
					v = inflater.inflate(R.layout.dialog_selection_details,
							null);
					text = (TextView) v.findViewById(R.id.vfileTotalSize);
					text.setText(getTotalSize(selectedFiles) + "KB");
					builder.setView(v)
							.setNegativeButton(R.string.close,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									}).create().show();
				} else {
					v = inflater.inflate(R.layout.dialog_file_details, null);
					text = (TextView) v.findViewById(R.id.vfileName);
					if (!selectedFiles.isEmpty()) {
						text.setText(gridView.getChildAt(selectedFiles.get(0))
								.findViewById(R.id.gDisplayName)
								.getTag(R.string.file_name).toString());
						text = (TextView) v.findViewById(R.id.vfileType);
						text.setText(fileProps.get(
								gridView.getChildAt(selectedFiles.get(0))
										.findViewById(R.id.gDisplayName)
										.getTag(R.string.file_name).toString())
								.get(0));
						text = (TextView) v.findViewById(R.id.vfileDate);
						text.setText(fileProps.get(
								gridView.getChildAt(selectedFiles.get(0))
										.findViewById(R.id.gDisplayName)
										.getTag(R.string.file_name).toString())
								.get(2));
						text = (TextView) v.findViewById(R.id.vfileSize);
						text.setText(fileProps.get(
								gridView.getChildAt(selectedFiles.get(0))
										.findViewById(R.id.gDisplayName)
										.getTag(R.string.file_name).toString())
								.get(1)
								+ "KB");

						builder.setView(v)
								.setNegativeButton(R.string.close,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										}).create().show();
					}
					return true;
				}
			default:
				return false;
			}
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.display, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			for (int i = 0; i < selectedFiles.size(); i++) {
				gridView.getChildAt(selectedFiles.get(i)).setBackgroundColor(
						getResources().getColor(R.color.background));
			}
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	}

}
