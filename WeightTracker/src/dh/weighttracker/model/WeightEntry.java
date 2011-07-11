package dh.weighttracker.model;

import java.util.Calendar;

import android.database.Cursor;
import dh.weighttracker.database.WeightDatabaseHelper;
import dh.weighttracker.database.WeightDatabaseHelper.Column;

public class WeightEntry {
	
	
    
	static final int NOT_IN_DB_OR_INSERT_FAIL = -1;
	
	//database row ID, -1 if 
	private long rowID = NOT_IN_DB_OR_INSERT_FAIL;
	//data
    private Calendar time;
    //weight stored as pounds
    private float weight;

    public Calendar getTime(){
    	return time;
    }
    
    public void setTime(Calendar time){
    	this.time = time;
    }
    
    public float getWeightInPounds(){
    	return weight;
    }
	
    public void setWeight(float weight, UserPrefernce.WeightUnitOption weightUnit){
    	this.weight = weightUnit.convertToPoundIfRequired(weight);
    }
    
    //constructor taking time and weight
    public WeightEntry(Calendar time, float weight, UserPrefernce.WeightUnitOption weightUnit){
    	this.time = time;
    	setWeight(weight, weightUnit);
    }
    
    
    public long getDBRowID(){
    	return rowID;
    }
    
    //only meant to be used by DB helper
    public void setDBRowID(long rowID){
    	this.rowID = rowID;
    }
    
    private boolean isInDB(){
    	return (rowID != NOT_IN_DB_OR_INSERT_FAIL);
    }
    
    //store to database, returns true if success, false otherwise
    public boolean saveToDB(WeightDatabaseHelper helper){
    	if(isInDB()){
    		return helper.updateWeightEntry(rowID, time, weight);
    	}
    	else{
    		rowID = helper.insertWeightEntry(time, weight);
    		return isInDB();
    	}
    }

}
