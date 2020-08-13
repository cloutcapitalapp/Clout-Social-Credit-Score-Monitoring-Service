package com.example.clout.MainActivities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.clout.MainActivities.Classes.ScoreHandler;
import com.example.clout.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//1 ... What this activity needs TODO: create a system that will allow each user to report activity between themselves and other users.

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase db;
    DatabaseReference mRefUsers;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    MaterialButton cloutScore;
    FirebaseDatabase mDatabaseRef = FirebaseDatabase.getInstance();
    DatabaseReference mVal = mDatabaseRef.getReference("cashval");
    DatabaseReference mCardInfo = mDatabaseRef.getReference("isCardOnFile");

    TextView textView3;
    ScoreHandler scrHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /////////////////////////////////////////
        //                                     //
        //Init Vars and Data Base Connection   //
        //                                     //
        /////////////////////////////////////////
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        mRefUsers = db.getReference("users");
        cloutScore = findViewById(R.id.CloutScore);
        textView3 = findViewById(R.id.textView3);
        scrHandle = new ScoreHandler();

        cashButtonHandle();
    }

    // OnStart Check what the users clout score is.
    @Override
    protected void onStart(){
        super.onStart();

        initScoreHandling();/* because there won't be a score
        when the user is new, create one { of 200 } send to DB and set button text to value */
        onClickGoToCreateNewSessionActivity(); /*  */

        adjustCashVal();
    }

    public void initScoreHandling(){
        if(mRefUsers != null){
            scoreGrab();
            // Nothing should happen if the value of the score is 200 already
        }else{
            // should ONLY EVER happen on first load.
            mRefUsers.setValue("200");
            scoreGrab();
        }
    }

    //init user score
    public void scoreGrab(){
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        final MaterialButton cloutScore1 = findViewById(R.id.CloutScore);

        // Read from the database to get the users score
        mRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("db_ref_success", "Value is: " + value);
                cloutScore1.setText(String.format(String.valueOf(value)));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("db_ref_failed", "Failed to read value.", error.toException());
            }
        });

    }
    //When the ScoreHolder button is clicked go to the CreateNewSession Activity
    public void onClickGoToCreateNewSessionActivity(){
        cloutScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //transAlert();
                Intent toCreateNewSession = new Intent(MainActivity.this, CreateNewSessionStart.class);
                startActivity(toCreateNewSession);
            }
        });
    }
    public void createScoreInit(){
        mRefUsers.child(mCurrentUser.getEmail().replace(".", ""));
        mRefUsers.child(mCurrentUser.getEmail().replace(".", "")).child("score").setValue("200.00");
    }
    public void cashButtonHandle(){
        // Read from the database
        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: add conditional statement that verifies user has not yet added banking info
                // Read from the database
                mCardInfo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        Log.d("CardSuccess", "Value is: " + value);

                        if(value.equals("NO")){

                            AlertDialog.Builder trackNo = new AlertDialog.Builder(MainActivity.this);
                            trackNo.setTitle("Add Payment Method?");
                            trackNo.setMessage("There is no card information on file, " +
                                    "would you like to add a payment method?");
                            trackNo.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent passToCardHandler = new Intent(MainActivity.this, AddPaymentMethodActivity.class);
                                    startActivity(passToCardHandler);
                                }
                            });
                            trackNo.setNegativeButton("Not Right Now", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            trackNo.create();
                            trackNo.show();

                        }else{

                            //go to add money activity
                            if(value.equals("YES")) {

                                AlertDialog.Builder trackYES = new AlertDialog.Builder(MainActivity.this);
                                trackYES.setTitle("Add Cash?");
                                trackYES.setMessage("Would you like to add funds to your account?");
                                trackYES.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent passToCardHandler = new Intent(MainActivity.this, addMoneyActivity.class);
                                        startActivity(passToCardHandler);
                                    }
                                });
                                trackYES.setNegativeButton("Not Right Now", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });

                                trackYES.create();
                                trackYES.show();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("Fail", "Failed to read value.", error.toException());
                    }
                });

            }
        });
    }

    private void adjustCashVal(){

        mVal.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("Success", "Value is: " + value);
                textView3 = findViewById(R.id.textView3);
                textView3.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Fail", "Failed to read value.", error.toException());
            }
        });

    }
}
