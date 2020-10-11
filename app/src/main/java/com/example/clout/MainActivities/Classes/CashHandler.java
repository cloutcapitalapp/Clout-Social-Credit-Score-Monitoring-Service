package com.example.clout.MainActivities.Classes;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CashHandler {

    FirebaseDatabase mDatabaseRef = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    DatabaseReference mRef = mDatabaseRef.getReference("Users");
    AccountKeyManager accKey;

    public void sendCash(String recievingUser, Double sentCash){

        //Send update to to-users cash amount
        /** Add sent amount to current to-users cash amount */
        mRef.child(recievingUser).child("Score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                assert value != null;
                Log.d("valueCheck", value);
                mRef.child(recievingUser).child("Score").setValue(sentCash);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
