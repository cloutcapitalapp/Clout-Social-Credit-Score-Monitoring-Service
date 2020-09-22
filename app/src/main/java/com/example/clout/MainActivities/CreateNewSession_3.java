package com.example.clout.MainActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.clout.MainActivities.Classes.AccountKeyGenerator;
import com.example.clout.MainActivities.Classes.ScoreHandler;
import com.example.clout.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateNewSession_3 extends AppCompatActivity {

    TextView date;
    Button fundsReturnedNoneRequired;
    Button chooseDateButton;
    ScoreHandler scrHandle;
    ImageButton continueButton;
    ImageButton backButton;
    AccountKeyGenerator accKey = new AccountKeyGenerator();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    DatabaseReference transactionRef = mDataBase.getReference("User_Transactions");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_session_3);

        date = findViewById(R.id.dateTextView);
        scrHandle = new ScoreHandler();
        fundsReturnedNoneRequired = findViewById(R.id.noReturnButton);
        chooseDateButton = findViewById(R.id.chooseDateButton);
        continueButton = findViewById(R.id.continueArrow);
        backButton = findViewById(R.id.backArrow);

        FundsNotRequiredForReturnMethod();
        chooseDateButtonMethod();
        openAnimations();
        continueButton();
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
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                String selectedDate = sdf.format(new Date(calView.getDate()));
                date.setText(selectedDate);
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

    public void stage3Transaction(){
        transactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionRef.child(accKey.createAccountKey(mCurrentUser.getEmail()));
                transactionRef.child(accKey.createAccountKey(mCurrentUser.getEmail()))
                        .child("date").setValue(date.getText());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void openAnimations(){
        ObjectAnimator animForwardArrow = ObjectAnimator.ofFloat(continueButton, "translationX", 60f);
        animForwardArrow.setDuration(1700);
        animForwardArrow.start();

        ObjectAnimator animBackArrow = ObjectAnimator.ofFloat(backButton, "translationX", -60f);
        animBackArrow.setDuration(1700);
        animBackArrow.start();
    }
    public void continueButton(){
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stage3Transaction();
                if(!date.getText().toString().equals("Date Selected")){
                    Intent returnToMain = new Intent(CreateNewSession_3.this, MainActivity.class);
                    startActivity(returnToMain);
                }else{
                    //Do nothing
                }
            }
        });
    }
}