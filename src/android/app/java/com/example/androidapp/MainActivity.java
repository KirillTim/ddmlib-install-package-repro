package com.example.androidapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Main activity for the Android app.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a TextView to display the greeting
        TextView textView = new TextView(this);

        textView.setText("Hello world from android app!");
        
        // Set the TextView as our content view
        setContentView(textView);
    }
}