package com.example.clout.MainActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.clout.MainActivities.Classes.AccountKeyGenerator;
import com.example.clout.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateNewSessionStart extends AppCompatActivity {

    TextInputLayout borrowerET;
    ImageButton continueButton;
    DatabaseReference myRef;
    DatabaseReference userTrans;
    FirebaseUser mCurrentUser;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    AccountKeyGenerator accKey = new AccountKeyGenerator();
    ImageButton backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_session_rebuild);

        borrowerET = findViewById(R.id.borrowerEmail);
        continueButton = findViewById(R.id.continueArrow);
        backArrow = findViewById(R.id.backArrow);
        mCurrentUser = mAuth.getCurrentUser();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        userTrans = database.getReference("User_Transactions");

        //
        // Before going to stage2 -
        // * Check if borrower is a listed user - this will be confirmed by checking if the Email
        // or account key entered is among the data children in the FBDB
        //
        // * If an email is listed do an email check to make sure the email is entered correctly
        //
        // * If an &AccountKey is entered check to make sure the account key is entered correctly (which really only entails making sure the &-prefix is assigned)
        //
        openAnimations();
        continueButtonMethod();
    }
    private void continueButtonMethod(){
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (/* case 1 - check if user has entered an email or an accKey*/ /* EMAIL CHECK FIRST */ borrowerET.getEditText().getText().toString().contains("@")){
                    Log.d("Contains", "Contains : @");
                    if(/*Check which email provider is being used*/ borrowerET.getEditText().getText().toString().contains("gmail.com")
                            || borrowerET.getEditText().getText().toString().contains("yahoo.com")
                            || borrowerET.getEditText().getText().toString().contains("protonmail.com")){

                        onCreateBuildTransaction();

                        Intent toStage2 = new Intent(CreateNewSessionStart.this, CreateNewSession_2.class);
                        startActivity(toStage2);
                    }
                }

                if(borrowerET.getEditText().getText().toString().contains("&")){
                    // Read from the database
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //This for-loop needs to check the entered value against the users in the database for accountkeys
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                String snappedEmail = (String) snapshot.child("Email").getValue();
                                String accKeySanpped = accKey.createAccountKey(snappedEmail);
                                if(borrowerET.getEditText().getText().toString().toLowerCase().equals(accKeySanpped)){
                                    Toast.makeText(CreateNewSessionStart.this, "Got a match", Toast.LENGTH_SHORT).show();
                                    onCreateBuildTransaction();
                                    Intent passToGate2 = new Intent(CreateNewSessionStart.this, CreateNewSession_2.class);
                                    passToGate2.putExtra("Email_To_Pass", borrowerET.getEditText().getText());
                                    startActivity(passToGate2);
                                    //Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + borrowerET.getEditText().getText());
                                    break;
                                }else{
                                    Log.d("containsFail", "Fail");
                                    Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + borrowerET.getEditText().getText());
                                    Toast.makeText(CreateNewSessionStart.this, "There are no users by that &AccountName", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w("UserCheck", "Failed to read value.", error.toException());
                        }
                    });
                }else{
                    Toast.makeText(CreateNewSessionStart.this, "Check the Email or AccountName and try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void onCreateBuildTransaction(){
        userTrans.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("createtest", "" + " : " + userTrans);
                userTrans.child(accKey.createAccountKey(mCurrentUser.getEmail()));
                userTrans.child(accKey.createAccountKey(mCurrentUser.getEmail()))
                        .child("transacting_users").setValue(accKey.createAccountKey(mCurrentUser.getEmail()) + " : " + borrowerET.getEditText().getText());
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

        ObjectAnimator animBackArrow = ObjectAnimator.ofFloat(backArrow, "translationX", -60f);
        animBackArrow.setDuration(1700);
        animBackArrow.start();
    }
}