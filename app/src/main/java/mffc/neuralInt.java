package mffc;

import android.content.Context;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;

public class neuralInt {

    private String TAG = "neuralInt";
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1000;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    private static final String MODEL_FILENAME = "model.tflite";
    private static final String OUTPUT_SCORES_NAME = "output";
    private TensorFlowInferenceInterface inferenceInterface;
    private static final String INPUT_DATA_NAME = "Placeholder:0";
    private static final char[] map = new char[]{'0', ' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};


   /* public String recognize(byte[] recordingBuffer, Context c) {
        inferenceInterface = new TensorFlowInferenceInterface(c.getAssets(), MODEL_FILENAME);
        Log.v(TAG, "Start recognition");

        short[] inputBuffer = new short[RECORDING_LENGTH];
        double[] doubleInputBuffer = new double[RECORDING_LENGTH];
        long[] outputScores = new long[157];
        String[] outputScoresNames = new String[]{OUTPUT_SCORES_NAME};



        try {
            int maxLength = recordingBuffer.length;
            System.arraycopy(recordingBuffer, 0, inputBuffer, 0, maxLength);
        } catch (Exception e){
            e.printStackTrace();
        }

        // We need to feed in float values between -1.0 and 1.0, so divide the
        // signed 16-bit inputs.
        for (int i = 0; i < RECORDING_LENGTH; ++i) {
            doubleInputBuffer[i] = inputBuffer[i] / 32767.0;
        }

        //MFCC java library.
        MFCC mfccConvert = new MFCC();
        float[] mfccInput = mfccConvert.process(doubleInputBuffer);
        Log.v(TAG, "MFCC Input======> " + Arrays.toString(mfccInput));

        // Run the model.
        inferenceInterface.feed(INPUT_DATA_NAME, mfccInput, 1, 157, 20);
        inferenceInterface.run(outputScoresNames);
        inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores);
        Log.v(TAG, "OUTPUT======> " + Arrays.toString(outputScores));


        //Output the result.
        String result = "";
        for (int i = 0;i<outputScores.length;i++) {
            if (outputScores[i] == 0)
                break;
            result += map[(int) outputScores[i]];
        }
        final String r = result;

        Log.v(TAG, "End recognition: " +result);

        return r;
    }*/

    public String recognize(byte[] recordingBuffer, Context c) {
        inferenceInterface = new TensorFlowInferenceInterface(c.getAssets(), MODEL_FILENAME);
        Log.v(TAG, "Start recognition");

        short[] inputBuffer = new short[RECORDING_LENGTH];
        double[] doubleInputBuffer = new double[RECORDING_LENGTH];
        long[] outputScores = new long[157];
        String[] outputScoresNames = new String[]{OUTPUT_SCORES_NAME};



        try {
            int maxLength = recordingBuffer.length;
            System.arraycopy(recordingBuffer, 0, inputBuffer, 0, maxLength);
        } catch (Exception e){
            e.printStackTrace();
        }

        // We need to feed in float values between -1.0 and 1.0, so divide the
        // signed 16-bit inputs.
        for (int i = 0; i < RECORDING_LENGTH; ++i) {
            doubleInputBuffer[i] = inputBuffer[i] / 32767.0;
        }

        //MFCC java library.
        MFCC mfccConvert = new MFCC();
        float[] mfccInput = mfccConvert.process(doubleInputBuffer);
        Log.v(TAG, "MFCC Input======> " + Arrays.toString(mfccInput));

        // Run the model.
        // Run the model.
        inferenceInterface.feed(INPUT_DATA_NAME, mfccInput, 1, 157, 20);
        inferenceInterface.run(outputScoresNames);
        inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores);
        Log.v(TAG, "OUTPUT======> " + Arrays.toString(outputScores));


        //Output the result.
        String result = "";
        for (int i = 0;i<outputScores.length;i++) {
            if (outputScores[i] == 0)
                break;
            result += map[(int) outputScores[i]];
        }
        final String r = result;

        Log.v(TAG, "End recognition: " +result);

        return r;
    }



}
