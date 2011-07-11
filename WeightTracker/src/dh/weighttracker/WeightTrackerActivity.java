package dh.weighttracker;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import dh.weighttracker.model.UserPrefernce;
import dh.weighttracker.view.SettingsActivity;
import dh.weighttracker.view.WeightEntryActivity;
import dh.weighttracker.view.WeightHistoryActivity;

public class WeightTrackerActivity extends TabActivity {
	
	//tab enum store info required by TabHost
	public enum Tab{
		WEIGHT_ENTRY("entry", "Entry", R.drawable.ic_tab_add, WeightEntryActivity.class),
		HISTORY("history", "History", R.drawable.ic_tab_history, WeightHistoryActivity.class),
		SETTINGS("settings", "Settings", R.drawable.ic_tab_edit, SettingsActivity.class);
		
		public final String tabTag;
		public final String label;
		public final int drawableID;
		public final Class intentClass;
		private Tab(String tabTag, String label, int drawableID, Class intentClass){
			this.tabTag = tabTag;
			this.label = label;
			this.drawableID = drawableID;
			this.intentClass = intentClass;
		}
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //restore preferences
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        UserPrefernce userData = UserPrefernce.getInstance();
        userData.loadFromPreferences(preferences);
        
        //initialize tabs
        final TabHost tabHost = getTabHost();
        initTabs(tabHost);
        //show settings tab if initial setup not done
        if(userData.isInitialSetuped()){
            tabHost.setCurrentTab(Tab.WEIGHT_ENTRY.ordinal());
        }
        else{
            tabHost.setCurrentTab(Tab.SETTINGS.ordinal());
        }
        
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
        	View prevTab = tabHost.getCurrentView();
        	int prevTabIdx = tabHost.getCurrentTab();
			@Override
			public void onTabChanged(String tabId) {
				//translate views depending on their tab index
				View curTab = tabHost.getCurrentView();
				int curTabIdx = tabHost.getCurrentTab();
				Animation curTabAnimation, prevTabAnimation;
				//use left to right or vice versa depending on current and previous tab index
				if(curTabIdx < prevTabIdx){
					curTabAnimation = AnimationUtils.loadAnimation(WeightTrackerActivity.this, R.anim.in_from_left);
					prevTabAnimation = AnimationUtils.loadAnimation(WeightTrackerActivity.this, R.anim.out_to_right);
				}
				else{
					curTabAnimation = AnimationUtils.loadAnimation(WeightTrackerActivity.this, R.anim.in_from_right);
					prevTabAnimation = AnimationUtils.loadAnimation(WeightTrackerActivity.this, R.anim.out_to_left);
				}
				curTab.setAnimation(curTabAnimation);
				prevTab.setAnimation(prevTabAnimation);
				
				//remove keyboard so views dont have to do it themselves
				InputMethodManager keyboard = (InputMethodManager)
				getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.hideSoftInputFromWindow(tabHost.getApplicationWindowToken(), 0);
				
				//store previous tab id
				prevTab = curTab;
				prevTabIdx = curTabIdx;
			}
		});
    }
    
    
    @Override
    protected void onStop() {
    	super.onStop();
    	//store preferences
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        UserPrefernce.getInstance().savePreferences(preferences);
    }
    
    private void initTabs(TabHost tabHost){
    	Resources res = getResources();
        Drawable tabIcon;
        TabHost.TabSpec spec;
        Intent intent;
        //create all of our tabs stored in Tab enum
    	for(Tab i:Tab.values()){
    		intent = new Intent().setClass(this, i.intentClass);
    		tabIcon = res.getDrawable(i.drawableID);
            spec = tabHost.newTabSpec(i.tabTag).setIndicator(i.label,tabIcon).setContent(intent);
            tabHost.addTab(spec);
    	}
    }
}
