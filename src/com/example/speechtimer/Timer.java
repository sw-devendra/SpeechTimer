package com.example.speechtimer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

import com.example.speechtimer.R;
import com.example.speechtimer.util.SpeakerEntry;
import com.example.speechtimer.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Timer extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_timer);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final Chronometer contentView = (Chronometer)findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
			
		});

		
		// add tick listner 
		
		contentView.setOnChronometerTickListener(new OnChronometerTickListener() {
			 
			@Override
			public void onChronometerTick(Chronometer chronometer) {
				Bundle extras = getIntent().getExtras();
				int speech_type=0;
				int min=0,mid=0,max=0;
				if (extras != null) {
					speech_type = extras.getInt("speech_type");
				}
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Resources res = getResources();
				switch(speech_type)
				{
				case R.id.speech:
					min = Integer.valueOf((sharedPref.getString(res.getString(R.string.speech_min_time),"0")));
					max = Integer.valueOf((sharedPref.getString(res.getString(R.string.speech_max_time),"120")))/2;
					mid = (min + max)/2;
					break;
					
				case R.id.table_topic:
					min = Integer.valueOf((sharedPref.getString(res.getString(R.string.tt_min_time),"0")));
					mid = min + 30;
					max = mid + 30;
					break;
					
				case R.id.evaluation:
					min = Integer.valueOf((sharedPref.getString(res.getString(R.string.eval_min_time),"0")));
					mid = min + 30;
					max = mid + 30;
					break;
										
				}
				
				 long timepassed = SystemClock.elapsedRealtime() - chronometer.getBase();
				 View v = findViewById(R.id.timer_layout);
				 if (timepassed >= max*1000)
					 v.setBackgroundColor(Color.RED);
				 else if (timepassed >= mid*1000)
					 v.setBackgroundColor(Color.rgb(255, 255, 150));
				 else if (timepassed >= min*1000)
					 v.setBackgroundColor(Color.GREEN);
				 else
					 v.setBackgroundColor(Color.WHITE);
			}
 
		});
		
		
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
		toggle.setOnTouchListener(mDelayHideTouchListener);
		
		toggle.toggle();
		contentView.start();
	}

	@Override
	public void onBackPressed()
	{
		ToggleButton tb = (ToggleButton) findViewById(R.id.toggleButton1);
		if (tb.isChecked())
		{
			writeReportToFile();
			tb.setChecked(false);
			Chronometer ch = (Chronometer) findViewById(R.id.fullscreen_content);
			ch.stop();
		}
		
		super.onBackPressed();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
    public void toggleOnClick(View v) {
    	    ToggleButton tb = (ToggleButton) v;
	    	final Chronometer contentView = (Chronometer)findViewById(R.id.fullscreen_content);	    
    	    if(tb.isChecked()) {
    	    	contentView.setBase(SystemClock.elapsedRealtime());
    	    	contentView.start();
    	    } else {
    	    	contentView.stop();
    	    	writeReportToFile();
    	    }
    }
    
    private void writeReportToFile()
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
            se.name = getIntent().getExtras().getString("name");
            switch(getIntent().getExtras().getInt("speech_type"))
            {
            case R.id.speech:
            	se.type = new String("Speaker");
            	break;
            case R.id.table_topic:
            	se.type = new String("Table Topic");
            	break;
            case R.id.evaluation:
            	se.type = new String("Evaluator");
            	break;
            
            }

            Chronometer contentView = (Chronometer)findViewById(R.id.fullscreen_content);
            se.duration = contentView.getText().toString();
            
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
