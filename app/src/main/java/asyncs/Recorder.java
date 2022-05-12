package asyncs;

import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


import androidx.core.app.ActivityCompat;


import com.example.poliquiz.MainActivity;
import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.sun.media.sound.FFT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;

import complex.Complex;
import interfaces.IRecordingDone;

public class Recorder {


    private final String TAG = "Recorder";

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

                        InputStream[] is = new InputStream[3];
                        int i=0;
                        for (String lan : MainActivity.lang){
                            try {
                                is[i] = am.open("parole/"+cartella+"/" + lan + ".wav");
                                i++;
                                if(i==3)
                                    break;
                            } catch (IOException ignored) {
                                ;
                            }
                        }
                        byte[] primo=null;
                        byte[] secondo=null;
                        byte[] terzo=null;


                        try {
                            primo = readFully(is[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            secondo = readFully(is[1]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            terzo = readFully(is[2]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        float punt1 = similarity(datab,primo);
                        float punt2 = similarity(datab,secondo);
                        float punt3 = similarity(datab,terzo);

                        iRecordingDone.onRecordingDone(-1, audioData);

                    }
                });
            }
        }).start();
    }



    //intelligenza artificiale https://www.fon.hum.uva.nl/praat/
    private float similarity(byte[] registrazione, byte[] corretto){

        //TODO inizializzarli
        Wave w1 = new Wave(new WaveHeader(),registrazione);
        Wave w2 = new Wave(new WaveHeader(),corretto);


        FingerprintSimilarity fpsc1 = w2.getFingerprintSimilarity(w1);

        float scorec = fpsc1.getScore();
        float simc= fpsc1.getSimilarity();

        Log.e(TAG,"score: "+scorec+" similarity: "+simc);





        return -1;
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
