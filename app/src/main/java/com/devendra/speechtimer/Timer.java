package com.devendra.speechtimer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.devendra.speechtimer.util.SpeakerEntry;
import com.devendra.speechtimer.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Timer extends Activity implements TextWatcher{
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
	
	static final int MAX_TIME_COUNT = 5;

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
	
	private ScaleGestureDetector mScaleDetector;
	
	private float mTextSize;

	private class ScaleListener 
    extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		Chronometer chrono = (Chronometer) findViewById(R.id.chronometer);
	    float oldSize = chrono.getTextSize();
	    // Don't let the object get too small or too large.
	    mTextSize = Math.max(10.0f, Math.min(oldSize*detector.getScaleFactor(), 800.0f));
	    chrono.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
	    return true;
	}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setLanguage();
		// No screen off 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_timer);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final Chronometer timerView = (Chronometer)findViewById(R.id.chronometer);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		// Setup Initial timer state
		View timerLayout = findViewById(R.id.timer_layout);
		boolean whiteBG = sharedPreferences.getBoolean("whitetimerbg", false);
		timerLayout.setBackgroundColor(whiteBG?Color.WHITE:Color.BLACK);
        timerView.setTextColor(whiteBG?Color.BLACK: Color.WHITE);
		mSpeechState = SPEECH_STATE_STARTED;
		
		mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
		

		if(!sharedPreferences.contains("timerTextSize")) {
			mTextSize = timerView.getTextSize();
			Editor editor = sharedPreferences.edit();
			editor.putFloat("timerTextSize", mTextSize);
			editor.commit();
		} 
		else {	
			mTextSize = sharedPreferences.getFloat("timerTextSize", 20.0f);
			timerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
		}
		
		controlsView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mScaleDetector.onTouchEvent(event);
				return true;
			}
		});
		
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
									.translationY(visible ? 0 : mControlsHeight*2)
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
		timerView.setOnClickListener(new View.OnClickListener() {
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
		
		timerView.setOnChronometerTickListener(new OnChronometerTickListener() {
			 
			private static final int VIBRATION_DURATION = 1500;

			private void handleStateChange(View v, Chronometer cm, int bgColor) {
				vibrateIfEnabled();
				v.setBackgroundColor(bgColor);
				cm.setTextColor(Color.argb(255,(~Color.red(bgColor))&0xff, (~Color.green(bgColor))&0xff, (~Color.blue(bgColor))&0xff));
			}

			private void vibrateIfEnabled()
			{
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				boolean vibrationOn = sharedPreferences.getBoolean("vibration", true);
				String durationStr = sharedPreferences.getString("vibration_strength", "1000");
				if (vibrationOn) {
					Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
				    vibrator.vibrate(VibrationEffect.createOneShot(Integer.valueOf(durationStr),VibrationEffect.DEFAULT_AMPLITUDE));
				}
			}
			
			@Override
			public void onChronometerTick(Chronometer chronometer) {
				EditText minTimeEd = findViewById(R.id.minTimeOnTimer);
				Button maxTimeBut = findViewById(R.id.maxTimeOnTimer);
				
				String minString = minTimeEd.getText().toString();
				int min = 0;
				if (!minString.isEmpty()) {
					min = Integer.parseInt(minString)*60000;
				}

				int max = Integer.parseInt(maxTimeBut.getText().toString())*60000;
				int mid = (min + max)/2;				
				long timepassed = SystemClock.elapsedRealtime() - chronometer.getBase();
				View v = findViewById(R.id.timer_layout);
				Chronometer cm = (Chronometer) findViewById(R.id.chronometer);

				int bgColor = Color.BLACK;

				// Preferred color for normal Timer
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				boolean whiteTimerBG = sharedPreferences.getBoolean("whitetimerbg", false);

				if (timepassed >= max && mSpeechState != SPEECH_STATE_MAX_TIME_CROSOSED) {
					bgColor = Color.RED;
					handleStateChange(v,cm,bgColor);
					 mSpeechState = SPEECH_STATE_MAX_TIME_CROSOSED;
				}
				else if (timepassed >= mid && timepassed < max && mSpeechState != SPEECH_STATE_MID_TIME_CROSOSED) {

					bgColor = Color.YELLOW;
					mSpeechState = SPEECH_STATE_MID_TIME_CROSOSED;
					handleStateChange(v,cm,bgColor);
				}
				else if (timepassed >= min && timepassed < mid && mSpeechState != SPEECH_STATE_MIN_TIME_CROSOSED) {

					bgColor = Color.GREEN;
					mSpeechState = SPEECH_STATE_MIN_TIME_CROSOSED;
					handleStateChange(v,cm,bgColor);
				}
				else if (timepassed < min && mSpeechState != SPEECH_STATE_STARTED ){
					bgColor = (whiteTimerBG?Color.WHITE:Color.BLACK);
					handleStateChange(v,cm,bgColor);
				    mSpeechState = SPEECH_STATE_STARTED;
				}


				
				// Show/hide Color symbol depending on current setting
				boolean showcolorsymbol = sharedPreferences.getBoolean("showcolorsymbol", true);
				
				TextView tv = (TextView) findViewById(R.id.colorsymbol);
				

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
				
				if (showtime) {
					cm.setVisibility(View.VISIBLE);
				}
				else
					cm.setVisibility(View.INVISIBLE);
			}
 
		});
		
		// Set Timer editors
		Bundle extras = getIntent().getExtras();
		
		EditText minTimeEd = (EditText)findViewById(R.id.minTimeOnTimer);
		minTimeEd.setText(Integer.toString(extras.getInt("min_time")));
		minTimeEd.addTextChangedListener(this);
		
		Button maxTimeBut = (Button)findViewById(R.id.maxTimeOnTimer);
		maxTimeBut.setText(Integer.toString(extras.getInt("max_time")));
		
		EditText nameTimeEd = (EditText)findViewById(R.id.nameOnTimer);
		nameTimeEd.setText(extras.getString("name"));
		nameTimeEd.setImeOptions(EditorInfo.IME_ACTION_DONE);

		
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
		toggle.setOnTouchListener(mDelayHideTouchListener);
		
		toggle.toggle();
		timerView.start();
	}

    private void setLanguage()
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
    protected void onStop() {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		editor.putFloat("timerTextSize", mTextSize);
		editor.commit();
		super.onStop();
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
    
	@Override
	public void afterTextChanged(Editable arg0) {
		String s = arg0.toString();
		int minNum = 0;
		if (s.length() > 0)
			minNum = Integer.parseInt(arg0.toString());

		Button maxEdit = (Button)findViewById(R.id.maxTimeOnTimer);
			
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
	                   Button maxTimeButton = (Button) findViewById(R.id.maxTimeOnTimer);
	                   maxTimeButton.setText(Integer.toString(minTimeInt + which));
	               }
	        }).show();
	    }	
  
	    private int getMinSpeechTime()
	    {
	    	EditText nameField = (EditText) findViewById(R.id.minTimeOnTimer);
	        String minTimeStr = nameField.getText().toString();
	        int minTimeInt = 0;
	        if (minTimeStr.length() > 0) {
	        	minTimeInt = Integer.parseInt(minTimeStr);
	        }
	    	return minTimeInt;
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
            EditText nameField = (EditText) findViewById(R.id.nameOnTimer);
            se.name = nameField.getText().toString().replace(',', ' ');
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
