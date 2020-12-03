package com.miware.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.miware.clout.MainActivities.Classes.AccountKeyManager;
import com.miware.clout.MainActivities.Classes.SessionActivityID;
import com.miware.clout.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public class CreateNewSessionStart extends AppCompatActivity {

    TextInputLayout borrowerET;
    ImageButton continueButton;
    DatabaseReference myRef;
    DatabaseReference userTrans;
    FirebaseUser mCurrentUser;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    AccountKeyManager accKey = new AccountKeyManager();
    ImageButton backArrow;
    SessionActivityID sessionActivityId = new SessionActivityID();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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
        //checkForChashAmount();
        openAnimations();
        continueButtonMethod();
    }
    protected void onStart(){
        super.onStart();
    }
    private void continueButtonMethod(){
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*** for now this method will be done away with, but it is for the purpose of checking for a valid email
                 * until further notice this method will only need to check for an account key ***/
                /*if (/* case 1 - check if user has entered an email or an accKey*/ /* EMAIL CHECK FIRST */ /*borrowerET.getEditText().getText().toString().contains("@")){
                    Log.d("Contains", "Contains : @");
                    if(/*Check which email provider is being used*/ /*borrowerET.getEditText().getText().toString().contains("gmail.com")
                            || borrowerET.getEditText().getText().toString().contains("yahoo.com")
                            || borrowerET.getEditText().getText().toString().contains("protonmail.com")){

                        onCreateBuildTransaction();

                        Bundle extras = new Bundle();
                        Intent toStage2 = new Intent(CreateNewSessionStart.this, CreateNewSession_2.class);
                        String getReceiver = String.valueOf(borrowerET.getEditText().getText());
                        extras.putString("receiver", "" + getReceiver.toString());
                        toStage2.putExtras(extras);
                        startActivity(toStage2);
                    }
                }*/

                /*** This if-statement needs to check if the sessionActivityId is present in the Current User_Transactions list ***/
                /*If it is present then it needs to reject adding to the DB and display alert so the user knows */
                /* possible issue - requesting the sessionActivityID may not compare easily
                 *  because the ID has a date attached to it. So i'll have to find a way to compare without checking the date*/

                /*** Possible solution will be to pull the sessionIDs and remove the date from the Id so we can make a comparison ***/
                /*** The comparison will be between the currentUser+enteredValue and the stripped sessionID | stripped will mean "the session ID without the date" ***/
                /*
                userTrans.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String startingUser = accKey.createAccountKey(mCurrentUser.getEmail())+borrowerET.getEditText().getText();

                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            /*Make sure values are not null*//*
                            String dateGrab = snapshot1.child("date").getValue(String.class);

                            assert dateGrab != null;
                            Log.d("checkNode", " : " + snapshot1.child("date"));
                            if(!dateGrab.equals("")){
                                String dateGrabRemoveSlashes = dateGrab.replaceAll("/", "-");
                            }
                            /*if(/* We will request the sessionActivity *//*){
                            }*//*
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/

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
                                    Bundle extras = new Bundle();
                                    Intent toStage2 = new Intent(CreateNewSessionStart.this, CreateNewSession_2.class);
                                    String getReceiver = String.valueOf(borrowerET.getEditText().getText());
                                    extras.putString("receiver", "" + getReceiver.toString());
                                    toStage2.putExtras(extras);
                                    startActivity(toStage2);
                                    //Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + borrowerET.getEditText().getText());
                                }else{
                                    //Log.d("containsFail", "Fail");
                                    //Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + borrowerET.getEditText().getText());
                                    Toast.makeText(CreateNewSessionStart.this, "There are no users by that &AccountName", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            //Log.w("UserCheck", "Failed to read value.", error.toException());
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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
                //Log.d("createtest", "" + " : " + userTrans);
                userTrans.child(/*** This needs to be changed to the accountSessionID ***/ sessionActivityId.generateSessionID(
                        accKey.createAccountKey(mCurrentUser.getEmail()), Objects.requireNonNull(borrowerET.getEditText()).getText().toString(), String.valueOf(currentDate)));
                userTrans.child(/*** This needs to be changed to the accountSessionID ***/ sessionActivityId.generateSessionID(
                        accKey.createAccountKey(mCurrentUser.getEmail()), Objects.requireNonNull(borrowerET.getEditText()).getText().toString(), String.valueOf(currentDate))).child("Transacting_Users").setValue(accKey.createAccountKey(mCurrentUser.getEmail()) + " : " + borrowerET.getEditText().getText());
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
    public void sessionAlreadyStartedAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(CreateNewSessionStart.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);
        confirm.setTitle("One at a time!");
        confirm.setMessage("You already have loan with this user. This loan must be satisfied before you start another loan.");

        MaterialButton button = new MaterialButton(CreateNewSessionStart.this);
        MaterialButton cancel = new MaterialButton(CreateNewSessionStart.this);
        button.setText(R.string.confirm);
        cancel.setText(R.string.cancel);
        button.setBackgroundResource(R.color.colorPrimary);
        cancel.setBackgroundResource(R.color.colorPrimaryDark);
        layout.addView(button);
        layout.addView(cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        confirm.show();
    }

    /*** We will need to check if the user has cash available to move before allowing passage to
     * the next activity ***/
    private void checkForChashAmount(){
        myRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("Cash").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String getCashVal = snapshot.getValue(String.class);
                if(getCashVal.equals("$0.00")){
                    checkForChashValAlert();
                }else{
                    // do nothing
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkForChashValAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(CreateNewSessionStart.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);
        confirm.setIcon(R.drawable.ic_baseline_attach_money_24);

        confirm.setCanceledOnTouchOutside(false);
        confirm.setCancelable(false);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("Please Add Cash");
        confirm.setMessage("Currently you don't have any cash available. If you'd like to " +
                "add cash to your account please tap the Add Cash button below. Other wise, " +
                "hit cancel and we'll take you back to your account.");

        MaterialButton button = new MaterialButton(CreateNewSessionStart.this);
        MaterialButton cancel = new MaterialButton(CreateNewSessionStart.this);
        button.setText(R.string.addCash);
        cancel.setText(R.string.cancel);
        button.setBackgroundResource(R.color.colorPrimary);
        cancel.setBackgroundResource(R.color.colorPrimaryDark);
        layout.addView(button);
        layout.addView(cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToAdCash = new Intent(CreateNewSessionStart.this, LoadCashActivity.class);
                startActivity(goToAdCash);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToMain = new Intent(CreateNewSessionStart.this, MainActivity.class);
                startActivity(backToMain);
            }
        });
        confirm.show();
    }
}

