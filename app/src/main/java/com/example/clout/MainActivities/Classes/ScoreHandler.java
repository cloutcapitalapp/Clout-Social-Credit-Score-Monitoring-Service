package com.example.clout.MainActivities.Classes;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.example.clout.MainActivities.CreateNewSession;
import com.example.clout.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

// This class needs to take in a score entry and add to the standing score or decrease the standing score.
public class ScoreHandler extends CreateNewSession {
    FirebaseDatabase mDatabaseRef = FirebaseDatabase.getInstance();
    DatabaseReference mRef = mDatabaseRef.getReference("users");
    DatabaseReference mVal = mDatabaseRef.getReference("cashval");
    TextView textView3;

    // T.O.D.O: there is an issue with this code. It keeps making a nonstop connection loop to the data base
    // The above issue has been rectified.
    public void sessionStartScoreIncrease(final double valuePlus){
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    Log.d("testLog", "val_report" + value);
                }
                double valueToString = Double.parseDouble(value.replace("CS", "")) + valuePlus;
                String valueSetToString = String.valueOf("CS"+valueToString);
                mRef.setValue(valueSetToString);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void cashValueAdjust(){
        // Read from the database

    }

    // TODO: API Build: Section 2: End date is reached, now the score needs to update based on successful transaction or failed transaction
    // TODO: Build prototype for money transaction because STRIPE CANT BE ACCESSED RIGHT NOW!!!!

}
