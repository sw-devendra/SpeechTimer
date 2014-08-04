package com.example.speechtimer;

import com.example.listtest.R;
import com.example.listtest.R.id;
import com.example.listtest.R.layout;
import com.example.listtest.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            //Inform the user the button1 has been clicked
               

        case R.id.table_topic:
            //Inform the user the button2 has been clicked
              

        case R.id.evaluation:
            //Inform the user the button2 has been clicked
        	Intent myIntent = new Intent(MainActivity.this, Timer.class);
        	MainActivity.this.startActivity(myIntent);
        break;
    	}              
    }
    
}
