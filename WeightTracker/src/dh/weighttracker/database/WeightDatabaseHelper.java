package dh.weighttracker.database;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import dh.weighttracker.model.UserPrefernce;
import dh.weighttracker.model.WeightEntry;

public class WeightDatabaseHelper {
	
	static final String LOG_TAG = "WeightDatabaseHelper";
	
	static final String DB_NAME = "weighttrackerdata";
	static final int DB_VERSION = 1;
	
	static final String DB_TABLE = "weight";
	public enum Column{
		ROW_ID(BaseColumns._ID),
		ENTRY_TIME("entry_time"),
		WEIGHT_ENTRY("weight_entry");	//all weight stored as pounds in db
		
		public final String colName;
		private Column(String name){
			this.colName = name;
		}
		
		@Override
		public String toString() {
			return colName;
		}
	}
	
	//db instance and helper
	private WeightDatabaseOpenHelper dbHelper;
	private SQLiteDatabase db;
	
	
	//singleton instance of WeightDatabaseHelper as 1 opened db is enough for our application
	private static WeightDatabaseHelper instance;
	public static WeightDatabaseHelper getInstance(Context context){
		if(instance == null){
			instance = new WeightDatabaseHelper(context);
		}
		return instance;
	}
	
	//constructor for singleton
	private WeightDatabaseHelper(Context context){
		this.dbHelper = new WeightDatabaseOpenHelper(context);
	}

	//helper for creating db and handle db upgrade
	public static class WeightDatabaseOpenHelper extends SQLiteOpenHelper{
		
		//create database statement
		static final String DB_CREATE =
			"create table " + DB_TABLE + " (" + Column.ROW_ID + " integer primary key autoincrement, " +
			Column.ENTRY_TIME + " integer not null, " + Column.WEIGHT_ENTRY + " real not null);";

		public WeightDatabaseOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		//called when creating db
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE);
			
		}
		
		//called when upgrading db
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//do conversion
			//drop table for now
//			db.execSQL("drop table if exists " + DB_TABLE);
//			
//			Log.d(LOG_TAG, "db onUpgrade from version " + oldVersion + " to version " + newVersion);
//			
//			//create database
//			onCreate(db);
		}
		
	}
	
	
	//open db
	public WeightDatabaseHelper open() throws SQLiteException{
		if(db == null || !db.isOpen()){
			db = dbHelper.getWritableDatabase();
		}
		return this;
	}
	
	//close db
	public void close(){
		db.close();
	}
	
	
	//insert weight entry, weight in pounds, returns rowID of inserted row, -1 if failure
	public long insertWeightEntry(Calendar time, float weight){
		ContentValues values = createContentValues(time, weight);
		//return rowID
		return db.insert(DB_TABLE, null, values);
	}
	
	//update weight entry, true if row updated, false otherwise
	public boolean updateWeightEntry(long rowID, Calendar time, float weight){
		ContentValues updateValues = createContentValues(time, weight);
		int updatedRows = db.update(DB_TABLE, updateValues, Column.ROW_ID + "=" + rowID, null);
		//true if row updated
		return updatedRows > 0; 
	}
	
	//delete weight entry, true if row deleted, false otherwise
	public boolean deleteWeightEntry(long rowID){
		int deletedRows = db.delete(DB_TABLE, Column.ROW_ID + "=" + rowID, null);
		//true if row deleted
		return deletedRows > 0;
	}
	
	
	//get all weight entry
	public Cursor getAllWeightEntries(){
		String[] columns = new String[] { Column.ROW_ID.colName, Column.ENTRY_TIME.colName, Column.WEIGHT_ENTRY.colName};
		String orderby = Column.ENTRY_TIME + " DESC";
		return db.query(DB_TABLE, columns,
				null, null, null, null, orderby);
	}
	
	//get specified weight entry
	public Cursor getWeightEntryCursor(long rowId){
		String[] columns = new String[] {Column.ENTRY_TIME.colName, Column.WEIGHT_ENTRY.colName};
		String select = Column.ROW_ID + "=" + rowId;
		
		Cursor mCursor = db.query(true, DB_TABLE, columns, select,
				null, null, null, null, null);
		
		if(mCursor != null){
			mCursor.moveToFirst();
		}
		
		return mCursor;
	}
	
	//get weight entry by database row id, return null if 
    public WeightEntry getWeightEntry(long id){
    	Cursor c = getWeightEntryCursor(id);
    	if(c == null){
    		return null;
    	}
    	//get entry time
    	int entryTimeIdx = c.getColumnIndex(Column.ENTRY_TIME.colName);
    	long entryTime = c.getLong(entryTimeIdx);
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(entryTime);
    	
    	//get weight
    	int weightIdx = c.getColumnIndex(Column.WEIGHT_ENTRY.colName);
    	float weight = c.getFloat(weightIdx);
    	
    	//return WeightEntry
    	WeightEntry entry = new WeightEntry(cal, weight, UserPrefernce.WeightUnitOption.POUND);
    	entry.setDBRowID(id);	//we use this to distinguish entry read from db or a new entry
    	return entry;
    }
	
	//create a ContentValues object for use by db insert and update
	private ContentValues createContentValues(Calendar time, float weight) {
		ContentValues values = new ContentValues();
		long timeMili = time.getTimeInMillis();
		values.put(Column.ENTRY_TIME.colName, timeMili);
		values.put(Column.WEIGHT_ENTRY.colName, weight);
		return values;
	}
}
