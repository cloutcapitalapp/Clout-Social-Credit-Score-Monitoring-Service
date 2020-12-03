package com.miware.clout.MainActivities.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.miware.clout.R;

public class LauncherActivity extends AppCompatActivity {

    Handler myHandler;
    TextView cTextView;
    TextView lTextView;
    TextView oTextView;
    TextView uTextView;
    TextView tTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        cTextView = findViewById(R.id.cTextView);
        lTextView = findViewById(R.id.lTextView);
        oTextView = findViewById(R.id.oTextView);
        uTextView = findViewById(R.id.uTextView);
        tTextView = findViewById(R.id.tTextView);

        letterAnimation();
    }
    private void letterAnimation(){
        ObjectAnimator cTextViewAnimation = ObjectAnimator.ofFloat(cTextView, "translationY", 60f);
        cTextViewAnimation.setDuration(1000);
        cTextViewAnimation.start();

        ObjectAnimator lTextViewAnimation = ObjectAnimator.ofFloat(lTextView, "translationY", 60f);
        lTextViewAnimation.setDuration(1200);
        lTextViewAnimation.start();

        ObjectAnimator oTextViewAnimation = ObjectAnimator.ofFloat(oTextView, "translationY", 60f);
        oTextViewAnimation.setDuration(1400);
        oTextViewAnimation.start();

        ObjectAnimator uTextViewAnimation = ObjectAnimator.ofFloat(uTextView, "translationY", 60f);
        uTextViewAnimation.setDuration(1600);
        uTextViewAnimation.start();

        ObjectAnimator tTextViewAnimation = ObjectAnimator.ofFloat(tTextView, "translationY", 60f);
        tTextViewAnimation.setDuration(1800);
        tTextViewAnimation.start();

        myHandler = new Handler();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent passToAuthActivity = new Intent(LauncherActivity.this, AuthActivity.class);
                startActivity(passToAuthActivity);
            }
        }, 2500);
    }
}