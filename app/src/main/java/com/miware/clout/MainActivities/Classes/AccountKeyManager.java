package com.miware.clout.MainActivities.Classes;

import com.miware.clout.MainActivities.objects.UserObject;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class AccountKeyManager {

    // This class will create an &AccountKey for each user which enters the app
    // It will be used in-tandum with the user object which has not been created yet
    // T.O.D.O: Create user object - done
    UserObject user = new UserObject();
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private Object AccountKeyManager;

    public String createAccountKey(String emailToPass){
        StringBuilder emailBreak = new StringBuilder(emailToPass);
        String emailAdd = String.valueOf(emailBreak.insert(0, "&")).split("@")[0].toLowerCase();
        String finalEmail = emailAdd.replaceAll("[^A-Za-z0-9 &]", "");
        //Log.d("accountkeytest", emailAdd);
        user.setAccountKey(finalEmail);
        myRef.child(finalEmail);
        if(user.getAccountKey() == null){
            //Log.d("nullKey", "Nothing was entered");
        }else{
            user.getAccountKey();
        }
        return finalEmail;
    }

    public String reversAccountKeyFromRecyclerViewAdapter(String accountKeyToPass){
        String reversedAccountKey = accountKeyToPass.split(" : ")[0];
        return reversedAccountKey;
    }
}
