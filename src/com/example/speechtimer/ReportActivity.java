package com.example.speechtimer;

import com.example.speechtimer.R;
import com.example.speechtimer.util.ReportData;
import com.example.speechtimer.util.SpeakerEntry;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;

import android.widget.ListView;


public class ReportActivity extends Activity {

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
}
