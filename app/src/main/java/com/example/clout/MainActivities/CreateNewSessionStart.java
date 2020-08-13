package com.example.clout.MainActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.clout.R;

public class CreateNewSessionStart extends AppCompatActivity {

    ImageButton continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_session_rebuild);

        continueButton = findViewById(R.id.continueArrow);
        continueButtonMethod();
    }

    private void continueButtonMethod(){
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toStage2 = new Intent(CreateNewSessionStart.this, CreateNewSession_2.class);
                startActivity(toStage2);
            }
        });
    }
}