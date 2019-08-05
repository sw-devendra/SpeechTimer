package com.devendra.speechtimer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.devendra.speechtimer.util.RecyclerViewAdapter;
import com.devendra.speechtimer.util.SpeakerEntry;
import com.devendra.speechtimer.util.SwipeToDeleteCallback;

import android.graphics.Color;
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
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
public class MainActivity extends AppCompatActivity implements TextWatcher {

	static final int MAX_TIME_COUNT = 11;
	static final int STOPPED = 1;
	static final int PAUSED = 2;
	static final int RUNNING = 3;

	class SpecialTimerData
	{
		public int timerId;
		public int playPauseId;
		public int logId;
		public int stopId;
		public int timerState;
		public long lastPauseTime;
		public String elapsedTime;
	};

	// Using Timer View Id as key because tags/string are language dependent and at time of run time
	// language change result may be unexpected as old stored key will be accessed by their translated version
	private HashMap<Integer, SpecialTimerData> helperMap = new HashMap<>();

	// Recycle View Implementation
	RecyclerView recyclerView;
	RecyclerViewAdapter mAdapter;
	OnSharedPreferenceChangeListener prefListener;
	
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
	    		SpeakerEntry se = mAdapter.getData().get(position);
	    		se.name = editedName;
				mAdapter.getData().remove(position);
	     	    mAdapter.getData().add(position, se);
	     	    mAdapter.setChanged();
	     	    mAdapter.notifyItemChanged(position);
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
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			
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

		if (helperMap.size() == 0) // If the helper map was not initialized in onRestoreInstanceState() (means no orientation change case)
        	initHelperMap();

		setTimerInitState(R.id.quicktimer,R.string.QuickTimer);
		setTimerInitState(R.id.wholemeeting,R.string.WholeMeeting);

	}

	private void initHelperMap() {
		helperMap.clear();
		SpecialTimerData std =  new SpecialTimerData();
		std.timerId = R.id.quicktimer;
		std.logId = R.id.quickTimerLog;
		std.playPauseId = R.id.quickTimerPlay;
		std.stopId = R.id.quickTimerStop;
		std.timerState = STOPPED;
		std.lastPauseTime = 0;

		helperMap.put( R.id.quicktimer, std);

		SpecialTimerData std2 =  new SpecialTimerData();
		std2.timerId = R.id.wholemeeting;
		std2.logId = R.id.wholeMeetingLog;
		std2.playPauseId = R.id.wholeMeetingPlay;
		std2.stopId = R.id.wholeMeetingStop;
		std2.timerState = STOPPED;
		std2.lastPauseTime = 0;
		helperMap.put(R.id.wholemeeting, std2);
	}
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
		for (Map.Entry<Integer, SpecialTimerData> entry : helperMap.entrySet()) {
			SpecialTimerData std = entry.getValue();
			savedInstanceState.putInt(entry.getKey() + "State", std.timerState);
			Chronometer qt = findViewById(std.timerId);
			if (std.timerState != STOPPED)
				savedInstanceState.putString(entry.getKey() + "Elapsed", qt.getText().toString());
		}
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
		initHelperMap();
		for (Map.Entry<Integer, SpecialTimerData> entry : helperMap.entrySet()) {
			SpecialTimerData std = entry.getValue();
			std.timerState = savedInstanceState.getInt(entry.getKey() + "State");
			if (std.timerState != STOPPED)
				std.elapsedTime = savedInstanceState.getString(entry.getKey() + "Elapsed");
			helperMap.put(entry.getKey(),std);
		}
    }

	@Override
	public void onBackPressed()
	{
		boolean updateModel = false;
		for (Map.Entry<Integer, SpecialTimerData> entry : helperMap.entrySet()) {
			updateModel = (updateModel || (entry.getValue().timerState != STOPPED));
		}
		if (updateModel)
			updateModelAndFile(0, false); // 0 ==> Back key pressed
		
		super.onBackPressed();
	}
	
    private void SetupActivity()
    {
    	
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		registerLangChangeListner();
		updateActivityLanguage();
		
        setContentView(R.layout.activity_main);
     
        EditText t1 = findViewById(R.id.editText2);
        t1.setGravity(Gravity.CENTER);
        t1.addTextChangedListener(this);
        /*
     // Empty report text
        TextView emptyReportText = new TextView(this);
        emptyReportText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT));
        emptyReportText.setText(getResources().getString(R.string.NoRecentSpeakers));
        emptyReportText.setTextSize(30);
        
        RelativeLayout rl =findViewById(R.id.reportLayout);
        rl.addView(emptyReportText); */
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
		((TextView) findViewById(R.id.emptyReportText)).setText(R.string.NoRecentSpeakers);
	    ((Button) findViewById(R.id.deleteAll)).setText(R.string.DeleteAll);
	    ((Chronometer) findViewById(R.id.quicktimer)).setText(R.string.QuickTimer);
    }

	private void handleCountChange() {
		RecyclerView lv = findViewById(R.id.recyclerView);
		Button b = findViewById(R.id.deleteAll);
		TextView tv = findViewById(R.id.emptyReportText);
		int count = lv.getAdapter().getItemCount();
		if ( count == 0) {
			tv.setVisibility(View.VISIBLE);
			b.setVisibility(View.GONE);
		}
		else {
			tv.setVisibility(View.GONE);
			b.setVisibility(View.VISIBLE);
			RecyclerView rv = findViewById(R.id.recyclerView);
			rv.scrollToPosition(count - 1);
		}
	}

    private void initializeReport()
    {
		// Recycler view
		recyclerView = findViewById(R.id.recyclerView);
		mAdapter = new RecyclerViewAdapter(this);
		recyclerView.setAdapter(mAdapter);
		handleCountChange();

		enableSwipeToDeleteAndUndo();
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	EditText nameText = findViewById(R.id.editText1);
    	nameText.setText("");
    	initializeReport();

    }
    
    @Override
    protected void onStop() {
		mAdapter.commitChanges(this);
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
		Chronometer timer = findViewById(timerID);
		String label = getResources().getString(labelID);
		SpecialTimerData std = helperMap.get(timerID);
		if (std.timerState == STOPPED) {
			timer.setText(label);

			findViewById(std.logId).setVisibility(View.GONE);
			findViewById(std.playPauseId).setVisibility(View.GONE);
			findViewById(std.stopId).setVisibility(View.GONE);
		}
		else {
			String timeFields[] = std.elapsedTime.split(":");
			timer.setBase(SystemClock.elapsedRealtime()
					- Integer.parseInt(timeFields[0])*60000 - Integer.parseInt(timeFields[1])*1000);
			timer.start();
		}
		helperMap.put(timerID, std);

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
		SpecialTimerData std = helperMap.get(v.getId());
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
		helperMap.put(std.timerId, std);
	}

	public void logSpecialTimer(View v) {
		updateModelAndFile(findTargetTimerId(v), true);
	}

	public void stopSpecialTimer(View v) {
		String tag = (String) v.getTag();
		SpecialTimerData std = helperMap.get(findTargetTimerId(v));
		Chronometer timer = findViewById(std.timerId);

		ImageButton playPause = findViewById(std.playPauseId);

		updateModelAndFile(findTargetTimerId(v), false);
		timer.stop();
		timer.setText(tag);
		std.timerState = STOPPED;
		playPause.setVisibility(View.GONE);
		findViewById(std.logId).setVisibility(View.GONE);
		findViewById(std.stopId).setVisibility(View.GONE);

	}

	public void playPauseSpecialTimer(View v) {
		String tag = (String) v.getTag();
		SpecialTimerData std = helperMap.get(findTargetTimerId(v));
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

		helperMap.put(std.timerId, std);
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

		Button maxEdit = findViewById(R.id.buttonMaxTime);
			
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
               	mAdapter.getData().clear();
               	mAdapter.notifyDataSetChanged();
               	mAdapter.setChanged();
               	handleCountChange();
               }

           })
           .setNegativeButton(R.string.no, null)
           .show();
	   }
	   
	   public void reportEntryEditClick(View v) {
 	      	    RecyclerView lv = findViewById(R.id.recyclerView);
		        int pos = lv.getChildAdapterPosition((View)v.getParent().getParent());
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

    
	    private void updateModelAndFile(Integer timerId, boolean log)
	    {
			Vector<Integer> timerIds = new Vector<>();


	    	if (timerId == 0) { // Is it a call from Back button handler?
	    		if (helperMap.get(R.id.quicktimer).timerState != STOPPED) {
	    			timerIds.add(R.id.quicktimer);
				}
				if (helperMap.get(R.id.wholemeeting).timerState != STOPPED) {
					timerIds.add(R.id.wholemeeting);
				}

				if(timerIds.size() == 0) {
					return; // No Special timer was running when back pressed
				}
			}
	    	else {
	    		timerIds.add(timerId);
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

	            for (Integer t: timerIds) {
					SpeakerEntry se = new SpeakerEntry();
					se.name = (log?"Elapsed":"");
					se.type = findViewById(t).getTag().toString();
					SpecialTimerData std = helperMap.get(t);
					Chronometer contentView = findViewById(std.timerId);
					se.duration = contentView.getText().toString();
					mAdapter.getData().add(se);
					pw.println(se.toFileLine());
				}
	            pw.close();
	            fw.close();
				handleCountChange();
	    	}
	    	catch(Exception e)
	    	{
	    		// Ignore if file could not be write
	    	}	
	    }

	private void enableSwipeToDeleteAndUndo() {
		SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


				final int position = viewHolder.getAdapterPosition();
				final SpeakerEntry item = mAdapter.getData().get(position);

				mAdapter.removeItem(position);
				handleCountChange();

				Snackbar snackbar = Snackbar
						.make(findViewById(R.id.recyclerView), "Item was removed from the list.", Snackbar.LENGTH_LONG);
				snackbar.setAction("UNDO", new View.OnClickListener() {
					@Override
					public void onClick(View view) {

						mAdapter.restoreItem(item, position);
						recyclerView.scrollToPosition(position);
						handleCountChange();
					}
				});

				snackbar.setActionTextColor(Color.YELLOW);
				snackbar.show();

			}
		};

		ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
		itemTouchhelper.attachToRecyclerView(recyclerView);
	}

	private int findTargetTimerId(View v) {
		GridLayout viewParent =  (GridLayout)v.getParent();
		for (int i = 0; i < viewParent.getChildCount(); i++) {
			View ch = viewParent.getChildAt(i);
			if (ch instanceof Chronometer) {
        		return ch.getId();
			}
		}
		return 0;
	}
}
