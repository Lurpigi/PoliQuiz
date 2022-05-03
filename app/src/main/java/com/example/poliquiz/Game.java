package com.example.poliquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.*;
import net.gotev.speech.ui.SpeechProgressView;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.ui.SpeechProgressView;

import java.util.List;


public class Game extends AppCompatActivity implements SpeechDelegate {

    private SpeechProgressView progress;
    private final int PERMISSIONS_REQUEST = 1;
    private static final String TAG = Game.class.getSimpleName();
    ImageButton bttPlay = null;
    private TextView text = null;
    private LinearLayout linearLayout;

    private TextToSpeech.OnInitListener mTttsInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(final int status) {
            switch (status) {
                case TextToSpeech.SUCCESS:
                    Log.i(TAG, "TextToSpeech engine successfully started");
                    break;

                case TextToSpeech.ERROR:
                    Log.e(TAG, "Error while initializing TextToSpeech engine!");
                    break;

                default:
                    Log.e(TAG, "Unknown TextToSpeech status: " + status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        Speech.init(this, getPackageName(), mTttsInitListener);

        bttPlay = findViewById(R.id.bttPlay);
        linearLayout = findViewById(R.id.linearLayout);
        bttPlay.setOnClickListener(view -> onButtonClick());
    }


    private void onButtonClick() {
        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                onRecordAudioPermissionGranted();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST);
            }
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

    private void onRecordAudioPermissionGranted() {


        try {
            Speech.getInstance().stopTextToSpeech();
            Speech.getInstance().startListening(progress, this);

        } catch (SpeechRecognitionNotAvailable exc) {
            //showSpeechNotSupportedDialog();

        } catch (GoogleVoiceTypingDisabledException exc) {
            //showEnableGoogleVoiceTyping();
        }
    }





    @Override
    public void onStartOfSpeech() {
    }

    @Override
    public void onSpeechRmsChanged(float value) {
        //Log.d(getClass().getSimpleName(), "Speech recognition rms is now " + value +  "dB");
    }

    @Override
    public void onSpeechResult(String result) {

        bttPlay.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);

        text.setText(result);

        if (result.isEmpty()) {
            Speech.getInstance().say(getString(R.string.repeat));

        } else {
            Speech.getInstance().say(result);
        }
    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        text.setText("");
        for (String partial : results) {
            text.append(partial + " ");
        }
    }
}