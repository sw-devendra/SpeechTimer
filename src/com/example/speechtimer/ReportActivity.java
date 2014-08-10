package com.example.speechtimer;

import com.example.speechtimer.R;
import com.example.speechtimer.R.layout;
import com.example.speechtimer.R.menu;
import com.example.speechtimer.util.ReportData;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;

public class ReportActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);
		
		ReportData rd = new ReportData(this, R.layout.report_entry);
		ListView lv = (ListView) findViewById(R.id.listView1);
		lv.setAdapter(rd);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.report, menu);
		return true;
	}

}
