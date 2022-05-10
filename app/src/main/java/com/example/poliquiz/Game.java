package com.example.poliquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import asyncs.Recorder;
import interfaces.IRecordingDone;


public class Game extends AppCompatActivity implements IRecordingDone {


    private final int PERMISSIONS_REQUEST = 1;
    private static final String TAG = Game.class.getSimpleName();
    private Button bttPlay = null;
    private ImageButton bttSkip = null;

    private Button retry = null;
    private Button home = null;

    private String[] parole = null;
    //private volatile boolean skip = false;
    private int punteggio = 0;

    private ImageView img = null;
    private TextView time = null;
    private TextView ttvStato1 = null;
    private TextView ttvStato2 = null;
    private TextView ttvStato3 = null;
    private boolean pri = false;
    private boolean sec = false;
    private boolean ter = false;
    private boolean fine = false;
    ConditionVariable wait = null;


    private final int recordinLength = 5; // 5 secondi
    private final int Fs = 44100; //Hz

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        wait = new ConditionVariable();
        img = findViewById(R.id.img);
        bttPlay = findViewById(R.id.bttPlay);
        bttSkip = findViewById(R.id.bttSkip);
        ttvStato1 = findViewById(R.id.statusOne);
        ttvStato2 = findViewById(R.id.statusTwo);
        ttvStato3 = findViewById(R.id.statusThree);
        time = findViewById(R.id.time);

        Intent _intent = getIntent();
        parole = _intent.getStringArrayExtra(getString(R.string.parole));
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

    public void showCustomDialog() {
        final Dialog dialog = new Dialog(this);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(false);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.dialog_fine);

        //Initializing the views of the dialog.

        retry = dialog.findViewById(R.id.riprova);
        home = dialog.findViewById(R.id.tornahome);


        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getString(R.string.activityGame));
                startActivity(intent);
                intent.putExtra(getString(R.string.parole), parole);
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
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
        AssetManager am = this.getAssets();
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    InputStream is = am.open("parole/"+s+"/"+s+".jpg");

                    Bitmap b = BitmapFactory.decodeStream(is);

                    img.setImageBitmap(b);
                    Log.i(TAG,"IMMAGINE"+s+" MESSA");
                } catch (IOException e) {
                    Log.i(TAG,"IMMAGINE"+s+"NON MESSA");
                    e.printStackTrace();
                }

            }
        });


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
            //3 minuti -> 180 secondi
            new CountDownTimer(180_000, 1000) {

                public void onTick(long millisUntilFinished) {
                    String sec = String.format("%02d:%02d", ((millisUntilFinished / 1000) / 60), ((millisUntilFinished / 1000) % 60));

                    time.setText(sec);
                    //here you can have your logic to set text to edittext
                }

                public void onFinish() {
                    time.setText(getString(R.string.fine));
                    fine = true;
                    wait.open();
                }

            }.start();

            new Thread(new Runnable() {                                         //gioco
                @Override
                public void run() {
                    for (int i = 0; i < parolepartita.size(); i++) {
                        setEnv(parolepartita.get(i));
                        wait.close();
                        allToFalse();
                        wait.block();
                        if(fine)
                            break;

                    }
                    Log.i(TAG,"finito");
                    //TODO: AVVISO PUNTEGGIO; RIGIOCA - HOME
                    Game.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //Toast.makeText(Game.this, "Gioco finito", Toast.LENGTH_SHORT).show();
                            bttPlay.setEnabled(false);
                            bttSkip.setEnabled(false);
                            showCustomDialog();
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