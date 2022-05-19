package asyncs;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


import androidx.core.app.ActivityCompat;


import com.example.poliquiz.MainActivity;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;

import com.google.protobuf.ByteString;


import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import interfaces.IRecordingDone;
import speech_services.Speech_Services;


public class Recorder {


    private final String TAG = "Recorder";

    private int recordingLenth;
    private int Fs;             //Frequenza in Hz
    private int nSamples;

    private Context c;


    private static final String PREFS = "SpeechService";
    private static final String PREF_ACCESS_TOKEN_VALUE = "access_token_value";
    private static final String PREF_ACCESS_TOKEN_EXPIRATION_TIME = "access_token_expiration_time";

    private String ris1 = null;
    private String ris2 = null;
    private String ris3 = null;

    private Activity activity;
    private IRecordingDone iRecordingDone;
    ArrayList<String> languageList = new ArrayList<>();


    private short[] audioData = null; // Campioni audio PCM, 16bit
    private AudioRecord audioRecord = null;
    private InputStream is;


    public Recorder(Activity activity, IRecordingDone iRecordingDone, int recordingLength, int Fs, Context c) {
        this.recordingLenth = recordingLength;
        this.Fs = Fs;
        this.activity = activity;
        this.iRecordingDone = iRecordingDone;
        this.nSamples = this.recordingLenth * this.Fs;
        this.audioData = new short[this.nSamples];
        this.c = c;



        languageList.add("es-ES");
        languageList.add("it-IT");
        languageList.add("en-GB");
        languageList.add("en-US");
        languageList.add("ja-JP");
        languageList.add("de-DE");
        languageList.add("fr-FR");
        languageList.add("pt-PT");
        languageList.add("el-GR");
        languageList.add("nl-NL");
        languageList.add("ar-SA");
        languageList.add("ru-RU");

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

        Speech_Services speech_services = new Speech_Services(languageList,c);


        new Thread(new Runnable() {
            @Override
            public void run() {
                //doinbackground
                byte[] datab = doRecording();
                ArrayList<String> lans = new ArrayList<String>();

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
                                for(String language : languageList)
                                    if(language.contains(lan.toUpperCase(Locale.ROOT))) {
                                        lans.add(language);
                                        break;
                                    }
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

                        String voce = null;


                        try {

                            voce = speech_services.transcribeMultiLanguage(datab,lans);
                            ris1 = speech_services.transcribeMultiLanguage(primo,lans);
                            ris2 = speech_services.transcribeMultiLanguage(secondo,lans);
                            ris3 = speech_services.transcribeMultiLanguage(terzo,lans);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        int result = -1;
                        if(voce!=null){
                            if(voce == ris1)
                                result = 0;
                            else if(voce == ris2)
                                result = 1;
                            else if(voce == ris3)
                                result = 2;
                        }


                        iRecordingDone.onRecordingDone(result, audioData);

                    }
                });
            }
        }).start();
    }






    //intelligenza artificiale https://www.fon.hum.uva.nl/praat/
   /* private float similarity(byte[] registrazione, byte[] corretto){

        //TODO inizializzarli
        Wave w1 = new Wave(new WaveHeader(),registrazione);
        Wave w2 = new Wave(new WaveHeader(),corretto);


        FingerprintSimilarity fpsc1 = w2.getFingerprintSimilarity(w1);

        float scorec = fpsc1.getScore();
        float simc= fpsc1.getSimilarity();

        Log.e(TAG,"score: "+scorec+" similarity: "+simc);





        return -1;
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
