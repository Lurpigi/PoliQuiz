package com.example.poliquiz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
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
    public static String[] parole = {"acqua", "asino", "borsa", "calendario", "cane", "cuore", "letto", "lupo", "patata", "sole"};
    public static String[] lang = {"us","fr","es"};
    int highScore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bttStart = findViewById(R.id.bttStart);
        bttInfo = findViewById(R.id.bttInfo);
        Record = findViewById(R.id.tvRecord);

        //mappa varibili interne, viene usata per salvare il record
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);


        /*

            edit highscore
            SharedPreferences.Editor editor = this.getPreferences(Context.MODE_PRIVATE).edit();
            editor.putInt(getString(R.string.saved_high_score_key), newHighScore);
            editor.apply();

        */

        int defaultValue = getResources().getInteger(R.integer.saved_high_score_default_key);
        highScore = sharedPref.getInt(getString(R.string.saved_high_score_key), defaultValue);
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
                startActivityForResult(intent,42);
            }
        });



    }

    //quando finisce il gioco viene aggiornato il record
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == 42){
            if(resultCode == Activity.RESULT_OK){
                String _s = data.getStringExtra(getString(R.string.passpunt));
                if(Integer.parseInt(_s) > highScore){
                    String newRecord = getString(R.string.record) + " " + _s;
                    Record.setText(newRecord);
                    SharedPreferences.Editor editor = this.getPreferences(this.MODE_PRIVATE).edit();
                    editor.putInt(getString(R.string.saved_high_score_key), Integer.parseInt(_s));
                    editor.apply();
                }

            }else{
                Log.i(TAG,"Request code Not OK");
            }

        }else{
            Log.i(TAG,"Request code error");
        }
    }

}