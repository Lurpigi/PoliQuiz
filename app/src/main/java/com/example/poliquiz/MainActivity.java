package com.example.poliquiz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {


    private final String TAG = "MainActivity";
    private Button bttStart = null;
    private Button bttInfo = null;
    private TextView Record = null;
    public static String[] parole = {"acqua", "asino", "biscotto", "cane", "cuscino", "fuoco", "lupo", "moto", "sedia",
            "armadio", "automobile", "borsa", "ciao", "dio", "gatto", "matita", "motore", "sole",
            "ascensore", "benzina", "calendario", "cinema", "forbici", "luna", "mela", "patata", "televisione", "letto", "mela", "nero", "si"};
    public static String[] lang = {"it","gb","us","jp","de","fr","es","pt","gr","nl","sa","ru"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bttStart = findViewById(R.id.bttStart);
        bttInfo = findViewById(R.id.bttInfo);
        Record = findViewById(R.id.tvRecord);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);


        /*

            edit highscore
            SharedPreferences.Editor editor = this.getPreferences(Context.MODE_PRIVATE).edit();
            editor.putInt(getString(R.string.saved_high_score_key), newHighScore);
            editor.apply();

        */

        int defaultValue = getResources().getInteger(R.integer.saved_high_score_default_key);
        int highScore = sharedPref.getInt(getString(R.string.saved_high_score_key), defaultValue);
        String newRecord = getString(R.string.record) + " " + String.valueOf(highScore);
        Record.setText(newRecord);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    0);
        }

        bttInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(TAG,"premuto info");
                Intent intent = new Intent(getString(R.string.activityInfo));
                startActivity(intent);
            }
        });

        bttStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(TAG,"premuto GO!");
                Intent intent = new Intent(getString(R.string.activityGame));
                startActivity(intent);
            }
        });



    }

}