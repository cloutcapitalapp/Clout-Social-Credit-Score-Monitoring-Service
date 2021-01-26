package com.miware.clout.SourceCode.Classes;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AccountKeyManager {

    // This class will create an &AccountKey for each user which enters the app
    // It will be used in-tandum with the user object which has not been created yet
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private Object AccountKeyManager;

    public String createAccountKey(String emailToPass){
        StringBuilder emailBreak = new StringBuilder(emailToPass);
        String emailAdd = String.valueOf(emailBreak.insert(0, "&")).split("@")[0].toLowerCase();
        String finalEmail = emailAdd.replaceAll("[^A-Za-z0-9 &]", "");
        myRef.child(finalEmail);
        return finalEmail;
    }

    public String reversAccountKeyFromRecyclerViewAdapter(String accountKeyToPass){
        String reversedAccountKey = accountKeyToPass.split(" : ")[0];
        return reversedAccountKey;
    }
}