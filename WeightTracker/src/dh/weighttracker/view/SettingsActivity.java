package dh.weighttracker.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import dh.weighttracker.R;
import dh.weighttracker.model.UserPrefernce;
import dh.weighttracker.model.UserPrefernce.WeightUnitOption;
import dh.weighttracker.model.UserPrefernce.RecipientEmailOption;

public class SettingsActivity extends Activity{
	
	static final int RECIPIENT_EMAIL_SELECT_DIALOG_ID = 0;
	
	static final int SELECT_CONTACT_ACTIVITY_REQUEST_CODE = 0;
	static final int SELECT_EMAIL_REQUEST_CODE = 1;
	static final int ADD_CONTACT_ACTIVITY_REQUEST_CODE = 2;
	
	
	private EditText mUserNameEditText;
	private EditText mUserEmailEditText;
	private EditText mRecipientEmailEditText;
	private Button mRecipientEmailOptionsButton;
	private RadioGroup mWeightDisplayOptionRadioGroup;
	
	//options for recipient select dialog
	private String[] recipientEmailOptions;
	
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        recipientEmailOptions = getResources().getStringArray(R.array.recipient_email_options);
        
    	setContentView(R.layout.settings);
        
    	//fetch handles to UI elements
    	mUserNameEditText = (EditText)findViewById(R.id.settings_userName);
    	mUserEmailEditText = (EditText)findViewById(R.id.settings_userEmail);
    	mRecipientEmailEditText = (EditText)findViewById(R.id.settings_recipientEmail);
    	mRecipientEmailOptionsButton = (Button) findViewById(R.id.settings_recipientEmail_select_button);
    	mRecipientEmailOptionsButton.setOnClickListener(mRecipientEmailSelectClickListener);
    	mWeightDisplayOptionRadioGroup = (RadioGroup)findViewById(R.id.settings_weight_display_option);
        
    	populateSettings();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	//TODO: decide to always show keyboard or not
//    	mUserNameEditText.requestFocus();
    	    	
//    	mUserNameEditText.postDelayed(new Runnable() {
//			public void run() {
//				InputMethodManager keyboard = (InputMethodManager)
//				getSystemService(Context.INPUT_METHOD_SERVICE);
//
//				keyboard.showSoftInput(mUserNameEditText, 0);
//			}
//		},200);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	saveSettings();
    }
    
    private void populateSettings(){
    	UserPrefernce userData = UserPrefernce.getInstance();
    	mUserNameEditText.setText(userData.getUserName());
    	mUserEmailEditText.setText(userData.getUserEmail());
    	mRecipientEmailEditText.setText(userData.getRecipientEmail());
    	UserPrefernce.WeightUnitOption option = userData.getWeightUnitOption();
    	mWeightDisplayOptionRadioGroup.check(option.getRadioButtonID());
    }
    
    public void saveSettings(){
    	UserPrefernce userData = UserPrefernce.getInstance();
    	userData.setSetuped(true);
    	userData.setUserName(mUserNameEditText.getText().toString());
    	userData.setUserEmail(mUserEmailEditText.getText().toString());
    	userData.setRecipientEmail(mRecipientEmailEditText.getText().toString());
    	WeightUnitOption weightOption = UserPrefernce.WeightUnitOption.getSelectedOptionByID(mWeightDisplayOptionRadioGroup.getCheckedRadioButtonId());
    	userData.setWeightUnitOption(weightOption);
    }
    
    //display contact select options: self, choose contact, add contact
    private OnClickListener mRecipientEmailSelectClickListener =
    	new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(RECIPIENT_EMAIL_SELECT_DIALOG_ID);
				
			}
		};
		
	//build recipient email select dialog and define callback for dialog click
	private AlertDialog buildRecipientEmailSelectDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Please select a recipient email")
			.setCancelable(true)
			.setItems(recipientEmailOptions, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	UserPrefernce userData = UserPrefernce.getInstance();
			    	RecipientEmailOption[] options = RecipientEmailOption.values();
			    	int optionIdx = item;
			    	if(optionIdx >= options.length){
			    		optionIdx = options.length-1;
			    	}
			    	RecipientEmailOption selectedOption = options[optionIdx];
			    	userData.setRecipientOption(selectedOption);
			    	
			    	//launch contact picker or copy user email as recipient email
			    	switch(selectedOption){
				    	case SELF:
				    		mRecipientEmailEditText.setText(mUserEmailEditText.getText());
				    		break;
				    	case CONTACT:
				    		Intent pickContactIntent = new Intent(Intent.ACTION_PICK);
							pickContactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
					    	startActivityForResult(pickContactIntent, SELECT_CONTACT_ACTIVITY_REQUEST_CODE);
					    	break;
				    	case ADD_CONTACT:
				    		Intent intent = new Intent(Intent.ACTION_INSERT);
				    		intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
				    		startActivityForResult(intent, ADD_CONTACT_ACTIVITY_REQUEST_CODE);
				    		break;
			    	}
			    }
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	           }
	       });
		return builder.create();
	}
	
	
	@Override
	//create our dialogs
    protected Dialog onCreateDialog(int id) {
    	//create dialogs and override window animation
        switch (id) {
	        case RECIPIENT_EMAIL_SELECT_DIALOG_ID:
	        	AlertDialog rDialog = buildRecipientEmailSelectDialog();
	        	rDialog.getWindow().getAttributes().windowAnimations = R.style.PickerDialogAnimation;
	            return rDialog;
	    }
        
        return null;
    }
    
    
    @Override
    //callback for when our launched activities return
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	//use emails chooser activity to select a recipient email
    	switch(requestCode) {
    		case(SELECT_CONTACT_ACTIVITY_REQUEST_CODE):
    			if(resultCode == Activity.RESULT_OK) {
    				//pass contact picker data to email chooser activity for selection
    				Intent chooseNumberIntent = new Intent(this, EmailsChooserActivity.class);
    				chooseNumberIntent.setData(data.getData());
    				startActivityForResult(chooseNumberIntent, SELECT_EMAIL_REQUEST_CODE);
    			}
    		break;
    		case(SELECT_EMAIL_REQUEST_CODE):
    			if(resultCode == Activity.RESULT_OK) {
					String email = data.getStringExtra(EmailsChooserActivity.EMAIL_DATA_ID);
					mRecipientEmailEditText.setText(email);
    			}
			break;
    		case ADD_CONTACT_ACTIVITY_REQUEST_CODE:
    			if(resultCode == Activity.RESULT_OK) {
        			
        			//pass add contact's data to email chooser activity for selection
    				Intent chooseNumberIntent = new Intent(this, EmailsChooserActivity.class);
    				chooseNumberIntent.setData(data.getData());
    				startActivityForResult(chooseNumberIntent, SELECT_EMAIL_REQUEST_CODE);
    			}
    			
			break;
    	}
    }
    
}
