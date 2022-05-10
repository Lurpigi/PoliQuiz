package asyncs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.poliquiz.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import Wave.WavIO;
import interfaces.IRecordingDone;

public class Recorder {


    private final String TAG = "Recorder";

    private String[] lang = {"it","uk","us","jp","de","fr","es","pt","el","du"};
    private int recordingLenth;
    private int Fs;             //Frequenza in Hz
    private int nSamples;
    private String parola;

    private Activity activity;
    private IRecordingDone iRecordingDone;

    private short[] audioData = null; // Campioni audio PCM, 16bit
    private AudioRecord audioRecord = null;


    public Recorder(Activity activity, IRecordingDone iRecordingDone, int recordingLength, int Fs) {
        this.recordingLenth = recordingLength;
        this.Fs = Fs;
        this.activity = activity;
        this.iRecordingDone = iRecordingDone;
        this.nSamples = this.recordingLenth * this.Fs;
        this.audioData = new short[this.nSamples];

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

    public static byte[] readFully(InputStream input) throws IOException
    {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

    public void go(String cartella,AssetManager am){

        new Thread(new Runnable() {
            @Override
            public void run() {
                //doinbackground
                byte[] datab = doRecording();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // onpostexecute
                        audioRecord.stop();
                        //TODO richiamare funzione IA sulle 3 e su data

                        InputStream[] is = new InputStream[3];
                        int i=0;
                        for (String lan : lang){
                            try {
                                is[i] = am.open("parole/"+cartella+"/" + lan + ".wav");
                                i++;
                                if(i==3)
                                    break;
                            } catch (IOException ignored) {
                                ;
                            }
                        }


                        try {
                            byte[] primo = readFully(is[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            byte[] secondo = readFully(is[1]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            byte[] terzo = readFully(is[2]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //int result = similarity(x,y);

                        iRecordingDone.onRecordingDone(-1, audioData);

                    }
                });
            }
        }).start();
    }

    //intelligenza artificiale https://www.fon.hum.uva.nl/praat/
    private float similarity(byte[] registrazione, byte[] corretto){


        return 0;
    }

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
