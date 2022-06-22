package com.example.poliquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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



import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import asyncs.Recorder;
import interfaces.IRecordingDone;



public class Game extends AppCompatActivity implements IRecordingDone {


    private static final String TAG = Game.class.getSimpleName();
    private Button bttPlay = null;
    private ImageButton bttSkip = null;

    private Button retry = null;
    private Button home = null;



    private String cartella = null;
    //private volatile boolean skip = false;
    private int punteggio = 0;
    SharedPreferences.Editor editor;
    private AssetManager am;
    private ImageView img = null;
    private TextView time = null;
    private ImageView flag1 = null;
    private ImageView flag2 = null;
    private ImageView flag3 = null;
    private ImageView[] flags;
    private TextView ttvStato1 = null;
    private TextView ttvStato2 = null;
    private TextView ttvStato3 = null;
    private boolean pri = false;
    private boolean sec = false;
    private boolean ter = false;
    private boolean fine = false;
    ConditionVariable wait = null;
    private String[] PERMISSIONS = new String[] {
            Manifest.permission.RECORD_AUDIO
    };

    private CountDownTimer timer;
    private Thread turni;


    private final int recordinLength = 1;
    private final int Fs = 44100; //Hz

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        //inizializzazioni variabili
        am = this.getAssets();
        wait = new ConditionVariable();
        img = findViewById(R.id.img);
        bttPlay = findViewById(R.id.bttPlay);
        bttSkip = findViewById(R.id.bttSkip);
        ttvStato1 = findViewById(R.id.statusOne);
        ttvStato2 = findViewById(R.id.statusTwo);
        ttvStato3 = findViewById(R.id.statusThree);
        flag1 = findViewById(R.id.firstFlag);
        flag2 = findViewById(R.id.secondFlag);
        flag3 = findViewById(R.id.thirdFlag);
        flags = new ImageView[]{flag1, flag2, flag3};
        time = findViewById(R.id.time);


        editor = this.getPreferences(this.MODE_PRIVATE).edit();


        allToFalse();



        bttPlay.setOnClickListener(view -> onButtonClick());
    }

    //inizializza le variabili di controllo e imposta i quadrati in rosso
    private void allToFalse(){
        pri = false;
        sec = false;
        ter = false;
        ttvStato1.setBackgroundColor(getResources().getColor(R.color.red));
        ttvStato2.setBackgroundColor(getResources().getColor(R.color.red));
        ttvStato3.setBackgroundColor(getResources().getColor(R.color.red));
    }

    //mostra il risultato a fine gioco e chiede se si vuole riprovare o tornare nella home
    public void showCustomDialog(int punteggio) {
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
        TextView tmp = dialog.findViewById(R.id.puntfin);
        if(punteggio == 1)
            tmp.append(" " + String.valueOf(punteggio) + " punto");
        else
            tmp.append(" "+ String.valueOf(punteggio) + " punti");

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent _i = new Intent();
                //passiamo il punteggio finale alla mainactivity
                _i.putExtra(getString(R.string.passpunt), String.valueOf(punteggio));
                setResult(Activity.RESULT_OK, _i);
                finish();
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getString(R.string.activityGame));
                dialog.dismiss();
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }


    //quando si clicca play viene controllato se sono stati concessi i permessi di registrazione audio e solo in quel caso viene avviato il gioco
    private void onButtonClick() {
        if (!hasPermissions(Game.this,PERMISSIONS)) {
            ActivityCompat.requestPermissions(Game.this,PERMISSIONS,1);
        }
        else
            onRecordAudioPermissionGranted();
    }

    //controllo dei permessi
    private boolean hasPermissions(Context context, String... PERMISSIONS) {
        if (context != null && PERMISSIONS != null)
            for (String permission: PERMISSIONS)
                if (ActivityCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
        return true;
    }

    //se si accettano i permessi parte il gioco
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "permessi approvati");
                    onRecordAudioPermissionGranted();

                } else {
                    Log.i("TAG", "permission denied by user");
                    Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    //ordina casualmente le parole
    private ArrayList<String> randomP(){
        ArrayList<String> random = new ArrayList<String>();

        Collections.addAll(random, MainActivity.parole);
        Collections.shuffle(random);

        return random;
    }


    //impostiamo le variabili del gioco per questa parola del turno
    //string S -> nome parola del turno(quindi anche della cartella)
    private void setEnv(String s){
        cartella = s;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    //impostiamo immagine
                    InputStream is = am.open("parole/"+s+"/"+s+".jpg");

                    Bitmap b = BitmapFactory.decodeStream(is);
                    int i=0;
                    //ciclo per salvarsi l immagine delle bandiere (possibilita di espansione)
                    for (String lan : MainActivity.lang){
                        try {
                            InputStream istmp = am.open("bandiere/"+lan+".png");
                            Bitmap f = BitmapFactory.decodeStream(istmp);
                            flags[i].setImageBitmap(f);
                            i++;
                        } catch (IOException ignored) {
                            ;
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                    }

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

        Recorder recorder = new Recorder(this, this, recordinLength, Fs, this);
        if(recorder.isAudioRecordInitialized()){
            Log.i(TAG,"creato oggetto recorder");
            //rendo visibile tutti gli elementi e cambio l' onclick di play
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
            timer = new CountDownTimer(180_000, 1000) {

                public void onTick(long millisUntilFinished) {
                    String sec = String.format("%02d:%02d", ((millisUntilFinished / 1000) / 60), ((millisUntilFinished / 1000) % 60));

                    time.setText(sec);
                    if(fine)
                        cancel();
                    //here you can have your logic to set text to edittext
                }

                public void onFinish() {
                    time.setText(getString(R.string.fine));
                    fine = true;
                    wait.open();
                }

            }.start();

            turni = new Thread(new Runnable() {                                         //gioco
                @Override
                public void run() {
                    for (int i = 0; i < parolepartita.size(); i++) {
                        setEnv(parolepartita.get(i));
                        wait.close();   //chiudo wait
                        allToFalse();
                        wait.block();   //blocco finche wait != open
                        if(fine)
                            break;

                    }
                    Log.i(TAG,"finito");
                    fine=true;

                    Game.this.runOnUiThread(new Runnable() {
                        public void run() {

                            editor.putInt(getString(R.string.saved_high_score_key), punteggio);
                            editor.commit();

                            //Toast.makeText(Game.this, "Gioco finito", Toast.LENGTH_SHORT).show();
                            bttPlay.setEnabled(false);
                            bttSkip.setEnabled(false);
                            showCustomDialog(punteggio);
                        }
                    });
                }
            });
            turni.start();

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
        bttSkip.setEnabled(false);
        recorder.go(cartella,am);

    }



    //fine recording
    @Override
    public void onRecordingDone(int result, String resultS) {
        Log.i(TAG,"onRecordingDone");
        bttPlay.setBackground(getDrawable(R.drawable.ic_mic));
        Toast.makeText(Game.this, resultS, Toast.LENGTH_SHORT).show();

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
        bttSkip.setEnabled(true);
        if(pri && sec && ter) {
            wait.open();
            Toast.makeText(Game.this,getString(R.string.turnofatto) , Toast.LENGTH_LONG).show();
        }
    }




    @Override
    protected void onDestroy() {

        super.onDestroy();
        timer.cancel();
        turni.interrupt();

    }
}