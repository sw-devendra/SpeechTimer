package com.devendra.speechtimer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

import com.devendra.speechtimer.R;
import com.devendra.speechtimer.util.SpeakerEntry;
import com.devendra.speechtimer.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.os.Vibrator;

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
	public static final int SPEECH_STATE_STARTED=0;
	public static final int SPEECH_STATE_MIN_TIME_CROSOSED=1;
	public static final int SPEECH_STATE_MID_TIME_CROSOSED=2;
	public static final int SPEECH_STATE_MAX_TIME_CROSOSED=3;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	public int mSpeechState = SPEECH_STATE_STARTED;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// No screen off 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_timer);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final Chronometer contentView = (Chronometer)findViewById(R.id.chronometer);

		// Setup Initial timer state
		View timerLayout = findViewById(R.id.timer_layout);
		timerLayout.setBackgroundColor(Color.WHITE);
		mSpeechState = SPEECH_STATE_STARTED;
		
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, timerLayout,HIDER_FLAGS);
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
			 
			private static final int VIBRATION_DURATION = 1500;
			private void vibrateIfEnabled()
			{
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				boolean vibrationOn = sharedPreferences.getBoolean("vibration", true);
				if (vibrationOn) {
					Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
				    vibrator.vibrate(VIBRATION_DURATION);
				}
			}
			
			@Override
			public void onChronometerTick(Chronometer chronometer) {
				Bundle extras = getIntent().getExtras();
				int min=extras.getInt("min_time")*60000;
				int max=extras.getInt("max_time")*60000;
				int mid = (min + max)/2;				
				long timepassed = SystemClock.elapsedRealtime() - chronometer.getBase();
				View v = findViewById(R.id.timer_layout);
				if (timepassed >= max && mSpeechState != SPEECH_STATE_MAX_TIME_CROSOSED) {
					 v.setBackgroundColor(Color.RED);
					 mSpeechState = SPEECH_STATE_MAX_TIME_CROSOSED;
					 vibrateIfEnabled();
				}
				else if (timepassed >= mid && timepassed < max && mSpeechState != SPEECH_STATE_MID_TIME_CROSOSED) {
					v.setBackgroundColor(Color.rgb(255, 255, 150));
					mSpeechState = SPEECH_STATE_MID_TIME_CROSOSED;
					vibrateIfEnabled();
				}
				else if (timepassed >= min && timepassed < mid && mSpeechState != SPEECH_STATE_MIN_TIME_CROSOSED) {
					v.setBackgroundColor(Color.GREEN);
					mSpeechState = SPEECH_STATE_MIN_TIME_CROSOSED;
					vibrateIfEnabled();
				}
				else if (timepassed < min && mSpeechState != SPEECH_STATE_STARTED ){
				 v.setBackgroundColor(Color.WHITE);
				 mSpeechState = SPEECH_STATE_STARTED;
				}
				
				// Show/hide Color symbol depending on current setting
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				boolean showcolorsymbol = sharedPreferences.getBoolean("showcolorsymbol", true);
				
				TextView tv = (TextView) findViewById(R.id.colorsymbol);
				
				Chronometer cm = (Chronometer) findViewById(R.id.chronometer);				
				if (showcolorsymbol && mSpeechState != SPEECH_STATE_STARTED) {
					String colorSymbol = new String();
					switch(mSpeechState) {
						case SPEECH_STATE_MIN_TIME_CROSOSED:
							colorSymbol = "G";
							break;
						case SPEECH_STATE_MID_TIME_CROSOSED:
							colorSymbol = "Y";
							break;
						case SPEECH_STATE_MAX_TIME_CROSOSED:
							colorSymbol = "R";
							break;
					};
					cm.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
					tv.setVisibility(View.VISIBLE);
					tv.setText(colorSymbol);
				} else {
					 cm.setGravity(Gravity.CENTER);
					 tv.setVisibility(View.INVISIBLE);					
				}
				
				// Show/hide time depending on current setting
				boolean showtime = sharedPreferences.getBoolean("showtime", true);
				
				if (showtime)
					cm.setVisibility(View.VISIBLE);
				else
					cm.setVisibility(View.INVISIBLE);
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
			Chronometer ch = (Chronometer) findViewById(R.id.chronometer);
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
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
            	Intent myIntent = new Intent(Timer.this, SettingsActivity.class);
            	Timer.this.startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
	    	final Chronometer contentView = (Chronometer)findViewById(R.id.chronometer);	    
    	    if(tb.isChecked()) {
    	    	contentView.setBase(SystemClock.elapsedRealtime());
    	    	mSpeechState = SPEECH_STATE_STARTED;
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

            Chronometer contentView = (Chronometer)findViewById(R.id.chronometer);
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
