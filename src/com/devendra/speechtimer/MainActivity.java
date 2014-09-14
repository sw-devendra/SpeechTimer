package com.devendra.speechtimer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

import com.devendra.speechtimer.R;
import com.devendra.speechtimer.util.ReportData;
import com.devendra.speechtimer.util.SpeakerEntry;

import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements TextWatcher {

	static final int MAX_TIME_COUNT = 5;
	private boolean mQuickTimerRunning = false;
	private String mElapsedTime = "";
	
	class TimerEntryDeleteClickListner implements DialogInterface.OnClickListener {
		private int position = -1;
		TimerEntryDeleteClickListner(int pos)
		{
			position = pos;
		}
		
		@Override
        public void onClick(DialogInterface dialog, int which) {
        	ListView lv = (ListView) findViewById(R.id.listView1);
        	ReportData rd = (ReportData)lv.getAdapter(); 
    		SpeakerEntry se = rd.getItem(position);
    		rd.remove(se);
     	    rd.setChanged();
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
        Chronometer qt = (Chronometer) findViewById(R.id.quicktimer);
        if (!mQuickTimerRunning) {
        	qt.setText("Quick Timer");
        }
        else {
        	String timeFields[] = mElapsedTime.split(":");
        	qt.setBase(SystemClock.elapsedRealtime() 
        			- Integer.parseInt(timeFields[0])*60000 - Integer.parseInt(timeFields[1])*1000);
        	qt.start();
        }
	}
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
      savedInstanceState.putBoolean("quickTimerRunning", mQuickTimerRunning);
      Chronometer qt = (Chronometer) findViewById(R.id.quicktimer);
      if (mQuickTimerRunning)
    	  savedInstanceState.putString("timeElapsed", qt.getText().toString());
      
      // etc.
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
      mQuickTimerRunning = savedInstanceState.getBoolean("quickTimerRunning");
      
      if (mQuickTimerRunning)
    	  mElapsedTime = savedInstanceState.getString("timeElapsed");
    }
    
    
	@Override
	public void onBackPressed()
	{
		if (mQuickTimerRunning)
			updateModelAndFile();
		
		super.onBackPressed();
	}
    
    private void SetupActivity()
    {
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        setContentView(R.layout.activity_main);
     
        EditText t1 = (EditText)findViewById(R.id.editText2);
        t1.setGravity(Gravity.CENTER);
        t1.addTextChangedListener(this);
        
     // Empty report text
        TextView emptyReportText = new TextView(this);
        emptyReportText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT));
        emptyReportText.setText("No information of recent speakers");
        emptyReportText.setTextSize(30);
        
        ListView lv = (ListView) findViewById(R.id.listView1);
        
        lv.setEmptyView(emptyReportText);
        RelativeLayout rl =(RelativeLayout) findViewById(R.id.reportLayout);
        rl.addView(emptyReportText);
   	
    }
    private void initializeReport()
    {
		ReportData rd = new ReportData(this, R.layout.report_entry, R.id.colorsymbol);
		ListView lv = (ListView) findViewById(R.id.listView1);
		lv.setAdapter(rd);
		rd.setNotifyOnChange(true);
		
		rd.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged()
			{
				ListView lv = (ListView) findViewById(R.id.listView1);
				Button b = (Button)findViewById(R.id.button1);
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
		
		Button b = (Button)findViewById(R.id.button1);
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
		ListView lv = (ListView) findViewById(R.id.listView1);
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
    
    public void quickTimerOnClick(View v) {
    	Chronometer quicktimer = (Chronometer) findViewById(R.id.quicktimer);
    	if (!mQuickTimerRunning) {
    		quicktimer.setBase(SystemClock.elapsedRealtime());
    		quicktimer.start();
    		mQuickTimerRunning = true;
    	}
    	else {
    		quicktimer.stop();
    		updateModelAndFile();
    		mQuickTimerRunning = false;
    		quicktimer.setText("Quick Timer");
    	}
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
            	Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            	MainActivity.this.startActivity(myIntent);
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
           .setTitle("Clearing Timer Report")
           .setMessage("Clear timer report?")
           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

               @Override
               public void onClick(DialogInterface dialog, int which) {
               	ListView lv = (ListView) findViewById(R.id.listView1);
               	ReportData rd = (ReportData)lv.getAdapter();
            	    rd.clear();  
            	    rd.setChanged();
               }

           })
           .setNegativeButton("No", null)
           .show();
	   }
	   
	   public void reportEntryDeleteClick(View v) {
	      	ListView lv = (ListView) findViewById(R.id.listView1);
	    		int pos = lv.getPositionForView((View)v.getParent());
	    		
	     	   // Ask user if he really want delete the item records
	            new AlertDialog.Builder(this)
	            .setTitle("Deleting Timer Entry")
	            .setMessage("Remove this entry?")
	            .setPositiveButton("Yes", new TimerEntryDeleteClickListner(pos))
	            .setNegativeButton("No", null)
	            .show();
	   }
    
	    private void updateModelAndFile()
	    {
	    	File file = new File(this.getFilesDir(), SpeakerEntry.REPORT_FILE);
	    	
	    	long l = file.lastModified();
	    	Date now = new Date();
	    	
	    	try 
	    	{
	            FileWriter fw = new FileWriter(file, 
	            		file.exists() && (now.getTime() - l < SpeakerEntry.MAX_MEET_DURATION)) ;
	            PrintWriter pw = new PrintWriter(fw);
	            
	            SpeakerEntry se = new SpeakerEntry();
	            se.name = "";
	            se.type = "Quick Timer";
	            Chronometer contentView = (Chronometer)findViewById(R.id.quicktimer);
	            se.duration = contentView.getText().toString();
	            
	            // Update report model
               	ListView lv = (ListView) findViewById(R.id.listView1);
               	ReportData rd = (ReportData)lv.getAdapter();
               	rd.add(se); 
            	rd.setChanged();
	            
	            pw.println(se.toFileLine()); 	
	            pw.close();
	            fw.close();
	    	}
	    	catch(Exception e)
	    	{
	    		// Ignore if file could not be write
	    	}	
	    }
}
