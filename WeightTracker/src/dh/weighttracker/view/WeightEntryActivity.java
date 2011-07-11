package dh.weighttracker.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import dh.weighttracker.R;
import dh.weighttracker.WeightTrackerActivity.Tab;
import dh.weighttracker.database.WeightDatabaseHelper;
import dh.weighttracker.model.UserPrefernce;
import dh.weighttracker.model.WeightEntry;

public class WeightEntryActivity extends Activity{
	
	//dialog return IDs to distinguish return value
	static final int DATE_PICKER_DIALOG_ID = 0;
	static final int TIME_PICKER_DIALOG_ID = 1;
	
	//UI objects
	private TextView mTitleEntryTime;
	private Button mDateButton;
	private Button mTimeButton;
	private TextView mWeightEntryTitle;
	private EditText mWeightEntry;
	private Button mSaveButton;
	private Button mDeleteButton;
	
	//weight entry we are viewing and editing
	private WeightEntry entry;
	
	//date formatter for year-month-day button
	private SimpleDateFormat dateFormat;
	//date formatter for hour:minute button
	private SimpleDateFormat timeFormat;
	
	//db helper
	private WeightDatabaseHelper dbHelper;
	
	//modes of operation
	private Mode mMode;
	public enum Mode{
		NEW_ENTRY, MODIFY_ENTRY;
		
		//tag used to store to bundle
		private static final String TAG = "Mode";
		public void storeToBundle(Bundle bundle){
			bundle.putString(TAG, this.name());
		}
		//default mode if bundle is null is new entry
		public static Mode getFromBundle(Bundle bundle){
			if(bundle == null){
				return NEW_ENTRY;
			}
			String s = bundle.getString(TAG);
			Mode mode = Mode.valueOf(s);
			return mode;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//create our date formatters
		dateFormat = new SimpleDateFormat("MMM-dd-yyyy");
		timeFormat = new SimpleDateFormat("HH:mm");
		
		setContentView(R.layout.weight_entry);
		
		//fetch handlers to UI elements
		mTitleEntryTime = (TextView)findViewById(R.id.weight_entry_title_entryTime);
		mDateButton = (Button)findViewById(R.id.weight_entry_dateDisplay);
		mTimeButton = (Button)findViewById(R.id.weight_entry_timeDisplay);
		mWeightEntryTitle = (TextView)findViewById(R.id.weight_entry_weight_title);
		mWeightEntry = (EditText)findViewById(R.id.weight_entry_weight_editText);
		mSaveButton = (Button)findViewById(R.id.weight_entry_saveButton);
		mDeleteButton = (Button)findViewById(R.id.weight_entry_deleteButton);
		
		//add click listener to date display to display date picker dialog
		mDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_PICKER_DIALOG_ID);
            }
        });
		//add click listener to time display to display time picker dialog
		mTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_PICKER_DIALOG_ID);
            }
        });
		
		//select all text when user focuses on weight text
		mWeightEntry.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				EditText edit = (EditText)v;
				edit.selectAll();
			}
		});
		
		//add click listener to save button to save to db
		mSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//get entered weight and set to our weight entry
				float weight = 0;
				String weightText = mWeightEntry.getText().toString();
				if(!weightText.isEmpty()){
					weight = Float.valueOf(mWeightEntry.getText().toString());
				}
				entry.setWeight(weight, UserPrefernce.getInstance().getWeightUnitOption());
				//save weight entry to db
				entry.saveToDB(dbHelper);
				
				//modify UI as needed
				switch(mMode){
				case NEW_ENTRY:
					//once we save a new entry we are modifying it
					mMode = Mode.MODIFY_ENTRY;
					updateSaveDeleteButtonText(mMode);
					updateDisplay();
					break;
				case MODIFY_ENTRY:
					break;
				}
			}
		});
		
		//add delete button action to delete entries
		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//we can only delete in modify mode
				if(mMode != Mode.MODIFY_ENTRY){
					return;
				}
				boolean deleted = dbHelper.deleteWeightEntry(entry.getDBRowID());
				//set result code
				Intent intent = getIntent();
				if(deleted){
					setResult(Activity.RESULT_OK, intent);
				}
				else{
					//TODO: define what to pass back when delete failed
					setResult(Activity.RESULT_FIRST_USER, intent);
				}
				
				Activity parent = getParent();
				//we are in tabhost mode, change to weight history tab
				if(parent != null && parent instanceof TabActivity){
					TabActivity tabActivity = (TabActivity)parent;
					tabActivity.getTabHost().setCurrentTab(Tab.HISTORY.ordinal());
				}
				else{
					//TODO: handle non tabhost mode
				}
			}
		});
		
		//process intent to get our mode of operation and init UI
		processIntent();
		
		//init db helper
        dbHelper = WeightDatabaseHelper.getInstance(this);
        dbHelper.open();
	}
	
	private Mode processIntent(){;
		Intent intent;
		Bundle bundle;
		//get intent from tabhost if we reside in a tabhost
		//otherwise get our own intent
		Activity parent = getParent();
		if(parent != null && parent instanceof TabActivity){
			TabActivity tabActivity = (TabActivity)parent;
			intent = tabActivity.getIntent();
		}
		else{
			intent = getIntent();
		}
		
		bundle = intent.getExtras();
		mMode = Mode.getFromBundle(bundle);
		
		
		switch(mMode){
		case NEW_ENTRY:
			//create new entry with current date and time and 0 weight
			Calendar c = Calendar.getInstance();
			entry = new WeightEntry(c, 0, UserPrefernce.getInstance().getWeightUnitOption());
			break;
		case MODIFY_ENTRY:
			//get entry from db
			long id = bundle.getLong(WeightDatabaseHelper.Column.ROW_ID.colName);
			entry = WeightDatabaseHelper.getInstance(this).getWeightEntry(id);
			break;
		}
		updateSaveDeleteButtonText(mMode);
		
		//clear intent bundle for tabhost mode, since intent no longer modified as user switches tabs,
		//causing old bundle data to be used when user switches back to this tab
		Bundle nullBundle = null;
		intent.replaceExtras(nullBundle);
		
		return mMode;
	}
	
	private void updateSaveDeleteButtonText(Mode mode){
		switch(mode){
		case NEW_ENTRY:
			//cant delete a new entry
			mDeleteButton.setEnabled(false);
			//set button to save new entry text
			mSaveButton.setText(R.string.save);
			break;
		case MODIFY_ENTRY:
			//we can delete an existing entry
			mDeleteButton.setEnabled(true);
			//set button to update new entry text
			mSaveButton.setText(R.string.update);
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateDisplay();
	}
	
	//update UI from weight entry data
	private void updateDisplay() {
		//update title
		switch(mMode){
		case NEW_ENTRY:
			mTitleEntryTime.setText(R.string.new_entry_time);
			break;
		case MODIFY_ENTRY:
			mTitleEntryTime.setText(R.string.modify_entry_time);
			break;
		}
		
		//update weight unit
		switch(UserPrefernce.getInstance().getWeightUnitOption()){
		case KILOGRAM:
			mWeightEntryTitle.setText(R.string.weight_kilograms);
		case POUND:
			mWeightEntryTitle.setText(R.string.weight_pounds);
		}
		
		//update date and time
		Date date = entry.getTime().getTime();
        mDateButton.setText(dateFormat.format(date));
        mTimeButton.setText(timeFormat.format(date));
        //update weight
        float convertedWeight = UserPrefernce.getInstance().getWeightUnitOption().convertFromPoundIfRequired(entry.getWeightInPounds());
        mWeightEntry.setText(String.format("%.1f", convertedWeight));
    }
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		//need to manually set intent as new intent is only passed to this method
		setIntent(intent);
		processIntent();
	}
	
	//create our dialogs
    @Override
    protected Dialog onCreateDialog(int id) {
    	//create dialogs and specify window animation
        switch (id) {
	        case DATE_PICKER_DIALOG_ID:
	        	Calendar c = entry.getTime();
	        	int month = c.get(Calendar.MONTH);
	    		int day = c.get(Calendar.DAY_OF_MONTH);
	    		int year = c.get(Calendar.YEAR);
	        	DatePickerDialog dateDialog = new DatePickerDialog(this, mDateSetListener, year, month, day);
	        	dateDialog.getWindow().getAttributes().windowAnimations = R.style.PickerDialogAnimation;
	            return dateDialog;
		    case TIME_PICKER_DIALOG_ID:
		    	c = entry.getTime();
		    	int hour = c.get(Calendar.HOUR_OF_DAY);
		        int minute = c.get(Calendar.MINUTE);
		    	TimePickerDialog timeDialog = new TimePickerDialog(this, mTimeSetListener, hour, minute, true);
		    	timeDialog.getWindow().getAttributes().windowAnimations = R.style.PickerDialogAnimation;
		    	return timeDialog;
	    }
        return null;
    }
    
    //callback when set is pressed in date picker dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, 
                                  int monthOfYear, int dayOfMonth) {
            	Calendar c = entry.getTime();
            	c.set(Calendar.YEAR, year);
            	c.set(Calendar.MONTH, monthOfYear);	//month is 0 indexed
            	c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDisplay();
            }
        };
        
	//callback when set is pressed in time picker dialog
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
	    new TimePickerDialog.OnTimeSetListener() {
	        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	        	Calendar c = entry.getTime();
            	c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            	c.set(Calendar.MINUTE, minute);
	            updateDisplay();
	        }
	    };

}

