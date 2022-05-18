package speech_services;

import android.content.Context;

import com.example.poliquiz.R;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.IOException;
import java.io.InputStream;

class SpeechCredentialsProvider{
    private Context val;
    SpeechCredentialsProvider(Context val){
        this.val = val;
    }

        public ServiceAccountCredentials getCredential() throws IOException {
            final InputStream stream = val.getResources().openRawResource(R.raw.credential);
            return ServiceAccountCredentials.fromStream(stream);
        }
}
