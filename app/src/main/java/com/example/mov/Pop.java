package com.example.mov;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.SeekBar;

/**
 * Created by Himchan Song
 */

public class Pop extends Activity {
    int speed;
    Intent fromMainIntent;
    Intent result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        SeekBar vSeekBar = (SeekBar) findViewById(R.id.seekBar);

        fromMainIntent = getIntent();
        speed = fromMainIntent.getIntExtra("speed", 0);
        result = new Intent(Pop.this, MainActivity.class);

        vSeekBar.setMax(10);
        vSeekBar.setProgress(speed);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.8), (int) (height*.3));

        vSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                speed = seekBar.getProgress();
                result.putExtra("speed", speed);
                setResult(RESULT_OK, result);
            }
        });
    }
}
