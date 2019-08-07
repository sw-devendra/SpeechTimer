package com.devendra.speechtimer;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;



import com.devendra.speechtimer.R;


public class SettingsActivity extends PreferenceActivity {
     OnSharedPreferenceChangeListener prefListener;
     
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		 registerLangChangeListner();
		 updateActivityLanguage();
	     super.onCreate(savedInstanceState);
	 }
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Fixed to landscape?
		boolean onlyLandscape = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("onlylandscape", false);
		if (onlyLandscape)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);		
	}
	
    private void registerLangChangeListner()
    {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @SuppressLint("NewApi")
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            	if (key.compareTo("language") == 0 )  	{
            		if (Build.VERSION.SDK_INT >= 11) {
            		    recreate();
            		} else {
            		    Intent intent = getIntent();
            		    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            		    finish();
            		    startActivity(intent);
            		}
            	}
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener);
    }
    
    private void updateActivityLanguage()
    {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String languageToLoad = sharedPreferences.getString("language", "en");
		// Setup language
	    Locale locale = new Locale(languageToLoad); 
	    Locale.setDefault(locale);
	    Configuration config = new Configuration();
	    config.locale = locale;
	    Resources rApp = getBaseContext().getResources();
	    rApp.updateConfiguration(config, rApp.getDisplayMetrics());
    }
}
