package com.devendra.speechtimer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.devendra.speechtimer.util.ReportData;
import com.devendra.speechtimer.util.SpeakerEntry;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements TextWatcher {

	static final int MAX_TIME_COUNT = 11;
	static final int STOPPED = 1;
	static final int PAUSED = 2;
	static final int RUNNING = 3;
	private int mQuickTimerState = STOPPED;
	private int mWholeTimerState = STOPPED;
	private String mElapsedTime = "";
	class SpecialTimerData
	{
		public int timerId;
		public int playPauseId;
		public int logId;
		public int stopId;
		public int timerState;
		public long lastPauseTime;
	};
	private HashMap<String, SpecialTimerData> helperMap = new HashMap<>();

	OnSharedPreferenceChangeListener prefListener;

	
	class TimerEntryDeleteClickListner implements DialogInterface.OnClickListener {
		private int position = -1;

		TimerEntryDeleteClickListner(int pos)
		{
			position = pos;
		}
		
		@Override
        public void onClick(DialogInterface dialog, int which) {
        	ListView lv = (ListView) findViewById(R.id.reportListView);
        	ReportData rd = (ReportData)lv.getAdapter(); 
    		SpeakerEntry se = rd.getItem(position);
    		rd.remove(se);
     	    rd.setChanged();
        }		
	}
	
	class TimerEntryEditClickListner implements DialogInterface.OnClickListener, TextWatcher{
		private int position = -1;
		private String editedName;
		TimerEntryEditClickListner(int pos)
		{
			position = pos;
			editedName = "";
		}
		
		@Override
        public void onClick(DialogInterface dialog, int which) {
			if (!editedName.isEmpty()) {
				ListView lv = (ListView) findViewById(R.id.reportListView);
	        	ReportData rd = (ReportData)lv.getAdapter(); 
	    		SpeakerEntry se = rd.getItem(position);
	    		rd.remove(se);
	
	    		se.name = editedName;
	    		rd.insert(se, position);
	     	    rd.setChanged();
			}
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
		
		@Override
		public void afterTextChanged(Editable arg0) {
			editedName = arg0.toString();
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			
		}

		
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SetupActivity();
    }

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
        // Set text of quick timer

		Toolbar myToolbar = findViewById(R.id.toolbar);
		myToolbar.setTitle("SpeechTimer");
		myToolbar.setTitleTextColor(0xFFFFFFFF);
		setSupportActionBar(myToolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);

		SpecialTimerData std =  new SpecialTimerData();
		std.timerId = R.id.quicktimer;
		std.logId = R.id.quickTimerLog;
		std.playPauseId = R.id.quickTimerPlay;
		std.stopId = R.id.quickTimerStop;
		std.timerState = STOPPED;
		std.lastPauseTime = 0;

		helperMap.put(getResources().getString(R.string.QuickTimer), std);

		SpecialTimerData std2 =  new SpecialTimerData();
		std2.timerId = R.id.wholemeeting;
		std2.logId = R.id.wholeMeetingLog;
		std2.playPauseId = R.id.wholeMeetingPlay;
		std2.stopId = R.id.wholeMeetingStop;
		std2.timerState = STOPPED;
		std2.lastPauseTime = 0;
		helperMap.put(getResources().getString(R.string.WholeMeeting), std2);

		setTimerInitState(R.id.quicktimer,R.string.QuickTimer);
		setTimerInitState(R.id.wholemeeting,R.string.WholeMeeting);

	}
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
      savedInstanceState.putInt("quickTimerState", mQuickTimerState);
      Chronometer qt = findViewById(R.id.quicktimer);
      if (mQuickTimerState == RUNNING)
    	  savedInstanceState.putString("quickTimeElapsed", qt.getText().toString());

      savedInstanceState.putInt("wholeTimerState", mWholeTimerState);
      qt = findViewById(R.id.wholemeeting);
		if (mWholeTimerState == RUNNING)
			savedInstanceState.putString("wholeTimeElapsed", qt.getText().toString());


		// etc.
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
      mQuickTimerState = savedInstanceState.getInt("quickTimerState");
      mWholeTimerState= savedInstanceState.getInt("wholeTimerState");
      
      if (mQuickTimerState == RUNNING)
    	  mElapsedTime = savedInstanceState.getString("quickTimeElapsed");

      if (mWholeTimerState == RUNNING)
      	mElapsedTime = savedInstanceState.getString("wholeTimeElapsed");
    }
    
    
	@Override
	public void onBackPressed()
	{
		if (mQuickTimerState == RUNNING || mWholeTimerState == RUNNING)
			updateModelAndFile("back", false);
		
		super.onBackPressed();
	}
	
    private void SetupActivity()
    {
    	
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		registerLangChangeListner();
		updateActivityLanguage();
		
        setContentView(R.layout.activity_main);
     
        EditText t1 = (EditText)findViewById(R.id.editText2);
        t1.setGravity(Gravity.CENTER);
        t1.addTextChangedListener(this);
        
     // Empty report text
        TextView emptyReportText = new TextView(this);
        emptyReportText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT));
        emptyReportText.setText(getResources().getString(R.string.NoRecentSpeakers));
        emptyReportText.setTextSize(30);
        
        ListView lv = (ListView) findViewById(R.id.reportListView);
        
        lv.setEmptyView(emptyReportText);
        RelativeLayout rl =(RelativeLayout) findViewById(R.id.reportLayout);
        rl.addView(emptyReportText);
   	
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
            			updateActivityLanguage();
            		    OnRunTimeLangUpdate();
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
	    getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
    
    private void OnRunTimeLangUpdate()
    {
	    ((EditText) findViewById(R.id.editText1)).setHint(R.string.next_presenter);
	    ((TextView) findViewById(R.id.toInSpeech)).setText(R.string.to);
	    ((TextView) findViewById(R.id.minBeforSpeech)).setText(R.string.min);
	    ((Button) findViewById(R.id.speech)).setText(R.string.speech);
	    ((Button) findViewById(R.id.table_topic)).setText(R.string.table);
	    ((Button) findViewById(R.id.evaluation)).setText(R.string.evaluation);
	    TextView emptyView = (TextView) ((ListView) findViewById(R.id.reportListView)).getEmptyView();
	    emptyView.setText(R.string.NoRecentSpeakers);
	    ((Button) findViewById(R.id.button1)).setText(R.string.DeleteAll);
	    ((Chronometer) findViewById(R.id.quicktimer)).setText(R.string.QuickTimer);
	    
    }

    private void initializeReport()
    {
		ReportData rd = new ReportData(this, R.layout.report_entry, R.id.colorsymbol);
		ListView lv = findViewById(R.id.reportListView);
		lv.setAdapter(rd);
		rd.setNotifyOnChange(true);
		
		rd.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged()
			{
				ListView lv = (ListView) findViewById(R.id.reportListView);
				Button b = (Button)findViewById(R.id.deletAll);
				if (lv.getAdapter().getCount() == 0)
					b.setVisibility(View.INVISIBLE);
				else
					b.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onInvalidated()
			{
				
			}
			
		});
		
		Button b = (Button)findViewById(R.id.deletAll);
		if (lv.getAdapter().getCount() == 0)
			b.setVisibility(View.INVISIBLE);
		else
			b.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	EditText nameText = (EditText) findViewById(R.id.editText1);
    	nameText.setText("");
    	initializeReport();
    }
    
    @Override
    protected void onStop() {
		ListView lv = (ListView) findViewById(R.id.reportListView);
		ReportData rd = (ReportData)lv.getAdapter();
		rd.commitChanges(this);	
		super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override    
    public boolean onPrepareOptionsMenu (Menu menu) {
    	super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_settings).setTitle(R.string.action_settings);
        menu.findItem(R.id.help).setTitle(R.string.helpVideo);
    	return true;
    }
    private int getMinSpeechTime()
    {
    	EditText nameField = (EditText) findViewById(R.id.editText2);
        String minTimeStr = nameField.getText().toString();
        int minTimeInt = 0;
        if (minTimeStr.length() > 0) {
        	minTimeInt = Integer.parseInt(minTimeStr);
        }
    	return minTimeInt;
    }
    
    private void launchTimer(int minTime, int maxTime, int type)
    {
        //Inform the user the button2 has been clicked
    	EditText nameField = (EditText) findViewById(R.id.editText1);
    	Intent myIntent = new Intent(MainActivity.this, Timer.class);
    	myIntent.putExtra("speech_type", type);
    	myIntent.putExtra("name", nameField.getText().toString().replace(',', ' '));
    	myIntent.putExtra("min_time", minTime);
    	myIntent.putExtra("max_time", maxTime);
    	MainActivity.this.startActivity(myIntent);
    }

    private void setTimerInitState(int timerID, int labelID) {
		Chronometer timer = (Chronometer) findViewById(timerID);
		String label = getResources().getString(labelID);
		SpecialTimerData std = helperMap.get(label);
		if (std.timerState == STOPPED) {
			timer.setText(label);

			findViewById(std.logId).setVisibility(View.GONE);
			findViewById(std.playPauseId).setVisibility(View.GONE);
			findViewById(std.stopId).setVisibility(View.GONE);
		}
		else {
			String timeFields[] = mElapsedTime.split(":");
			timer.setBase(SystemClock.elapsedRealtime()
					- Integer.parseInt(timeFields[0])*60000 - Integer.parseInt(timeFields[1])*1000);
			timer.start();
		}
		helperMap.put(label, std);

	}
    public void buttonOnClick(View v) {
    	switch(v.getId()) {
        case R.id.speech:
        	int minSpeechTime = getMinSpeechTime();
        	Button maxTimeButton = (Button) findViewById(R.id.buttonMaxTime);
        	int maxSpeechTime = Integer.parseInt(maxTimeButton.getText().toString());
        	launchTimer(minSpeechTime, maxSpeechTime, R.id.speech);
        	break;
        case R.id.table_topic:
        	launchTimer(1, 2, R.id.table_topic);
        	break;
        case R.id.evaluation:
        	launchTimer(2, 3, R.id.evaluation);
        	break;       	
    	}              
    }


	public void specialTimerOnClick(View v) {
		String tag = (String) v.getTag();
		SpecialTimerData std = helperMap.get(tag);
		Chronometer timer = findViewById(std.timerId);
		ImageButton playPause = findViewById(std.playPauseId);
		if (std.timerState == STOPPED) {
			timer.setBase(SystemClock.elapsedRealtime());
			timer.start();

			playPause.setImageResource(R.drawable.ic_action_pause);
			playPause.setVisibility(View.VISIBLE);
			findViewById(std.logId).setVisibility(View.VISIBLE);
			findViewById(std.stopId).setVisibility(View.VISIBLE);
			std.timerState = RUNNING;
			std.lastPauseTime = 0;
		}
		else {
			playPauseSpecialTimer(v);
		}
		helperMap.put(tag, std);
	}

	public void logSpecialTimer(View v) {
		updateModelAndFile((String)v.getTag(), true);
	}

	public void stopSpecialTimer(View v) {
		String tag = (String) v.getTag();
		SpecialTimerData std = helperMap.get(tag);
		Chronometer timer = findViewById(std.timerId);

		ImageButton playPause = findViewById(std.playPauseId);

		updateModelAndFile(tag, false);
		timer.stop();
		timer.setText(tag);
		std.timerState = STOPPED;
		playPause.setVisibility(View.GONE);
		findViewById(std.logId).setVisibility(View.GONE);
		findViewById(std.stopId).setVisibility(View.GONE);

	}

	public void playPauseSpecialTimer(View v) {
		String tag = (String) v.getTag();
		SpecialTimerData std = helperMap.get(tag);
		Chronometer timer = findViewById(std.timerId);

		ImageButton playPause = findViewById(std.playPauseId);

		if (std.timerState == RUNNING) {
			timer.stop();
			std.timerState = PAUSED;
			std.lastPauseTime = SystemClock.elapsedRealtime();
			playPause.setImageResource(R.drawable.ic_action_play);
			playPause.setVisibility(View.VISIBLE);
			findViewById(std.logId).setVisibility(View.VISIBLE);
			findViewById(std.stopId).setVisibility(View.VISIBLE);
		}
		else if (std.timerState == PAUSED) {

			timer.start();
			std.timerState = RUNNING;
			long intervalOnPause = (SystemClock.elapsedRealtime() - std.lastPauseTime);
			timer.setBase( timer.getBase() + intervalOnPause );

			playPause.setImageResource(R.drawable.ic_action_pause);
			playPause.setVisibility(View.VISIBLE);
			findViewById(std.logId).setVisibility(View.VISIBLE);
			findViewById(std.stopId).setVisibility(View.VISIBLE);
		}

		helperMap.put(tag, std);
	}

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
            	Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            	MainActivity.this.startActivity(myIntent);
                return true;
                
            case R.id.help:
            	Intent intent = new Intent(Intent.ACTION_VIEW);
            	intent.setData(Uri.parse("http://www.youtube.com/watch?v=TecpzqIHW3U"));
            	startActivity(intent);
            	return true;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub

		String s = arg0.toString();
		int minNum = 0;
		if (s.length() > 0)
			minNum = Integer.parseInt(arg0.toString());

		Button maxEdit = (Button)findViewById(R.id.buttonMaxTime);
			
		maxEdit.setText(Integer.toString(Math.min(minNum + 2, 99), 10)); 

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}
	
	   public void maxButtonOnClick(View v) {
		   CharSequence maxTime[] = new CharSequence[MAX_TIME_COUNT];
           int minTimeInt = getMinSpeechTime();

	       	for (int i=0; i < MAX_TIME_COUNT; i++) {
	       		maxTime[i] = Integer.toString(i + minTimeInt);
	       	}
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Select Maximum Time")
	               .setItems(maxTime, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int which) {
	                   // The 'which' argument contains the index position
	                   // of the selected item
                       int minTimeInt = getMinSpeechTime();
	                   Button maxTimeButton = (Button) findViewById(R.id.buttonMaxTime);
	                   maxTimeButton.setText(Integer.toString(minTimeInt + which));
	               }
	        }).show();
	    }	
	   
	   public void deleteAllClick(View v) {
    	   // Ask user if he really want delete all records
           new AlertDialog.Builder(this)
           .setMessage(R.string.clearReportDlgTitle)
           .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

               @Override
               public void onClick(DialogInterface dialog, int which) {
               	ListView lv = (ListView) findViewById(R.id.reportListView);
               	ReportData rd = (ReportData)lv.getAdapter();
            	    rd.clear();  
            	    rd.setChanged();
               }

           })
           .setNegativeButton(R.string.no, null)
           .show();
	   }
	   
	   public void reportEntryEditClick(View v) {
 	      	    ListView lv = (ListView) findViewById(R.id.reportListView);
 	    		int pos = lv.getPositionForView((View)v.getParent());    		
	     	   // Ask user if he really want delete the item records

	    		TimerEntryEditClickListner nameEditorDlgListner = new TimerEntryEditClickListner(pos);

	    		// Set an EditText view to get user input 
	    		final EditText input = new EditText(this);
	    		input.setId(R.id.nameEditText);
	    		input.addTextChangedListener(nameEditorDlgListner);
	    		input.setHint(R.string.enterName);

	    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    		
	    		alert.setView(input)
	    		.setPositiveButton("Ok", nameEditorDlgListner)
	    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    		  public void onClick(DialogInterface dialog, int whichButton) {
	    		    // Canceled.
	    			  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  
	    		  }
	    		});
	    		
	    		final AlertDialog dialog = alert.create();
	    		Configuration config = getResources().getConfiguration();
	    		if (config.keyboard == Configuration.KEYBOARD_NOKEYS) {
		    		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    			@Override
		    			public void onFocusChange(View v, boolean hasFocus) {
		    				if (hasFocus) {
		    					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		    				}
		    			}
		    		});
	    		}
	    		dialog.show();
	   }
	   
	   public void reportEntryDeleteClick(View v) {
	      	ListView lv = (ListView) findViewById(R.id.reportListView);
	    		int pos = lv.getPositionForView((View)v.getParent());
	    		
	     	   // Ask user if he really want delete the item records
	            new AlertDialog.Builder(this)
	            .setMessage(R.string.removeEntryDlgTitle)
	            .setPositiveButton(R.string.yes, new TimerEntryDeleteClickListner(pos))
	            .setNegativeButton(R.string.no, null)
	            .show();
	   }
    
	    private void updateModelAndFile(String tag, boolean log)
	    {
			Vector<String> tags = new Vector<>();


	    	if (tag == "back") { // Is it a call from Back button handler?
	    		String tag1 = getResources().getString(R.string.QuickTimer);
	    		SpecialTimerData std = helperMap.get(tag1);
	    		if (std.timerState != STOPPED) {
	    			tags.add(tag1);
				}
				String tag2 = getResources().getString(R.string.WholeMeeting);
				if (helperMap.get(tag2).timerState != STOPPED) {
					tags.add(tag2);
				}

				if(tags.size() == 0) {
					return; // No Special timer was running when back pressed
				}
			}
	    	else {
	    		tags.add(tag);
			}

	    	File file = new File(this.getFilesDir(), SpeakerEntry.REPORT_FILE);
	    	
	    	long l = file.lastModified();
	    	Date now = new Date();
	    	
	    	try 
	    	{
	            FileWriter fw = new FileWriter(file, 
	            		file.exists() && (now.getTime() - l < SpeakerEntry.MAX_MEET_DURATION)) ;
	            PrintWriter pw = new PrintWriter(fw);

				// Update report model
				ListView lv = (ListView) findViewById(R.id.reportListView);
				ReportData rd = (ReportData)lv.getAdapter();

	            for (String t: tags) {
					SpeakerEntry se = new SpeakerEntry();
					se.name = (log?"Elapsed":"");
					se.type = t;
					SpecialTimerData std = helperMap.get(t);
					Chronometer contentView = findViewById(std.timerId);
					se.duration = contentView.getText().toString();
					rd.add(se);
					pw.println(se.toFileLine());
				}

            	rd.setChanged(); // Modal changed
	            pw.close();
	            fw.close();
	    	}
	    	catch(Exception e)
	    	{
	    		// Ignore if file could not be write
	    	}	
	    }
}
