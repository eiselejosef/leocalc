package de.sepp.mobile.leocalc.app;

import de.sepp.mobile.leocalc.app.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
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

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        final TextView tvFormel = (TextView) findViewById(R.id.textViewFormular);
        // addition or subtraction
        int choice = (int) (Math.random() * 2);

        switch (choice) {
            case 0:  tvFormel.setText(generateFormularAddition()); break;
            case 1:  tvFormel.setText(generateFormularSubtraction()); break;
            default: tvFormel.setText(generateFormularSubtraction()); break;
        }

        final TextView tvResult = (TextView) findViewById(R.id.editViewResult);
        tvResult.setText("");

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
                    // Sound
                    MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.oleole);
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.start();

                    mPlayer.setOnCompletionListener(
                            new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer arg0) {
                                    finish();
                                    startActivity(getIntent());
                                }
                            }
                    );


                } else {
                    contentView.setBackgroundColor(Color.RED);
                }


            }
        });




        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.button_enter).setOnTouchListener(mDelayHideTouchListener);
    }

    private boolean doCalc(String formel, long result) {

        if (formel.contains("+")) {
            long summand1 = Long.parseLong(formel.substring(0, formel.indexOf("+")).trim());
            long summand2 = Long.parseLong(formel.substring(formel.indexOf("+") + 1, formel.indexOf("=")).trim());

            return (summand1 + summand2 == result);
        } else {
            long minuend = Long.parseLong(formel.substring(0, formel.indexOf("-")).trim());
            long subtrahend = Long.parseLong(formel.substring(formel.indexOf("-") + 1, formel.indexOf("=")).trim());

            return (minuend - subtrahend == result);
        }
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

    private String generateFormularAddition() {
        Random randomGenerator = new Random();
        String summand1 = String.valueOf(randomGenerator.nextInt(200));
        String summand2 = String.valueOf(randomGenerator.nextInt(200));

        // 34 + 12 =
        return summand1 + " + " + summand2 + " =";
    }

    private String generateFormularSubtraction() {
        Random randomGenerator = new Random();
        String minuend = String.valueOf(randomGenerator.nextInt(200));
        String subtrahend = String.valueOf(randomGenerator.nextInt(200));
        while (Integer.parseInt(minuend) < Integer.parseInt(subtrahend)) {
            minuend = String.valueOf(randomGenerator.nextInt(200));
            subtrahend = String.valueOf(randomGenerator.nextInt(200));
        }

        // 34 - 12 =
        return minuend + " - " + subtrahend + " =";
    }
}
