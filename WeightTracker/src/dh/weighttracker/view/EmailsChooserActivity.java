package dh.weighttracker.view;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class EmailsChooserActivity extends ListActivity {
	
	public static final String EMAIL_DATA_ID = "email";
	
	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//get data from starting intent
		Intent startingIntent = getIntent();
		mUri = startingIntent.getData();
		
		//fetch contact name
		String contactName = "";
		Cursor c = managedQuery(mUri, null, null, null, null);
		if(c.moveToFirst()) {
			 contactName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
		}
		
	    String[] emails = getEmails(mUri);
	    if(emails == null){
	    	Toast.makeText(getApplicationContext(), "Oops, no email for " + contactName, Toast.LENGTH_SHORT).show();
			finish();
			return;
	    }
		
		//display emails for user to choose
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emails));
		//allow list to be filtered by keyboard entry
		getListView().setTextFilterEnabled(true);
		
		//set title using contact name
		setTitle("Select " + contactName + "'s email");
		
	}

	private String[] getEmails(Uri contactUri) {
		//get contact ID for query
		String id = contactUri.getLastPathSegment();
		
		//query email
	    Cursor cursor = getContentResolver().query(
            Email.CONTENT_URI, null,
            Email.CONTACT_ID + "=?",
            new String[]{id}, null);
	    
	    //get number of entries, return null if no email
		int emailEntries = cursor.getCount();
		if(emailEntries <= 0) {
			return null; 
		}
		String[] emails = new String[emailEntries];
		                                 
		//iterate through phone rows and fetch phone number
		int rowIdx = 0;
		while(cursor.moveToNext()) {
			int emailColumnIdx = cursor.getColumnIndex(Email.DATA);
			int emailTypeColumnIdx = cursor.getColumnIndex(Email.DATA);
			CharSequence emailTypeLabel = Email.getTypeLabel(getResources(), emailTypeColumnIdx, null);
			
			String email = cursor.getString(emailColumnIdx);
			
			emails[rowIdx++] = emailTypeLabel.toString() +
									"\n" +
									email;
						
		}
		return emails;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String selectedItem = ((String)l.getItemAtPosition(position));
		//filter out the email type prefix
		String selectedPhoneNumber = selectedItem.substring(selectedItem.indexOf("\n")+1);
		
		//pass result back
		Intent result = new Intent();
		result.setData(mUri);
		result.putExtra(EMAIL_DATA_ID, selectedPhoneNumber);
		setResult(RESULT_OK, result);
		finish();
		return;
	}
	
}
