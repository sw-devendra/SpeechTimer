package com.devendra.speechtimer;

import com.devendra.speechtimer.R;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        setContentView(R.layout.activity_main);
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
    
    public void buttonOnClick(View v) {
    	switch(v.getId()) {
        case R.id.speech:
        case R.id.table_topic:
        case R.id.evaluation:
            //Inform the user the button2 has been clicked
        	EditText nameField = (EditText) findViewById(R.id.editText1);
        	Intent myIntent = new Intent(MainActivity.this, Timer.class);
        	myIntent.putExtra("speech_type", v.getId());
        	myIntent.putExtra("name", nameField.getText().toString().replace(',', ' '));
        	MainActivity.this.startActivity(myIntent);
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
    
}
