package asyncs;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

public class Request {

    private static String TAG = "Request";


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String newR(File f) throws IOException {
        String requestURL = "http://54.227.76.197/predict";
        URL url = null;
        try {
            url = new URL(requestURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection httpConn = null;
        try {
            httpConn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);

        try {
            httpConn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        httpConn.setRequestProperty("Connection", "Keep-Alive");
        //httpConn.setRequestProperty("Cache-Control", "no-cache");
        //httpConn.setRequestProperty("Authorization", "Bearer XXXXXXXXXXXXXXXXXXXXXX");
        httpConn.setRequestProperty("Content-Type", "audio/wav");

        Log.i(TAG,"Creato header");

        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataOutputStream request = null;
        try {
            request = new DataOutputStream(httpConn.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            request.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            request.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            request.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG,"Creata richiesta");

        String response = "";
// checks server's status code first
        int status = 0;
        try {
            status = httpConn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (status == HttpURLConnection.HTTP_OK) {
            InputStream responseStream = null;
            try {
                responseStream = new BufferedInputStream(httpConn.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONParser jsonParser = new JSONParser();
            try {
                JSONObject jsonObject = (JSONObject)jsonParser.parse(
                        new InputStreamReader(responseStream, "UTF-8"));
                Log.i(TAG,"risposta : ottenuta = "+(String) jsonObject.get("keyword"));
                return (String) jsonObject.get("keyword");
            } catch (ParseException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

    }

}
