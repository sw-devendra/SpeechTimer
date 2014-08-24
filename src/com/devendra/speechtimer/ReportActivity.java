package com.devendra.speechtimer;

import com.devendra.speechtimer.R;
import com.devendra.speechtimer.util.ReportData;
import com.devendra.speechtimer.util.SpeakerEntry;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ListView;
import android.database.DataSetObserver;


public class ReportActivity extends Activity{

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
		setContentView(R.layout.activity_report);
		
		ReportData rd = new ReportData(this, R.layout.report_entry, R.id.textView1);
		ListView lv = (ListView) findViewById(R.id.listView1);
		lv.setAdapter(rd);
		rd.setNotifyOnChange(true);
		
		rd.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged()
			{
				ListView lv = (ListView) findViewById(R.id.listView1);
				Button b = (Button)findViewById(R.id.button1);
				b.setEnabled(lv.getAdapter().getCount() != 0);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.report, menu);
		return true;
	}

	@Override
	public void onBackPressed()
	{
		ListView lv = (ListView) findViewById(R.id.listView1);
		ReportData rd = (ReportData)lv.getAdapter();
		rd.commitChanges(this);	
		super.onBackPressed();
	}
	
    public void buttonOnClick(View v) {
    	int vid = v.getId();
    	if (vid == R.id.button1) {
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

    	} else 	{    
    		
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
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
            	Intent myIntent = new Intent(ReportActivity.this, SettingsActivity.class);
            	ReportActivity.this.startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
