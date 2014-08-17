package in.ceeq.explorer;

import in.ceeq.R;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
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
import android.widget.TextView;

public class ExplorerGridAdapter extends BaseAdapter implements MultiChoiceModeListener
{
	public static final int CONTACT_FILE = 1;
	public static final int MESSAGE_FILE = 2;
	public static final int CALLS_FILE = 3;
	public static final int DICTIONARY_FILE = 4;
	private LayoutInflater inflater;
	private HashMap<String, ArrayList<String>> fileProperties;
	private ArrayList<String> fileNames;
	private ArrayList<Integer> selectedFiles = new ArrayList<Integer>();
	private GridView gridView;
	
	public static ExplorerGridAdapter getInstance(Context context, GridView gridView) {
		return new ExplorerGridAdapter(context, gridView);
	}

	public ExplorerGridAdapter(Context context, GridView gridView) {
		// if (filesHelper.haveBackupFiles()) {
		// fileNames = new
		// ArrayList<String>(filesHelper.getFileNames(filesHelper.getFiles(FilesHelper.BACKUP_PATH)).keySet());
		// fileProps = filesHelper.getFileNames(filesHelper
		// .getFiles(FilesHelper.BACKUP_PATH));
		this.gridView = gridView;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// }
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
		TextView displayImage;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.grid_columns, null);
		}
		displayImage = (TextView) convertView.findViewById(R.id.fileIcon);

		if (fileNames.get(position).contains("contact")) {
			displayImage.setText("C");
			displayImage.setTag(R.string.file_name, fileNames.get(position));
			displayImage.setTag(R.string.file_type, CONTACT_FILE);
		}

		else if (fileNames.get(position).contains("message")) {
			displayImage.setText("M");
			displayImage.setTag(R.string.file_name, fileNames.get(position));
			displayImage.setTag(R.string.file_type, MESSAGE_FILE);
		}

		else if (fileNames.get(position).contains("call")) {
			displayImage.setText("L");
			displayImage.setTag(R.string.file_name, fileNames.get(position));
			displayImage.setTag(R.string.file_type, CALLS_FILE);
		}

		else if (fileNames.get(position).contains("dictionary")) {
			displayImage.setText("D");
			displayImage.setTag(R.string.file_name, fileNames.get(position));
			displayImage.setTag(R.string.file_type, DICTIONARY_FILE);

		}
		return convertView;
	}

	 @Override
	 public void onItemCheckedStateChanged(ActionMode mode, int position,
	 long id, boolean checked) {
	 if (checked) {
	 // gridView.getChildAt(position).setBackgroundColor(
	 // getResources().getColor(R.color.light_blue));
	 // selectedFiles.add((Integer) position);
	 } else {
	 // gridView.getChildAt(position).setBackgroundColor(
	 // getResources().getColor(R.color.background));
	 // selectedFiles.remove((Integer) position);
	 }
	 }

	// public int getTotalSize(ArrayList<Integer> selectedFiles) {
	// int total = 0;
	// for (Integer i : selectedFiles)
	// // total += Integer.parseInt(fileProps.get(
	// // gridView.getChildAt(selectedFiles.get(i))
	// // .findViewById(R.id.gDisplayName)
	// // .getTag(R.string.file_name).toString()).get(1));
	// return total;
	// }

	 @Override
	 public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//	 switch (item.getItemId()) {
//	
//	 case R.id.restore:
//	 String fileName;
//	 int fileType;
//	 try {
//	 // for (int i = 0; i < selectedFiles.size(); i++) {
//	 // fileType = (Integer) gridView.getChildAt(
//	 // selectedFiles.get(i))
//	 // .getTag(R.string.file_type);
//	 // fileName = gridView.getChildAt(selectedFiles.get(i))
//	 // .getTag(R.string.file_name).toString();
//	 // switch (fileType) {
//	 // case 1:
//	 // Logger.d(fileName);
//	 // break;
//	 // case 2:
//	 // break;
//	 // case 3:
//	 // break;
//	 // case 4:
//	 // break;
//	 }
		 return false;
	 }
	//
	// if (selectedFiles.size() == 1)
	// Toast.makeText(Explorer.this,
	// selectedFiles.size() + " file restored.",
	// Toast.LENGTH_SHORT).show();
	// else
	// Toast.makeText(Explorer.this,
	// selectedFiles.size() + " files restored.",
	// Toast.LENGTH_SHORT).show();
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// selectedFiles.clear();
	// if (filesHelper.haveBackupFiles()) {
	// fileNames = new ArrayList<String>(
	// filesHelper
	// .getFileNames(
	// filesHelper
	// .getFiles(FilesHelper.BACKUP_PATH))
	// .keySet());
	// }
	// gridViewAdapter.notifyDataSetChanged();
	// gridView.setAdapter(gridViewAdapter);
	// }
	// return true;
	//
	// case R.id.delete:
	// String[] deleteFiles = new String[selectedFiles.size()];
	// try {
	// for (int i = 0; i < selectedFiles.size(); i++) {
	// deleteFiles[i] = gridView
	// .getChildAt(selectedFiles.get(i))
	// .findViewById(R.id.gDisplayName)
	// .getTag(R.string.file_name).toString();
	// }
	// filesHelper
	// .deleteFile(FilesHelper.BACKUP_PATH, deleteFiles);
	// if (selectedFiles.size() == 1)
	// Toast.makeText(Explorer.this,
	// selectedFiles.size() + " file deleted.",
	// Toast.LENGTH_SHORT).show();
	// else
	// Toast.makeText(Explorer.this,
	// selectedFiles.size() + " files deleted.",
	// Toast.LENGTH_SHORT).show();
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// selectedFiles.clear();
	// if (filesHelper.haveBackupFiles()) {
	// fileNames = new ArrayList<String>(
	// filesHelper
	// .getFileNames(
	// filesHelper
	// .getFiles(FilesHelper.BACKUP_PATH))
	// .keySet());
	// } else {
	// findViewById(R.id.help02).setVisibility(TextView.GONE);
	// gridView.setVisibility(TextView.GONE);
	// findViewById(R.id.help01).setVisibility(
	// TextView.VISIBLE);
	// }
	// gridViewAdapter.notifyDataSetChanged();
	// gridView.setAdapter(gridViewAdapter);
	// }
	// return true;
	// case R.id.props:
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// Explorer.this);
	// LayoutInflater inflater = (LayoutInflater)
	// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// View v;
	// TextView text;
	// builder.setTitle("Properties");
	// if (selectedFiles.size() > 1) {
	// v = inflater.inflate(R.layout.dialog_selection_details,
	// null);
	// text = (TextView) v.findViewById(R.id.vfileTotalSize);
	// text.setText(getTotalSize(selectedFiles) + "KB");
	// builder.setView(v)
	// .setNegativeButton(R.string.close,
	// new DialogInterface.OnClickListener() {
	// public void onClick(
	// DialogInterface dialog, int id) {
	// dialog.cancel();
	// }
	// }).create().show();
	// } else {
	// v = inflater.inflate(R.layout.dialog_file_details, null);
	// text = (TextView) v.findViewById(R.id.vfileName);
	// if (!selectedFiles.isEmpty()) {
	// text.setText(gridView.getChildAt(selectedFiles.get(0))
	// .findViewById(R.id.gDisplayName)
	// .getTag(R.string.file_name).toString());
	// text = (TextView) v.findViewById(R.id.vfileType);
	// text.setText(fileProps.get(
	// gridView.getChildAt(selectedFiles.get(0))
	// .findViewById(R.id.gDisplayName)
	// .getTag(R.string.file_name).toString())
	// .get(0));
	// text = (TextView) v.findViewById(R.id.vfileDate);
	// text.setText(fileProps.get(
	// gridView.getChildAt(selectedFiles.get(0))
	// .findViewById(R.id.gDisplayName)
	// .getTag(R.string.file_name).toString())
	// .get(2));
	// text = (TextView) v.findViewById(R.id.vfileSize);
	// text.setText(fileProps.get(
	// gridView.getChildAt(selectedFiles.get(0))
	// .findViewById(R.id.gDisplayName)
	// .getTag(R.string.file_name).toString())
	// .get(1)
	// + "KB");
	//
	// builder.setView(v)
	// .setNegativeButton(R.string.close,
	// new DialogInterface.OnClickListener() {
	// public void onClick(
	// DialogInterface dialog,
	// int id) {
	// dialog.cancel();
	// }
	// }).create().show();
	// }
	// return true;
	// }
	// default:
	// return false;
	// }
	// }
	
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.display, menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
//		for (int i = 0; i < selectedFiles.size(); i++) {
//			gridView.getChildAt(selectedFiles.get(i)).setBackgroundColor(getResources().getColor(R.color.background));
//		}
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}
}