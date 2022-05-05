package asyncs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import Wave.WavIO;
import interfaces.IRecordingDone;

public class Recorder {


    private final String TAG = "Recorder";

    private int recordingLenth;
    private int Fs;             //Frequenza in Hz
    private int nSamples;
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



    public void go(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //doinbackground
                doRecording();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // onpostexecute
                        audioRecord.stop();
                        iRecordingDone.onRecordingDone(1, audioData);

                    }
                });
            }
        }).start();
    }

    private void doRecording(){

        audioRecord.startRecording();
        audioRecord.read(audioData,0,nSamples);


        byte[] dataByte = short2byte(audioData);



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
