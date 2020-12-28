package com.miware.clout.MainActivities.Classes;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mailjet.client.Main;
import com.miware.clout.MainActivities.activities.MainActivity;

import java.util.Locale;

// This class needs to take in a score entry and add to the standing score or decrease the standing score.
public class ScoreHandler extends MainActivity {
    FirebaseDatabase mDatabaseRef = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    DatabaseReference mRef = mDatabaseRef.getReference("Users");
    AccountKeyManager accKey;

    // T.O.D.O: there is an issue with this code. It keeps making a nonstop connection loop to the data base
    // The above issue has been rectified.
    /**This method will connect to firebase realtime db and increase the current users score*/
    public void sessionStartScoreIncrease(final double valuePlus){
        accKey = new AccountKeyManager();
        //Log.d("testNow", "" + "Test");
        mRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("Score")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("CheckLocation", "" + mRef);
                String value = dataSnapshot.getValue(String.class);

                //assert value != null;
                assert value != null;
                double valueToString = Double.parseDouble(value.replace("CS", "")) + valuePlus;
                String convertValueToSTringDec = String.format(Locale.ENGLISH,"%.02f", valueToString);
                String valueSetToString = String.valueOf("CS"+convertValueToSTringDec);
                mRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("Score").setValue(valueSetToString);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Todo Consider an alert that tells the user to try again later
            }
        });
    }

    public void increaseEndUserScore(final String endUser, final double valuePlus){
        Log.d("wascalled", "True");
        accKey = new AccountKeyManager();
        //Log.d("testNow", "" + "Test");
        mRef.child(endUser.trim()).child("Score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d("valueFound", "Found" + ": " + endUser + dataSnapshot.getKey() + " : " + dataSnapshot.getValue());

                String value = dataSnapshot.getValue(String.class);

                double valueToString = Double.parseDouble(value.replace("CS", "")) + valuePlus;
                String convertValueToSTringDec = String.format(Locale.ENGLISH,"%.02f", valueToString);
                String valueSetToString = String.valueOf("CS"+convertValueToSTringDec);
                mRef.child(endUser).child("Score").setValue(valueSetToString);
                Log.d("ScoreTest", valueSetToString);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Todo Consider an alert that tells the user to try again later
            }
        });
    }
    public void decreaseEndUserScore(final String endUser, final double valuePlus){
        accKey = new AccountKeyManager();
        //Log.d("testNow", "" + "Test");
        mRef.child(endUser.trim()).child("Score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d("valueFound", "Found" + ": " + endUser + dataSnapshot.getKey() + " : " + dataSnapshot.getValue());

                String value = dataSnapshot.getValue(String.class);

                double valueToString = Double.parseDouble(value.replace("CS", "")) - valuePlus;
                String convertValueToSTringDec = String.format(Locale.ENGLISH,"%.02f", valueToString);
                String valueSetToString = String.valueOf("CS"+convertValueToSTringDec);
                mRef.child(endUser).child("Score").setValue(valueSetToString);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Todo Consider an alert that tells the user to try again later
            }
        });
    }
    // TODO: API Build: Section 2: End date is reached, now the score needs to update based on successful transaction or failed transaction
    // TODO: Build prototype for money transaction because STRIPE CANT BE ACCESSED RIGHT NOW!!!!
}
