package com.example.clout.MainActivities.Classes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.clout.MainActivities.activities.CreateNewSessionStart;
import com.example.clout.MainActivities.activities.CreateNewSession_2;
import com.example.clout.MainActivities.activities.EventTransactionActivity;
import com.example.clout.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static java.security.AccessController.getContext;

public class CheckAccountName extends Activity {
    AccountKeyManager accKey = new AccountKeyManager();
    FirebaseDatabase mDatabaseRef;
    DatabaseReference mVal;

    /**This method will run a check against all users in the firebase Users database
     * returns true if User is found*/
    public void
    checkFirebaseUsername(EditText editTextVal){
        //Connect to firebase
        mDatabaseRef = FirebaseDatabase.getInstance();
        mVal = mDatabaseRef.getReference("Users");

        //Change editTextVal to string
        String editTextToString = editTextVal.getText().toString().toLowerCase().trim();
        
        if(editTextToString.contains("&")){
            // Read from the database
            mVal.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean isMatchTrue = false;

                    //This for-loop needs to check the entered value against the users in the database for accountkeys
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String snappedEmail = (String) snapshot.child("Email").getValue();
                        String accKeySanpped = accKey.createAccountKey(snappedEmail);

                        if(editTextToString.equals(accKeySanpped)){
                            Log.d("Match", "Match Found");
                            Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + editTextVal.getText().toString());
                            isMatchTrue = true;
                            if (isMatchTrue)
                                //allow pass
                            break;
                        }else{
                            Log.d("Match", "Match Not Found");
                            Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + editTextVal.getText().toString());
                            isMatchTrue = false;
                            if (!isMatchTrue){
                                //alert user there is no matching user
                                noMatchingUserAlert();
                            }
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
            Log.d("containsFail", "Fail");
        }
    }
    public void noMatchingUserAlert(){
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(getApplicationContext()).create();

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_mood_bad_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("One Sec!");
        noMatchAlert.setMessage("There were no matching users with that &Accountname");

        MaterialButton confirmButton = new MaterialButton(getApplicationContext());
        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);
        layout.addView(confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noMatchAlert.dismiss();
            }
        });
        noMatchAlert.show();
    }
}
