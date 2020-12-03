package com.miware.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miware.clout.MainActivities.Classes.ScoreHandler;
import com.miware.clout.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/* A session will mean a new activity where a clout evaluation will exist */
/* A Clout evaluation will mean an event where a debt will be evaluated between two clout users */
public class CreateNewSession extends AppCompatActivity {

    /* Init Vars */
    FirebaseDatabase db;
    DatabaseReference mRef;
    DatabaseReference mRefActivity;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    TextView getDate;
    Button DATE;
    Button submit;
    TextInputLayout borrowerEmail;
    TextInputLayout loanerEmail;
    TextInputLayout amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_session);

        /* Assign Vars */
        mAuth         = FirebaseAuth.getInstance();
        mCurrentUser  = mAuth.getCurrentUser();
        db            = FirebaseDatabase.getInstance();
        mRef          = db.getReference("Score");
        mRefActivity  = db.getReference("SessionActivities");
        submit        = findViewById(R.id.submit);
        borrowerEmail = findViewById(R.id.borrowerEmail);
        loanerEmail   = findViewById(R.id.loanerEmail);
        amount        = findViewById(R.id.amount);
        /* Assign Vars */



        /* Functionality */
        submitOnclick(); /* When submit button is clicked condition all input fields */

        dateOnClick(); /* When date picker is clicked  condition date for sessionID and
        set dateTextView to date selected */
    }

    public String createSessionID(String loaner, String borrower, String getDate) {

        // this method will take the 'borrower' and 'loaner' entered by the user and
        // create a session activity.

        // Formula for creating sessionId's
        String sessionID = loaner + borrower + getDate;

        // ................[TODO BELOW]
        // Creates child   [temporary]
        // temporary; specify - Temporarily stores 'activityHandeling' Model in Firebase
        // session will be monitored and upon deadline, session will transfer to activity status
        // after passing into activity status, activity will be marked as complete and activity will
        // be updated as completed on good terms or completed on bad terms.
        mRefActivity.setValue(sessionID);
        //Log.d("sent", "data was sent");
        return sessionID;
    }
    public void selectDateFromDatePickerOnClick() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Select A Date");
        LinearLayout layout = new LinearLayout(this);
        alertDialogBuilder.setView(layout);
        final CalendarView calView = new CalendarView(this);
        layout.addView(calView);
        final TextView dateText = findViewById(R.id.dateTextViewRec);
        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String getDate = year + "/" + month + 1 + "/" + "" + dayOfMonth;
                dateText.setText(getDate);
                // Create activity session, sessionID and creates child in Firebase Database.
                //Log.d("dataCheck", "" + getDate);
            }
        });
        alertDialogBuilder.setPositiveButton("OKay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertConfirmation();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //... Do nothing right now
            }
        });
        alertDialogBuilder.show();
    }
    public void dateOnClick() {
        DATE = findViewById(R.id.DATE);
        DATE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDateFromDatePickerOnClick();
            }
        });
    }
    public void submitOnclick(){
        submit.setOnClickListener(new View.OnClickListener() {
            final ScoreHandler sessionStartIncreaseScore = new ScoreHandler();

            @Override
            public void onClick(View v) {

                // The user can only create sessions if there is payment info stored on File//
                if (!borrowerEmail.getEditText().getText().toString().equals(null) &&
                        borrowerEmail.getEditText().getText().toString().contains("@") &&
                        !loanerEmail.getEditText().getText().toString().equals(null) &&
                        loanerEmail.getEditText().getText().toString().contains("@") &&
                        !amount.getEditText().getText().toString().equals(null)) {

                    String borrowerEmailString = borrowerEmail.getEditText().getText().toString();
                    String loanerEmailString = loanerEmail.getEditText().getText().toString();
                    getDate = findViewById(R.id.dateTextViewRec);

                    createActivitySession(createSessionID(borrowerEmailString.replace(".", ""),
                            loanerEmailString.replace(".", ""),
                            getDate.getText().toString()));

                    Intent returnToMain = new Intent(CreateNewSession.this, MainActivity.class);
                    startActivity(returnToMain);
                }else{
                    //Log.d("didpass", "failed");
                }
                sessionStartIncreaseScore.sessionStartScoreIncrease(0.25);
            }
        });
    }
    public void alertConfirmation() {
        MaterialAlertDialogBuilder confirmationAlert = new MaterialAlertDialogBuilder(this);
        confirmationAlert.setTitle("Confirm");
        confirmationAlert.setMessage("Is this date the correct date?");
        confirmationAlert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }
    public void createActivitySession(final String sessionActivity){
        mRefActivity.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    String sessionActivity = dataSnapshot.getValue(String.class);
                    mRefActivity.child(mCurrentUser.getEmail().replace(".", ""));
                    mRefActivity.child(mCurrentUser.getEmail().replace(".", "")).child("sessionActivity").setValue(sessionActivity);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

