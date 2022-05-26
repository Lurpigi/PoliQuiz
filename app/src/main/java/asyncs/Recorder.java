package asyncs;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Environment;
import android.util.Log;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;


import com.example.poliquiz.MainActivity;



import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import Wave.WavIO;
import interfaces.IRecordingDone;



public class Recorder {


    private final String TAG = "Recorder";

    private int recordingLenth;
    private int Fs;             //Frequenza in Hz
    private int nSamples;

    private Context c;


    private String ris1 = null;
    private String ris2 = null;
    private String ris3 = null;
    private String FILE_NAME ="Rec.wav";
    private final String PATH = "CANCELLAMI";

    private Activity activity;
    private IRecordingDone iRecordingDone;
    ConditionVariable waitreq = null;
    ArrayList<String> languageList = new ArrayList<>();


    private short[] audioData = null; // Campioni audio PCM, 16bit
    private AudioRecord audioRecord = null;


    public Recorder(Activity activity, IRecordingDone iRecordingDone, int recordingLength, int Fs, Context c) {
        this.recordingLenth = recordingLength;
        this.Fs = Fs;
        this.activity = activity;
        this.iRecordingDone = iRecordingDone;
        this.nSamples = this.recordingLenth * this.Fs;
        this.audioData = new short[this.nSamples];
        this.c = c;
        waitreq = new ConditionVariable();


        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    0);

        }else {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, this.Fs, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 2 * this.nSamples);
        }
    }

    public boolean isAudioRecordInitialized(){
        return audioRecord!=null;
    }

    public void go(String cartella,AssetManager am){



        new Thread(new Runnable() {
            @Override
            public void run() {
                //doinbackground
                byte[] datab = doRecording();


                activity.runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        // onpostexecute
                        audioRecord.stop();

                        //InputStream[] is = new InputStream[3];
                        BufferedReader[] is = new BufferedReader[3];

                        int i=0;
                        for (String lan : MainActivity.lang){
                            try {
                                //is[i] = new InputStream(am.open("parole/"+cartella+"/" + lan + ".wav"));//wav
                                BufferedReader reader = new BufferedReader(new InputStreamReader(am.open("parole/"+cartella+"/" + lan + ".txt")));
                                is[i]=reader;
                                i++;
                                if(i==3)
                                    break;
                            } catch (IOException ignored) {
                                ;
                            }
                        }

                        //byte[] per i file wav
                        String primo=null;
                        String secondo=null;
                        String terzo=null;


                        try {
                            //primo = readFully(is[0]);
                            primo = is[0].readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            //secondo = readFully(is[1]);
                            secondo = is[1].readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            //terzo = readFully(is[2]);
                            terzo = is[2].readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        final String[] voce = {null};


                        try{
                            // datab in WAV
                            String filename = "myrec";
                            try (FileOutputStream fos = c.openFileOutput(filename, Context.MODE_PRIVATE)) {
                                WavIO wavIo = new WavIO("_fullPath", 16, 1, 1, Fs, 2, 16, datab);
                                wavIo.saveInt(fos);
                                Log.i(TAG,"Creazione file riuscita");
                            }

                            File file = new File(c.getFilesDir(), filename);
                            waitreq.close();
                            Thread gfgThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try  {
                                        voce[0] = Request.newR(file);
                                        //Thread.sleep(100);
                                        waitreq.open();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        waitreq.open();
                                    }
                                }
                            });
                            gfgThread.start();
                            waitreq.block();

                            // chiamarci classe request
                            //
                            Log.e(TAG, voce[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        int result = -1;

                        if(voce[0] !=null){
                            if(voce[0].equals(primo))
                                result = 0;
                            else if(voce[0].equals(secondo))
                                result = 1;
                            else if(voce[0].equals(terzo))
                                result = 2;
                        }


                        iRecordingDone.onRecordingDone(result, audioData);

                    }
                });
            }
        }).start();
    }






    //intelligenza artificiale https://www.fon.hum.uva.nl/praat/
   /* private float similarity(){

        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
    .requireWifi()
    .build();
FirebaseModelDownloader.getInstance()
    .getModel("polyquiz-1", DownloadType.LOCAL_MODEL, conditions)
    .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
      @Override
      public void onSuccess(CustomModel model) {
        // Download complete. Depending on your app, you could enable
        // the ML feature, or switch from the local model to the remote
        // model, etc.
         File modelFile = model.getFile();
        if (modelFile != null) {
            interpreter = new Interpreter(modelFile);




            modelOutput.rewind();
FloatBuffer probabilities = modelOutput.asFloatBuffer();
try {
    BufferedReader reader = new BufferedReader(
            new InputStreamReader(getAssets().open("custom_labels.txt")));
    for (int i = 0; i < probabilities.capacity(); i++) {
        String label = reader.readLine();
        float probability = probabilities.get(i);
        Log.i(TAG, String.format("%s: %1.4f", label, probability));
    }
} catch (IOException e) {
    // File not found?
}
        }
      }
    });
    }*/

    private byte[] doRecording(){

        audioRecord.startRecording();
        audioRecord.read(audioData,0,nSamples);


        byte[] dataByte = short2byte(audioData);
        return dataByte;


    }


    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }
}