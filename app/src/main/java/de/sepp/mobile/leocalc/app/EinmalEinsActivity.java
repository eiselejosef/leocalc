package de.sepp.mobile.leocalc.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;


public class EinmalEinsActivity extends Activity {

    private static final int MAX_VALUE = 10;
    private int count_success = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_einmaleins);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        final TextView tvFormel = (TextView) findViewById(R.id.textViewFormular);
        tvFormel.setText(generateFormularMulti());

        final EditText tvResult = (EditText) findViewById(R.id.editViewResult);
        final TextView tvCount = (TextView) findViewById(R.id.countSuccess);
        tvResult.setText("");
        // to always show soft keyboard
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                tvResult.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                tvResult.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
            }
        }, 200);


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

    }


    private boolean doCalc(String formel, long result) {

        long multi1 = Long.parseLong(formel.substring(0, formel.indexOf("*")).trim());
        long multi2 = Long.parseLong(formel.substring(formel.indexOf("*") + 1, formel.indexOf("=")).trim());

        return (multi1 * multi2 == result);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private String generateFormularMulti() {
        Random randomGenerator = new Random();
        String multi1 = String.valueOf(randomGenerator.nextInt(MAX_VALUE));
        String multi2 = String.valueOf(randomGenerator.nextInt(MAX_VALUE));

        // 5 * 4 =
        return multi1 + " * " + multi2 + " =";
    }

}
