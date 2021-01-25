package com.miware.clout.SourceCode.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.miware.clout.R;
import com.miware.clout.SourceCode.Classes.SettingsHandler;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    //Declare reference to settings class
    SettingsHandler settingsHandler;

    //Declare material buttons
    private MaterialButton generateRandomUserKey_Button, darkMode_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Hide the actionbar
        Objects.requireNonNull(getSupportActionBar()).hide();

        //Init all
        initVars();

        //reference generateRanUserKey OnClick
        generateRandomUserKeyOnClick();

        //Reference setDarkModeButton OnClick
        setDarkModeButtonOnClick();
    }

    @Override
    protected void onStart(){
        super.onStart();
        //Init all declared vars
        initVars();
        //settings handler ref
        //TODO : move to seperate method.
        settingsHandler.getSerializedKey(this, generateRandomUserKey_Button);
    }

    //init vars
    private void initVars(){
        generateRandomUserKey_Button = findViewById(R.id.generateRandomUserKey_Button);
        darkMode_Button = findViewById(R.id.darkMode_Button);
        settingsHandler = new SettingsHandler();
    }

    //generate random user key button onClickListener
    private void generateRandomUserKeyOnClick(){
        generateRandomUserKey_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsHandler.serializeGeneratedKey(SettingsActivity.this
                );
                generateRandomUserKey_Button.setText(settingsHandler.generateRandomKey());
                //finish();
                //startActivity(getIntent());
            }
        });
    }
    //Dark mode button onClickListener
    private void setDarkModeButtonOnClick(){
        darkMode_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //For now create snack bar that alerts : Not ready yet
                Snackbar.make(getWindow().getDecorView().getRootView(),
                        "This feature is quite ready yet",
                        Snackbar.LENGTH_LONG);
            }
        });
    }
}