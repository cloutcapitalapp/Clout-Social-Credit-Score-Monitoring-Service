package com.miware.clout.MainActivities.Classes;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.Locale;

public class CashHandler {

    FirebaseDatabase mDatabaseRef = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    DatabaseReference mRef = mDatabaseRef.getReference("Users");
    AccountKeyManager accKey;

    public void sendCash(String recievingUser, Double sentCash){

        accKey = new AccountKeyManager();
        //Subtract funds from currentUsersAccount
        mRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("Cash").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                assert value != null;
                double valueToDouble = Double.parseDouble(value.replace("$", ""));
                String subtractCash = MessageFormat.format("{0}{1}", "$", String.format(Locale.ENGLISH, "%.2f", valueToDouble - sentCash/100));
                mRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("Cash").setValue(String.valueOf(subtractCash));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Send update to to-users cash amount
        /** Add sent amount to current to-users cash amount */
        mRef.child(recievingUser).child("Cash").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                double valueToDouble = Double.parseDouble(value.replace("$", ""));
                String addCash = MessageFormat.format("{0}{1}", "$", String.format(Locale.ENGLISH, "%.2f", valueToDouble + sentCash/100));
                mRef.child(recievingUser).child("Cash").setValue(String.valueOf(addCash));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
