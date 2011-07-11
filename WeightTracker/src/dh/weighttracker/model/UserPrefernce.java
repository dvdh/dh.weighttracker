package dh.weighttracker.model;

import android.content.SharedPreferences;
import dh.weighttracker.R;

public class UserPrefernce {
	
	//keeps track whether initial setup screen has been completed
	private boolean initialSetuped = false;
	
	//settings data
	private String userName, userEmail, recipientEmail;
	
	
	
	//getter and setters for data
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getRecipientEmail() {
		return recipientEmail;
	}
	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}
	public WeightUnitOption getWeightUnitOption() {
		return option;
	}
	public void setWeightUnitOption(WeightUnitOption option) {
		this.option = option;
	}


	//default option to display weight as pound or kg
	private WeightUnitOption option = WeightUnitOption.POUND;
	public enum WeightUnitOption{
		POUND(R.id.settings_pounds, "lb"), KILOGRAM(R.id.settings_kilograms, "kg");
		
		//pound to kilogram conversion
		public static final float POUND_TO_KILOGRAM = 0.45359237f;
		public static final float KILOGRAM_TO_POUND = 2.20462262f;
		
		//radio button id stored for radio group to store and restore selected radio button
		final int radioButtonID;
		//short name for unites
		final String shortUnit;

		public int getRadioButtonID(){
			return radioButtonID;
		}
		
		public String getShortUnit(){
			return shortUnit;
		}
		
		//constructor, requires radio button id
		private WeightUnitOption(int radioButtonID, String shortUnit){
			this.radioButtonID = radioButtonID;
			this.shortUnit = shortUnit;
		}
		
		//searches for option enum based on given radio button id
		public static WeightUnitOption getSelectedOptionByID(int radioButtonID){
			//find the matching option with the given radio button id
			for(WeightUnitOption i:WeightUnitOption.values()){
				if(i.getRadioButtonID() == radioButtonID){
					return i;
				}
			}
			return null;
		}
		
		//performs conversion from pound to kilogram if current option is kilogram
		public float convertFromPoundIfRequired(float weight){
			if(this == KILOGRAM){
				return poundToKilogram(weight);
			}
			else{
				return weight;
			}
		}
		
		//convert weight to pounds if we are in kilogram option, do nothing if we are in pounds option
		public float convertToPoundIfRequired(float weight){
			if(this == KILOGRAM){
				return kilogramToPound(weight);
			}
			else{
				return weight;
			}
		}
		//convert weight to kilograms if we are in pound option, do nothing if we are in kilogram option
		public float convertToKilogramIdRequired(float weight){
			switch(this){
			case POUND:
				return poundToKilogram(weight);
			case KILOGRAM:
			}
			return weight;
		}
		
		public static float poundToKilogram(float pounds){
			return pounds * POUND_TO_KILOGRAM;
		}
		
		public static float kilogramToPound(float kilogram){
			return kilogram * KILOGRAM_TO_POUND;
		}
		
		//preference stored as: pound = true, kg = false
		public boolean isPound(){
			return (this == POUND);
		}
		public static WeightUnitOption loadPreference(boolean isPound){
			return (isPound) ? POUND : KILOGRAM;
		}
	}
	
	//used by settings activity to match recipient email options
	private RecipientEmailOption recipientOption = RecipientEmailOption.CONTACT;
	public enum RecipientEmailOption{
		SELF, CONTACT, ADD_CONTACT;
	}
	
	public RecipientEmailOption getRecipientOption() {
		return recipientOption;
	}
	public void setRecipientOption(RecipientEmailOption recipientOption) {
		this.recipientOption = recipientOption;
	}
	
	//returns have initial setup been done
	public boolean isInitialSetuped(){
		return initialSetuped;
	}
	//isSetupped true to indicate initial setup has been done
	public void setSetuped(boolean isSetupped){
		initialSetuped = isSetupped;
	}
	
	
	//singleton instance, could use an application as well
	private static UserPrefernce mInstance;
	
	//returns singleton instance of UserData
	public static UserPrefernce getInstance(){
		if(mInstance == null){
			mInstance = new UserPrefernce();
		}
		return mInstance;
	}
	
	//user data used as a singleton, so constructor is limited
	private UserPrefernce(){
		
	}

	
	//load user settings from preferences
	public void loadFromPreferences(SharedPreferences preferences){
		initialSetuped = preferences.getBoolean("initialSetupped", false);
		userName = preferences.getString("username", "");
		userEmail = preferences.getString("useremail", "");
		recipientEmail = preferences.getString("recipientemail", "");
		option = WeightUnitOption.loadPreference(preferences.getBoolean("option", true));
	}
	
	//store user settings to preferences
	public void savePreferences(SharedPreferences preferences){
		SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("initialSetupped", initialSetuped);
        editor.putString("username", userName);
        editor.putString("useremail", userEmail);
        editor.putString("recipientemail", recipientEmail);
        editor.putBoolean("option", option.isPound());

        // Commit the edits!
        editor.commit();
	}
	
}
