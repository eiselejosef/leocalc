package de.sepp.mobile.leocalc.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import java.util.Random;

import de.sepp.mobile.leocalc.app.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class EinmalEinsActivity extends Activity {
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


    private static final int MAX_VALUE = 10;
    private int count_success = 0;

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

        setContentView(R.layout.activity_einmaleins);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        final TextView tvFormel = (TextView) findViewById(R.id.textViewFormular);
        tvFormel.setText(generateFormularMulti());

        final TextView tvResult = (TextView) findViewById(R.id.editViewResult);
        final TextView tvCount = (TextView) findViewById(R.id.countSuccess);
        tvResult.setText("");

        final Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        timer.stop();
        timer.setBase(SystemClock.elapsedRealtime());

        if (getIntent().getExtras() != null) {
            count_success = getIntent().getExtras().getInt("count_success");
            tvCount.setText("" + count_success);

            if ( getIntent().getExtras().getLong("timer") != 0) {
                timer.setBase(getIntent().getExtras().getLong("timer"));
            }
        }
        timer.start();

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
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
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
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

        final Button button = (Button) findViewById(R.id.button_enter);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String formel = tvFormel.getText().toString();
                String res = tvResult.getText().toString();

                long result = 0;
                if (res != null && !res.isEmpty()) {
                    result = Long.parseLong(res.trim());
                }
                if (doCalc(formel, result)) {
                    contentView.setBackgroundColor(Color.GREEN);
                    tvCount.setText("" + ++count_success);

                    if (count_success >= 10) {
                        tvFormel.setText("SUPER !!!");
                        timer.stop();
                    } else {
                        finish();
                        getIntent().putExtra("count_success", count_success);
                        getIntent().putExtra("timer", timer.getBase());
                        startActivity(getIntent());
                    }

                } else {
                    contentView.setBackgroundColor(Color.RED);
                    count_success = 0;
                }
            }
        });



        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.button_enter).setOnTouchListener(mDelayHideTouchListener);
    }

    private boolean doCalc(String formel, long result) {

        long multi1 = Long.parseLong(formel.substring(0, formel.indexOf("*")).trim());
        long multi2 = Long.parseLong(formel.substring(formel.indexOf("*") + 1, formel.indexOf("=")).trim());

        return (multi1 * multi2 == result);
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

    private String generateFormularMulti() {
        Random randomGenerator = new Random();
        String multi1 = String.valueOf(randomGenerator.nextInt(MAX_VALUE));
        String multi2 = String.valueOf(randomGenerator.nextInt(MAX_VALUE));

        // 5 * 4 =
        return multi1 + " * " + multi2 + " =";
    }

}
