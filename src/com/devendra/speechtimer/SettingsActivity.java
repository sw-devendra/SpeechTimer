package com.devendra.speechtimer;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


import com.devendra.speechtimer.R;


public class SettingsActivity extends PreferenceActivity {

	private static final int MAX_SPEECH_TIME_RANGE = 5;
	class OnSpeechPrefChangeListner implements Preference.OnPreferenceChangeListener {
		
		private SettingsActivity mActivity;
		public boolean mOnStart = false;
		
		OnSpeechPrefChangeListner(SettingsActivity act) {
			mActivity = act;
		}
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);
				
				Resources res = mActivity.getResources();
				CharSequence text = "Hello toast!";

				if (!mOnStart && preference.getKey() == res.getString(R.string.speech_min_time)) {
					mActivity.setupMaxPreferenceRange(stringValue);
				}

			} else {
				int intValue = Integer.valueOf(stringValue)/60;
				preference.setSummary(Integer.toString(intValue) + ((intValue == 1)?" minute":" minutes"));
			}
			return true;
		}
		
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

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
		
		Resources res = getResources();
		
		bindPreferenceSummaryToValue(findPreference(res.getString(R.string.speech_min_time)));
		bindPreferenceSummaryToValue(findPreference(res.getString(R.string.tt_min_time)));
		bindPreferenceSummaryToValue(findPreference(res.getString(R.string.eval_min_time)));
		bindPreferenceSummaryToValue(findPreference(res.getString(R.string.speech_max_time)));
		
	}

	@SuppressWarnings("deprecation")
	public void setupMaxPreferenceRange(String minTimeStringValue) {
		Resources res = getResources();

		ListPreference speechMaxTimePref = (ListPreference) findPreference(res.getString(R.string.speech_max_time));
		int speechMinTimeValue = Integer.valueOf(minTimeStringValue);
		int speechMaxTimeValue = speechMinTimeValue + 60;
		CharSequence entries[] = new CharSequence[MAX_SPEECH_TIME_RANGE];
		CharSequence entryValues[] = new CharSequence[MAX_SPEECH_TIME_RANGE];
		for (int i=0; i<MAX_SPEECH_TIME_RANGE; i++) {
			entries[i] =  Integer.toString(speechMaxTimeValue/60 + i) + " minutes";
			entryValues[i] =  Integer.toString(speechMaxTimeValue + i*60);
		}
		
		speechMaxTimePref.setEntries(entries);
		speechMaxTimePref.setEntryValues(entryValues);
		
		int currentValue = Integer.valueOf(speechMaxTimePref.getValue());
		if (currentValue <= speechMinTimeValue || currentValue > Integer.valueOf(entryValues[MAX_SPEECH_TIME_RANGE -1].toString()) ) {
			speechMaxTimePref.setValueIndex(1); 
		}
		speechMaxTimePref.setSummary(speechMaxTimePref.getEntry());
	}


	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private OnSpeechPrefChangeListner mBindPreferenceSummaryToValueListener = new OnSpeechPrefChangeListner(this);

	private void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(mBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		mBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
		
	}

}
