/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.explorer;

import in.ceeq.R;
import in.ceeq.commons.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

public class ExplorerActivity extends Activity {

	private HashMap<String, ArrayList<String>> fileProperties;
	private ArrayList<String> fileNames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_backups);
		setupActionbar();
		setupFileGrid();
	}

	private void setupActionbar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
	}
	
	private GridView gridView;
	private ExplorerGridAdapter gridManager;

	private void setupFileGrid() {
		if (Utils.haveBackupFiles(this)) {
			Toast.makeText(this, "Touch and hold a file for more options.", Toast.LENGTH_SHORT).show();
		}
		gridView = (GridView) findViewById(R.id.gridView);
		gridView.setEmptyView(getLayoutInflater().inflate(R.layout.grid_empty, null)); // set empty
		gridManager = ExplorerGridAdapter.getInstance(this, gridView);
		gridView.setAdapter(gridManager);
		gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		gridView.setMultiChoiceModeListener(gridManager);
	}
	
	private void getFiles(){
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
