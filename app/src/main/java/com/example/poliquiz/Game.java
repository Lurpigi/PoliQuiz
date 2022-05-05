package com.example.poliquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collections;

import asyncs.Recorder;
import banners.MyAlertDialogFragment;
import interfaces.IRecordingDone;


public class Game extends AppCompatActivity implements IRecordingDone {


    private final int PERMISSIONS_REQUEST = 1;
    private static final String TAG = Game.class.getSimpleName();
    Button bttPlay = null;
    ImageButton bttSkip = null;
    private String[] parole = {"la","lu"};
    //private volatile boolean skip = false;
    private int punteggio = 0;


    private TextView ttvStato1 = null;
    private TextView ttvStato2 = null;
    private TextView ttvStato3 = null;
    private boolean pri = false;
    private boolean sec = false;
    private boolean ter = false;
    ConditionVariable wait = null;


    private final int recordinLength = 5; // 5 secondi
    private final int Fs = 44100; //Hz

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        wait = new ConditionVariable();
        bttPlay = findViewById(R.id.bttPlay);
        bttSkip = findViewById(R.id.bttSkip);
        ttvStato1 = findViewById(R.id.statusOne);
        ttvStato2 = findViewById(R.id.statusTwo);
        ttvStato3 = findViewById(R.id.statusThree);
        allToFalse();


        bttPlay.setOnClickListener(view -> onButtonClick());
    }

    private void allToFalse(){
        pri = false;
        sec = false;
        ter = false;
        ttvStato1.setBackgroundColor(getResources().getColor(R.color.red));
        ttvStato2.setBackgroundColor(getResources().getColor(R.color.red));
        ttvStato3.setBackgroundColor(getResources().getColor(R.color.red));
    }


    private void onButtonClick() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) ) {
            onRecordAudioPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay!
                onRecordAudioPermissionGranted();
            } else {
                // permission denied, boo!
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    private ArrayList<String> randomP(){
        ArrayList<String> random = new ArrayList<String>();

        Collections.addAll(random, parole);
        Collections.shuffle(random);
        //random.get(0)
        return random;
    }

    private void setEnv(String s){

    }

    //inizio gioco
    private void onRecordAudioPermissionGranted() {

        Recorder recorder = new Recorder(this, this, recordinLength, Fs );
        if(recorder.isAudioRecordInitialized()){
            Log.i(TAG,"creato oggetto recorder");
            ArrayList<String> parolepartita = randomP();
            bttSkip.setOnClickListener(view -> onClickskip());
            bttPlay.setOnClickListener(view -> onClickGame(recorder));
            bttPlay.setBackground(getDrawable(R.drawable.ic_mic));
            bttPlay.setText("");
            ttvStato1.setVisibility(View.VISIBLE);
            ttvStato2.setVisibility(View.VISIBLE);
            ttvStato3.setVisibility(View.VISIBLE);
            bttSkip.setVisibility(View.VISIBLE);

            new Thread(new Runnable() {                                         //gioco
                @Override
                public void run() {
                    for (int i = 0; i < parolepartita.size(); i++) {
                        setEnv(parolepartita.get(i));
                        wait.close();
                        allToFalse();
                        wait.block();
                    }
                    Log.i(TAG,"finito");
                    //TODO: AVVISO PUNTEGGIO; RIGIOCA - HOME
                    Game.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(Game.this, "Gioco finito kohone", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();

        }
    }

    public void onClickskip(){
        wait.open();
        Log.i(TAG,"skip");
    }

    public void onClickGame(Recorder recorder) {
        // pre execute
        Log.i(TAG,"start recording");
        bttPlay.setBackground(getDrawable(R.drawable.ic_micred));
        bttPlay.setEnabled(false);
        recorder.go();

    }



    //fine recording
    @Override
    public void onRecordingDone(int result, short[] audioData) {
        Log.i(TAG,"onRecordingDone");
        bttPlay.setBackground(getDrawable(R.drawable.ic_mic));
        result = -1;        //da cancellare
        switch(result){            //risultato
            case 0:
                if(!pri) {
                    pri = true;
                    punteggio++;
                    ttvStato1.setBackgroundColor(getResources().getColor(R.color.green));
                }
                break;
            case 1:
                if(!sec) {
                    sec = true;
                    punteggio++;
                    ttvStato2.setBackgroundColor(getResources().getColor(R.color.green));
                }
                break;
            case 2:
                if(!ter) {
                    ter = true;
                    punteggio++;
                    ttvStato3.setBackgroundColor(getResources().getColor(R.color.green));
                }
                break;
        }
        bttPlay.setEnabled(true);
        if(pri && sec && ter)
            wait.open();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}