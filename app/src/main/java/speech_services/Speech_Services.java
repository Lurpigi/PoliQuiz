package speech_services;

import android.content.Context;
import android.util.Log;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

public class Speech_Services {


    private final String TAG = "Speech_Services";
    private SpeechClient speechClient = null;
    private Context context;

    public Speech_Services(ArrayList<String> languageList, Context context) {

        this.context = context;
    }

    public String transcribeMultiLanguage(byte[] content,ArrayList<String> languageList) throws Exception {
        String endPoint = "eu-speech.googleapis.com:443";
        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(new SpeechCredentialsProvider(context).getCredential());
        SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();

        ArrayList<String> alternativelan = new ArrayList<String>();
        alternativelan.add(languageList.get(1));
        alternativelan.add(languageList.get(2));

        try{
            SpeechClient speechClient = SpeechClient.create(settings);
            RecognitionAudio recognitionAudio =
                    RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();


            // Configure request to enable multiple languages
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(44100)
                            .setLanguageCode(languageList.get(0))
                            .addAllAlternativeLanguageCodes(alternativelan)
                            .build();
            // Perform the transcription request

            //RecognizeResponse recognizeResponse = speechClient.recognize(config, recognitionAudio);

            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speechClient.longRunningRecognizeAsync(config, recognitionAudio);

            while (!response.isDone()) {
                //System.out.println("Waiting for response...");
                //Thread.sleep(1000);
                ;
            }

            List<SpeechRecognitionResult> results = response.get().getResultsList();

            // Print out the results
            for (/*SpeechRecognitionResult result : recognizeResponse.getResultsList()*/SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                Log.e(TAG,"Transcript : " + alternative.getTranscript());
                return alternative.getTranscript();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String transcribeMultiLanguageWav(byte[] content,ArrayList<String> languageList) throws Exception {
        String endPoint = "eu-speech.googleapis.com:443";
        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(new SpeechCredentialsProvider(context).getCredential());
        SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();

        ArrayList<String> alternativelan = new ArrayList<String>();
        alternativelan.add(languageList.get(1));
        alternativelan.add(languageList.get(2));

        try{
            SpeechClient speechClient = SpeechClient.create(settings);
            RecognitionAudio recognitionAudio =
                    RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();


            // Configure request to enable multiple languages
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setAudioChannelCount(2)
                            .setEnableSeparateRecognitionPerChannel(true)
                            .setLanguageCode(languageList.get(0))
                            .addAllAlternativeLanguageCodes(alternativelan)
                            .build();
            // Perform the transcription request

            //RecognizeResponse recognizeResponse = speechClient.recognize(config, recognitionAudio);

            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speechClient.longRunningRecognizeAsync(config, recognitionAudio);

            while (!response.isDone()) {
                //System.out.println("Waiting for response...");
                //Thread.sleep(1000);
                ;
            }

            List<SpeechRecognitionResult> results = response.get().getResultsList();

            // Print out the results
            for (/*SpeechRecognitionResult result : recognizeResponse.getResultsList()*/SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                Log.e(TAG,"Transcript : " + alternative.getTranscript());
                return alternative.getTranscript();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
