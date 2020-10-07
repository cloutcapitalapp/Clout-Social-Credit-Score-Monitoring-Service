package com.example.clout.MainActivities.Classes;


import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SessionActivityID {

    /*** This class will create Session Activity ID's
     * The current model will take 3 parameters
     * Those params are listed in the generateSessionID(p1,p2,p3) method below
     * ***/

    int indexed;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();
    DatabaseReference mDataBaseReference = mDataBase.getReference();
    FirebaseUser mUser = mAuth.getCurrentUser();

    // This class needs to generate a session activity ID.
    // This will be so the database can generate unique nodes for User_Transactions in the DB.
    // Rules = Sender + Receiver + currentDate + ReturnDate + number of entries with this id / starting at 0

    public String generateSessionID(
            /*the Sender will be the current user*/ String sender,
            /* the receiver will be the user receiving the loan */ String receiver,
            /* the Current Date will of-course be the date of 'Today' */ String currentDate){

        String createdID = sender + receiver + currentDate;
        return createdID;
    }

    /*** The below method is an indexer that it will provide a semi-random
     * value to the end of the session Id but will not be finished in the MVP ***/
    public int entryIndexCalc(String sender, String receiver, String currentDate, String returnDate){
        // Check if the user has generated the kind that is about to be generated
        if(mDataBaseReference.child(generateSessionID(sender,  receiver,  currentDate)).equals(generateSessionID( sender,  receiver,  currentDate))){
            // If there is an equal ID found check the last number.
            // If there is no final number add 0
            // If there is a 0 create the new ID with a 1
            mDataBaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return indexed;
    }
}
