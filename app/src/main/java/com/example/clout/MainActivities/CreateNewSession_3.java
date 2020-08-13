package com.example.clout.MainActivities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;

import com.example.clout.MainActivities.Classes.ScoreHandler;
import com.example.clout.R;

public class CreateNewSession_3 extends AppCompatActivity {

    Button fundsReturnedNoneRequired;
    Button chooseDateButton;
    ScoreHandler scrHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_session_3);

        scrHandle = new ScoreHandler();
        fundsReturnedNoneRequired = findViewById(R.id.noReturnButton);
        chooseDateButton = findViewById(R.id.chooseDateButton);

        FundsNotRequiredForReturnMethod();
        chooseDateButtonMethod();
    }

    private void chooseDateButtonMethod(){

        chooseDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseDateAlert();
            }
        });

    }
    private void ChooseDateAlert(){

        AlertDialog.Builder datePickerAlert = new AlertDialog.Builder(this);
        datePickerAlert.setTitle("Please Select A Date To Be Repaid");
        LinearLayout layout = new LinearLayout(this);
        datePickerAlert.setView(layout);
        CalendarView calView = new CalendarView(this);
        layout.addView(calView);
        datePickerAlert.setPositiveButton("Submit Date", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        datePickerAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        datePickerAlert.create();
        datePickerAlert.show();
    }

    private void FundsNotRequiredForReturnMethod(){

        fundsReturnedNoneRequired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FundsNotRequiredForReturnAlert();
            }
        });

    }
    private void FundsNotRequiredForReturnAlert(){

        AlertDialog.Builder NotRequired = new AlertDialog.Builder(this);
        NotRequired.setTitle("Are You Sure?");
        NotRequired.setMessage("Accepting these terms means that you are not expecting a return " +
                "on the funds you are allowing the receiver to borrow. You will see a score increase " +
                "immediately, but please understand that you will NOT be repaid!");
        NotRequired.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If clicked, send user back to Main Activity
                // Then user should see an increase in their CS
                //

                scrHandle.sessionStartScoreIncrease(.25);
                Intent returnToMainActivity = new Intent(CreateNewSession_3.this, MainActivity.class);
                startActivity(returnToMainActivity);
            }
        });

        NotRequired.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        NotRequired.create();
        NotRequired.show();
    }
}