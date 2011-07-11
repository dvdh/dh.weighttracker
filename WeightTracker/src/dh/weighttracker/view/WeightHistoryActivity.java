package dh.weighttracker.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.ListActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import dh.weighttracker.R;
import dh.weighttracker.WeightTrackerActivity.Tab;
import dh.weighttracker.database.WeightDatabaseHelper;
import dh.weighttracker.database.WeightDatabaseHelper.Column;
import dh.weighttracker.model.UserPrefernce;

public class WeightHistoryActivity extends ListActivity{
	
	//db helper
	private WeightDatabaseHelper mdbHelper;
	//cursor
	private Cursor mCursor;
	
	//UI element
	private TextView mWeightUnit;
	private ListView mWeightHistoryListView;
	
	//listview adapter to bind cursor
	private SimpleCursorAdapter mAdapter;
	
	//formatter for adapter view binder
	private SimpleDateFormat mFormat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		setContentView(R.layout.weight_history);
		
		LinearLayout titleLLayout = (LinearLayout)findViewById(R.id.weight_history_title);
		mWeightUnit = (TextView)titleLLayout.findViewById(R.id.weight_history_entry_weight);
		mWeightHistoryListView = (ListView)findViewById(android.R.id.list);
		
		//add on click listerner to weight history item rows
		mWeightHistoryListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//launch weight entry activity to modify clicked item
				Intent intent = getIntent();
				Bundle bundle = new Bundle();
				//set modify mode to intent
				WeightEntryActivity.Mode.MODIFY_ENTRY.storeToBundle(bundle);
				//set db row id of entry we want modified
				bundle.putLong(WeightDatabaseHelper.Column.ROW_ID.colName, id);
				intent.putExtras(bundle);
				setIntent(intent);
				//setResult(Activity.RESULT_OK, intent);
				//finish current activity or pass result back to tabhost
				Activity a = getParent();
				//we are in tabhost mode, use tabactivity to pass our intent
				if(a != null && a instanceof TabActivity){
					TabActivity ta = (TabActivity)a;
					ta.setIntent(intent);
					ta.getTabHost().setCurrentTab(Tab.WEIGHT_ENTRY.ordinal());
				}
				else{
					//TODO: handle non tabhost mode
//					finish();
				}
			}
		});
		
		
		
		//init our date formatter for adapter view binder
		mFormat = new SimpleDateFormat("MMM-dd-yyyy");
		
		//initialise db helper
        mdbHelper = WeightDatabaseHelper.getInstance(this);
        
        mdbHelper.open();
        //init adapter and view binder
        loadData();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mCursor.requery();
		//update title weight unit
		mWeightUnit.setText(UserPrefernce.getInstance().getWeightUnitOption().getShortUnit());
	}
	
	@Override
	protected void onStop() {
		super.onPause();
	}
	
	private void loadData() {
		mCursor = mdbHelper.getAllWeightEntries();
		startManagingCursor(mCursor);
		
		String[] from = new String[] {Column.ENTRY_TIME.colName, Column.WEIGHT_ENTRY.colName};
		int[] to = new int[] {R.id.weight_history_entry_date, R.id.weight_history_entry_weight};
		
		//adapter to bind cursor to row
		mAdapter = new SimpleCursorAdapter(this,
				R.layout.weight_history_row, mCursor, from, to);
		mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			int entryTimeIdx = mCursor.getColumnIndex(Column.ENTRY_TIME.colName);
			int weightEntryIdx = mCursor.getColumnIndex(Column.WEIGHT_ENTRY.colName);
		    @Override
		    public boolean setViewValue(View view, Cursor cursor, int column) {
		    	TextView tv = (TextView) view;
		    	if(column == entryTimeIdx){
		    		//display time
		            long time = cursor.getLong(column);
		            Calendar c = Calendar.getInstance();
		            c.setTimeInMillis(time);
		            tv.setText(mFormat.format(c.getTime()));
		            return true;
		    	}
		    	else if(column == weightEntryIdx){
		    		//convert weight from pounds to kg if necessary
		    		float weight = cursor.getFloat(column);
		    		float converted = UserPrefernce.getInstance().getWeightUnitOption().convertFromPoundIfRequired(weight);
		    		tv.setText(String.format("%.1f", converted));
		    		return true;
		    	}
		        return false;
		    }
		});
		setListAdapter(mAdapter);
	}
	
}
