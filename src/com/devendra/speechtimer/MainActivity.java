package com.devendra.speechtimer;

import com.devendra.speechtimer.R;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements TextWatcher {

	static final int MAX_TIME_COUNT = 5;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        setContentView(R.layout.activity_main);
     
        EditText t1 = (EditText)findViewById(R.id.editText2);
        t1.setGravity(Gravity.CENTER);
        t1.addTextChangedListener(this);
        
  /*      EditText t2 = (EditText)findViewById(R.id.editText3);
        t2.setGravity(Gravity.CENTER);
   //     t2.addTextChangedListener(this);  */
    }

    @Override
    protected void onStart() {
    	super.onStart();
    	EditText nameText = (EditText) findViewById(R.id.editText1);
    	nameText.setText("");
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
        
        case R.id.report:
        	Intent myIntent2 = new Intent(MainActivity.this, ReportActivity.class);
        	MainActivity.this.startActivity(myIntent2);
        break;        	
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
    
}
