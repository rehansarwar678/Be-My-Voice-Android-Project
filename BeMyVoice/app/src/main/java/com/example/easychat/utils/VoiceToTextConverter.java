package com.example.easychat.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.easychat.interfaces.VoiceToTextListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class VoiceToTextConverter {
    private SpeechRecognizer speechRecognizer;

    // Call this method to start speech recognition
    public void startSpeechRecognition(Context context, VoiceToTextListener listener) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Called when the recognition engine is ready for speech input
            }

            @Override
            public void onBeginningOfSpeech() {
                // Called when the user has started to speak
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Called when the RMS value of the audio being processed changes
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Called when the audio buffer has been received
            }

            @Override
            public void onEndOfSpeech() {
                // Called when the user has finished speaking
            }

            @Override
            public void onError(int error) {
                // Called when an error occurs during speech recognition
            }

            @Override
            public void onResults(Bundle results) {
                // Called when recognition results are ready
                // Extract the recognized text from the results
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.size() > 0) {
                    String recognizedText = matches.get(0);
                    Toast.makeText(context, ""+recognizedText, Toast.LENGTH_SHORT).show();
                    listener.onConvert(recognizedText);
                    // Handle the recognized text as needed
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Called when partial recognition results are available
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Called when an event related to the recognition process occurs
            }
        });

        // Start listening without displaying the default dialog
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true); // Optional: Use offline recognition if available
        speechRecognizer.startListening(intent);
    }

    // Call this method to stop speech recognition
    private void stopSpeechRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
    }
}

